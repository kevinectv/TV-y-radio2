package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
                    .padding(start = 48.dp, end = 48.dp, bottom = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp) // Reducido para modo TV
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    // 1. Logo o Título
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (richMeta.logoUrl != null) {
                            AsyncImage(
                                model = richMeta.logoUrl,
                                contentDescription = richMeta.title,
                                modifier = Modifier
                                    .heightIn(max = 55.dp)
                                    .widthIn(max = 240.dp),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.CenterStart
                            )
                        } else {
                            Text(
                                text = richMeta.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
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
                                lineHeight = 26.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 2. Información base agrupada (Año, Género, Duración)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = richMeta.year, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                        Text(text = richMeta.genres, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                        Text(text = richMeta.duration, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Platform & Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Platform Logo
                        val fallbackLogoUrl = when(richMeta.platform) {
                            "Netflix" -> "https://image.tmdb.org/t/p/w154/t2yyOv40HZeVlLjVrCsPhIdZfC4.jpg"
                            "Prime Video" -> "https://image.tmdb.org/t/p/w154/5NyLm42TmCqCMOZFvH4fcoSNKEW.jpg"
                            "Disney+" -> "https://image.tmdb.org/t/p/w154/7rwgEs15tFwyR9NPQ5vlURnb3x1.jpg"
                            "Max" -> "https://image.tmdb.org/t/p/w154/c2uuPbxqFJoGtwAunvGqHk98jC8.jpg"
                            "Apple TV+" -> "https://image.tmdb.org/t/p/w154/q6tl6Ib6X5FT80RMlcDbexIo4St.jpg"
                            else -> "https://image.tmdb.org/t/p/w154/t2yyOv40HZeVlLjVrCsPhIdZfC4.jpg"
                        }
                        val platformLogoUrl = richMeta.platformLogoUrl ?: fallbackLogoUrl

                        coil.compose.AsyncImage(
                            model = platformLogoUrl,
                            contentDescription = richMeta.platform,
                            modifier = Modifier
                                .height(22.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)

                        // IMDb Logo + Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFE5B91E), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "IMDb", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = richMeta.ratingImdb, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Sinopsis y Tags extras
                    val extendedDescription = buildString {
                        append(richMeta.description)
                        if (richMeta.premiumBadges.isNotEmpty() || richMeta.techIndicators.isNotEmpty()) {
                            append("\n\n")
                            val tags = richMeta.premiumBadges + richMeta.techIndicators
                            append(tags.joinToString(" • "))
                        }
                    }
                    
                    Text(
                        text = extendedDescription,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        maxLines = 3,
                        lineHeight = 15.sp,
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
                    .padding(
                        start = 20.dp.responsive(),
                        end = 20.dp.responsive(),
                        bottom = 12.dp.responsive(),
                        top = 12.dp.responsive()
                    ),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp.responsive())
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    // 1. Logo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (richMeta.logoUrl != null) {
                            AsyncImage(
                                model = richMeta.logoUrl,
                                contentDescription = richMeta.title,
                                modifier = Modifier
                                    .heightIn(max = 45.dp.responsive()) // Reducido
                                    .widthIn(max = 140.dp.responsive()),
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.CenterStart
                            )
                        } else {
                            Text(
                                text = richMeta.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp.responsive(),
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp,
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.95f),
                                        offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                        blurRadius = 12f
                                    )
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp.responsive()))

                    // 2. Info base
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp.responsive())
                    ) {
                        Text(text = richMeta.year, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp.responsive())
                        Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp.responsive())
                        Text(text = richMeta.genres, color = Color.White, fontSize = 11.sp.responsive(), fontWeight = FontWeight.Medium)
                        Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp.responsive())
                        Text(text = richMeta.duration, color = Color.White, fontSize = 11.sp.responsive(), fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(10.dp.responsive()))

                    // 3. Platform & Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp.responsive())
                    ) {
                        // Platform Logo
                        val fallbackLogoUrl = when(richMeta.platform) {
                            "Netflix" -> "https://image.tmdb.org/t/p/w154/t2yyOv40HZeVlLjVrCsPhIdZfC4.jpg"
                            "Prime Video" -> "https://image.tmdb.org/t/p/w154/5NyLm42TmCqCMOZFvH4fcoSNKEW.jpg"
                            "Disney+" -> "https://image.tmdb.org/t/p/w154/7rwgEs15tFwyR9NPQ5vlURnb3x1.jpg"
                            "Max" -> "https://image.tmdb.org/t/p/w154/c2uuPbxqFJoGtwAunvGqHk98jC8.jpg"
                            "Apple TV+" -> "https://image.tmdb.org/t/p/w154/q6tl6Ib6X5FT80RMlcDbexIo4St.jpg"
                            else -> "https://image.tmdb.org/t/p/w154/t2yyOv40HZeVlLjVrCsPhIdZfC4.jpg"
                        }
                        val platformLogoUrl = richMeta.platformLogoUrl ?: fallbackLogoUrl

                        coil.compose.AsyncImage(
                            model = platformLogoUrl,
                            contentDescription = richMeta.platform,
                            modifier = Modifier
                                .height(16.dp.responsive())
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Text(text = "|", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp.responsive())

                        // IMDb Logo + Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFE5B91E), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp.responsive(), vertical = 2.dp.responsive())
                        ) {
                            Text(text = "IMDb", color = Color.Black, fontSize = 10.sp.responsive(), fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(3.dp.responsive()))
                            Text(text = richMeta.ratingImdb, color = Color.Black, fontSize = 11.sp.responsive(), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp.responsive()))

                    // 4. Sinopsis y Tags extras
                    val extendedDescription = buildString {
                        append(richMeta.description)
                        if (richMeta.premiumBadges.isNotEmpty() || richMeta.techIndicators.isNotEmpty()) {
                            append("\n\n")
                            val tags = richMeta.premiumBadges + richMeta.techIndicators
                            append(tags.joinToString(" • "))
                        }
                    }

                    Text(
                        text = extendedDescription,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp.responsive(),
                        maxLines = 3,
                        lineHeight = 15.sp.responsive(),
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
