package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun getProfiles(): Flow<List<ProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)

    @Query("DELETE FROM playlists WHERE profileId = :profileId")
    suspend fun deletePlaylistsByProfile(profileId: String)

    @Query("DELETE FROM channels WHERE playlistId IN (SELECT id FROM playlists WHERE profileId = :profileId)")
    suspend fun deleteChannelsByProfile(profileId: String)

    @Query("DELETE FROM epg_sources WHERE profileId = :profileId")
    suspend fun deleteEpgSourcesByProfile(profileId: String)

    @Query("DELETE FROM favorites WHERE profileId = :profileId")
    suspend fun deleteFavoritesByProfile(profileId: String)

    @Query("SELECT * FROM favorites WHERE profileId = :profileId ORDER BY addedAt DESC")
    fun getFavorites(profileId: String): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id LIMIT 1)")
    suspend fun isFavorite(id: String): Boolean

    @Query("SELECT * FROM recents WHERE profileId = :profileId ORDER BY lastPlayedAt DESC LIMIT 20")
    fun getRecents(profileId: String): Flow<List<RecentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(recent: RecentEntity)

    @Query("DELETE FROM recents WHERE id = :id")
    suspend fun deleteRecent(id: String)

    @Query("DELETE FROM recents WHERE profileId = :profileId")
    suspend fun clearRecents(profileId: String)

    // Playlist Manager Queries
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE profileId = :profileId ORDER BY name ASC")
    fun getPlaylistsForProfile(profileId: String): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    // EPG Sources Queries
    @Query("SELECT * FROM epg_sources ORDER BY name ASC")
    fun getAllEpgSources(): Flow<List<EpgSourceEntity>>

    @Query("SELECT * FROM epg_sources WHERE profileId = :profileId ORDER BY name ASC")
    fun getEpgSourcesForProfile(profileId: String): Flow<List<EpgSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpgSource(source: EpgSourceEntity)

    @Query("DELETE FROM epg_sources WHERE id = :id")
    suspend fun deleteEpgSource(id: String)
    
    // EPG Programs Queries
    @Query("SELECT * FROM epg_programs WHERE channelId = :channelId")
    suspend fun getEpgProgramsByChannel(channelId: String): List<EpgProgramEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpgPrograms(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE channelId = :channelId")
    suspend fun deleteEpgProgramsByChannel(channelId: String)

    // Channel Queries
    @Query("SELECT * FROM channels ORDER BY number ASC, name ASC")
    fun getAllChannelEntities(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels ORDER BY number ASC, name ASC")
    suspend fun getAllChannelEntitiesList(): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    suspend fun getChannelsByPlaylist(playlistId: String): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: String)

    // Radio Station Queries
    @Query("SELECT * FROM radio_stations ORDER BY name ASC")
    fun getAllRadioStations(): Flow<List<RadioStationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRadioStation(station: RadioStationEntity)

    @Query("DELETE FROM radio_stations WHERE id = :id")
    suspend fun deleteRadioStation(id: String)
}
