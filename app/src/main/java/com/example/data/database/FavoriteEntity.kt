package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String, // format: "${profileId}_${type}_$itemId"
    val profileId: String,
    val type: String, // "channel" or "radio"
    val itemId: String,
    val addedAt: Long = System.currentTimeMillis()
)
