package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val profileId: String = "",
    val name: String,
    val type: String, // "M3U URL", "M3U8 URL", "Local M3U", "Xtream Codes"
    val url: String,
    val username: String = "",
    val password: String = "",
    val channelsCount: Int = 0,
    val groupsCount: Int = 0,
    val isEnabled: Boolean = true,
    val lastSynced: Long = 0L,
    val syncStatus: String = "Pending" // "Pending", "Syncing", "Success", "Error"
)
