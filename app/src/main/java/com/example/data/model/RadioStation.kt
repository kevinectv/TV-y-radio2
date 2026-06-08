package com.example.data.model

data class RadioStation(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String,
    val genre: String,
    val frequency: String,
    val themeColorHex: String = "#6b4efe",
    val isLive: Boolean = true
)
