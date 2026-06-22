package com.example.data.model

data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String,
    val category: String,
    val description: String,
    val number: Int,
    val tvgId: String = "",
    val tvgName: String = ""
)
