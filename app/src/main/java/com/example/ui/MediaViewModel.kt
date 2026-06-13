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
import java.util.UUID

enum class AppTab(val label: String) {
    HOME("Home"),
    WATCHLIST("Watchlist"),
    TV("IPTV TV"),
    RADIO("Radio"),
    SETTINGS("Settings")
}

class MediaViewModel(
    val repository: MediaRepository,
    private val sharedPreferences: android.content.SharedPreferences? = null
) : ViewModel() {

    // In-app Update Manager (Assigned on Activity creation)
    var updateManager: com.example.data.util.UpdateManager? = null

    // Selected App Tab
    var currentTab by mutableStateOf(AppTab.HOME)
        private set

    fun selectTab(tab: AppTab) {
        currentTab = tab
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
        selectedLanguage = profile.languagePref
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
        isKids: Boolean
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
                interfacePref = "dark"
            )
            repository.insertProfile(newProfile)
            
            // If it's the first profile, auto-select it!
            val currentProfiles = repository.getProfiles().first()
            if (currentProfiles.isEmpty() || currentProfiles.size == 1) {
                selectProfile(newProfile)
            }
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
        isKids: Boolean
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
                languagePref = selectedLanguage,
                interfacePref = if (isDarkTheme) "dark" else "light"
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

    // Settings States
    var isDarkTheme by mutableStateOf(true)
        private set
    var streamingQuality by mutableStateOf("1080p (FHD)")
        private set
    var epgScale by mutableStateOf("Standard")
        private set
    var selectedLanguage by mutableStateOf("Español")
        private set
    var selectedRegion by mutableStateOf("LATAM")
        private set
    var playerDecoder by mutableStateOf("Hardware (HW+)")
        private set

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
        activeProfile?.let { prof ->
            val updated = prof.copy(interfacePref = if (isDarkTheme) "dark" else "light")
            activeProfile = updated
            viewModelScope.launch {
                repository.insertProfile(updated)
            }
        }
    }

    fun updateStreamQuality(quality: String) {
        streamingQuality = quality
    }

    fun updateEpgScale(scale: String) {
        epgScale = scale
    }

    fun updateLanguage(lang: String) {
        selectedLanguage = lang
        activeProfile?.let { prof ->
            val updated = prof.copy(languagePref = lang)
            activeProfile = updated
            viewModelScope.launch {
                repository.insertProfile(updated)
            }
        }
    }

    fun updateRegion(reg: String) {
        selectedRegion = reg
    }

    fun updateDecoder(dec: String) {
        playerDecoder = dec
    }

    // Reactive Watchlist, Favorites, and Recents Combine flows
    val allChannels: StateFlow<List<Channel>> = repository.getAllChannelsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.channelsList)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val favoriteChannels: StateFlow<List<Channel>> = activeProfileIdFlow
        .flatMapLatest { pId ->
            repository.getFavorites(pId).combine(repository.getAllChannelsFlow()) { favEntities, allChans ->
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
            repository.getRecents(pId).combine(repository.getAllChannelsFlow()) { recentEntities, allChans ->
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
    val playlists: StateFlow<List<PlaylistEntity>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val epgSources: StateFlow<List<EpgSourceEntity>> = repository.getAllEpgSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            allRadioStations.collect { stations ->
                if (stations.isNotEmpty() && !stations.any { it.id == selectedRadioStation.id }) {
                    selectedRadioStation = stations.first()
                }
            }
        }

        viewModelScope.launch {
            try {
                val currentPlaylists = repository.getAllPlaylists().first()
                if (currentPlaylists.isEmpty()) {
                    repository.insertPlaylist(
                        PlaylistEntity(
                            id = "pluto_es",
                            name = "Pluto TV España",
                            type = "M3U URL",
                            url = "https://i.mjh.nz/PlutoTV/es.json",
                            channelsCount = 84,
                            groupsCount = 12,
                            isEnabled = true,
                            lastSynced = System.currentTimeMillis() - 3600000L * 3, // 3 hours ago
                            syncStatus = "Success"
                        )
                    )
                    repository.insertPlaylist(
                        PlaylistEntity(
                            id = "samsung_latam",
                            name = "Samsung TV Plus LATAM",
                            type = "M3U8 URL",
                            url = "https://example.com/samsung_latam.m3u8",
                            channelsCount = 120,
                            groupsCount = 15,
                            isEnabled = true,
                            lastSynced = System.currentTimeMillis() - 3600000L * 24, // 24 hours ago
                            syncStatus = "Success"
                        )
                    )
                    repository.insertPlaylist(
                        PlaylistEntity(
                            id = "xtream_demo",
                            name = "Lumina Premium Xtream",
                            type = "Xtream Codes",
                            url = "http://premium-iptv.dns.to:8080",
                            username = "lumina_guest",
                            password = "••••••••••••",
                            channelsCount = 1850,
                            groupsCount = 42,
                            isEnabled = false,
                            lastSynced = System.currentTimeMillis() - 3600000L * 48,
                            syncStatus = "Error"
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore initialization failures
            }
        }

        viewModelScope.launch {
            try {
                val currentEpg = repository.getAllEpgSources().first()
                if (currentEpg.isEmpty()) {
                    repository.insertEpgSource(
                        EpgSourceEntity(
                            id = "epg_es",
                            name = "EPG Oficial España XML",
                            url = "https://i.mjh.nz/PlutoTV/es.xml",
                            lastSynced = System.currentTimeMillis() - 3600000L * 2,
                            syncStatus = "Success",
                            isEnabled = true
                        )
                    )
                    repository.insertEpgSource(
                        EpgSourceEntity(
                            id = "epg_global",
                            name = "EPG Global Backup",
                            url = "https://example.com/epg/global.xml.gz",
                            lastSynced = System.currentTimeMillis() - 3600000L * 12,
                            syncStatus = "Success",
                            isEnabled = false
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore initialization failures
            }
        }
    }

    // IPTV Database CRUD handlers
    fun addPlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.insertPlaylist(playlist)
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
            repository.insertPlaylist(playlist)
        }
    }

    // EPG Database CRUD handlers
    fun addEpgSource(source: EpgSourceEntity) {
        viewModelScope.launch {
            repository.insertEpgSource(source)
        }
    }

    fun deleteEpgSource(id: String) {
        viewModelScope.launch {
            repository.deleteEpgSource(id)
        }
    }

    fun toggleEpgSource(source: EpgSourceEntity) {
        viewModelScope.launch {
            repository.insertEpgSource(source.copy(isEnabled = !source.isEnabled))
        }
    }

    fun updateEpgSource(source: EpgSourceEntity) {
        viewModelScope.launch {
            repository.insertEpgSource(source)
        }
    }

    fun syncPlaylist(playlist: PlaylistEntity, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.insertPlaylist(playlist.copy(syncStatus = "Syncing..."))
                val finalUrl = if (playlist.type.equals("Xtream Codes", ignoreCase = true)) {
                    val baseUrl = playlist.url.removeSuffix("/")
                    "${baseUrl}/get.php?username=${playlist.username}&password=${playlist.password}&output=ts"
                } else {
                    playlist.url
                }
                
                val success = repository.syncPlaylistChannels(playlist.id, finalUrl, playlist.type)
                
                if (success) {
                    val channelsCount = repository.getChannelsCountForPlaylist(playlist.id)
                    val groupsCount = repository.getGroupsCountForPlaylist(playlist.id)
                    repository.insertPlaylist(
                        playlist.copy(
                            syncStatus = "Success",
                            lastSynced = System.currentTimeMillis(),
                            channelsCount = channelsCount,
                            groupsCount = groupsCount
                        )
                    )
                    onComplete(true)
                } else {
                    repository.insertPlaylist(playlist.copy(syncStatus = "Error"))
                    onComplete(false)
                }
            } catch (e: Exception) {
                repository.insertPlaylist(playlist.copy(syncStatus = "Error"))
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

                val list = repository.getProfiles().first()
                if (list.isEmpty()) {
                    // Let the user create their own custom profile from scratch!
                    showProfileSelector = true
                } else {
                    // Check if there was a saved active profile from the last session
                    val lastActiveId = sharedPreferences?.getString("last_active_profile_id", null)
                    val matchedProfile = list.find { it.id == lastActiveId }
                    if (matchedProfile != null) {
                        selectProfile(matchedProfile)
                    } else {
                        showProfileSelector = true
                    }
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
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MediaViewModel(repository, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
