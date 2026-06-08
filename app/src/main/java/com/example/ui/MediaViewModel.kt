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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
