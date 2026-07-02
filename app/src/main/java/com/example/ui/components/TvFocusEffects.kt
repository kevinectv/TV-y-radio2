package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
 * Handles D-pad selection zoom, elegant 3D lift/elevation ("resorte"), and vibrant borders on focus.
 */
fun Modifier.tvFocusEffect(
    shape: Shape = RoundedCornerShape(12.dp),
    focusedBorderColor: Color = Color.White,
    unfocusedBorderColor: Color = Color.Transparent,
    borderWidth: Dp = 1.8.dp,
    scaleAmount: Float = 1.05f,
    focusRequester: FocusRequester? = null,
    interactionSource: MutableInteractionSource? = null,
    onFocus: () -> Unit = {}
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "tvFocusEffect"
        properties["shape"] = shape
        properties["focusedBorderColor"] = focusedBorderColor
        properties["unfocusedBorderColor"] = unfocusedBorderColor
    }
) {
    val internalInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isHovered by internalInteractionSource.collectIsHoveredAsState()
    val isPressed by internalInteractionSource.collectIsPressedAsState()
    var isFocusedState by remember { mutableStateOf(false) }

    val isActive = isFocusedState || isHovered || isPressed

    LaunchedEffect(isActive) {
        if (isActive) {
            onFocus()
        }
    }

    // Smooth spring animation ("resorte") for lifting up when hovering/focusing
    val springSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    }

    val scale by animateFloatAsState(
        targetValue = if (isActive) scaleAmount else 1f,
        animationSpec = springSpec,
        label = "tv_focus_scale"
    )

    val liftY by animateFloatAsState(
        targetValue = if (isActive) -6f else 0f,
        animationSpec = springSpec,
        label = "tv_focus_lift"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isActive) 12f else 0f,
        animationSpec = springSpec,
        label = "tv_focus_elevation"
    )

    val focusBgOverlayAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.10f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "tv_focus_bg_overlay"
    )

    val currentBorderColor by animateColorAsState(
        targetValue = if (isActive) focusedBorderColor.copy(alpha = 0.85f) else unfocusedBorderColor,
        animationSpec = tween(durationMillis = 150),
        label = "tv_focus_border_color"
    )

    val baseModifier = this
        .hoverable(internalInteractionSource)
        .focusable(interactionSource = internalInteractionSource)
        .onFocusChanged { focusState ->
            isFocusedState = focusState.isFocused || focusState.hasFocus
        }
        .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            this.translationY = liftY * density // Smooth "resorte" lift animation
            this.shadowElevation = elevation
            this.shape = shape
            this.clip = false
        }
        .background(
            color = if (isActive) focusedBorderColor.copy(alpha = focusBgOverlayAlpha) else Color.Transparent,
            shape = shape
        )
        .border(
            width = borderWidth,
            color = currentBorderColor,
            shape = shape
        )

    if (focusRequester != null) {
        baseModifier.focusRequester(focusRequester)
    } else {
        baseModifier
    }
}

