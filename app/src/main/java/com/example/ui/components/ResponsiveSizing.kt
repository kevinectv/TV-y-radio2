package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Universal dynamic scaling system for Lumina.
 * Auto-detects device classes based on screen width dp constraints.
 */
@Composable
fun calculateUiScale(): Float {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    return when {
        // TV / Smart TV / Chromecast / Android TV / Google TV / Fire TV
        screenWidth >= 960 -> 0.85f   // TV = 0.85f (15% visual reduction to show more content)
        // Tablets
        screenWidth >= 600 -> 0.92f   // Tablet = 0.92f (8% visual reduction to pack rows efficiently)
        // Phones
        else -> 1.0f                  // Phone = 1.0f (compact & perfectly fitted)
    }
}

/**
 * Global scale variable mapping to calculateUiScale()
 */
val UiScale: Float
    @Composable
    get() = calculateUiScale()

@Composable
fun getResponsiveScale(): Float {
    return UiScale
}

@Composable
fun Dp.responsive(): Dp {
    return this * getResponsiveScale()
}

@Composable
fun TextUnit.responsive(): TextUnit {
    return (this.value * getResponsiveScale()).sp
}

