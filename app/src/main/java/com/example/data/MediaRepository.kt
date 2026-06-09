package com.example.data

import com.example.data.database.FavoriteEntity
import com.example.data.database.MediaDao
import com.example.data.database.RecentEntity
import com.example.data.database.PlaylistEntity
import com.example.data.database.EpgSourceEntity
import com.example.data.database.ChannelEntity
import com.example.data.model.Channel
import com.example.data.model.EPGProgram
import com.example.data.model.RadioStation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine

class MediaRepository(private val mediaDao: MediaDao) {

    // Predefined IPTV Channels
    val channelsList = emptyList<Channel>()

    // Predefined Radio Stations
    val radioStationsList = listOf(
        RadioStation(
            id = "lofi_beats",
            name = "Lofi Cozy Beats",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            logoUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=300",
            genre = "Lofi Hip Hop",
            frequency = "88.5 FM",
            themeColorHex = "#2E1A47" // Deep Purple
        ),
        RadioStation(
            id = "classic_jazz",
            name = "Golden Era Jazz",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            logoUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?q=80&w=300",
            genre = "Classic Jazz",
            frequency = "95.1 FM",
            themeColorHex = "#3E2723" // Dark Brown Warm
        ),
        RadioStation(
            id = "synth_future",
            name = "Synthwave Retro 1984",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            logoUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=300",
            genre = "Outrun Synthwave",
            frequency = "101.9 FM",
            themeColorHex = "#1A0033" // Electric Neon Purple
        ),
        RadioStation(
            id = "ambient_space",
            name = "Atmospheric Void",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            logoUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=300",
            genre = "Space Ambient",
            frequency = "104.3 FM",
            themeColorHex = "#0D1B2A" // Void Blue
        ),
        RadioStation(
            id = "rock_classic",
            name = "Infinite Rock Legends",
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            logoUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=300",
            genre = "Hard Rock",
            frequency = "98.7 FM",
            themeColorHex = "#1C0D02" // Lava Red Black
        )
    )

    // Predefined programs covering a standard timeline. Empty list as test channels are removed
    val programsList = emptyList<EPGProgram>()

    // Dynamic EPG generation so the channel ALWAYS has complete program info details!
    fun getProgramsForChannel(channelId: String): List<EPGProgram> {
        val seed = channelId.hashCode().toLong()
        val random = java.util.Random(seed)
        
        val titles = listOf(
            "Cine Estelar", "Deportes 360", "Noticias de Última Hora", "Mundo Documental",
            "Magazine de la Mañana", "Tarde de Acción", "Grandes Misterios", "Aventura Extrema",
            "Musical Retro", "Zona de Comedia", "Lo Mejor del Espectáculo", "Series de Estreno"
        )
        val descs = listOf(
            "Disfruta de la mejor recopilación de contenido en transmisión continua y en alta definición para toda la familia.",
            "Análisis detallados, comentarios en directo y cobertura completa de toda la actualidad nacional e internacional.",
            "Una mirada profunda a los secretos de la naturaleza, la ciencia moderna y los hitos históricos del planeta entero.",
            "Los mejores momentos, programación especial, entrevistas exclusivas y entretenimiento asegurado las 24 horas del día."
        )
        val thumbnails = listOf(
            "https://images.unsplash.com/photo-1598257006458-087169a1f08d?q=80&w=300",
            "https://images.unsplash.com/photo-1540575467063-178a50c2df87?q=80&w=300",
            "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=300",
            "https://images.unsplash.com/photo-1461151304267-38535e780c79?q=80&w=300"
        )
        
        val result = mutableListOf<EPGProgram>()
        var currentHour = 8.0f // Starts at 08:00 AM matches timeline
        var index = 1
        
        while (currentHour < 24.0f) {
            val duration = 1.0f + (random.nextInt(3) * 0.5f) // 1.0h, 1.5h, 2.0h
            val endHour = (currentHour + duration).coerceAtMost(24.0f)
            
            val tIndex = random.nextInt(titles.size)
            val dIndex = random.nextInt(descs.size)
            val thumbIndex = random.nextInt(thumbnails.size)
            
            val title = titles[tIndex]
            val desc = descs[dIndex]
            val thumbUrl = thumbnails[thumbIndex]
            
            val startHourFormattedStr = formatDecimalTime(currentHour)
            val endHourFormattedStr = formatDecimalTime(endHour)
            
            result.add(
                EPGProgram(
                    id = "${channelId}_prog_$index",
                    channelId = channelId,
                    title = title,
                    description = desc,
                    startTime = startHourFormattedStr,
                    endTime = endHourFormattedStr,
                    startHourDecimal = currentHour,
                    durationHours = duration,
                    thumbnailUrl = thumbUrl,
                    category = "Entretenimiento"
                )
            )
            
            currentHour = endHour
            index++
        }
        return result
    }
    
    private fun formatDecimalTime(decimal: Float): String {
        val hourInt = decimal.toInt()
        val minuteInt = ((decimal - hourInt) * 60).toInt()
        val ampm = if (hourInt < 12) "AM" else "PM"
        val displayHour = when {
            hourInt == 0 -> 12
            hourInt > 12 -> hourInt - 12
            else -> hourInt
        }
        return String.format("%02d:%02d %s", displayHour, minuteInt, ampm)
    }


    // Reactive streams from the DB
    fun getFavorites(): Flow<List<FavoriteEntity>> = mediaDao.getFavorites()
    fun getRecents(): Flow<List<RecentEntity>> = mediaDao.getRecents()

    suspend fun addFavorite(itemId: String, type: String) {
        mediaDao.insertFavorite(FavoriteEntity(id = "${type}_$itemId", type = type, itemId = itemId))
    }

    suspend fun removeFavorite(itemId: String, type: String) {
        mediaDao.deleteFavorite("${type}_$itemId")
    }

    suspend fun isFavorite(itemId: String, type: String): Boolean {
        return mediaDao.isFavorite("${type}_$itemId")
    }

    suspend fun markAsRecent(itemId: String, type: String) {
        mediaDao.insertRecent(RecentEntity(id = "${type}_$itemId", type = type, itemId = itemId, lastPlayedAt = System.currentTimeMillis()))
    }

    suspend fun removeRecent(itemId: String, type: String) {
        mediaDao.deleteRecent("${type}_$itemId")
    }

    suspend fun clearRecentsHistory() {
        mediaDao.clearRecents()
    }

    fun getAllChannelsFlow(): Flow<List<Channel>> {
        return mediaDao.getAllChannelEntities().combine(mediaDao.getAllPlaylists()) { dbChans, playlists ->
            val enabledPlaylistIds = playlists.filter { it.isEnabled }.map { it.id }.toSet()
            val dynamicList = dbChans.filter { it.playlistId in enabledPlaylistIds }.map { entity ->
                Channel(
                    id = entity.id,
                    name = entity.name,
                    streamUrl = entity.streamUrl,
                    logoUrl = entity.logoUrl,
                    category = entity.category,
                    description = entity.description.ifEmpty { "Canal de la lista IPTV" },
                    number = entity.number
                )
            }
            channelsList + dynamicList
        }
    }

    suspend fun syncPlaylistChannels(playlistId: String, url: String, type: String): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .build()
                
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext false
                val bodyString = response.body?.string() ?: return@withContext false
                
                val parsedList = if (bodyString.trim().startsWith("{")) {
                    parseJsonPlaylist(bodyString, playlistId)
                } else {
                    parseM3uPlaylist(bodyString, playlistId)
                }
                
                mediaDao.deleteChannelsByPlaylist(playlistId)
                if (parsedList.isNotEmpty()) {
                    mediaDao.insertChannels(parsedList)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun parseJsonPlaylist(jsonStr: String, playlistId: String): List<ChannelEntity> {
        val list = mutableListOf<ChannelEntity>()
        try {
            val root = org.json.JSONObject(jsonStr)
            if (root.has("channels")) {
                val channelsObj = root.getJSONObject("channels")
                val keys = channelsObj.keys()
                var index = 1
                while (keys.hasNext()) {
                    val key = keys.next()
                    val chanObj = channelsObj.getJSONObject(key)
                    val name = chanObj.optString("name", "Unknown Channel")
                    val logoUrl = chanObj.optString("logo", "")
                    val streamUrl = chanObj.optString("url", "")
                    
                    var category = "General"
                    if (chanObj.has("groups")) {
                        val groupsArr = chanObj.optJSONArray("groups")
                        if (groupsArr != null && groupsArr.length() > 0) {
                            category = groupsArr.optString(0, "General")
                        } else {
                            val groupsStr = chanObj.optString("groups", "")
                            if (groupsStr.isNotEmpty()) {
                                category = groupsStr
                            }
                        }
                    }
                    
                    if (streamUrl.isNotEmpty()) {
                        list.add(
                            ChannelEntity(
                                id = "${playlistId}_$key",
                                playlistId = playlistId,
                                name = name,
                                streamUrl = streamUrl,
                                logoUrl = logoUrl,
                                category = category,
                                description = "Canal de la lista IPTV",
                                number = 100 + index
                            )
                        )
                        index++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun parseM3uPlaylist(m3uContent: String, playlistId: String): List<ChannelEntity> {
        val list = mutableListOf<ChannelEntity>()
        try {
            val lines = m3uContent.lines()
            var currentName = ""
            var currentLogo = ""
            var currentCategory = "General"
            var index = 1
            
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("#EXTINF:")) {
                    val lastComma = trimmed.lastIndexOf(',')
                    currentName = if (lastComma != -1) {
                        trimmed.substring(lastComma + 1).trim()
                    } else {
                        "Channel $index"
                    }
                    
                    currentLogo = extractAttribute(trimmed, "tvg-logo")
                    currentCategory = extractAttribute(trimmed, "group-title")
                    if (currentCategory.isEmpty()) {
                        currentCategory = "General"
                    }
                } else if (trimmed.startsWith("#EXTGRP:")) {
                    val cat = trimmed.substring(8).trim()
                    if (cat.isNotEmpty()) {
                        currentCategory = cat
                    }
                } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    if (currentName.isEmpty()) {
                        currentName = "Channel $index"
                    }
                    list.add(
                        ChannelEntity(
                            id = "${playlistId}_$index",
                            playlistId = playlistId,
                            name = currentName,
                            streamUrl = trimmed,
                            logoUrl = currentLogo,
                            category = currentCategory,
                            description = "Canal de la lista IPTV",
                            number = 100 + index
                        )
                    )
                    currentName = ""
                    currentLogo = ""
                    currentCategory = "General"
                    index++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun extractAttribute(line: String, attrName: String): String {
        val key = "$attrName=\""
        val start = line.indexOf(key)
        if (start == -1) return ""
        val fromKey = line.substring(start + key.length)
        val end = fromKey.indexOf('"')
        if (end == -1) return ""
        return fromKey.substring(0, end)
    }

    suspend fun getChannelsCountForPlaylist(playlistId: String): Int {
        return mediaDao.getChannelsByPlaylist(playlistId).size
    }

    suspend fun getGroupsCountForPlaylist(playlistId: String): Int {
        return mediaDao.getChannelsByPlaylist(playlistId).map { it.category }.distinct().size
    }

    // Playlist & EPG Manager actions
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = mediaDao.getAllPlaylists()
    suspend fun insertPlaylist(playlist: PlaylistEntity) = mediaDao.insertPlaylist(playlist)
    
    suspend fun deletePlaylist(id: String) {
        mediaDao.deletePlaylist(id)
        mediaDao.deleteChannelsByPlaylist(id)
    }
    
    suspend fun getPlaylistById(id: String): PlaylistEntity? = mediaDao.getPlaylistById(id)

    fun getAllEpgSources(): Flow<List<EpgSourceEntity>> = mediaDao.getAllEpgSources()
    suspend fun insertEpgSource(source: EpgSourceEntity) = mediaDao.insertEpgSource(source)
    suspend fun deleteEpgSource(id: String) = mediaDao.deleteEpgSource(id)
}
