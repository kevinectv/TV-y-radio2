package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epg_sources")
data class EpgSourceEntity(
    @PrimaryKey val id: String,
    val profileId: String = "",
    val name: String,
    val url: String,
    val lastSynced: Long = 0L,
    val syncStatus: String = "Pending", // "Pending", "Syncing", "Success", "Error"
    val isEnabled: Boolean = true
)
