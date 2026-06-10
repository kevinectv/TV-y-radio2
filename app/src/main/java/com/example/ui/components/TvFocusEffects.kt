package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
    focusedBorderColor: Color = Color.White,
    unfocusedBorderColor: Color = Color.Transparent,
    borderWidth: Dp = 3.dp,
    scaleAmount: Float = 1.02f
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "tvFocusEffect"
        properties["shape"] = shape
        properties["focusedBorderColor"] = focusedBorderColor
        properties["unfocusedBorderColor"] = unfocusedBorderColor
    }
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTabletOrTv = configuration.screenWidthDp >= 580

    if (!isTabletOrTv) {
        return@composed this
    }

    var isFocused by remember { mutableStateOf(false) }

    // Instant, ultra-responsive transitions for fast DPAD/remote navigation
    val scale by animateFloatAsState(
        targetValue = if (isFocused) scaleAmount else 1f,
        animationSpec = tween(durationMillis = 80),
        label = "tv_focus_scale"
    )

    val focusBgOverlayAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.16f else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "tv_focus_bg_overlay"
    )

    this
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused || focusState.hasFocus
        }
        .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            this.translationY = 0f // Completely disable vertical offset lift to avoid "resorte" layout shifting
            this.shadowElevation = if (isFocused) 8f else 0f
            this.shape = shape
            this.clip = false
        }
        .background(
            color = if (isFocused) focusedBorderColor.copy(alpha = focusBgOverlayAlpha) else Color.Transparent,
            shape = shape
        )
        .border(
            width = borderWidth,
            color = if (isFocused) focusedBorderColor else unfocusedBorderColor,
            shape = shape
        )
}
