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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    HOME("Home"),
    WATCHLIST("Watchlist"),
    TV("IPTV TV"),
    RADIO("Radio"),
    SETTINGS("Settings")
}

class MediaViewModel(val repository: MediaRepository) : ViewModel() {

    // Selected App Tab
    var currentTab by mutableStateOf(AppTab.HOME)
        private set

    fun selectTab(tab: AppTab) {
        currentTab = tab
    }

    // Interactive Media States - TV
    var selectedChannel by mutableStateOf<Channel>(repository.channelsList.first())
        private set
    var isTvPlaying by mutableStateOf(true)
        private set
    var tvVolume by mutableFloatStateOf(0.81f)
        private set

    fun selectChannel(channel: Channel) {
        selectedChannel = channel
        isTvPlaying = true
        // Add to history
        viewModelScope.launch {
            repository.markAsRecent(channel.id, "channel")
        }
    }

    fun toggleTvPlay() {
        isTvPlaying = !isTvPlaying
    }

    fun setTvVolumeLevel(level: Float) {
        tvVolume = level.coerceIn(0f, 1f)
    }

    fun selectNextChannel() {
        val list = repository.channelsList
        val index = list.indexOfFirst { it.id == selectedChannel.id }
        if (index != -1) {
            val nextIndex = (index + 1) % list.size
            selectChannel(list[nextIndex])
        }
    }

    fun selectPrevChannel() {
        val list = repository.channelsList
        val index = list.indexOfFirst { it.id == selectedChannel.id }
        if (index != -1) {
            val prevIndex = if (index - 1 < 0) list.size - 1 else index - 1
            selectChannel(list[prevIndex])
        }
    }

    // EPG Guide interactive details
    var selectedEpgProgram by mutableStateOf<EPGProgram>(repository.programsList.first())
        private set

    fun selectEpgProgram(program: EPGProgram) {
        selectedEpgProgram = program
    }

    // Media States - Radio
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
            repository.markAsRecent(station.id, "radio")
        }
    }

    fun toggleRadioPlay() {
        isRadioPlaying = !isRadioPlaying
    }

    fun setRadioVolumeLevel(level: Float) {
        radioVolume = level.coerceIn(0f, 1f)
    }

    fun selectNextRadio() {
        val list = repository.radioStationsList
        val index = list.indexOfFirst { it.id == selectedRadioStation.id }
        if (index != -1) {
            val nextIndex = (index + 1) % list.size
            selectRadioStation(list[nextIndex])
        }
    }

    fun selectPrevRadio() {
        val list = repository.radioStationsList
        val index = list.indexOfFirst { it.id == selectedRadioStation.id }
        if (index != -1) {
            val prevIndex = if (index - 1 < 0) list.size - 1 else index - 1
            selectRadioStation(list[prevIndex])
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
    }

    fun updateStreamQuality(quality: String) {
        streamingQuality = quality
    }

    fun updateEpgScale(scale: String) {
        epgScale = scale
    }

    fun updateLanguage(lang: String) {
        selectedLanguage = lang
    }

    fun updateRegion(reg: String) {
        selectedRegion = reg
    }

    fun updateDecoder(dec: String) {
        playerDecoder = dec
    }

    // Reactive Watchlist, Favorites, and Recents Combine flows
    val favoriteChannels: StateFlow<List<Channel>> = repository.getFavorites()
        .combine(MutableStateFlow(repository.channelsList)) { favEntities, allChannels ->
            val favIds = favEntities.filter { it.type == "channel" }.map { it.itemId }.toSet()
            allChannels.filter { it.id in favIds }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteRadioStations: StateFlow<List<RadioStation>> = repository.getFavorites()
        .combine(MutableStateFlow(repository.radioStationsList)) { favEntities, allRadios ->
            val favIds = favEntities.filter { it.type == "radio" }.map { it.itemId }.toSet()
            allRadios.filter { it.id in favIds }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentChannels: StateFlow<List<Channel>> = repository.getRecents()
        .combine(MutableStateFlow(repository.channelsList)) { recentEntities, allChannels ->
            val recentIds = recentEntities.filter { it.type == "channel" }.map { it.itemId }
            // Filter and match order of played
            recentIds.mapNotNull { id -> allChannels.find { it.id == id } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentRadioStations: StateFlow<List<RadioStation>> = repository.getRecents()
        .combine(MutableStateFlow(repository.radioStationsList)) { recentEntities, allRadios ->
            val recentIds = recentEntities.filter { it.type == "radio" }.map { it.itemId }
            recentIds.mapNotNull { id -> allRadios.find { it.id == id } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Direct helper to toggle favorite
    fun toggleChannelFavorite(channelId: String) {
        viewModelScope.launch {
            val favId = "channel_$channelId"
            if (repository.isFavorite(channelId, "channel")) {
                repository.removeFavorite(channelId, "channel")
            } else {
                repository.addFavorite(channelId, "channel")
            }
        }
    }

    fun toggleRadioFavorite(radioId: String) {
        viewModelScope.launch {
            if (repository.isFavorite(radioId, "radio")) {
                repository.removeFavorite(radioId, "radio")
            } else {
                repository.addFavorite(radioId, "radio")
            }
        }
    }

    suspend fun isChannelFavorite(channelId: String): Boolean {
        return repository.isFavorite(channelId, "channel")
    }

    suspend fun isRadioFavorite(radioId: String): Boolean {
        return repository.isFavorite(radioId, "radio")
    }

    fun clearRecentsHistory() {
        viewModelScope.launch {
            repository.clearRecentsHistory()
        }
    }

    // IPTV sources / EPG manager persistent streams
    val playlists: StateFlow<List<PlaylistEntity>> = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val epgSources: StateFlow<List<EpgSourceEntity>> = repository.getAllEpgSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
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
}

// Simple Factory for Constructor Injection
class MediaViewModelFactory(private val repository: MediaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MediaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
