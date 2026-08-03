package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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

    val hash = item.title.hashCode()
    val absHash = if (hash < 0) -hash else hash
    val popScore = 150.0 + (absHash % 750) + (ratingFloat * 12)
    val popularityText = String.format(java.util.Locale.US, "%.1f", popScore)

    val idx = featuredMovies.indexOfFirst { it.id == item.id }
    val trendPosition = if (idx >= 0) idx + 1 else (absHash % 10) + 1
    val trendPositionText = "N.º $trendPosition en tendencias hoy"

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
            android.util.Log.d("LuminaHeroBanner", "Arrived at Hero Banner (TV) - Title: ${targetMovie.title}")
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 16.dp.responsive(), end = 48.dp, bottom = 8.dp.responsive(), top = 36.dp.responsive()),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.58f),
                    verticalArrangement = Arrangement.spacedBy(4.dp.responsive()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp.responsive()),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Badge TENDENCIA
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2563EB).copy(alpha = 0.22f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp.responsive(), vertical = 3.dp.responsive()),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp.responsive())
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Whatshot,
                                    contentDescription = null,
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(11.dp.responsive())
                                )
                                Text(
                                    text = "TENDENCIA",
                                    color = Color(0xFF93C5FD),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp.responsive(),
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        // Logo o Título (grande)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp.responsive()),
                            contentAlignment = Alignment.CenterStart
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
                                        .heightIn(max = 70.dp.responsive())
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
                        }
                    }

                    // Año | Duración | Género
                    Row(
                        modifier = Modifier.height(20.dp.responsive()),
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

                    // Sinopsis
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp.responsive()),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = richMeta.description,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp.responsive(),
                            maxLines = 2,
                            lineHeight = 18.sp.responsive(),
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Plataforma + IMDb + Clasificación
                    Row(
                        modifier = Modifier.height(26.dp.responsive()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp.responsive())
                    ) {
                        val platformLogoUrl = richMeta.platformLogoUrl
                        if (!platformLogoUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .height(24.dp.responsive())
                                    .widthIn(max = 85.dp.responsive())
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp.responsive(), vertical = 1.dp.responsive()),
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
                                    .height(24.dp.responsive())
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp.responsive(), vertical = 2.dp.responsive()),
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

                        val ageRating = targetMovie.classification?.ifBlank { null } ?: if ((richMeta.ratingImdb.toFloatOrNull() ?: 7.5f) >= 7.8f) "+16" else "+12"
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF0F0F15).copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp.responsive(), vertical = 1.dp.responsive()),
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

                    Spacer(modifier = Modifier.height(2.dp.responsive()))

                    // Botones de acción TV
                    Row(
                        modifier = Modifier.height(42.dp.responsive()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp.responsive()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .height(42.dp.responsive())
                                .background(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(21.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(21.dp)
                                )
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
                                fontSize = 13.sp.responsive(),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val isFav = targetMovie.id in favoriteCatalogItems
                        Box(
                            modifier = Modifier
                                .size(42.dp.responsive())
                                .background(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape
                                )
                                .padding(4.dp.responsive()),
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
