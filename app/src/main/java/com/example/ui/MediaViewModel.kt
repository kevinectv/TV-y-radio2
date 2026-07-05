package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MediaRepository
import com.example.data.model.Channel
import com.example.data.model.EPGProgram
import com.example.data.model.RadioStation
import com.example.data.database.PlaylistEntity
import com.example.data.database.EpgSourceEntity
import com.example.data.database.RadioStationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.data.database.ProfileEntity
import com.example.data.util.ApiConfig
import java.util.UUID

enum class AppTab(val label: String) {
    HOME("Home"),
    WATCHLIST("Watchlist"),
    TV("IPTV TV"),
    RADIO("Radio"),
    SEARCH("Buscar"),
    SETTINGS("Settings")
}

class MediaViewModel(
    val repository: MediaRepository,
    val settingsManager: com.example.data.SettingsManager,
    private val sharedPreferences: android.content.SharedPreferences? = null
) : ViewModel() {

    // In-app Update Manager (Assigned on Activity creation)
    var updateManager: com.example.data.util.UpdateManager? = null

    // Catalogs Repository & Realtime States
    var catalogRepository: com.example.data.CatalogRepository? = null

    val catalogsStateFlow: kotlinx.coroutines.flow.StateFlow<List<com.example.data.model.Catalog>>
        get() = catalogRepository?.catalogs ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList())

    val lastSyncTime: kotlinx.coroutines.flow.StateFlow<String>
        get() = catalogRepository?.lastSyncTime ?: kotlinx.coroutines.flow.MutableStateFlow("Nunca")

    val storedItemsCount: kotlinx.coroutines.flow.StateFlow<Int>
        get() = catalogRepository?.storedItemsCount ?: kotlinx.coroutines.flow.MutableStateFlow(0)

    val cacheSize: kotlinx.coroutines.flow.StateFlow<String>
        get() = catalogRepository?.cacheSize ?: kotlinx.coroutines.flow.MutableStateFlow("0.00 B")

    fun updateStats() {
        catalogRepository?.updateStats()
    }

    fun addCatalog(catalog: com.example.data.model.Catalog) {
        viewModelScope.launch {
            catalogRepository?.addCatalog(catalog)
        }
    }

    fun updateCatalog(catalog: com.example.data.model.Catalog) {
        viewModelScope.launch {
            catalogRepository?.updateCatalog(catalog)
        }
    }

    fun deleteCatalog(id: String) {
        viewModelScope.launch {
            catalogRepository?.deleteCatalog(id)
        }
    }

    fun moveCatalogUp(id: String) {
        viewModelScope.launch {
            catalogRepository?.moveUp(id)
        }
    }

    fun moveCatalogDown(id: String) {
        viewModelScope.launch {
            catalogRepository?.moveDown(id)
        }
    }

    fun syncCatalog(id: String) {
        viewModelScope.launch {
            catalogRepository?.syncNow(id)
        }
    }

    fun syncAllCatalogs() {
        viewModelScope.launch {
            catalogRepository?.syncAll()
        }
    }

    fun refreshCatalogs() {
        viewModelScope.launch {
            catalogRepository?.refreshLocalCatalogs()
        }
    }

    // Lumina Catalog Engine Search State & Logs
    var catalogSearchQuery by mutableStateOf("")
        private set

    var catalogSearchResults by mutableStateOf<List<com.example.data.model.Catalog>>(emptyList())
        private set

    var catalogSearchLogs by mutableStateOf("")
        private set

    // MDBList Search State
    var mdbListSearchService: com.example.data.MdbListSearchService? = null
    var mdbListSearchQuery by mutableStateOf("")
        private set
    var mdbListSearchResults by mutableStateOf<List<com.example.data.model.MdbListSearchResult>>(emptyList())
        private set
    var isMdbListSearching by mutableStateOf(false)
        private set

    fun updateMdbListSearchQuery(query: String) {
        mdbListSearchQuery = query
    }

    fun searchMdbLists(query: String) {
        mdbListSearchQuery = query
        viewModelScope.launch {
            isMdbListSearching = true
            try {
                mdbListSearchResults = mdbListSearchService?.searchCatalogs(query) ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isMdbListSearching = false
            }
        }
    }

    fun SearchCatalogs(query: String) {
        catalogSearchQuery = query
        val allStored = catalogsStateFlow.value
        val q = query.trim()
        if (q.isBlank()) {
            catalogSearchResults = allStored
            catalogSearchLogs = "Texto buscado: (Ninguno/Todos) | Catálogos disponibles: ${allStored.size} | Coincidencias encontradas: ${allStored.size} | Resultado final enviado a la UI: ${allStored.size} catálogos"
            android.util.Log.d("LuminaCatalogEngine", catalogSearchLogs)
            return
        }
        val matches = allStored.filter {
            it.name.contains(q, ignoreCase = true) ||
            it.sourceType.contains(q, ignoreCase = true) ||
            it.layoutType.contains(q, ignoreCase = true) ||
            (q.equals("Action", ignoreCase = true) && it.name.contains("Acción", ignoreCase = true)) ||
            (q.equals("Accion", ignoreCase = true) && it.name.contains("Acción", ignoreCase = true)) ||
            (q.equals("Top Rated", ignoreCase = true) && it.name.contains("Top Rated", ignoreCase = true)) ||
            (q.equals("Sci-Fi", ignoreCase = true) && it.name.contains("Ciencia Ficción", ignoreCase = true))
        }
        catalogSearchResults = matches
        catalogSearchLogs = "Texto buscado: '$q' | Catálogos disponibles: ${allStored.size} | Coincidencias encontradas: ${matches.size} | Resultado final enviado a la UI: ${matches.size} catálogos"
        android.util.Log.i("LuminaCatalogEngine", catalogSearchLogs)
    }

    // Selected App Tab
    var currentTab by mutableStateOf(AppTab.HOME)
        private set

    val selectedDetailsItem = MutableStateFlow<com.example.data.model.CatalogItem?>(null)

    fun selectTab(tab: AppTab) {
        currentTab = tab
    }

    // --- Premium Catalog Item Metadata (Seen progress & Favorites) ---
    private val _seenProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val seenProgress: StateFlow<Map<String, Float>> = _seenProgress

    private val _favoriteCatalogItems = MutableStateFlow<Set<String>>(emptySet())
    val favoriteCatalogItems: StateFlow<Set<String>> = _favoriteCatalogItems

    init {
        loadPremiumMetadata()
        viewModelScope.launch {
            repository.loadEpgCacheFromDb()
        }
    }

    private fun loadPremiumMetadata() {
        viewModelScope.launch {
            try {
                sharedPreferences?.let { prefs ->
                    val favSet = prefs.getStringSet("premium_fav_items", null)
                    if (favSet != null) {
                        _favoriteCatalogItems.value = favSet.toSet()
                    } else {
                        // Preseed some cool favorites initially for stellar visual impact!
                        val defaults = setOf("f_dune2", "f_shogun", "f_wick4")
                        _favoriteCatalogItems.value = defaults
                        prefs.edit().putStringSet("premium_fav_items", defaults).apply()
                    }

                    val progString = prefs.getString("premium_seen_progress", null)
                    if (progString != null) {
                        val map = progString.split(",").mapNotNull {
                            val parts = it.split(":")
                            if (parts.size == 2) {
                                val id = parts[0]
                                val progress = parts[1].toFloatOrNull() ?: 0f
                                id to progress
                            } else null
                        }.toMap()
                        _seenProgress.value = map
                    } else {
                        // Preseed some cool watch progress values so they are immediately visible on the UI!
                        val defaultProgress = mapOf(
                            "f_dune2" to 0.68f,
                            "f_boys" to 0.85f,
                            "f_stranger" to 0.40f,
                            "f_lastofus" to 0.22f
                        )
                        _seenProgress.value = defaultProgress
                        val serString = defaultProgress.map { "${it.key}:${it.value}" }.joinToString(",")
                        prefs.edit().putString("premium_seen_progress", serString).apply()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleCatalogItemFavorite(itemId: String) {
        val current = _favoriteCatalogItems.value.toMutableSet()
        if (current.contains(itemId)) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        _favoriteCatalogItems.value = current
        viewModelScope.launch {
            try {
                sharedPreferences?.edit()?.putStringSet("premium_fav_items", current)?.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setCatalogItemProgress(itemId: String, progress: Float) {
        val current = _seenProgress.value.toMutableMap()
        current[itemId] = progress
        _seenProgress.value = current
        viewModelScope.launch {
            try {
                val progString = current.map { "${it.key}:${it.value}" }.joinToString(",")
                sharedPreferences?.edit()?.putString("premium_seen_progress", progString)?.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Active Profile and Selector State
    val activeProfileIdFlow = MutableStateFlow<String>("")

    var activeProfile by mutableStateOf<ProfileEntity?>(null)
        internal set

    var showProfileSelector by mutableStateOf(true)

    val profiles: StateFlow<List<ProfileEntity>> = repository.getProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectProfile(profile: ProfileEntity) {
        activeProfile = profile
        isDarkTheme = (profile.interfacePref == "dark")
        updateLanguage(profile.languagePref)
        activeProfileIdFlow.value = profile.id
        showProfileSelector = false
        sharedPreferences?.edit()?.putString("last_active_profile_id", profile.id)?.apply()
    }

    fun logoutProfile() {
        activeProfile = null
        showProfileSelector = true
        sharedPreferences?.edit()?.remove("last_active_profile_id")?.apply()
    }

    fun createProfile(
        name: String,
        avatarStyle: String,
        skinColor: String,
        hairColor: String,
        accessory: String,
        expression: String,
        profileColor: String,
        isKids: Boolean,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val newProfile = ProfileEntity(
                id = id,
                name = name,
                avatarStyle = avatarStyle,
                avatarSkinColor = skinColor,
                avatarHairColor = hairColor,
                avatarAccessory = accessory,
                avatarExpression = expression,
                profileColor = profileColor,
                isKids = isKids,
                languagePref = "Español",
                interfacePref = "dark",
                photoUri = photoUri
            )
            repository.insertProfile(newProfile)
        }
    }

    fun updateProfile(
        id: String,
        name: String,
        avatarStyle: String,
        skinColor: String,
        hairColor: String,
        accessory: String,
        expression: String,
        profileColor: String,
        isKids: Boolean,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val updated = ProfileEntity(
                id = id,
                name = name,
                avatarStyle = avatarStyle,
                avatarSkinColor = skinColor,
                avatarHairColor = hairColor,
                avatarAccessory = accessory,
                avatarExpression = expression,
                profileColor = profileColor,
                isKids = isKids,
                languagePref = selectedLanguage.value,
                interfacePref = if (isDarkTheme) "dark" else "light",
                photoUri = photoUri
            )
            repository.insertProfile(updated)
            if (activeProfile?.id == id) {
                activeProfile = updated
            }
        }
    }

    fun deleteProfile(id: String) {
        viewModelScope.launch {
            repository.deleteProfile(id)
            if (activeProfile?.id == id) {
                activeProfile = null
                showProfileSelector = true
                sharedPreferences?.edit()?.remove("last_active_profile_id")?.apply()
            }
        }
    }

    companion object {
        val DefaultChannel = Channel(
            id = "no_channel",
            name = "Ningún canal cargado",
            streamUrl = "",
            logoUrl = "https://images.unsplash.com/photo-1542204172-e7052809a8a7?q=80&w=200",
            category = "Sin Categoría",
            description = "Añade una fuente de IPTV desde Ajustes o el menú de lista para comenzar a reproducir.",
            number = 0
        )
        val DefaultProgram = EPGProgram(
            id = "no_program",
            channelId = "no_channel",
            title = "Sin Información de Guía",
            description = "No hay programación activa. Carga una lista con guía de canales para ver la información.",
            startTime = "00:00",
            endTime = "24:00",
            startHourDecimal = 0.0f,
            durationHours = 24.0f,
            thumbnailUrl = "https://images.unsplash.com/photo-1542204172-e7052809a8a7?q=80&w=200",
            category = "General"
        )
    }

    // Interactive Media States - TV
    var isFullscreenPlayerActive by mutableStateOf(false)
    var activeTrailerItem by mutableStateOf<com.example.data.model.CatalogItem?>(null)
    var selectedChannel by mutableStateOf<Channel>(DefaultChannel)
        private set
    var isTvPlaying by mutableStateOf(true)
        private set
    var tvVolume by mutableFloatStateOf(0.81f)
        private set

    fun getProgramsForChannel(channelId: String): List<EPGProgram> {
        return repository.getProgramsForChannel(channelId)
    }

    fun selectChannel(channel: Channel) {
        selectedChannel = channel
        isTvPlaying = true
        
        // Find or generate active program matching current hour
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY).toFloat()
        val minute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE).toFloat()
        val currentTimeDecimal = hour + (minute / 60.0f)
        val programs = repository.getProgramsForChannel(channel.id)
        val running = programs.find { it.isActiveAt(currentTimeDecimal) } ?: programs.firstOrNull() ?: DefaultProgram
        selectedEpgProgram = running

        // Add to history
        viewModelScope.launch {
            activeProfile?.id?.let { pId ->
                repository.markAsRecent(pId, channel.id, "channel")
            }
        }
    }

    fun toggleTvPlay() {
        isTvPlaying = !isTvPlaying
    }

    fun restartTvPlay() {
        val currentChannel = selectedChannel
        isTvPlaying = false
        viewModelScope.launch {
            kotlinx.coroutines.delay(200)
            selectedChannel = currentChannel
            isTvPlaying = true
        }
    }

    fun setTvVolumeLevel(level: Float) {
        tvVolume = level.coerceIn(0f, 1f)
    }

    fun selectNextChannel() {
        val list = allChannels.value
        val index = list.indexOfFirst { it.id == selectedChannel.id }
        if (index != -1) {
            val nextIndex = (index + 1) % list.size
            selectChannel(list[nextIndex])
        }
    }

    fun selectPrevChannel() {
        val list = allChannels.value
        val index = list.indexOfFirst { it.id == selectedChannel.id }
        if (index != -1) {
            val prevIndex = if (index - 1 < 0) list.size - 1 else index - 1
            selectChannel(list[prevIndex])
        }
    }

    // EPG Guide interactive details
    var selectedEpgProgram by mutableStateOf<EPGProgram>(DefaultProgram)
        private set

    fun selectEpgProgram(program: EPGProgram) {
        selectedEpgProgram = program
    }

    // Media States - Radio
    val allRadioStations: StateFlow<List<RadioStation>> = repository.getAllRadioStationsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.radioStationsList
        )

    var selectedRadioStation by mutableStateOf<RadioStation>(repository.radioStationsList.first())
        private set
    var isRadioPlaying by mutableStateOf(false)
        private set
    var radioVolume by mutableFloatStateOf(0.75f)
        private set

    fun selectRadioStation(station: RadioStation) {
        selectedRadioStation = station
        isRadioPlaying = true
        // Add to history
        viewModelScope.launch {
            activeProfile?.id?.let { pId ->
                repository.markAsRecent(pId, station.id, "radio")
            }
        }
    }

    fun toggleRadioPlay() {
        isRadioPlaying = !isRadioPlaying
    }

    fun setRadioVolumeLevel(level: Float) {
        radioVolume = level.coerceIn(0f, 1f)
    }

    fun selectNextRadio() {
        val list = allRadioStations.value
        if (list.isEmpty()) return
        val index = list.indexOfFirst { it.id == selectedRadioStation.id }
        if (index != -1) {
            val nextIndex = (index + 1) % list.size
            selectRadioStation(list[nextIndex])
        }
    }

    fun selectPrevRadio() {
        val list = allRadioStations.value
        if (list.isEmpty()) return
        val index = list.indexOfFirst { it.id == selectedRadioStation.id }
        if (index != -1) {
            val prevIndex = if (index - 1 < 0) list.size - 1 else index - 1
            selectRadioStation(list[prevIndex])
        }
    }

    fun addRadioStation(station: RadioStationEntity) {
        viewModelScope.launch {
            repository.insertRadioStation(station)
        }
    }

    fun removeRadioStation(id: String) {
        viewModelScope.launch {
            repository.deleteRadioStation(id)
            // If deleting the currently playing/selected radio station, switch to another
            if (selectedRadioStation.id == id) {
                val list = allRadioStations.value.filter { it.id != id }
                if (list.isNotEmpty()) {
                    selectRadioStation(list.first())
                }
            }
        }
    }

    // Global Search Query
    var searchQuery by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    // Settings States (Persistent via SettingsManager)
    val themeMode = settingsManager.themeMode.stateIn(viewModelScope, SharingStarted.Eagerly, "system")
    val streamingQuality = settingsManager.streamingQuality.stateIn(viewModelScope, SharingStarted.Eagerly, "1080p (FHD)")
    val imageQuality = settingsManager.imageQuality.stateIn(viewModelScope, SharingStarted.Eagerly, "Alta")
    val autoPlay = settingsManager.autoPlay.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val autoPlayTrailers = settingsManager.autoPlayTrailers.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val continueWatching = settingsManager.continueWatching.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val pushNotifications = settingsManager.pushNotifications.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val updateNotifications = settingsManager.updateNotifications.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val selectedLanguage = settingsManager.language.stateIn(viewModelScope, SharingStarted.Eagerly, "Español")
    val selectedRegion = settingsManager.region.stateIn(viewModelScope, SharingStarted.Eagerly, "LATAM")
    val playerDecoder = settingsManager.playerDecoder.stateIn(viewModelScope, SharingStarted.Eagerly, "Hardware (HW+)")
    val epgScale = settingsManager.epgScale.stateIn(viewModelScope, SharingStarted.Eagerly, "Standard")
    
    val autoEpgSync = settingsManager.autoEpgSync.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val downloadLogos = settingsManager.downloadLogos.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val bufferLatency = settingsManager.bufferLatency.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val hwAudioSync = settingsManager.hwAudioSync.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val eac3Audio = settingsManager.eac3Audio.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val realtimeShadows = settingsManager.realtimeShadows.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val fluidAnimations = settingsManager.fluidAnimations.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val ramOptimization = settingsManager.ramOptimization.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val forced60fps = settingsManager.forced60fps.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val sendErrorStats = settingsManager.sendErrorStats.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val keepLocalHistory = settingsManager.keepLocalHistory.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    var isDarkTheme by mutableStateOf(true)
        private set

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setThemeMode(mode)
            if (mode != "system") {
                isDarkTheme = (mode == "dark")
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val nextMode = if (isDarkTheme) "light" else "dark"
            setThemeMode(nextMode)
            activeProfile?.let { prof ->
                val updated = prof.copy(interfacePref = nextMode)
                activeProfile = updated
                repository.insertProfile(updated)
            }
        }
    }

    fun updateStreamQuality(quality: String) {
        viewModelScope.launch { settingsManager.setStreamingQuality(quality) }
    }

    fun updateImageQuality(quality: String) {
        viewModelScope.launch { settingsManager.setImageQuality(quality) }
    }

    fun updateAutoPlay(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setAutoPlay(enabled) }
    }

    fun updateAutoPlayTrailers(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setAutoPlayTrailers(enabled) }
    }

    fun updateContinueWatching(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setContinueWatching(enabled) }
    }

    fun updatePushNotifications(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setPushNotifications(enabled) }
    }

    fun updateUpdateNotifications(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setUpdateNotifications(enabled) }
    }

    fun updateEpgScale(scale: String) {
        viewModelScope.launch { settingsManager.setEpgScale(scale) }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            settingsManager.setLanguage(lang)
            activeProfile?.let { prof ->
                val updated = prof.copy(languagePref = lang)
                activeProfile = updated
                repository.insertProfile(updated)
            }
        }
    }

    fun updateRegion(reg: String) {
        viewModelScope.launch { settingsManager.setRegion(reg) }
    }

    fun updateDecoder(dec: String) {
        viewModelScope.launch { settingsManager.setPlayerDecoder(dec) }
    }

    fun updateAutoEpgSync(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setAutoEpgSync(enabled) }
    }

    fun updateDownloadLogos(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setDownloadLogos(enabled) }
    }

    fun updateBufferLatency(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setBufferLatency(enabled) }
    }

    fun updateHwAudioSync(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setHwAudioSync(enabled) }
    }

    fun updateEac3Audio(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setEac3Audio(enabled) }
    }

    fun updateRealtimeShadows(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setRealtimeShadows(enabled) }
    }

    fun updateFluidAnimations(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setFluidAnimations(enabled) }
    }

    fun updateRamOptimization(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setRamOptimization(enabled) }
    }

    fun updateForced60fps(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setForced60fps(enabled) }
    }

    fun updateSendErrorStats(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setSendErrorStats(enabled) }
    }

    fun updateKeepLocalHistory(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setKeepLocalHistory(enabled) }
    }

    fun clearCache(context: android.content.Context) {
        viewModelScope.launch {
            settingsManager.clearCache(context)
        }
    }

    fun restoreDefaultSettings() {
        viewModelScope.launch {
            settingsManager.restoreDefaultSettings()
        }
    }

    // Reactive Watchlist, Favorites, and Recents Combine flows
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allChannels: StateFlow<List<Channel>> = activeProfileIdFlow
        .flatMapLatest { pId ->
            repository.getAllChannelsFlow(pId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val favoriteChannels: StateFlow<List<Channel>> = activeProfileIdFlow
        .flatMapLatest { pId ->
            repository.getFavorites(pId).combine(repository.getAllChannelsFlow(pId)) { favEntities, allChans ->
                val favIds = favEntities.filter { it.type == "channel" }.map { it.itemId }.toSet()
                allChans.filter { it.id in favIds }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val favoriteRadioStations: StateFlow<List<RadioStation>> = activeProfileIdFlow
        .flatMapLatest { pId ->
            repository.getFavorites(pId).combine(MutableStateFlow(repository.radioStationsList)) { favEntities, allRadios ->
                val favIds = favEntities.filter { it.type == "radio" }.map { it.itemId }.toSet()
                allRadios.filter { it.id in favIds }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recentChannels: StateFlow<List<Channel>> = activeProfileIdFlow
        .flatMapLatest { pId ->
            repository.getRecents(pId).combine(repository.getAllChannelsFlow(pId)) { recentEntities, allChans ->
                val recentIds = recentEntities.filter { it.type == "channel" }.map { it.itemId }
                recentIds.mapNotNull { id -> allChans.find { it.id == id } }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recentRadioStations: StateFlow<List<RadioStation>> = activeProfileIdFlow
        .flatMapLatest { pId ->
            repository.getRecents(pId).combine(MutableStateFlow(repository.radioStationsList)) { recentEntities, allRadios ->
                val recentIds = recentEntities.filter { it.type == "radio" }.map { it.itemId }
                recentIds.mapNotNull { id -> allRadios.find { it.id == id } }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Direct helper to toggle favorite
    fun toggleChannelFavorite(channelId: String) {
        viewModelScope.launch {
            val pId = activeProfile?.id ?: return@launch
            if (repository.isFavorite(pId, channelId, "channel")) {
                repository.removeFavorite(pId, channelId, "channel")
            } else {
                repository.addFavorite(pId, channelId, "channel")
            }
        }
    }

    fun toggleRadioFavorite(radioId: String) {
        viewModelScope.launch {
            val pId = activeProfile?.id ?: return@launch
            if (repository.isFavorite(pId, radioId, "radio")) {
                repository.removeFavorite(pId, radioId, "radio")
            } else {
                repository.addFavorite(pId, radioId, "radio")
            }
        }
    }

    suspend fun isChannelFavorite(channelId: String): Boolean {
        val pId = activeProfile?.id ?: return false
        return repository.isFavorite(pId, channelId, "channel")
    }

    suspend fun isRadioFavorite(radioId: String): Boolean {
        val pId = activeProfile?.id ?: return false
        return repository.isFavorite(pId, radioId, "radio")
    }

    fun clearRecentsHistory() {
        viewModelScope.launch {
            val pId = activeProfile?.id ?: return@launch
            repository.clearRecentsHistory(pId)
        }
    }

    // IPTV sources / EPG manager persistent streams
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val playlists: StateFlow<List<PlaylistEntity>> = activeProfileIdFlow
        .flatMapLatest { pId ->
            repository.getPlaylistsForProfile(pId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val epgSources: StateFlow<List<EpgSourceEntity>> = activeProfileIdFlow
        .flatMapLatest { pId ->
            repository.getEpgSourcesForProfile(pId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            allRadioStations.collect { stations ->
                if (stations.isNotEmpty() && !stations.any { it.id == selectedRadioStation.id }) {
                    selectedRadioStation = stations.first()
                }
            }
        }
    }

    // IPTV Database CRUD handlers
    fun addPlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            val pId = activeProfile?.id ?: ""
            repository.insertPlaylist(playlist.copy(profileId = pId))
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
        }
    }

    fun togglePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.insertPlaylist(playlist.copy(isEnabled = !playlist.isEnabled))
        }
    }

    fun updatePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            val pId = playlist.profileId.ifEmpty { activeProfile?.id ?: "" }
            repository.insertPlaylist(playlist.copy(profileId = pId))
        }
    }

    fun syncEpgSource(source: EpgSourceEntity, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val pId = source.profileId.ifEmpty { activeProfile?.id ?: "" }
            val sourceWithProfile = source.copy(profileId = pId)
            repository.insertEpgSource(sourceWithProfile.copy(syncStatus = "Syncing..."))
            val success = repository.syncEpgSource(sourceWithProfile)
            if (!success) {
                repository.insertEpgSource(sourceWithProfile.copy(syncStatus = "Error"))
            }
            onComplete(success)
        }
    }
    
    // EPG Database CRUD handlers
    fun addEpgSource(source: EpgSourceEntity) {
        viewModelScope.launch {
            val pId = activeProfile?.id ?: ""
            repository.insertEpgSource(source.copy(profileId = pId))
        }
    }

    fun deleteEpgSource(id: String) {
        viewModelScope.launch {
            repository.deleteEpgSource(id)
        }
    }

    fun toggleEpgSource(source: EpgSourceEntity) {
        viewModelScope.launch {
            val pId = source.profileId.ifEmpty { activeProfile?.id ?: "" }
            repository.insertEpgSource(source.copy(profileId = pId, isEnabled = !source.isEnabled))
        }
    }

    fun updateEpgSource(source: EpgSourceEntity) {
        viewModelScope.launch {
            val pId = source.profileId.ifEmpty { activeProfile?.id ?: "" }
            repository.insertEpgSource(source.copy(profileId = pId))
        }
    }

    fun syncPlaylist(playlist: PlaylistEntity, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val pId = playlist.profileId.ifEmpty { activeProfile?.id ?: "" }
            val playlistWithProfile = playlist.copy(profileId = pId)
            try {
                repository.insertPlaylist(playlistWithProfile.copy(syncStatus = "Syncing..."))
                val finalUrl = if (playlistWithProfile.type.equals("Xtream Codes", ignoreCase = true)) {
                    val baseUrl = playlistWithProfile.url.removeSuffix("/")
                    "${baseUrl}/get.php?username=${playlistWithProfile.username}&password=${playlistWithProfile.password}&output=ts"
                } else {
                    playlistWithProfile.url
                }
                
                val success = repository.syncPlaylistChannels(playlistWithProfile.id, finalUrl, playlistWithProfile.type)
                
                if (success) {
                    val channelsCount = repository.getChannelsCountForPlaylist(playlistWithProfile.id)
                    val groupsCount = repository.getGroupsCountForPlaylist(playlistWithProfile.id)
                    repository.insertPlaylist(
                        playlistWithProfile.copy(
                            syncStatus = "Success",
                            lastSynced = System.currentTimeMillis(),
                            channelsCount = channelsCount,
                            groupsCount = groupsCount
                        )
                    )
                    catalogRepository?.refreshLocalCatalogs()
                    onComplete(true)
                } else {
                    repository.insertPlaylist(playlistWithProfile.copy(syncStatus = "Error"))
                    onComplete(false)
                }
            } catch (e: Exception) {
                repository.insertPlaylist(playlistWithProfile.copy(syncStatus = "Error"))
                onComplete(false)
            }
        }
    }

    init {
        viewModelScope.launch {
            allChannels.collect { channels ->
                if (channels.isNotEmpty() && (selectedChannel == DefaultChannel || !channels.any { it.id == selectedChannel.id })) {
                    selectChannel(channels.first())
                }
            }
        }
        viewModelScope.launch {
            try {
                // Safely purge legacy default profiles (p1, p2, p3, p4) on startup to keep DB clean
                repository.deleteProfile("p1")
                repository.deleteProfile("p2")
                repository.deleteProfile("p3")
                repository.deleteProfile("p4")

                // Try to auto-restore last active profile
                val lastId = sharedPreferences?.getString("last_active_profile_id", null)
                if (lastId != null) {
                    val allProfiles = repository.getProfiles().first()
                    val lastProfile = allProfiles.find { it.id == lastId }
                    if (lastProfile != null) {
                        activeProfile = lastProfile
                        showProfileSelector = false
                    } else {
                        showProfileSelector = true
                        activeProfile = null
                    }
                } else {
                    showProfileSelector = true
                    activeProfile = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showProfileSelector = true
            }
        }
    }
}

// Simple Factory for Constructor Injection
class MediaViewModelFactory(
    private val repository: MediaRepository,
    private val settingsManager: com.example.data.SettingsManager,
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MediaViewModel(repository, settingsManager, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
