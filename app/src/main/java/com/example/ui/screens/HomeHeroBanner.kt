package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
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

private val PlatformLogos = mapOf(
    "Netflix" to "https://image.tmdb.org/t/p/w300/t2yyOv40HZeVlLjVrCsPhIdZfC4.jpg",
    "Disney+" to "https://image.tmdb.org/t/p/w300/7rwgEs15tFwyR9NPQ5vlURnb3x1.jpg",
    "Prime Video" to "https://image.tmdb.org/t/p/w300/5NyLm42TmCqCMOZFvH4fcoSNKEW.jpg",
    "Apple TV+" to "https://image.tmdb.org/t/p/w300/6uhKBfmtzFqOcLousHwZuzcrScK.jpg",
    "Max" to "https://image.tmdb.org/t/p/w300/c2uuPbxqFJoGtwAunvGqHk98jC8.jpg",
    "Hulu" to "https://image.tmdb.org/t/p/w300/giwM8XX4V2AQb9vsoN7yti82tKK.jpg",
    "Paramount+" to "https://image.tmdb.org/t/p/w300/xbhHHa1YgtpwhC8lb1NQ3ACVcLd.jpg",
    "Peacock" to "https://image.tmdb.org/t/p/w300/dB8G41Q6tSL5NBisrIeqByfepBc.jpg",
    "Crunchyroll" to "https://image.tmdb.org/t/p/w300/f6TRLB3H4jDpFEZ0z2KWSSvu1SB.jpg",
    "MUBI" to "https://image.tmdb.org/t/p/w300/aS2zvJWn9mwiCOZI4GAnkQxH0mL.jpg",
    "Pluto TV" to "https://image.tmdb.org/t/p/w300/4KAy34EHvRM25Ih8wb82AuGU7zJ.jpg",
    "Tubi TV" to "https://image.tmdb.org/t/p/w300/3QQKYFUD8x5g4PUNn7IEtqJWhJK.jpg"
)

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

    val logoUrl = if (!loaded?.logoUrl.isNullOrEmpty()) {
        if (loaded?.logoUrl!!.startsWith("/")) "https://image.tmdb.org/t/p/w500${loaded.logoUrl}" else loaded.logoUrl
    } else {
        item.getFullLogoUrl()
    }
    
    val backdropUrl = if (!loaded?.backdropUrl.isNullOrEmpty()) {
        if (loaded?.backdropUrl!!.startsWith("/")) "https://image.tmdb.org/t/p/original${loaded.backdropUrl}" else loaded.backdropUrl
    } else {
        item.getFullBackdropUrl()
    }

    val platformNames = listOf("Netflix", "Max", "Prime Video", "Disney+", "Apple TV+")
    val platformName = loaded?.platformName ?: platformNames[absHash % platformNames.size]
    val platformLogoUrl = loaded?.platformLogoUrl

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
            .height(bannerHeight)
    ) {
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(500),
            label = "hero_content_fade"
        ) { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 38.dp, end = 48.dp, bottom = 28.dp, top = 74.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.58f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // 1. Logo o Título en la parte superior del bloque (estilo cine / Apple TV+)
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
                                .heightIn(max = 85.dp)
                                .widthIn(max = 340.dp),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                            loading = { },
                            error = {
                                Text(
                                    text = richMeta.title,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp,
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
                                fontSize = 28.sp,
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

                    // 2. Línea 1 de metadatos: Fecha / Año | Género | Duración
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = richMeta.year,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(text = "|", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                        Text(
                            text = if (richMeta.genres.isNotBlank()) richMeta.genres else "Cine / Drama",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(text = "|", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                        Text(
                            text = richMeta.duration,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    // 3. Línea 2 de metadatos: Logo Plataforma | Badge IMDb | Badges adicionales
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Logo de plataforma
                        val platformLogoUrl = if (!richMeta.platformLogoUrl.isNullOrBlank()) {
                            richMeta.platformLogoUrl
                        } else {
                            PlatformLogos[richMeta.platform]
                        }

                        if (!platformLogoUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .widthIn(max = 85.dp)
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
                                    .height(24.dp)
                                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = richMeta.platform, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(text = "|", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)

                        // Badge IMDb amarillo auténtico como en la foto de referencia
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF5C518), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "IMDb",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = richMeta.ratingImdb,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Etiquetas técnicas / presupuesto
                        val extrasText = if (richMeta.premiumBadges.isNotEmpty()) {
                            richMeta.premiumBadges.joinToString(" • ")
                        } else if (richMeta.techIndicators.isNotEmpty()) {
                            richMeta.techIndicators.joinToString(" • ")
                        } else {
                            "4K HDR • Dolby Atmos"
                        }

                        Text(text = "|", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                        Text(
                            text = extrasText,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 4. Sinopsis clara debajo de los metadatos sin botones (estilo Apple TV+ televisión)
                    Text(
                        text = richMeta.description,
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 13.sp,
                        maxLines = 3,
                        lineHeight = 18.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
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
            val backdropUrlToUse = richMeta.backdropUrl

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
                        val platformLogoUrl = if (!richMeta.platformLogoUrl.isNullOrBlank()) {
                            richMeta.platformLogoUrl
                        } else {
                            PlatformLogos[richMeta.platform]
                        }

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
            val softwareBitmap = if (input.config == Bitmap.Config.HARDWARE || !input.isMutable) {
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
