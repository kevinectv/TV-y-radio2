package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CatalogItem
import com.example.ui.MediaViewModel
import com.example.ui.components.responsive
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
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

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HomeHeroBannerTv(
    featuredMovies: List<CatalogItem>,
    favoriteCatalogItems: Set<String>,
    bannerHeight: androidx.compose.ui.unit.Dp,
    viewModel: MediaViewModel,
    scrollState: LazyListState,
    onTrailerClick: (CatalogItem) -> Unit,
    onDetailsClick: (CatalogItem) -> Unit
) {
    if (featuredMovies.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight)
                .background(Color(0xFF030406))
        )
        return
    }

    var heroIndex by remember(featuredMovies) { mutableStateOf(0) }
    var autoRotateTrigger by remember { mutableStateOf(0) }

    // Automatic carousel rotation independent of cards, reset when manual navigation occurs
    LaunchedEffect(autoRotateTrigger, featuredMovies) {
        while (true) {
            kotlinx.coroutines.delay(8500L)
            if (featuredMovies.isNotEmpty()) {
                heroIndex = (heroIndex + 1) % featuredMovies.size
            }
        }
    }

    val currentMovie = featuredMovies[heroIndex.coerceIn(0, featuredMovies.size - 1)]
    val activeHeroLoadedDetails = remember(currentMovie.id) {
        LoadedTmdbDetails(
            description = currentMovie.description,
            rating = currentMovie.rating,
            year = currentMovie.year,
            logoUrl = currentMovie.logoUrl,
            backdropUrl = currentMovie.backdropUrl ?: "",
            duration = currentMovie.duration,
            genre = currentMovie.genre
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .background(Color(0xFF030406))
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft -> {
                            heroIndex = (heroIndex - 1 + featuredMovies.size) % featuredMovies.size
                            autoRotateTrigger++
                            true
                        }
                        Key.DirectionRight -> {
                            heroIndex = (heroIndex + 1) % featuredMovies.size
                            autoRotateTrigger++
                            true
                        }
                        Key.DirectionCenter, Key.Enter, Key.Spacebar -> {
                            onDetailsClick(currentMovie)
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        // Background Backdrop with crossfade for carousel changes
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(600),
            label = "hero_tv_backdrop_fade"
        ) { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = richMeta.backdropUrl.ifBlank { targetMovie.backdropUrl ?: targetMovie.posterUrl },
                    contentDescription = richMeta.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Cinematic gradient overlays for professional streaming look
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.92f),
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                endX = 1200f
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f),
                                    Color(0xFF030406)
                                )
                            )
                        )
                )
            }
        }

        // Main Content Area with click for details
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(500),
            label = "hero_tv_content_fade"
        ) { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onDetailsClick(targetMovie) }
                    .padding(start = 56.dp, end = 48.dp, top = 36.dp, bottom = 20.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Badge TENDENCIA / DESTACADO
                        Surface(
                            color = Color(0xFF3B82F6).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF60A5FA).copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Whatshot,
                                    contentDescription = null,
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = richMeta.trendPositionText ?: "TENDENCIA GLOBAL EN TV",
                                    color = Color(0xFF93C5FD),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Logo o Título Oficial
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp),
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
                                        .heightIn(max = 80.dp)
                                        .widthIn(max = 380.dp),
                                    contentScale = ContentScale.Fit,
                                    alignment = Alignment.CenterStart,
                                    loading = { },
                                    error = {
                                        Text(
                                            text = richMeta.title,
                                            style = TextStyle(
                                                fontWeight = FontWeight.Black,
                                                fontSize = 34.sp,
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
                                        fontSize = 34.sp,
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

                        // Año | Duración | Géneros | Plataforma | IMDb | Clasificación
                        Row(
                            modifier = Modifier.height(26.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = richMeta.year,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(text = "•", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                            Text(
                                text = richMeta.duration,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(text = "•", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                            Text(
                                text = richMeta.genres,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        // Plataforma + IMDb + Clasificación independientes
                        Row(
                            modifier = Modifier.height(28.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val platformLogoUrl = richMeta.platformLogoUrl
                            if (!platformLogoUrl.isNullOrBlank()) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .widthIn(max = 85.dp)
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = platformLogoUrl,
                                            contentDescription = richMeta.platform,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    color = Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                                ) {
                                    Text(
                                        text = richMeta.platform,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_imdb),
                                    contentDescription = "IMDb",
                                    modifier = Modifier
                                        .height(18.dp)
                                        .width(36.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = richMeta.ratingImdb,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            val ageRating = targetMovie.classification?.ifBlank { null } ?: "+16"
                            Surface(
                                color = Color(0xFF1F2937),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = ageRating,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Sinopsis corta
                        Text(
                            text = richMeta.description,
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 14.sp,
                            maxLines = 2,
                            lineHeight = 20.sp,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 45.dp)
                        )
                    }

                    // Botón inferior secundario (ej. Más información / Ver detalles)
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .clickable { onDetailsClick(targetMovie) }
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Más información",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // PERMANENT VISUAL FLOATING PLAY BUTTON (Purely design element, non-interactive)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 80.dp, bottom = 48.dp)
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(64.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Reproducir",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Centered Carousel Indicators with active pill shape representing all banners
        val currentIndex = featuredMovies.indexOfFirst { it.id == currentMovie.id }.coerceAtLeast(0)
        if (featuredMovies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                featuredMovies.forEachIndexed { index, _ ->
                    val isActive = index == currentIndex
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isActive) 24.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .clickable { 
                                heroIndex = index 
                                autoRotateTrigger++
                            }
                            .background(
                                color = if (isActive) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

