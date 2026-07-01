package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val logoUrl = loaded?.logoUrl ?: item.logoUrl
    val backdropUrl = loaded?.backdropUrl ?: item.backdropUrl ?: item.posterUrl

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
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, end = 48.dp, bottom = 12.dp, top = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // 1. IZQUIERDA: Logo o Título de la película (acostado al lado de la información)
                Box(
                    modifier = Modifier
                        .weight(0.40f)
                        .wrapContentHeight(),
                    contentAlignment = Alignment.BottomStart
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
                                .heightIn(max = 100.dp)
                                .fillMaxWidth(),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.BottomStart,
                            loading = { },
                            error = {
                                Text(
                                    text = richMeta.title,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = Color.White,
                                        letterSpacing = (-1).sp,
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.95f),
                                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                            blurRadius = 12f
                                        )
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 28.sp
                                )
                            }
                        )
                    } else {
                        Text(
                            text = richMeta.title,
                            style = TextStyle(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = Color.White,
                                letterSpacing = (-1).sp,
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.95f),
                                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                    blurRadius = 12f
                                )
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 28.sp
                        )
                    }
                }

                // 2. DERECHA: Logo plataforma, Porcentaje estrellas (puntuación), Año, Duración y Sinopsis
                Column(
                    modifier = Modifier
                        .weight(0.60f)
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Logo plataforma streaming "bien hecho"
                        val platformLogoUrl = if (!richMeta.platformLogoUrl.isNullOrBlank()) {
                            richMeta.platformLogoUrl
                        } else {
                            PlatformLogos[richMeta.platform]
                        }

                        if (!platformLogoUrl.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .height(26.dp)
                                    .widthIn(max = 95.dp)
                                    .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
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
                                    .height(26.dp)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = richMeta.platform, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Porcentaje de estrellas (puntuación)
                        val ratingFloatVal = (activeHeroLoadedDetails?.rating ?: currentMovie.rating).toFloatOrNull() ?: 7.8f
                        val percentScore = (ratingFloatVal * 10).toInt().coerceIn(10, 99)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFE5B91E), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$percentScore% • ${richMeta.ratingImdb}/10",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(text = "|", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                        Text(text = richMeta.year, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "|", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                        Text(text = richMeta.duration, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    }

                    // Sinopsis compacta y tags en 2 líneas
                    val extendedDescription = buildString {
                        append(richMeta.description)
                        if (richMeta.premiumBadges.isNotEmpty() || richMeta.techIndicators.isNotEmpty()) {
                            append(" • ")
                            val tags = richMeta.premiumBadges + richMeta.techIndicators
                            append(tags.joinToString(" • "))
                        }
                    }
                    
                    Text(
                        text = extendedDescription,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        lineHeight = 16.sp,
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
    ) {
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(500),
            label = "hero_content_fade"
        ) { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp.responsive(), vertical = 6.dp.responsive()),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // IZQUIERDA: Logo compacto de película + Puntuación en estrellas y Año
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp.responsive())
                ) {
                    // Logo o título compacto
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
                                .heightIn(max = 44.dp.responsive())
                                .widthIn(max = 150.dp.responsive()),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                            loading = { },
                            error = {
                                Text(
                                    text = richMeta.title,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp.responsive(),
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    } else {
                        Text(
                            text = richMeta.title,
                            style = TextStyle(
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp.responsive(),
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Píldora compacta de Puntuación (Porcentaje de estrellas) + Año
                    val ratingFloatVal = (activeHeroLoadedDetails?.rating ?: currentMovie.rating).toFloatOrNull() ?: 7.8f
                    val percentScore = (ratingFloatVal * 10).toInt().coerceIn(10, 99)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp.responsive(), vertical = 4.dp.responsive())
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFE5B91E),
                            modifier = Modifier.size(12.dp.responsive())
                        )
                        Spacer(modifier = Modifier.width(3.dp.responsive()))
                        Text(
                            text = "$percentScore%",
                            color = Color.White,
                            fontSize = 11.sp.responsive(),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(5.dp.responsive()))
                        Text(
                            text = "• ${richMeta.year}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp.responsive()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp.responsive()))

                // DERECHA: Logo plataforma de streaming compacto
                val platformLogoUrl = if (!richMeta.platformLogoUrl.isNullOrBlank()) {
                    richMeta.platformLogoUrl
                } else {
                    PlatformLogos[richMeta.platform]
                }

                if (!platformLogoUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .height(24.dp.responsive())
                            .widthIn(max = 70.dp.responsive())
                            .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp.responsive(), vertical = 3.dp.responsive()),
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
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp.responsive(), vertical = 3.dp.responsive()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = richMeta.platform,
                            color = Color.White,
                            fontSize = 10.sp.responsive(),
                            fontWeight = FontWeight.Bold
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
