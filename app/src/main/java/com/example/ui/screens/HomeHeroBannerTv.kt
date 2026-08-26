package com.example.ui.screens

import com.example.ui.components.responsive


import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
    val coroutineScope = rememberCoroutineScope()
    val playButtonFocusRequester = remember { FocusRequester() }

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

    val effectiveHeight = bannerHeight + 220.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(effectiveHeight)
            .focusRequester(playButtonFocusRequester)
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
        // Top Header Row (Logo LUMINA + Clock) integrated into the top of the Hero banner scroll area
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isWideLayout = configuration.screenWidthDp >= 580
        Row(
            modifier = Modifier
                .zIndex(1f)
                .fillMaxWidth()
                .padding(
                    start = if (isWideLayout) 24.dp else 12.dp,
                    end = if (isWideLayout) 32.dp else 16.dp,
                    top = if (isWideLayout) 24.dp else 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prepared Logo image using R.drawable.img_lumina_logo_user_v2 (fully prepared for user's logo file replacement)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_lumina_logo_user_v2),
                contentDescription = "Lumina Logo",
                modifier = Modifier
                    .height(if (isWideLayout) 48.dp else 36.dp)
                    .widthIn(max = if (isWideLayout) 260.dp else 190.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.weight(1f))

            // Live Clock
            Text(
                text = timeString,
                color = Color.White,
                fontSize = if (isWideLayout) 13.sp.responsive() else 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Background Backdrop with crossfade (light overlay for vivid backdrop)
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(600),
            label = "hero_tv_backdrop_fade",
            modifier = Modifier.layout { measurable, constraints ->
                val shift = 68.dp.roundToPx()
                // Extend generously to the right to cover any system insets, nav bars, or parent paddings
                val rightExtension = 120.dp.roundToPx() 
                val newWidth = constraints.maxWidth + shift + rightExtension
                val placeable = measurable.measure(constraints.copy(
                    minWidth = newWidth,
                    maxWidth = newWidth
                ))
                layout(placeable.width, placeable.height) {
                    placeable.place(-shift, 0)
                }
            }
        ) { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = richMeta.backdropUrl.ifBlank { targetMovie.backdropUrl ?: targetMovie.posterUrl },
                    contentDescription = richMeta.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Light cinematic gradients so backdrop is the absolute protagonist
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.8f),
                                    Color.Black.copy(alpha = 0.4f),
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
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.35f),
                                    Color(0xFF030406)
                                )
                            )
                        )
                )
            }
        }

        // Main Content Area (Left aligned naturally at 32.dp, positioned lower down)
        Crossfade(
            targetState = currentMovie,
            animationSpec = tween(500),
            label = "hero_tv_content_fade"
        ) { targetMovie ->
            val richMeta = resolveHeroMetadata(targetMovie, activeHeroLoadedDetails, featuredMovies)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 32.dp, end = 48.dp, top = 96.dp, bottom = 48.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // 1. Logo or Title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
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
                                    .heightIn(max = 110.dp)
                                    .widthIn(max = 480.dp),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.CenterStart,
                                loading = { },
                                error = {
                                    Text(
                                        text = richMeta.title,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 44.sp,
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
                                    fontSize = 44.sp,
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

                    // 2. Year • Duration • Genres
                    Row(
                        modifier = Modifier.wrapContentWidth(),
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

                    // 3. Short Synopsis
                    Text(
                        text = richMeta.description,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        maxLines = 3,
                        lineHeight = 20.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 4. Primary Play Button & Carousel Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(48.dp)
                    ) {
                        // Original Rectangular Play Button
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { onTrailerClick(targetMovie) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Reproducir",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Reproducir",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        
                        // 5. Carousel Indicators
                        val currentIndex = featuredMovies.indexOfFirst { it.id == currentMovie.id }.coerceAtLeast(0)
                        if (featuredMovies.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.CenterVertically).offset(y = 4.dp)
                            ) {
                                featuredMovies.forEachIndexed { index, _ ->
                                    val isActive = index == currentIndex
                                    Box(
                                        modifier = Modifier
                                            .height(6.dp)
                                            .width(if (isActive) 24.dp else 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                color = if (isActive) Color.White else Color.White.copy(alpha = 0.35f),
                                                shape = RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Floating Play Button on the right (decorative, non-focusable)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 138.dp, end = 80.dp)
                .focusable(false)
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(68.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }
    }
}
