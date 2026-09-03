package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * TvPivotBringIntoViewSpec:
 * Professional TV BringIntoView specification for horizontal content rows.
 * Maintains a stable focus pivot zone at the desired offset (76.dp, aligning with the TV start padding),
 * allowing cards to scroll smoothly underneath the focus frame and beneath the lateral menu drawer.
 */
@OptIn(ExperimentalFoundationApi::class)
class TvPivotBringIntoViewSpec(
    private val pivotOffsetPx: Float
) : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
        // Aligns the leading edge of the active item with the stable pivot offset
        return offset - pivotOffsetPx
    }
}

data class TvFocusBounds(
    val rect: Rect,
    val shapeRadius: Dp = 12.dp,
    val key: Any? = null
)

interface TvFocusReporter {
    fun onCardFocused(bounds: TvFocusBounds) {}
    fun onCardUnfocused(key: Any?) {}
}

val LocalTvFocusReporter = compositionLocalOf<TvFocusReporter?> { null }
val LocalTvRowCoordinates = compositionLocalOf<(() -> LayoutCoordinates?)?> { null }

/**
 * TvFocusRowContainer:
 * Manages TV row presentation with a stable focus pivot zone.
 * On wide/TV layouts, provides a custom BringIntoViewSpec so that as items in the row
 * receive focus, they scroll beneath a stable focus zone, and content scrolls under the lateral drawer.
 * On phone/narrow screens, defaults to standard mobile layout with zero side effects.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvFocusRowContainer(
    modifier: Modifier = Modifier,
    isRowActive: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val isTv = remember(context) { com.example.ui.screens.isAndroidTvDevice(context) }
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580 || isTv

    if (isWideLayout) {
        val pivotOffsetPx = with(density) { 76.dp.toPx() }
        val pivotBringIntoViewSpec = remember(pivotOffsetPx) {
            TvPivotBringIntoViewSpec(pivotOffsetPx)
        }
        CompositionLocalProvider(LocalBringIntoViewSpec provides pivotBringIntoViewSpec) {
            Box(modifier = modifier) {
                content()
            }
        }
    } else {
        Box(modifier = modifier) {
            content()
        }
    }
}

