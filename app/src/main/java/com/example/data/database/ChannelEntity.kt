package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val playlistId: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String,
    val category: String,
    val description: String = "",
    val number: Int = 0
)
