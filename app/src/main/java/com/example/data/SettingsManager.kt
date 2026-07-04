package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode") // "light", "dark", "system"
        val STREAMING_QUALITY = stringPreferencesKey("streaming_quality")
        val IMAGE_QUALITY = stringPreferencesKey("image_quality")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")
        val AUTO_PLAY_TRAILERS = booleanPreferencesKey("auto_play_trailers")
        val CONTINUE_WATCHING = booleanPreferencesKey("continue_watching")
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        val UPDATE_NOTIFICATIONS = booleanPreferencesKey("update_notifications")
        val LANGUAGE = stringPreferencesKey("language")
        val REGION = stringPreferencesKey("region")
        val PLAYER_DECODER = stringPreferencesKey("player_decoder")
        val EPG_SCALE = stringPreferencesKey("epg_scale")
        
        val AUTO_EPG_SYNC = booleanPreferencesKey("auto_epg_sync")
        val DOWNLOAD_LOGOS = booleanPreferencesKey("download_logos")
        val BUFFER_LATENCY = booleanPreferencesKey("buffer_latency")
        val HW_AUDIO_SYNC = booleanPreferencesKey("hw_audio_sync")
        val EAC3_AUDIO = booleanPreferencesKey("eac3_audio")
        val REALTIME_SHADOWS = booleanPreferencesKey("realtime_shadows")
        val FLUID_ANIMATIONS = booleanPreferencesKey("fluid_animations")
        val RAM_OPTIMIZATION = booleanPreferencesKey("ram_optimization")
        val FORCED_60FPS = booleanPreferencesKey("forced_60fps")
        val SEND_ERROR_STATS = booleanPreferencesKey("send_error_stats")
        val KEEP_LOCAL_HISTORY = booleanPreferencesKey("keep_local_history")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "system" }
    val streamingQuality: Flow<String> = context.dataStore.data.map { it[STREAMING_QUALITY] ?: "1080p (FHD)" }
    val imageQuality: Flow<String> = context.dataStore.data.map { it[IMAGE_QUALITY] ?: "Alta" }
    val autoPlay: Flow<Boolean> = context.dataStore.data.map { it[AUTO_PLAY] ?: true }
    val autoPlayTrailers: Flow<Boolean> = context.dataStore.data.map { it[AUTO_PLAY_TRAILERS] ?: true }
    val continueWatching: Flow<Boolean> = context.dataStore.data.map { it[CONTINUE_WATCHING] ?: true }
    val pushNotifications: Flow<Boolean> = context.dataStore.data.map { it[PUSH_NOTIFICATIONS] ?: true }
    val updateNotifications: Flow<Boolean> = context.dataStore.data.map { it[UPDATE_NOTIFICATIONS] ?: true }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "Español" }
    val region: Flow<String> = context.dataStore.data.map { it[REGION] ?: "LATAM" }
    val playerDecoder: Flow<String> = context.dataStore.data.map { it[PLAYER_DECODER] ?: "Hardware (HW+)" }
    val epgScale: Flow<String> = context.dataStore.data.map { it[EPG_SCALE] ?: "Standard" }
    
    val autoEpgSync: Flow<Boolean> = context.dataStore.data.map { it[AUTO_EPG_SYNC] ?: true }
    val downloadLogos: Flow<Boolean> = context.dataStore.data.map { it[DOWNLOAD_LOGOS] ?: true }
    val bufferLatency: Flow<Boolean> = context.dataStore.data.map { it[BUFFER_LATENCY] ?: true }
    val hwAudioSync: Flow<Boolean> = context.dataStore.data.map { it[HW_AUDIO_SYNC] ?: true }
    val eac3Audio: Flow<Boolean> = context.dataStore.data.map { it[EAC3_AUDIO] ?: true }
    val realtimeShadows: Flow<Boolean> = context.dataStore.data.map { it[REALTIME_SHADOWS] ?: true }
    val fluidAnimations: Flow<Boolean> = context.dataStore.data.map { it[FLUID_ANIMATIONS] ?: true }
    val ramOptimization: Flow<Boolean> = context.dataStore.data.map { it[RAM_OPTIMIZATION] ?: true }
    val forced60fps: Flow<Boolean> = context.dataStore.data.map { it[FORCED_60FPS] ?: false }
    val sendErrorStats: Flow<Boolean> = context.dataStore.data.map { it[SEND_ERROR_STATS] ?: false }
    val keepLocalHistory: Flow<Boolean> = context.dataStore.data.map { it[KEEP_LOCAL_HISTORY] ?: true }

    suspend fun setThemeMode(mode: String) { context.dataStore.edit { it[THEME_MODE] = mode } }
    suspend fun setStreamingQuality(quality: String) { context.dataStore.edit { it[STREAMING_QUALITY] = quality } }
    suspend fun setImageQuality(quality: String) { context.dataStore.edit { it[IMAGE_QUALITY] = quality } }
    suspend fun setAutoPlay(enabled: Boolean) { context.dataStore.edit { it[AUTO_PLAY] = enabled } }
    suspend fun setAutoPlayTrailers(enabled: Boolean) { context.dataStore.edit { it[AUTO_PLAY_TRAILERS] = enabled } }
    suspend fun setContinueWatching(enabled: Boolean) { context.dataStore.edit { it[CONTINUE_WATCHING] = enabled } }
    suspend fun setPushNotifications(enabled: Boolean) { context.dataStore.edit { it[PUSH_NOTIFICATIONS] = enabled } }
    suspend fun setUpdateNotifications(enabled: Boolean) { context.dataStore.edit { it[UPDATE_NOTIFICATIONS] = enabled } }
    suspend fun setLanguage(lang: String) { context.dataStore.edit { it[LANGUAGE] = lang } }
    suspend fun setRegion(reg: String) { context.dataStore.edit { it[REGION] = reg } }
    suspend fun setPlayerDecoder(dec: String) { context.dataStore.edit { it[PLAYER_DECODER] = dec } }
    suspend fun setEpgScale(scale: String) { context.dataStore.edit { it[EPG_SCALE] = scale } }
    
    suspend fun setAutoEpgSync(enabled: Boolean) { context.dataStore.edit { it[AUTO_EPG_SYNC] = enabled } }
    suspend fun setDownloadLogos(enabled: Boolean) { context.dataStore.edit { it[DOWNLOAD_LOGOS] = enabled } }
    suspend fun setBufferLatency(enabled: Boolean) { context.dataStore.edit { it[BUFFER_LATENCY] = enabled } }
    suspend fun setHwAudioSync(enabled: Boolean) { context.dataStore.edit { it[HW_AUDIO_SYNC] = enabled } }
    suspend fun setEac3Audio(enabled: Boolean) { context.dataStore.edit { it[EAC3_AUDIO] = enabled } }
    suspend fun setRealtimeShadows(enabled: Boolean) { context.dataStore.edit { it[REALTIME_SHADOWS] = enabled } }
    suspend fun setFluidAnimations(enabled: Boolean) { context.dataStore.edit { it[FLUID_ANIMATIONS] = enabled } }
    suspend fun setRamOptimization(enabled: Boolean) { context.dataStore.edit { it[RAM_OPTIMIZATION] = enabled } }
    suspend fun setForced60fps(enabled: Boolean) { context.dataStore.edit { it[FORCED_60FPS] = enabled } }
    suspend fun setSendErrorStats(enabled: Boolean) { context.dataStore.edit { it[SEND_ERROR_STATS] = enabled } }
    suspend fun setKeepLocalHistory(enabled: Boolean) { context.dataStore.edit { it[KEEP_LOCAL_HISTORY] = enabled } }

    suspend fun clearCache(context: Context) {
        // Implementation for clearing cache
        try {
            context.cacheDir.deleteRecursively()
            context.externalCacheDir?.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun restoreDefaultSettings() {
        context.dataStore.edit { it.clear() }
    }
}
