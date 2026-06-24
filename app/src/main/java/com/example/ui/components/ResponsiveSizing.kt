package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun getResponsiveScale(): Float {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    return when {
        screenWidth >= 1440 -> 1.55f  // Very Large Smart TVs / UHD
        screenWidth >= 1200 -> 1.45f  // Large Smart TVs (Samsung, TCL 65"+ etc.)
        screenWidth >= 960 -> 1.30f   // Standard Smart TVs (TCL, Samsung, LG 43"-55")
        screenWidth >= 720 -> 1.18f   // Large Tablets / Wide displays
        screenWidth >= 480 -> 1.08f   // Small Tablets / Huge Phablets / Foldables
        screenWidth < 360 -> 0.92f    // Extremely small/narrow screens - scale down slightly to avoid narrow squishing
        else -> 1.0f                  // Normal phone
    }
}

@Composable
fun Dp.responsive(): Dp {
    return this * getResponsiveScale()
}

@Composable
fun TextUnit.responsive(): TextUnit {
    return (this.value * getResponsiveScale()).sp
}
