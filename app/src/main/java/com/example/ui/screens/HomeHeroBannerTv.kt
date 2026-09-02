package com.example.ui.screens

import com.example.ui.components.responsive


import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CatalogItem
import com.example.ui.MediaViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import coil.request.ImageRequest
import coil.transform.Transformation
import android.graphics.Bitmap
import kotlinx.coroutines.launch

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
    val logoUrl: String?,
    val backdropUrl: String,
    val platform: String = "Netflix",
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
        "Disfruta de ${item.title}, una sensacional producción con una cautivadora historia, actuaciones memorables y un asombroso despliegue visual en alta definición."
    } else {
        rawDesc
    }

    val year = loaded?.year?.ifEmpty { null } ?: item.year.ifEmpty { "2024" }
    
    val rawGenres = loaded?.genre ?: item.genre
    val genres = if (rawGenres.isEmpty()) "Drama" else rawGenres.split("/")[0].trim()

    val duration = loaded?.duration ?: item.duration ?: run {
        if (item.isTvShow) "Serie" else "2h 15m"
    }

    val rawLogoUrl = loaded?.logoUrl ?: item.logoUrl
    val logoUrl = if (rawLogoUrl.isNullOrBlank() || rawLogoUrl == "null" || rawLogoUrl == "NULL") null else rawLogoUrl
    val backdropUrl = loaded?.backdropUrl ?: item.backdropUrl ?: ""

    val platformNames = listOf("Netflix", "Max", "Prime Video", "Disney+", "Apple TV+")
    val hash = item.title.hashCode()
    val absHash = if (hash < 0) -hash else hash
    val platformName = if (!item.platform.isNullOrBlank()) item.platform else (loaded?.platformName ?: platformNames[absHash % platformNames.size])
    val platformLogoUrl = if (!item.platformLogo.isNullOrBlank()) item.platformLogo else loaded?.platformLogoUrl

    return RichHeroMetadata(
        title = title,
        description = filteredDesc,
        year = year,
        genres = genres,
        duration = duration,
        logoUrl = logoUrl,
        backdropUrl = backdropUrl,
        platform = platformName,
        platformLogoUrl = platformLogoUrl
    )
}

class TrimTransparentPixelsTransformation : Transformation {
    override val cacheKey: String = "TrimTransparentPixelsTransformation_v3"

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
        )
        return
    }

    var heroIndex by remember(featuredMovies) { mutableStateOf(0) }
    var autoRotateTrigger by remember { mutableStateOf(0) }
    val playButtonFocusRequester = remember { FocusRequester() }
    var isHeroFocused by remember { mutableStateOf(false) }

    // Live Clock ticker for the Hero header
    var timeString by remember { mutableStateOf("12:00 PM") }
    LaunchedEffect(Unit) {
        while (true) {
            val calendar = java.util.Calendar.getInstance()
            val rawHour = calendar.get(java.util.Calendar.HOUR)
            val min = calendar.get(java.util.Calendar.MINUTE)
            val hour = if (rawHour == 0) 12 else rawHour
            val amPm = if (calendar.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
            timeString = String.format("%d:%02d %s", hour, min, amPm)
            kotlinx.coroutines.delay(1000)
        }
    }

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

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWideLayout = configuration.screenWidthDp >= 580

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 24.dp.responsive())
    ) {
        // 1. TOP HEADER: Clean Lumina Brand Logo & Live Time
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp.responsive(),
                    end = 8.dp.responsive(),
                    top = 16.dp.responsive(),
                    bottom = 12.dp.responsive()
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.lumina_logo_custom),
                contentDescription = "Lumina",
                modifier = Modifier
                    .height(if (isWideLayout) 28.dp.responsive() else 22.dp.responsive())
                    .widthIn(max = 140.dp.responsive()),
                contentScale = ContentScale.Fit
            )

            Text(
                text = timeString,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = if (isWideLayout) 12.sp.responsive() else 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }

        // 2. HERO FEATURE CARD CONTAINER (Exact spec from reference images)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(316.dp.responsive())
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF141724),
                            Color(0xFF0C0E17)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = if (isHeroFocused) Color.White else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(24.dp)
                )
                .focusRequester(playButtonFocusRequester)
                .onFocusChanged { isHeroFocused = it.isFocused }
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.key) {
                            Key.DirectionLeft -> {
                                if (heroIndex == 0) {
                                    false // Let focus bubble up to the drawer
                                } else {
                                    heroIndex = (heroIndex - 1 + featuredMovies.size) % featuredMovies.size
                                    autoRotateTrigger++
                                    true
                                }
                            }
                            Key.DirectionRight -> {
                                heroIndex = (heroIndex + 1) % featuredMovies.size
                                autoRotateTrigger++
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
                .focusable()
                .clickable { onDetailsClick(currentMovie) }
        ) {
            // Crossfade transitions when cycling featured content
            Crossfade(
                targetState = currentMovie,
                animationSpec = tween(400),
                label = "hero_tv_card_fade",
                modifier = Modifier.fillMaxSize()
            ) { targetMovie ->
                val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // A. Backdrop Image (Framed on the right & background)
                    val context = LocalContext.current
                    val backdropModel = remember(richMeta.backdropUrl, targetMovie.backdropUrl, targetMovie.posterUrl) {
                        ImageRequest.Builder(context)
                            .data(richMeta.backdropUrl.ifBlank { targetMovie.backdropUrl ?: targetMovie.posterUrl })
                            .crossfade(200)
                            .build()
                    }

                    AsyncImage(
                        model = backdropModel,
                        contentDescription = richMeta.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.CenterEnd
                    )

                    // B. Progressive Dark Gradient Scrim (Horizontal blend for left text legibility)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color(0xFF0C0E17).copy(alpha = 0.98f),
                                        0.35f to Color(0xFF0C0E17).copy(alpha = 0.92f),
                                        0.55f to Color(0xFF0C0E17).copy(alpha = 0.65f),
                                        0.75f to Color(0xFF0C0E17).copy(alpha = 0.25f),
                                        1.0f to Color.Transparent
                                    )
                                )
                            )
                    )

                    // C. Progressive Dark Gradient Scrim (Vertical subtle bottom shade)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Transparent,
                                        0.60f to Color.Transparent,
                                        1.0f to Color(0xFF0C0E17).copy(alpha = 0.60f)
                                    )
                                )
                            )
                    )

                    // D. Left Content Area (Specs: Subtitle, Title, Description, Watch Now Button)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 340.dp.responsive())
                            .padding(
                                start = 32.dp.responsive(),
                                top = 32.dp.responsive(),
                                bottom = 32.dp.responsive(),
                                end = 16.dp.responsive()
                            ),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp.responsive())
                        ) {
                            // Subtitle Metadata Line: Category • Year • Duration
                            val metadataParts = listOfNotNull(
                                richMeta.genres.ifEmpty { null },
                                richMeta.year.ifEmpty { null },
                                richMeta.duration.ifEmpty { null }
                            )
                            Text(
                                text = metadataParts.joinToString(" • "),
                                fontSize = 11.sp.responsive(),
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Main Title Display
                            Text(
                                text = richMeta.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp.responsive(),
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Synopsis Overview
                            Text(
                                text = richMeta.description,
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 11.5.sp.responsive(),
                                lineHeight = 17.sp.responsive(),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Primary Action Pill Button ("Watch now" / "Reproducir")
                        Surface(
                            color = if (isHeroFocused) Color.White else Color(0xFFE6E8F0),
                            shape = CircleShape,
                            shadowElevation = if (isHeroFocused) 8.dp else 2.dp,
                            modifier = Modifier
                                .clickable { onTrailerClick(targetMovie) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp.responsive()),
                                modifier = Modifier.padding(
                                    horizontal = 22.dp.responsive(),
                                    vertical = 10.dp.responsive()
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFF10131B),
                                    modifier = Modifier.size(18.dp.responsive())
                                )
                                Text(
                                    text = "Reproducir",
                                    color = Color(0xFF10131B),
                                    fontSize = 12.5.sp.responsive(),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // E. Carousel Indicator Dots (Bottom Right Pill Capsule)
                    val currentIndex = featuredMovies.indexOfFirst { it.id == currentMovie.id }.coerceAtLeast(0)
                    if (featuredMovies.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 28.dp.responsive(), bottom = 28.dp.responsive())
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.40f))
                                .padding(horizontal = 10.dp.responsive(), vertical = 6.dp.responsive())
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp.responsive()),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dotCount = featuredMovies.size.coerceAtMost(7)
                                val activeDotIndex = (currentIndex % dotCount)
                                for (i in 0 until dotCount) {
                                    val isActive = i == activeDotIndex
                                    Box(
                                        modifier = Modifier
                                            .size(if (isActive) 6.5.dp.responsive() else 5.dp.responsive())
                                            .clip(CircleShape)
                                            .background(
                                                if (isActive) Color.White else Color.White.copy(alpha = 0.35f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
