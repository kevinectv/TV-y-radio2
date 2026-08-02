package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.model.CatalogItem
import com.example.ui.components.responsive

@Composable
private fun getLocalNormalCardWidth(isWideLayout: Boolean): Dp =
    if (isWideLayout) 100.dp.responsive() else 115.dp.responsive()

@Composable
private fun getLocalExpandedCardWidth(isWideLayout: Boolean): Dp =
    if (isWideLayout) 200.dp.responsive() else 230.dp.responsive()

@Composable
private fun getLocalCardHeight(isWideLayout: Boolean): Dp =
    if (isWideLayout) 150.dp.responsive() else 172.dp.responsive()

/**
 * LuminaPremiumCard - Rebuilt from scratch to deliver an ultra-premium, modern, and highly-optimized
 * visual identity for Lumina on Android TV and mobile.
 *
 * Visual principles:
 * - Minimalist, elegant design. Zero unnecessary visual noise.
 * - Sized perfectly (slightly smaller to fit more cards on screen).
 * - Standard 2:3 aspect ratio posters that are never deformed or squished.
 * - Android TV Mode: Only shows the clean poster. No title/metadata underneath.
 * - Phone Mode: Displays a highly-polished, clean title and metadata row underneath the poster.
 * - Smooth luxury horizontal expansion in rows with active neighbor shifting and opacity fade.
 * - Lightweight graphicsLayer-based translations & scaling for guaranteed smooth 60fps.
 */
@Composable
fun LuminaPremiumCard(
    item: CatalogItem,
    layoutType: String = "Horizontal Poster Row",
    isFavorite: Boolean = false,
    progress: Float = 0f,
    onFocus: () -> Unit = {},
    onFocusChange: (Boolean, Boolean) -> Unit = { _, _ -> },
    isOtherFocusedInRow: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardIndex: Int = 0,
    focusedIndex: Int? = null,
    rank: Int? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val density = LocalDensity.current

    // Detect screen type
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    val isVerticalGrid = layoutType == "Vertical" && rank == null

    // Precise Card Sizing Metrics
    val normalWidth = getLocalNormalCardWidth(isWideLayout)
    val expandedWidth = getLocalExpandedCardWidth(isWideLayout)
    val cardHeight = getLocalCardHeight(isWideLayout)
    val widthDelta = expandedWidth - normalWidth

    // 1. High Performance Fluid Core Animations
    val scaleOnFocus by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale_focus"
    )

    // Horizontal width expansion for Carousel rows
    val animatedWidth by animateDpAsState(
        targetValue = if (isFocused) expandedWidth else normalWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "expanded_width"
    )

    // Smooth backdrop crossfade on expansion
    val backdropAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "crossfade_alpha"
    )

    // Pulse animation for the soft radial focus aura behind the focused card
    val infiniteTransition = rememberInfiniteTransition(label = "aura_pulse_transition")
    val breathePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    // 2. Neighboring Card Shift Dynamics (Carousel mode)
    val neighborScaleTarget = if (isOtherFocusedInRow) 0.94f else 1f
    val animatedNeighborScale by animateFloatAsState(
        targetValue = neighborScaleTarget,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "neighbor_scale"
    )

    val neighborAlphaTarget = if (isOtherFocusedInRow) 0.55f else 1f
    val animatedNeighborAlpha by animateFloatAsState(
        targetValue = neighborAlphaTarget,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "neighbor_alpha"
    )

    val neighborShiftTarget = if (isOtherFocusedInRow && focusedIndex != null) {
        val direction = if (cardIndex > focusedIndex) 1f else -1f
        14.dp * direction
    } else {
        0.dp
    }
    val animatedNeighborShift by animateDpAsState(
        targetValue = neighborShiftTarget,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "neighbor_shift"
    )

    // Main layout: On Phone (not isWideLayout), wrap the Card and the text underneath in a unified column.
    Column(
        modifier = modifier
            .onFocusChanged { state ->
                isFocused = state.isFocused
                onFocusChange(state.isFocused, isWideLayout)
                if (state.isFocused) {
                    onFocus()
                }
            }
            .focusable()
            .clickable { onClick() }
            // Apply standard touch target padding
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Visual Card Body Container
        val cardModifier = if (isVerticalGrid) {
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .zIndex(if (isFocused) 10f else 1f)
                .graphicsLayer {
                    scaleX = scaleOnFocus
                    scaleY = scaleOnFocus
                }
        } else {
            Modifier
                .width(animatedWidth)
                .height(cardHeight)
                .zIndex(if (isFocused) 10f else 1f)
                .graphicsLayer {
                    scaleX = animatedNeighborScale
                    scaleY = animatedNeighborScale
                    alpha = animatedNeighborAlpha
                    translationX = animatedNeighborShift.toPx()
                }
        }

        Box(
            modifier = cardModifier,
            contentAlignment = Alignment.CenterStart
        ) {
            // Soft Radiant Focus Ambient Glow
            if (isFocused) {
                val auraModifier = if (isVerticalGrid) {
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = breathePulse * 1.08f
                            scaleY = breathePulse * 1.06f
                        }
                } else {
                    Modifier
                        .fillMaxHeight()
                        .width(expandedWidth)
                        .graphicsLayer {
                            scaleX = breathePulse * 1.08f
                            scaleY = breathePulse * 1.06f
                        }
                }

                Box(
                    modifier = auraModifier.drawBehind {
                        val radialBrush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E5FF).copy(alpha = 0.28f),
                                Color(0xFF8B5CF6).copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                        drawRoundRect(
                            brush = radialBrush,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                        )
                    }
                )
            }

            // Main Card Surface Wrap (rounded & clipped)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = if (isFocused) 1.5.dp else 0.5.dp,
                        brush = if (isFocused) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00E5FF), // Pure Neon Cyan
                                    Color(0xFF8B5CF6)  // Elegant Amethyst Purple
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.08f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color(0xFF0D0E15))
            ) {
                if (isVerticalGrid) {
                    // Vertical Grid Visual: Always show high-quality full poster
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )

                    // Soft bottom darkening gradient shadow to protect title/progress legibility if needed
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.5f)
                                    ),
                                    startY = 0.5f
                                )
                            )
                    )
                } else {
                    // Horizontal Row Visual with Smooth Cinematic Expansion
                    Box(
                        modifier = Modifier
                            .width(expandedWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // 1. Unfocused/Compressed Poster View
                        AsyncImage(
                            model = item.posterUrl,
                            contentDescription = item.title,
                            modifier = Modifier
                                .width(normalWidth)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.CenterStart
                        )

                        // 2. Focused Crossfade Widescreen Backdrop View
                        if (isFocused || backdropAlpha > 0f) {
                            val backdropModel = if (!item.backdropUrl.isNullOrEmpty()) item.backdropUrl else item.posterUrl
                            AsyncImage(
                                model = backdropModel,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .width(expandedWidth)
                                    .fillMaxHeight()
                                    .graphicsLayer { alpha = backdropAlpha },
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.CenterStart
                            )

                            // Subtle overlay gradient on focused backdrop for cinema feel
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { alpha = backdropAlpha }
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.7f)
                                            ),
                                            startY = 0.4f
                                        )
                                    )
                            )
                        }
                    }
                }

                // Mini Glowing Progress Bar
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp.responsive())
                            .align(Alignment.BottomStart)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF00E5FF),
                                            Color(0xFF8B5CF6)
                                        )
                                    )
                                )
                        )
                    }
                }

                // Minimal Indicator Badges (Favorite Heart)
                if (isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(20.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // High-End 3D Rank Offset Number for Rank Rows (Bleeds elegantly to the left of the poster)
            if (rank != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-12).dp.responsive(), y = 6.dp.responsive())
                        .zIndex(15f)
                ) {
                    Text(
                        text = rank.toString(),
                        color = Color(0xFF08090E),
                        fontSize = 68.sp.responsive(),
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF00E5FF).copy(alpha = 0.9f),
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }

        // 3. Metadata Section (ONLY show on Phone views to preserve perfect TV minimalism)
        if (!isWideLayout) {
            Spacer(modifier = Modifier.height(6.dp))

            // Text wraps neatly, restricted to Card Width
            val textWidthModifier = if (isVerticalGrid) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.width(animatedWidth)
            }

            Column(
                modifier = textWidthModifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White.copy(alpha = if (isFocused) 1.0f else 0.9f),
                    fontSize = 11.sp.responsive(),
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.year,
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 9.sp.responsive()
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.genre,
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 9.sp.responsive(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
