package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.example.data.database.ProfileEntity

@Composable
fun ProfileAvatar(
    profile: ProfileEntity,
    modifier: Modifier = Modifier
) {
    ProfileAvatar(
        style = profile.avatarStyle,
        skinColorHex = profile.avatarSkinColor,
        hairColorHex = profile.avatarHairColor,
        accessory = profile.avatarAccessory,
        expression = profile.avatarExpression,
        profileColorHex = profile.profileColor,
        photoUri = profile.photoUri,
        modifier = modifier
    )
}

@Composable
fun ProfileAvatar(
    style: String,
    skinColorHex: String,
    hairColorHex: String,
    accessory: String,
    expression: String,
    profileColorHex: String,
    photoUri: String? = null,
    modifier: Modifier = Modifier
) {
    val profileColor = try {
        Color(android.graphics.Color.parseColor(profileColorHex))
    } catch (e: Exception) {
        Color(0xFF00E5FF)
    }

    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        profileColor.copy(alpha = 0.35f),
                        profileColor.copy(alpha = 0.15f),
                        Color.Black.copy(alpha = 0.9f)
                    )
                )
            )
    ) {
        if (!photoUri.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = photoUri,
                contentDescription = "Foto de perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = {
                    CharacterAvatar(
                        style = style,
                        skinColorHex = skinColorHex,
                        hairColorHex = hairColorHex,
                        accessory = accessory,
                        expression = expression,
                        profileColorHex = profileColorHex,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        } else {
            CharacterAvatar(
                style = style,
                skinColorHex = skinColorHex,
                hairColorHex = hairColorHex,
                accessory = accessory,
                expression = expression,
                profileColorHex = profileColorHex,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Premium inner glow matching profile color
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, profileColor.copy(alpha = 0.15f)),
                    center = center,
                    radius = size.maxDimension / 2
                ),
                blendMode = BlendMode.Screen
            )
        }
    }
}
