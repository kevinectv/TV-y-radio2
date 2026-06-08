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
    val infiniteTransition = rememberInfiniteTransition(label = "background_pulsing")

    // Dynamic color calculation if provided, else use default deep violet
    val dynamicAccent = remember(accentColorHex) {
        accentColorHex?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color(0xFF6B4EFE)
    }

    // High performance background animations
    val beamOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beam_offset_1"
    )

    val beamOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beam_offset_2"
    )

    val pulseIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_intensity"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF04060F)) // Very deep obsidian black
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw a massive base radial gradient representing background ambient glow
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        dynamicAccent.copy(alpha = 0.18f * pulseIntensity),
                        Color(0xFF03050B)
                    ),
                    center = Offset(width * 0.5f, height * 0.4f),
                    radius = width * 0.9f
                )
            )

            // 2. Draw side ambient accent lights (Muted crimson on bottom left, neon indigo on top right)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF3B30).copy(alpha = 0.08f * pulseIntensity),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.1f, height * 0.9f),
                    radius = width * 0.5f
                )
            )

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF007AFF).copy(alpha = 0.12f * pulseIntensity),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.9f, height * 0.1f),
                    radius = width * 0.6f
                )
            )

            // 3. Draw vertical light lines (laser beams) with soft blur simulations
            // Beam 1: Left drift
            val beamX1 = width * (0.25f + 0.1f * sin(beamOffset1))
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF007AFF).copy(alpha = 0.08f * pulseIntensity),
                        dynamicAccent.copy(alpha = 0.14f * pulseIntensity),
                        Color(0xFF007AFF).copy(alpha = 0.08f * pulseIntensity),
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
                        Color(0xFFFF3B30).copy(alpha = 0.06f * pulseIntensity),
                        dynamicAccent.copy(alpha = 0.12f * pulseIntensity),
                        Color(0xFF9B51E0).copy(alpha = 0.08f * pulseIntensity),
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
                        Color(0xFF03050B).copy(alpha = 0.85f)
                    ),
                    startY = height * 0.6f,
                    endY = height
                )
            )
        }
    }
}
