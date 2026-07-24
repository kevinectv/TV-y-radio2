package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import com.example.ui.components.tvFocusEffect
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
import com.example.ui.components.responsive

import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import coil.transform.Transformation
import android.graphics.Bitmap



data class LoadedTmdbDetails(
    val description: String,
    val rating: String,
    val year: String,
    val logoUrl: String?,
    val backdropUrl: String?,
    val duration: String? = null,
    val genre: String? = null,
    val platformName: String? = null,
    val platformLogoUrl: String? = null
)

data class RichHeroMetadata(
    val title: String,
    val description: String,
    val year: String,
    val genres: String,
    val duration: String,
    val ratingImdb: String,
    val ratingTmdb: String,
    val popularityText: String,
    val trendPositionText: String?,
    val premiumBadges: List<String>,
    val techIndicators: List<String>,
    val logoUrl: String?,
    val backdropUrl: String,
    val platform: String,
    val platformLogoUrl: String? = null
)

fun resolveHeroMetadata(
    item: CatalogItem,
    loaded: LoadedTmdbDetails?,
    featuredMovies: List<CatalogItem>
): RichHeroMetadata {
    val title = item.title

    // Resolve description - remove placeholders!
    val rawDesc = loaded?.description ?: item.description
    val filteredDesc = if (rawDesc.contains("Contenido sintonizado") || rawDesc.contains("sintonizado en Lumina") || rawDesc.trim().isEmpty() || rawDesc.contains("película espectacular llena de misterios")) {
        "Disfruta de ${item.title}, una sensacional producción de ${loaded?.genre ?: item.genre} con una cautivadora historia, actuaciones memorables y un asombroso despliegue visual en alta definición."
    } else {
        rawDesc
    }

    val year = loaded?.year?.ifEmpty { null } ?: item.year.ifEmpty { "2024" }
    
    val rawGenres = loaded?.genre ?: item.genre
    val genres = if (rawGenres.isEmpty()) "Acción / Drama" else rawGenres

    val duration = loaded?.duration ?: item.duration ?: run {
        if (item.isTvShow) "4 Temporadas" else "2h 15m"
    }

    val ratingFloat = (loaded?.rating ?: item.rating).toFloatOrNull() ?: 7.8f
    val tRating = String.format(java.util.Locale.US, "%.1f", ratingFloat)
    val imdbCalculated = (ratingFloat - 0.2f).coerceIn(1.0f, 10.0f)
    val iRating = String.format(java.util.Locale.US, "%.1f", imdbCalculated)

    // Deteministic popularity
    val hash = item.title.hashCode()
    val absHash = if (hash < 0) -hash else hash
    val popScore = 150.0 + (absHash % 750) + (ratingFloat * 12)
    val popularityText = String.format(java.util.Locale.US, "%.1f", popScore)

    // Position trend
    val idx = featuredMovies.indexOfFirst { it.id == item.id }
    val trendPosition = if (idx >= 0) idx + 1 else (absHash % 10) + 1
    val trendPositionText = "N.º $trendPosition en tendencias hoy"

    // Premium Badges
    val premiumBadges = mutableListOf<String>()
    if (ratingFloat >= 8.2f) {
        premiumBadges.add("Tendencia Global")
        premiumBadges.add("Top 10")
    } else if (ratingFloat >= 7.6f) {
        premiumBadges.add("Popular esta semana")
        premiumBadges.add("Recomendado de Lumina")
    } else {
        premiumBadges.add("Recomendado para ti")
    }

    val yearVal = year.toIntOrNull() ?: 2024
    if (yearVal >= 2025) {
        premiumBadges.add("Estreno")
    } else if (yearVal >= 2024) {
        premiumBadges.add("Nuevo")
    }

    // Technology capabilities
    val techIndicators = mutableListOf<String>()
    if (absHash % 2 == 0) {
        techIndicators.add("4K")
        techIndicators.add("HDR")
    } else {
        techIndicators.add("HD")
        techIndicators.add("HDR10")
    }
    if (absHash % 3 == 0) {
        techIndicators.add("Dolby Vision")
    }
    if (absHash % 4 == 0) {
        techIndicators.add("Dolby Atmos")
    } else {
        techIndicators.add("5.1 Audio")
    }
    techIndicators.add("Español (ES)")
    if (absHash % 2 == 0) {
        techIndicators.add("Subtítulos (CC)")
    } else {
        techIndicators.add("Subtítulos")
    }

    val rawLogoUrl = loaded?.logoUrl ?: item.logoUrl
    val logoUrl = if (rawLogoUrl.isNullOrBlank() || rawLogoUrl == "null" || rawLogoUrl == "NULL") null else rawLogoUrl
    val backdropUrl = loaded?.backdropUrl ?: item.backdropUrl ?: ""

    val platformNames = listOf("Netflix", "Max", "Prime Video", "Disney+", "Apple TV+")
    val platformName = if (!item.platform.isNullOrBlank()) item.platform else (loaded?.platformName ?: platformNames[absHash % platformNames.size])
    val platformLogoUrl = if (!item.platformLogo.isNullOrBlank()) item.platformLogo else loaded?.platformLogoUrl

    return RichHeroMetadata(
        title = title,
        description = filteredDesc,
        year = year,
        genres = genres,
        duration = duration,
        ratingImdb = iRating,
        ratingTmdb = tRating,
        popularityText = popularityText,
        trendPositionText = trendPositionText,
        premiumBadges = premiumBadges,
        techIndicators = techIndicators,
        logoUrl = logoUrl,
        backdropUrl = backdropUrl,
        platform = platformName,
        platformLogoUrl = platformLogoUrl
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HomeHeroBanner(
    currentMovie: CatalogItem,
    activeHeroLoadedDetails: LoadedTmdbDetails?,
    featuredMovies: List<CatalogItem>,
    favoriteCatalogItems: Set<String>,
    bannerHeight: androidx.compose.ui.unit.Dp,
    isWideLayout: Boolean,
    viewModel: MediaViewModel,
    scrollState: LazyListState,
    onTrailerClick: (CatalogItem) -> Unit,
    onDetailsClick: (CatalogItem) -> Unit
) {
    if (isWideLayout) {
        HomeHeroBannerTv(currentMovie, activeHeroLoadedDetails, featuredMovies, favoriteCatalogItems, bannerHeight, viewModel, scrollState, onTrailerClick, onDetailsClick)
    } else {
        HomeHeroBannerMobile(currentMovie, activeHeroLoadedDetails, featuredMovies, favoriteCatalogItems, bannerHeight, viewModel, scrollState, onTrailerClick, onDetailsClick)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HomeHeroBannerTv(
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
            .heightIn(min = bannerHeight)
    ) {
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(500),
            label = "hero_content_fade"
        ) { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            android.util.Log.d("LuminaHeroBanner", "Arrived at Hero Banner (TV) - Title: ${targetMovie.title}, Logo: ${richMeta.logoUrl}, Cast: ${targetMovie.castJson}")
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 16.dp.responsive(), end = 48.dp, bottom = 12.dp.responsive(), top = 84.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.58f),
                    verticalArrangement = Arrangement.spacedBy(8.dp.responsive()),
                    horizontalAlignment = Alignment.Start
                ) {
                    // 0. Badge TENDENCIA
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2563EB).copy(alpha = 0.22f), CircleShape)
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.45f), CircleShape)
                            .padding(horizontal = 12.dp.responsive(), vertical = 4.dp.responsive()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔥 TENDENCIA",
                            color = Color(0xFF93C5FD),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp.responsive(),
                            letterSpacing = 0.8.sp
                        )
                    }

                    // 1. Logo o Título (grande)
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
                                .heightIn(max = 80.dp.responsive())
                                .widthIn(max = 340.dp.responsive()),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                            loading = { },
                            error = {
                                Text(
                                    text = richMeta.title,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp.responsive(),
                                        color = Color.White,
                                        letterSpacing = (-1).sp,
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.9f),
                                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                            blurRadius = 8f
                                        )
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
                                fontSize = 28.sp.responsive(),
                                color = Color.White,
                                letterSpacing = (-1).sp,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.9f),
                                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                    blurRadius = 8f
                                )
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 2. Línea de metadatos: Año | Duración | Género (perfectamente ordenados)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp.responsive())
                    ) {
                        Text(
                            text = richMeta.year,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp.responsive()
                        )
                        Text(text = "•", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp.responsive())
                        Text(
                            text = richMeta.duration,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp.responsive()
                        )
                        Text(text = "•", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp.responsive())
                        Text(
                            text = if (richMeta.genres.isNotBlank()) richMeta.genres else "Cine / Drama",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp.responsive()
                        )
                    }

                    // 3. Sinopsis clara debajo de los metadatos
                    Text(
                        text = richMeta.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp.responsive(),
                        maxLines = 3,
                        lineHeight = 18.sp.responsive(),
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 4. Línea de metadatos 2: Logo Plataforma + Calificación IMDb + Clasificación por edad
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp.responsive())
                    ) {
                        // Logo de plataforma o badge de texto
                        val platformLogoUrl = richMeta.platformLogoUrl
                        if (!platformLogoUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .height(24.dp.responsive())
                                    .widthIn(max = 85.dp.responsive())
                                    .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                coil.compose.AsyncImage(
                                    model = platformLogoUrl,
                                    contentDescription = richMeta.platform,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .height(24.dp.responsive())
                                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = richMeta.platform,
                                    color = Color.White,
                                    fontSize = 11.sp.responsive(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(text = "|", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp.responsive())

                        // Badge IMDb
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp.responsive())
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_imdb),
                                contentDescription = "IMDb Logo",
                                modifier = Modifier
                                    .height(18.dp.responsive())
                                    .width(36.dp.responsive()),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = richMeta.ratingImdb,
                                color = Color.White,
                                fontSize = 13.sp.responsive(),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Text(text = "|", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp.responsive())

                        // Clasificación por edad (age rating / classification)
                        val ageRating = targetMovie.classification?.ifBlank { null } ?: if ((richMeta.ratingImdb.toFloatOrNull() ?: 7.5f) >= 7.8f) "+16" else "+12"
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                                .border(0.8.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 7.dp.responsive(), vertical = 2.5.dp.responsive()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ageRating,
                                color = Color.White,
                                fontSize = 9.sp.responsive(),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp.responsive()))

                    // 5. Botones de acción: Reproducir y + (Favoritos)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp.responsive()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val playInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        val isPlayFocused = playInteractionSource.collectIsFocusedAsState().value
                        val playScale by animateFloatAsState(
                            targetValue = if (isPlayFocused) 1.04f else 1.0f,
                            animationSpec = tween(durationMillis = 150),
                            label = "playScale"
                        )
                        
                        Row(
                            modifier = Modifier
                                .height(44.dp.responsive())
                                .graphicsLayer {
                                    scaleX = playScale
                                    scaleY = playScale
                                }
                                .background(
                                    color = if (isPlayFocused) Color(0xFF6C5CE7).copy(alpha = 0.95f) else Color(0xFF6C5CE7).copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .border(
                                    width = if (isPlayFocused) 2.dp else 1.dp,
                                    color = if (isPlayFocused) Color.White else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .clickable(
                                    interactionSource = playInteractionSource,
                                    indication = null
                                ) {
                                    onTrailerClick(targetMovie)
                                }
                                .padding(horizontal = 24.dp.responsive()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Reproducir",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp.responsive())
                            )
                            Spacer(modifier = Modifier.width(6.dp.responsive()))
                            Text(
                                text = "Reproducir",
                                color = Color.White,
                                fontSize = 14.sp.responsive(),
                                fontWeight = FontWeight.Bold
                              )
                        }

                        val favInteractionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        val isFavFocused = favInteractionSource.collectIsFocusedAsState().value
                        val isFav = targetMovie.id in favoriteCatalogItems
                        val favScale by animateFloatAsState(
                            targetValue = if (isFavFocused) 1.04f else 1.0f,
                            animationSpec = tween(durationMillis = 150),
                            label = "favScale"
                        )

                        Box(
                            modifier = Modifier
                                .size(44.dp.responsive())
                                .graphicsLayer {
                                    scaleX = favScale
                                    scaleY = favScale
                                }
                                .background(
                                    color = if (isFavFocused) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isFavFocused) 2.dp else 1.dp,
                                    color = if (isFavFocused) Color.White else Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape
                                )
                                .clickable(
                                    interactionSource = favInteractionSource,
                                    indication = null
                                ) {
                                    viewModel.toggleCatalogItemFavorite(targetMovie.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = "Favorito",
                                tint = if (isFav) Color(0xFF00FF87) else Color.White,
                                modifier = Modifier.size(20.dp.responsive())
                            )
                        }
                    }
                }
            }

            // 6. Indicadores del carrusel en la esquina inferior derecha
            val currentIndex = featuredMovies.indexOfFirst { it.id == targetMovie.id }.coerceAtLeast(0)
            if (featuredMovies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 48.dp, bottom = 12.dp.responsive()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp.responsive()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    featuredMovies.take(6).forEachIndexed { index, _ ->
                        val isActive = index == currentIndex
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 8.dp.responsive() else 6.dp.responsive())
                                .background(
                                    color = if (isActive) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}


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
            android.util.Log.d("LuminaHeroBanner", "Arrived at Hero Banner (Mobile) - Title: ${targetMovie.title}, Logo: ${richMeta.logoUrl}, Cast: ${targetMovie.castJson}")
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

                // 3. Degradado inferior dramático fundiéndose hacia el fondo de la app (0xFF030406)
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
                    // Badge Tendencia
                    Surface(
                        color = Color(0xFFE5B91E).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "🔥 DESTACADO HOY",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Logo o Título Centrado
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fila de Metadata (Logo Plataforma + Estrellas Puntuación % + Año + Duración)
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
                                    .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                coil.compose.AsyncImage(
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
                                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
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

                    // Géneros
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

                    // Botones de acción estilo App Móvil (Lista - Reproducir - Info)
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
                            modifier = Modifier.height(44.dp)
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

class TrimTransparentPixelsTransformation : Transformation {
    override val cacheKey: String = "TrimTransparentPixelsTransformation_v2"

    override suspend fun transform(input: Bitmap, size: coil.size.Size): Bitmap {
        return try {
            val isHardware = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && input.config == Bitmap.Config.HARDWARE
            val softwareBitmap = if (isHardware || !input.isMutable) {
                input.copy(Bitmap.Config.ARGB_8888, true) ?: return input
            } else {
                input
            }
            val width = softwareBitmap.width
            val height = softwareBitmap.height
            if (width <= 0 || height <= 0 || width * height > 16_000_000) return input

            val pixels = IntArray(width * height)
            softwareBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            var minX = width
            var minY = height
            var maxX = -1
            var maxY = -1

            for (y in 0 until height) {
                val rowOffset = y * width
                for (x in 0 until width) {
                    val alpha = (pixels[rowOffset + x] ushr 24) and 0xFF
                    // Use alpha threshold (> 10) to ignore near-transparent glows/shadows and empty padding
                    if (alpha > 10) {
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
            }

            if (maxX < minX || maxY < minY) return input

            if (minX == 0 && minY == 0 && maxX == width - 1 && maxY == height - 1) {
                return if (softwareBitmap !== input) softwareBitmap else input
            }

            Bitmap.createBitmap(softwareBitmap, minX, minY, maxX - minX + 1, maxY - minY + 1)
        } catch (e: Throwable) {
            input
        }
    }
}
