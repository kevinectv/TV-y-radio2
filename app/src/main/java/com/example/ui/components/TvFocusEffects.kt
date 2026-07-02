package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A reusable focus modifier tailored for hybrid mobile/TV applications.
 * Highlights with a vibrant illuminated border. Optionally provides a spring lift effect when liftOnFocus is true.
 */
fun Modifier.tvFocusEffect(
    shape: Shape = RoundedCornerShape(12.dp),
    focusedBorderColor: Color = Color.White,
    unfocusedBorderColor: Color = Color.White.copy(alpha = 0.12f),
    borderWidth: Dp = 2.5.dp,
    scaleAmount: Float = 1.0f,
    liftOnFocus: Boolean = false,
    focusRequester: FocusRequester? = null,
    interactionSource: MutableInteractionSource? = null,
    onFocus: () -> Unit = {}
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "tvFocusEffect"
        properties["shape"] = shape
        properties["focusedBorderColor"] = focusedBorderColor
        properties["unfocusedBorderColor"] = unfocusedBorderColor
        properties["liftOnFocus"] = liftOnFocus
    }
) {
    val isHovered = interactionSource?.collectIsHoveredAsState()?.value ?: false
    val isFocusedFromSource = interactionSource?.collectIsFocusedAsState()?.value ?: false
    var isFocusedState by remember { mutableStateOf(false) }

    val isActive = isFocusedState || isFocusedFromSource || isHovered

    LaunchedEffect(isActive) {
        if (isActive) {
            onFocus()
        }
    }

    val density = LocalDensity.current.density

    val springSpec = remember {
        spring<Float>(
            dampingRatio = 0.55f, // Bouncy resorte effect
            stiffness = 380f
        )
    }

    val scale by animateFloatAsState(
        targetValue = if (isActive && liftOnFocus) (if (scaleAmount > 1.0f) scaleAmount else 1.15f) else 1f,
        animationSpec = if (liftOnFocus) springSpec else tween(durationMillis = 150),
        label = "tv_focus_scale"
    )

    val liftY by animateFloatAsState(
        targetValue = if (isActive && liftOnFocus) -12f else 0f,
        animationSpec = if (liftOnFocus) springSpec else tween(durationMillis = 150),
        label = "tv_focus_lift"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isActive && liftOnFocus) 16f else 0f,
        animationSpec = if (liftOnFocus) springSpec else tween(durationMillis = 150),
        label = "tv_focus_elevation"
    )

    val focusBgOverlayAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.12f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "tv_focus_bg_overlay"
    )

    val currentBorderColor by animateColorAsState(
        targetValue = if (isActive) focusedBorderColor else unfocusedBorderColor,
        animationSpec = tween(durationMillis = 150),
        label = "tv_focus_border_color"
    )

    val currentBorderWidth by animateDpAsState(
        targetValue = if (isActive) borderWidth.coerceAtLeast(2.5.dp) else borderWidth.coerceAtMost(1.2.dp),
        animationSpec = tween(durationMillis = 150),
        label = "tv_focus_border_width"
    )

    val baseModifier = this
        .onFocusEvent { focusState ->
            if (focusState.isFocused || focusState.hasFocus) {
                isFocusedState = true
            } else {
                isFocusedState = false
            }
        }
        .onFocusChanged { focusState ->
            isFocusedState = isFocusedState || focusState.isFocused || focusState.hasFocus
        }
        .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            this.translationY = liftY * density
            this.shadowElevation = elevation
            this.shape = shape
            this.clip = !liftOnFocus
        }
        .background(
            color = if (isActive) focusedBorderColor.copy(alpha = focusBgOverlayAlpha) else Color.Transparent,
            shape = shape
        )
        .border(
            width = currentBorderWidth,
            color = currentBorderColor,
            shape = shape
        )

    if (focusRequester != null) {
        baseModifier.focusRequester(focusRequester)
    } else {
        baseModifier
    }
}


