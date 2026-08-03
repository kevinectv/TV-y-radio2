package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CatalogItem
import com.example.ui.MediaViewModel
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HomeHeroBannerMobile(
    currentMovie: CatalogItem,
    activeHeroLoadedDetails: LoadedTmdbDetails?,
    featuredMovies: List<CatalogItem>,
    favoriteCatalogItems: Set<String>,
    bannerHeight: androidx.compose.ui.unit.Dp,
    viewModel: MediaViewModel,
    scrollState: LazyListState,
    onTrailerClick: (CatalogItem) -> Unit,
    onDetailsClick: (CatalogItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .background(Color(0xFF030406))
    ) {
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(600),
            label = "hero_mobile_fade"
        ) { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            android.util.Log.d("LuminaHeroBanner", "Arrived at Hero Banner (Mobile) - Title: ${targetMovie.title}")
            val backdropUrlToUse = activeHeroLoadedDetails?.backdropUrl ?: targetMovie.backdropUrl ?: ""

            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Imagen de fondo vertical estilo móvil
                AsyncImage(
                    model = backdropUrlToUse,
                    contentDescription = richMeta.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // 2. Degradado superior suave
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )

                // 3. Degradado inferior dramático fundiéndose hacia el fondo de la app
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.85f),
                                    Color(0xFF030406)
                                ),
                                startY = 150f
                            )
                        )
                )

                // 4. Contenido vertical centrado
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = Color(0xFFE5B91E).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Whatshot,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "DESTACADO HOY",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!richMeta.logoUrl.isNullOrBlank()) {
                            val context = LocalContext.current
                            coil.compose.SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(richMeta.logoUrl)
                                    .crossfade(true)
                                    .allowHardware(false)
                                    .transformations(TrimTransparentPixelsTransformation())
                                    .build(),
                                contentDescription = richMeta.title,
                                modifier = Modifier
                                    .heightIn(max = 75.dp)
                                    .widthIn(max = 240.dp),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.Center,
                                loading = { },
                                error = {
                                    Text(
                                        text = richMeta.title,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 26.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        } else {
                            Text(
                                text = richMeta.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 26.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val platformLogoUrl = richMeta.platformLogoUrl

                        if (!platformLogoUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .height(22.dp)
                                    .widthIn(max = 75.dp)
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = platformLogoUrl,
                                    contentDescription = richMeta.platform,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .height(22.dp)
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = richMeta.platform, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        val ratingFloatVal = (activeHeroLoadedDetails?.rating ?: currentMovie.rating).toFloatOrNull() ?: 7.8f
                        val percentScore = (ratingFloatVal * 10).toInt().coerceIn(10, 99)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFE5B91E), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color.Black, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("$percentScore%", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Text("•", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Text(richMeta.year, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("•", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Text(richMeta.duration, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (richMeta.genres.isNotBlank()) {
                        Text(
                            text = richMeta.genres,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isFav = currentMovie.id in favoriteCatalogItems
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleCatalogItemFavorite(currentMovie.id) }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = "Lista",
                                tint = if (isFav) Color(0xFF00FF87) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isFav) "En Lista" else "Mi Lista",
                                color = if (isFav) Color(0xFF00FF87) else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { onTrailerClick(currentMovie) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Reproducir", modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("REPRODUCIR", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onDetailsClick(currentMovie) }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Info",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Info",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
