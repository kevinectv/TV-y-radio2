package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id LIMIT 1)")
    suspend fun isFavorite(id: String): Boolean

    @Query("SELECT * FROM recents ORDER BY lastPlayedAt DESC LIMIT 20")
    fun getRecents(): Flow<List<RecentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(recent: RecentEntity)

    @Query("DELETE FROM recents WHERE id = :id")
    suspend fun deleteRecent(id: String)

    @Query("DELETE FROM recents")
    suspend fun clearRecents()
}
