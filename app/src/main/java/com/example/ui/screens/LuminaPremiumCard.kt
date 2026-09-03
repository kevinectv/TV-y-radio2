package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    if (isWideLayout) 230.dp.responsive() else 115.dp.responsive()

@Composable
private fun getLocalCardHeight(isWideLayout: Boolean): Dp =
    if (isWideLayout) 130.dp.responsive() else 172.dp.responsive()

/**
 * LuminaPremiumCard - Modern landscape card design for Movies and Series on Android TV.
 *
 * Visual principles:
 * - Horizontal layout (16:9 ratio, wider than tall).
 * - Fixed size: absolutely NO expansion, zoom, or scale on focus.
 * - Backdrop/horizontal image as the primary visual layer.
 * - Movie/Series logo overlaid proportionally on the image when available.
 * - If no logo is available, displays the horizontal image with subtle title text.
 * - Focus is strictly a visual indicator: crisp, pure white border (Color.White, 2.5.dp).
 * - Mobile/phone layout remains unchanged with vertical 2:3 poster and metadata underneath.
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
    val isTv = remember(context) { isAndroidTvDevice(context) }
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580 || isTv
    val isVerticalGrid = layoutType == "Vertical" && rank == null

    // Fixed Card Sizing Metrics: NO scale, NO expansion
    val normalWidth = getLocalNormalCardWidth(isWideLayout)
    val cardHeight = getLocalCardHeight(isWideLayout)

    // Image to display: On TV/wide, prioritize horizontal backdrop; fallback to poster
    val imageUrl = remember(isWideLayout, item.backdropUrl, item.posterUrl) {
        if (isWideLayout) {
            when {
                !item.backdropUrl.isNullOrBlank() -> item.backdropUrl
                !item.posterUrl.isNullOrBlank() -> item.posterUrl
                else -> ""
            }
        } else {
            item.posterUrl
        }
    }

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
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        val cardFrameModifier = if (isVerticalGrid) {
            Modifier
                .fillMaxWidth()
                .aspectRatio(if (isWideLayout) (16f / 9f) else (2f / 3f))
        } else {
            Modifier
                .width(normalWidth)
                .height(cardHeight)
        }

        Box(
            modifier = cardFrameModifier
                .zIndex(if (isFocused) 2f else 1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D0E15))
                .border(
                    width = if (isFocused) 2.5.dp else 1.dp,
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Horizontal Backdrop / Poster Image
            AsyncImage(
                model = imageUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            )

            // Cinematic gradient overlay for depth and logo readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isWideLayout) 0.60f else 0.45f)
                            ),
                            startY = 0.35f
                        )
                    )
            )

            // TV Mode: Movie/Series Logo overlay or Title text if logo is not available
            if (isWideLayout) {
                if (!item.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.logoUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.68f)
                            .fillMaxHeight(0.48f)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp.responsive(), vertical = 10.dp.responsive())
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 13.sp.responsive(),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Progress Bar (if watched/in-progress)
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

            // Favorite Indicator Heart Badge
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

            // Numbered rank for Top rows
            if (rank != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-8).dp.responsive(), y = 6.dp.responsive())
                        .zIndex(15f)
                ) {
                    Text(
                        text = rank.toString(),
                        color = Color(0xFF08090E),
                        fontSize = 54.sp.responsive(),
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.White.copy(alpha = 0.9f),
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }

        // Metadata Section (Phone mode only: vertical poster has title/year/genre underneath)
        if (!isWideLayout) {
            Spacer(modifier = Modifier.height(6.dp))

            val textWidthModifier = if (isVerticalGrid) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.width(normalWidth)
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
