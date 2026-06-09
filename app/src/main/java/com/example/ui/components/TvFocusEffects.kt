package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A reusable focus modifier tailored for hybrid mobile/TV applications.
 * Handles D-pad selection zoom, elegant 3D lift/elevation, and vibrant borders on focus.
 */
fun Modifier.tvFocusEffect(
    shape: Shape = RoundedCornerShape(12.dp),
    focusedBorderColor: Color = Color(0xFF4A89FF),
    unfocusedBorderColor: Color = Color.Transparent,
    borderWidth: Dp = 2.dp,
    scaleAmount: Float = 1.12f
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "tvFocusEffect"
        properties["shape"] = shape
        properties["focusedBorderColor"] = focusedBorderColor
        properties["unfocusedBorderColor"] = unfocusedBorderColor
    }
) {
    var isFocused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    
    // Convert 10dp to pixels dynamically so it is pronounced on TVs of all resolutions
    val liftPx = remember(density) { with(density) { 10.dp.toPx() } }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) scaleAmount else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 220f),
        label = "tv_focus_scale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (isFocused) -liftPx else 0f, // physically lifts up on Y axis gracefully
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 220f),
        label = "tv_focus_translation_y"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isFocused) 12f else 0f, // shadow depth
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 220f),
        label = "tv_focus_elevation"
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 220f),
        label = "tv_focus_border_alpha"
    )

    this
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused || focusState.hasFocus
        }
        .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            this.translationY = translationY
            this.shadowElevation = elevation
            this.shape = shape
            this.clip = false
        }
        .border(
            width = borderWidth,
            color = if (isFocused) focusedBorderColor.copy(alpha = borderAlpha) else unfocusedBorderColor,
            shape = shape
        )
}
