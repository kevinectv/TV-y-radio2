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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    // EPG Sources Queries
    @Query("SELECT * FROM epg_sources ORDER BY name ASC")
    fun getAllEpgSources(): Flow<List<EpgSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpgSource(source: EpgSourceEntity)

    @Query("DELETE FROM epg_sources WHERE id = :id")
    suspend fun deleteEpgSource(id: String)

    // Channel Queries
    @Query("SELECT * FROM channels ORDER BY number ASC, name ASC")
    fun getAllChannelEntities(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId")
    suspend fun getChannelsByPlaylist(playlistId: String): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: String)
}
