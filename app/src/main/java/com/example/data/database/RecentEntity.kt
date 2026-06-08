package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recents")
data class RecentEntity(
    @PrimaryKey val id: String, // format: "$type_$itemId"
    val type: String, // "channel" or "radio"
    val itemId: String,
    val lastPlayedAt: Long = System.currentTimeMillis()
)
