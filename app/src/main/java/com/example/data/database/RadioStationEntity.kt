package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "radio_stations")
data class RadioStationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val genre: String = "",
    val frequency: String = "",
    val themeColorHex: String = "#6b4efe",
    val isLive: Boolean = true
)
