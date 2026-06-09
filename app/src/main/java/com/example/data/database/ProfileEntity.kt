package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarStyle: String, // ninja, explorer, futuristic, wizard, pirate, superhero, urban, fantasy, youth, elegant
    val avatarSkinColor: String,
    val avatarHairColor: String,
    val avatarAccessory: String,
    val avatarExpression: String,
    val profileColor: String, // hex highlight color
    val isKids: Boolean = false,
    val languagePref: String = "es",
    val interfacePref: String = "dark"
)
