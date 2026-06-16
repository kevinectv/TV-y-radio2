package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.inset
import kotlin.math.sin

@Composable
fun ElegantBackground(
    modifier: Modifier = Modifier,
    accentColorHex: String? = null // For dynamic radio station theme matching
) {
    val baseGlowColor = remember(accentColorHex) {
        try {
            if (accentColorHex != null) {
                Color(android.graphics.Color.parseColor(accentColorHex))
            } else {
                Color(0xFF030406) // Deep luxury obsidian black/charcoal
            }
        } catch (e: Exception) {
            Color(0xFF030406)
        }
    }

    // Fixed static values to prevent any moving animations, keeping the background completely still
    val beamOffset1 = 0.5f
    val beamOffset2 = 1.2f
    val pulseIntensity = 0.65f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Pure obsidian black base
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw a massive base radial gradient representing background ambient glow matching station/brand
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseGlowColor.copy(alpha = 0.11f * pulseIntensity),
                        Color(0xFF050508)
                    ),
                    center = Offset(width * 0.5f, height * 0.4f),
                    radius = width * 1.0f
                )
            )

            // 2. Draw side ambient accent lights
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseGlowColor.copy(alpha = 0.05f * pulseIntensity),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.85f),
                    radius = width * 0.6f
                )
            )

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseGlowColor.copy(alpha = 0.04f * pulseIntensity),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.15f),
                    radius = width * 0.7f
                )
            )

            // 3. Draw vertical light lines with soft blurred matching highlights
            // Beam 1: Left drift
            val beamX1 = width * (0.25f + 0.1f * sin(beamOffset1))
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        baseGlowColor.copy(alpha = 0.015f * pulseIntensity),
                        baseGlowColor.copy(alpha = 0.035f * pulseIntensity),
                        baseGlowColor.copy(alpha = 0.015f * pulseIntensity),
                        Color.Transparent
                    ),
                    startX = beamX1 - width * 0.12f,
                    endX = beamX1 + width * 0.12f
                )
            )

            // Beam 2: Right drift
            val beamX2 = width * (0.75f + 0.12f * sin(beamOffset2))
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        baseGlowColor.copy(alpha = 0.01f * pulseIntensity),
                        baseGlowColor.copy(alpha = 0.025f * pulseIntensity),
                        baseGlowColor.copy(alpha = 0.01f * pulseIntensity),
                        Color.Transparent
                    ),
                    startX = beamX2 - width * 0.15f,
                    endX = beamX2 + width * 0.15f
                )
            )

            // Ambient dark bottom vignette to anchor panels
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF000000).copy(alpha = 0.9f)
                    ),
                    startY = height * 0.6f,
                    endY = height
                )
            )
        }
    }
}
