package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epg_programs")
data class EpgProgramEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val title: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val startHourDecimal: Float,
    val durationHours: Float,
    val thumbnailUrl: String = "",
    val category: String = "General"
)
