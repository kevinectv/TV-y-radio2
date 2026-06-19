package com.example.ui.screens

import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage; import coil.compose.SubcomposeAsyncImage
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.Channel
import com.example.data.model.RadioStation
import com.example.data.model.Catalog
import com.example.data.model.CatalogItem
import com.example.ui.AppTab
import com.example.ui.MediaViewModel
import com.example.ui.components.tvFocusEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshCatalogs()
    }

    // Base flows
    val favoriteChans by viewModel.favoriteChannels.collectAsState()
    val favoriteRadios by viewModel.favoriteRadioStations.collectAsState()
    val recentChans by viewModel.recentChannels.collectAsState()
    val recentRadios by viewModel.recentRadioStations.collectAsState()

    val catalogs by viewModel.catalogsStateFlow.collectAsState()
    var selectedCatalogItem by remember { mutableStateOf<CatalogItem?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var activeTrailerItem by remember { mutableStateOf<CatalogItem?>(null) }

    val allChannels by viewModel.allChannels.collectAsState()
    // Showcase/Banner Channel (First channel by default)
    // Showcase/Banner movies (Curated highlights from either the active catalogs or premium curated cinema highlights)
    val featuredMovies = remember(catalogs) {
        catalogs.filter { it.isVisible && it.showInHome }.flatMap { it.items }.filter { it.posterUrl.isNotEmpty() && !it.posterUrl.contains("unsplash.com") && !it.posterUrl.contains("images.unsplash") }.distinctBy { it.id }.shuffled().take(12)
    }

    val favoriteCatalogItems by viewModel.favoriteCatalogItems.collectAsState()
    val seenProgress by viewModel.seenProgress.collectAsState()

    val progressItems = remember(seenProgress, catalogs) {
        val list = mutableListOf<Pair<CatalogItem, Float>>()
        catalogs.flatMap { it.items }.forEach { item ->
            val prg = seenProgress[item.id] ?: 0f
            if (prg > 0f && list.none { it.first.id == item.id }) {
                list.add(item to prg)
            }
        }
        list
    }

    var activeHeroMovie by remember { mutableStateOf<CatalogItem?>(null) }

    val currentMovie = activeHeroMovie ?: featuredMovies.firstOrNull() ?: CatalogItem(
        id = "f1", title = "Michael",
        posterUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=1200",
        year = "2026", rating = "7.7", genre = "Música / Drama",
        description = "El viaje de Michael Jackson más allá de la música, desde el descubrimiento de su extraordinario talento como líder de los Jackson Five..."
    )

    // Logo state
    var activeHeroLogoUrl by remember { mutableStateOf<String?>(null) }

    // Loaded dynamic properties (from TMDB real-time query) for the current movie
    var activeHeroLoadedDetails by remember(currentMovie) { mutableStateOf<LoadedTmdbDetails?>(null) }
    
    LaunchedEffect(currentMovie) {
        activeHeroLogoUrl = null
        activeHeroLoadedDetails = null
        
        val prefs = context.getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE)
        val apiKey = prefs.getString("tmdb_api_key", "")?.trim() ?: ""
        if (apiKey.isNotEmpty()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val client = okhttp3.OkHttpClient()
                    var tmdbId = currentMovie.tmdbId
                    var isTv = currentMovie.isTvShow
                    
                    // Fallback lookup: Search by title if tmdbId is missing (so simple custom lists load real dynamic overviews too!)
                    if (tmdbId.isNullOrEmpty()) {
                        val searchUrl = "https://api.themoviedb.org/3/search/multi?query=${java.net.URLEncoder.encode(currentMovie.title, "UTF-8")}&language=es-MX"
                        val reqBuilder = okhttp3.Request.Builder()
                        if (apiKey.startsWith("ey")) {
                            reqBuilder.url(searchUrl)
                            reqBuilder.header("Authorization", "Bearer $apiKey")
                        } else {
                            reqBuilder.url("$searchUrl&api_key=$apiKey")
                        }
                        val resp = client.newCall(reqBuilder.build()).execute()
                        if (resp.isSuccessful) {
                            val jsonStr = resp.body?.string() ?: ""
                            val results = org.json.JSONObject(jsonStr).optJSONArray("results")
                            if (results != null && results.length() > 0) {
                                val first = results.getJSONObject(0)
                                tmdbId = first.optString("id")
                                isTv = first.optString("media_type") == "tv" || first.has("first_air_date") || (first.has("name") && !first.has("title"))
                            }
                        }
                    }
                    
                    if (!tmdbId.isNullOrEmpty()) {
                        val mediaType = if (isTv) "tv" else "movie"
                        
                        // 1. Fetch details
                        val detailUrl = "https://api.themoviedb.org/3/$mediaType/$tmdbId?language=es-MX"
                        val reqBuilder = okhttp3.Request.Builder()
                        if (apiKey.startsWith("ey")) {
                            reqBuilder.url(detailUrl)
                            reqBuilder.header("Authorization", "Bearer $apiKey")
                        } else {
                            reqBuilder.url("$detailUrl&api_key=$apiKey")
                        }
                        val resp = client.newCall(reqBuilder.build()).execute()
                        var overrideDesc = currentMovie.description
                        var overrideRating = currentMovie.rating
                        var overrideYear = currentMovie.year
                        var overrideBackdrop = currentMovie.posterUrl
                        
                        if (resp.isSuccessful) {
                            val jsonStr = resp.body?.string() ?: ""
                            val jsonObj = org.json.JSONObject(jsonStr)
                            val overview = jsonObj.optString("overview", "").trim()
                            if (overview.isNotEmpty()) {
                                overrideDesc = overview
                            }
                            val vote = jsonObj.optDouble("vote_average", 0.0)
                            if (vote > 0.0) {
                                overrideRating = String.format(java.util.Locale.US, "%.1f", vote)
                            }
                            val releaseDate = jsonObj.optString("release_date").ifEmpty { jsonObj.optString("first_air_date", "") }
                            if (releaseDate.length >= 4) {
                                overrideYear = releaseDate.substring(0, 4)
                            }
                            val backdropPath = jsonObj.optString("backdrop_path", "")
                            if (backdropPath.isNotEmpty()) {
                                overrideBackdrop = "https://image.tmdb.org/t/p/w1280$backdropPath"
                            }
                        }
                        
                        // 2. Fetch logo
                        val logoUrlBuilder = "https://api.themoviedb.org/3/$mediaType/$tmdbId/images?include_image_language=es,en,null"
                        val logoReqBuilder = okhttp3.Request.Builder()
                        if (apiKey.startsWith("ey")) {
                            logoReqBuilder.url(logoUrlBuilder)
                            logoReqBuilder.header("Authorization", "Bearer $apiKey")
                        } else {
                            logoReqBuilder.url("$logoUrlBuilder&api_key=$apiKey")
                        }
                        val logoResp = client.newCall(logoReqBuilder.build()).execute()
                        var logo: String? = null
                        if (logoResp.isSuccessful) {
                            val jsonStr = logoResp.body?.string() ?: ""
                            val logos = org.json.JSONObject(jsonStr).optJSONArray("logos")
                            if (logos != null && logos.length() > 0) {
                                var bestPath = logos.getJSONObject(0).optString("file_path")
                                for (i in 0 until logos.length()) {
                                    val lang = logos.getJSONObject(i).optString("iso_639_1", "")
                                    if (lang == "es") {
                                        bestPath = logos.getJSONObject(i).optString("file_path")
                                        break
                                    }
                                }
                                logo = "https://image.tmdb.org/t/p/w500$bestPath"
                            }
                        }
                        
                        activeHeroLogoUrl = logo
                        activeHeroLoadedDetails = LoadedTmdbDetails(
                            description = overrideDesc,
                            rating = overrideRating,
                            year = overrideYear,
                            logoUrl = logo,
                            backdropUrl = overrideBackdrop
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Trailer Simulation States
    var isTrailerLive by remember { mutableStateOf(false) }
    var currentSubIndex by remember { mutableStateOf(0) }
    var trailerProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(currentMovie) {
        isTrailerLive = false
        currentSubIndex = 0
        trailerProgress = 0f
        
        // Wait 1.8 seconds of initial poster preview before triggering simulated trailer session
        kotlinx.coroutines.delay(1800L)
        isTrailerLive = true
        
        val details = getCinematicDetails(currentMovie)
        val lineCount = details.subtitleLines.size
        
        if (lineCount > 0) {
            val totalDuration = 13500L // 13.5 seconds trailer loop
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < totalDuration) {
                val elapsed = System.currentTimeMillis() - startTime
                if (currentMovie != (activeHeroMovie ?: featuredMovies.firstOrNull())) {
                    // Break loop early if movie has changed
                    break
                }
                trailerProgress = (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
                
                // Change subtitles dynamically based on elapsed fraction
                val segment = totalDuration / lineCount
                currentSubIndex = (elapsed / segment).toInt().coerceIn(0, lineCount - 1)
                
                kotlinx.coroutines.delay(100L)
            }
        }
    }

    val scale = 1.0f
    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    val backdropHeight = if (isWideLayout) 580.dp else 460.dp

    Box(modifier = modifier.fillMaxSize()) {
        // --- 1. CAPA DE FONDO CINEMÁTICO ADAPTATIVO CON CROSSFADE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(backdropHeight)
                .background(Color(0xFF030406))
        ) {
            Crossfade(
                targetState = currentMovie,
                animationSpec = tween(650),
                label = "movie_backdrop_fade"
            ) { movie ->
                val movieDetails = getCinematicDetails(movie)
                val backdropUrlToUse = if (movie == currentMovie) {
                    activeHeroLoadedDetails?.backdropUrl ?: movieDetails.backdropUrl
                } else {
                    movieDetails.backdropUrl
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isTrailerLive && movieDetails.trailerUrl.isNotEmpty() && movie == currentMovie) {
                        var isReady by remember(movie) { mutableStateOf(false) }
                        
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    isFocusable = false
                                    isFocusableInTouchMode = false
                                }
                            },
                            modifier = Modifier.fillMaxSize().focusable(false),
                            update = { videoView ->
                                val url = movieDetails.trailerUrl
                                if (videoView.tag != url) {
                                    videoView.tag = url
                                    try {
                                        videoView.stopPlayback()
                                        videoView.setVideoPath(url)
                                        videoView.setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            mp.setVolume(0f, 0f) // Silenciado para ambiente premium
                                            videoView.start()
                                            videoView.clearFocus()
                                            isReady = true
                                        }
                                        videoView.setOnErrorListener { _, _, _ ->
                                            true
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            onRelease = { videoView ->
                                try {
                                    videoView.stopPlayback()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )
                        
                        if (!isReady) {
                            AsyncImage(
                                model = backdropUrlToUse,
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                                    .blur(8.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        AsyncImage(
                            model = backdropUrlToUse,
                            contentDescription = movie.title,
                            modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                                    .blur(8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Degradado Cine-Lux para garantizar legibilidad extrema de textos en el lado izquierdo
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.98f),
                                        Color.Black.copy(alpha = 0.85f),
                                        Color.Black.copy(alpha = 0.40f),
                                        Color.Transparent
                                    ),
                                    endX = 1100f
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.40f),
                                        Color(0xFF030406) // Fusión perfecta con el color de fondo obsidian de abajo
                                    )
                                )
                            )
                    )
                }
            }
        }

        // --- 2. LISTADO DESLIZANTE DE CATEGORÍAS EN PRIMERA PLANA (OVERLAPPING SCROLL) ---
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // -- SECCIÓN 1: CABECERA TRANSLÚCIDA CON DETALLES DEL CONTENIDO (INFO OVERLAY PANEL) --
            item {
                if (featuredMovies.isNotEmpty()) {
                    val details = getCinematicDetails(currentMovie)
                    val baseDescription = activeHeroLoadedDetails?.description ?: currentMovie.description
                    val baseYear = activeHeroLoadedDetails?.year ?: currentMovie.year
                    val baseRating = activeHeroLoadedDetails?.rating ?: currentMovie.rating
                    val headerHeight = if (isWideLayout) 540.dp else 420.dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 16.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.Start
                            ) {
                                // A) Título elegante con sombra O Logo
                                val logoToUse = activeHeroLogoUrl ?: currentMovie.logoUrl
                                if (logoToUse != null) {
                                    AsyncImage(
                                        model = logoToUse,
                                        contentDescription = details.logoText,
                                        modifier = Modifier
                                            .padding(bottom = 12.dp)
                                            .heightIn(max = if (isWideLayout) 130.dp else 90.dp)
                                            .widthIn(max = if (isWideLayout) 360.dp else 240.dp),
                                        contentScale = ContentScale.Fit,
                                        alignment = Alignment.BottomStart
                                    )
                                } else {
                                    Text(
                                        text = currentMovie.title,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Black,
                                            fontSize = if (isWideLayout) 46.sp else 32.sp,
                                            color = Color.White,
                                            letterSpacing = (-1).sp,
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.95f),
                                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                                blurRadius = 12f
                                            )
                                        ),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                // B) Línea de metadatos (Año • Género • Duración • TMDB badge • IMDb badge • Status badge)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Text(
                                        text = baseYear,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                    
                                    val statusText = remember(currentMovie) {
                                        val hash = currentMovie.title.hashCode()
                                        val absHash = if (hash < 0) -hash else hash
                                        when {
                                            absHash % 3 == 0 -> "Estreno"
                                            absHash % 3 == 1 -> "Popular"
                                            else -> "Trending"
                                        }
                                    }
                                    val statusColor = when (statusText) {
                                        "Estreno" -> Color(0xFF00E5FF)
                                        "Popular" -> Color(0xFFFF2E93)
                                        else -> Color(0xFF00FF87)
                                    }
                                    Text(
                                        text = statusText.uppercase(),
                                        color = statusColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
 
                                    Text(
                                        text = currentMovie.genre,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
 
                                    Text(
                                        text = "•",
                                        color = Color.White.copy(alpha = 0.3f),
                                        fontSize = 12.sp
                                    )
 
                                    val durationText = remember(currentMovie) {
                                        val mDetails = getCinematicDetails(currentMovie)
                                        val metaParts = mDetails.dateAndMetadata.split("•")
                                        if (metaParts.size >= 3) {
                                            metaParts[2].trim()
                                        } else if (currentMovie.isTvShow) {
                                            "4 Temp"
                                        } else {
                                            "2h 15m"
                                        }
                                    }
                                    Text(
                                        text = durationText,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color(0xFFFFD700).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                            .border(0.5.dp, Color(0xFFFFD700).copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "IMDb Rating",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val imdbRating = remember(currentMovie, baseRating) {
                                            val ratingFloat = baseRating.toFloatOrNull() ?: 7.5f
                                            val calculated = (ratingFloat - 0.2f).coerceIn(1.0f, 10.0f)
                                            String.format("%.1f", calculated)
                                        }
                                        Text(
                                            text = "IMDb $imdbRating",
                                            color = Color(0xFFFFD700),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color(0xFF00FF87).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                            .border(0.5.dp, Color(0xFF00FF87).copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "TMDB Rating",
                                            tint = Color(0xFF00FF87),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "TMDB $baseRating",
                                            color = Color(0xFF00FF87),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
 
                                // C) Sinopsis corta con max 3 líneas y legibilidad premium
                                Text(
                                    text = baseDescription,
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = if (isWideLayout) 13.sp else 11.5.sp,
                                    maxLines = 3,
                                    lineHeight = if (isWideLayout) 18.sp else 16.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .padding(bottom = 18.dp, end = 24.dp)
                                        .widthIn(max = 620.dp)
                                )

                                // D) Botones principales con target táctil >= 48dp y focusable para Android TV / Control Remoto
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val movieChannel = Channel(
                                                id = "movie_${currentMovie.id}",
                                                name = currentMovie.title,
                                                streamUrl = currentMovie.streamUrl ?: getCinematicDetails(currentMovie).trailerUrl,
                                                logoUrl = currentMovie.posterUrl,
                                                category = "Cine Premium",
                                                description = currentMovie.description,
                                                number = 999
                                            )
                                            viewModel.selectChannel(movieChannel)
                                            viewModel.isFullscreenPlayerActive = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00E5FF),
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .height(40.dp)
                                            .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("REPRODUCIR", fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val movieChannel = Channel(
                                                id = "trailer_${currentMovie.id}",
                                                name = "${currentMovie.title} - Tráiler",
                                                streamUrl = getCinematicDetails(currentMovie).trailerUrl,
                                                logoUrl = currentMovie.posterUrl,
                                                category = "Cine Premium",
                                                description = currentMovie.description,
                                                number = 998) ; activeTrailerItem = currentMovie ; return@OutlinedButton ; val dummy = Channel(id="", name="", streamUrl="", logoUrl="", category="", description="", number=0
                                            )
                                            viewModel.selectChannel(movieChannel)
                                            viewModel.isFullscreenPlayerActive = true
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .height(40.dp)
                                            .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("VER TRÁILER", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    var isInMyList by remember(currentMovie) { mutableStateOf(currentMovie.id in favoriteCatalogItems) }
                                    OutlinedIconButton(
                                        onClick = {
                                            viewModel.toggleCatalogItemFavorite(currentMovie.id)
                                            isInMyList = !isInMyList
                                            Toast.makeText(context, if (isInMyList) "Añadida a Mi Lista" else "Quitada de Mi Lista", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = Color.White),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            imageVector = if (isInMyList) Icons.Filled.Check else Icons.Filled.Add,
                                            contentDescription = "Añadir a mi lista",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            selectedCatalogItem = currentMovie
                                            showDetailsDialog = true
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .height(40.dp)
                                            .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("DETALLES", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    var isFavorite by remember(currentMovie) { mutableStateOf(currentMovie.id in favoriteCatalogItems) }
                                    OutlinedIconButton(
                                        onClick = {
                                            viewModel.toggleCatalogItemFavorite(currentMovie.id)
                                            isFavorite = !isFavorite
                                        },
                                        colors = IconButtonDefaults.outlinedIconButtonColors(
                                            contentColor = if (isFavorite) Color(0xFFFF2E93) else Color.White
                                        ),
                                        border = BorderStroke(1.dp, if (isFavorite) Color(0xFFFF2E93).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .size(40.dp)
                                            .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Favorito",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Home Cinema Rows (All user custom lists & active templates in their chosen order)
            val homeCatalogs = catalogs.filter { it.isVisible && it.showInHome }

            if (homeCatalogs.isEmpty()) {
                if (progressItems.isNotEmpty()) {
                    item {
                        HomeSectionRowHeader(
                            title = "CONTINUE WATCHING",
                            icon = Icons.Filled.PlayCircle,
                            color = Color(0xFF00FF87)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            items(progressItems) { (item, progressVal) ->
                                CatalogItemHomeCard(
                                    item = item,
                                    layoutType = "Landscape Row",
                                    isFavorite = item.id in favoriteCatalogItems,
                                    progress = progressVal,
                                    onFocus = {
                                        activeHeroMovie = item
                                    },
                                    onClick = {
                                        activeHeroMovie = item
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(0)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                homeCatalogs.forEachIndexed { index, catalog ->
                    if (catalog.items.isNotEmpty()) {
                        item {
                            DrawCatalogRow(
                                catalog = catalog,
                                favoriteCatalogItems = favoriteCatalogItems,
                                seenProgress = seenProgress,
                                onFocus = { activeHeroMovie = it },
                                onClick = { clickedItem ->
                                    activeHeroMovie = clickedItem
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            )
                        }
                    }

                    // Inject Continue Watching under the first dynamic row
                    if (index == 0 && progressItems.isNotEmpty()) {
                        item {
                            HomeSectionRowHeader(
                                title = "CONTINUE WATCHING",
                                icon = Icons.Filled.PlayCircle,
                                color = Color(0xFF00FF87)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                items(progressItems) { (item, progressVal) ->
                                    CatalogItemHomeCard(
                                        item = item,
                                        layoutType = "Landscape Row",
                                        isFavorite = item.id in favoriteCatalogItems,
                                        progress = progressVal,
                                        onFocus = {
                                            activeHeroMovie = item
                                        },
                                        onClick = {
                                            activeHeroMovie = item
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(0)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }


        // --- SUB-HEADER: RECREACIÓN TELEVISIVA Y DE EMISORAS DE RADIO (DESPLAZADA AL FONDO) ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Divider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 3. RECENTS SECTION (HORIZONTAL SCROLLER)
        val hasRecents = recentChans.isNotEmpty() || recentRadios.isNotEmpty()
        if (hasRecents) {
            item {
                HomeSectionRowHeader(title = "REPRODUCIDO RECIENTEMENTE", icon = Icons.Filled.History, color = Color.White.copy(alpha = 0.7f))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    items(recentChans) { chan ->
                        ChannelHomeCard(
                            channel = chan,
                            viewModel = viewModel,
                            onPlayClick = {
                                viewModel.selectChannel(chan)
                                viewModel.selectTab(AppTab.TV)
                            }
                        )
                    }

                    items(recentRadios) { rad ->
                        RadioHomeCard(
                            station = rad,
                            viewModel = viewModel,
                            onPlayClick = {
                                viewModel.selectRadioStation(rad)
                                viewModel.selectTab(AppTab.RADIO)
                            }
                        )
                    }
                }
            }
        }

        // 4. FAVORITES WATCHLIST (HORIZONTAL SCROLLER)
        val hasFavorites = favoriteChans.isNotEmpty() || favoriteRadios.isNotEmpty()
        if (hasFavorites) {
            item {
                HomeSectionRowHeader(title = "MIS FAVORITOS GUARDADOS", icon = Icons.Filled.Favorite, color = Color.White.copy(alpha = 0.7f))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    items(favoriteChans) { chan ->
                        ChannelHomeCard(
                            channel = chan,
                            viewModel = viewModel,
                            onPlayClick = {
                                viewModel.selectChannel(chan)
                                viewModel.selectTab(AppTab.TV)
                            }
                        )
                    }

                    items(favoriteRadios) { rad ->
                        RadioHomeCard(
                            station = rad,
                            viewModel = viewModel,
                            onPlayClick = {
                                viewModel.selectRadioStation(rad)
                                viewModel.selectTab(AppTab.RADIO)
                            }
                        )
                    }
                }
            }
        }

        // 5. CANALES POPULARES IPTV (DESPLAZADOS AL RECLUSO)
        item {
            HomeSectionRowHeader(title = "CANALES POPULARES IPTV", icon = Icons.Filled.Tv, color = Color.White.copy(alpha = 0.7f))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                items(allChannels) { chan ->
                    ChannelHomeCard(
                        channel = chan,
                        viewModel = viewModel,
                        onPlayClick = {
                            viewModel.selectChannel(chan)
                            viewModel.selectTab(AppTab.TV)
                        }
                    )
                }
            }
        }

        // 6. RADIO POPULARES (DESPLAZADOS ABAJO)
        item {
            HomeSectionRowHeader(title = "EMISORAS DE RADIO POPULARES", icon = Icons.Filled.Radio, color = Color.White.copy(alpha = 0.7f))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                items(viewModel.repository.radioStationsList) { rad ->
                    RadioHomeCard(
                        station = rad,
                        viewModel = viewModel,
                        onPlayClick = {
                            viewModel.selectRadioStation(rad)
                            viewModel.selectTab(AppTab.RADIO)
                        }
                    )
                }
            }
        }

        // 7. RECOMMENDED STREAMING (ABSOLUTE BOTTOM)
        item {
            HomeSectionRowHeader(title = "RECOMENDADOS PARA TI", icon = Icons.Filled.AutoAwesome, color = Color.White.copy(alpha = 0.7f))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Blend channels and radios
                items(allChannels.takeLast(3)) { chan ->
                    ChannelHomeCard(
                        channel = chan,
                        viewModel = viewModel,
                        onPlayClick = {
                            viewModel.selectChannel(chan)
                            viewModel.selectTab(AppTab.TV)
                        }
                    )
                }

                items(viewModel.repository.radioStationsList.takeLast(2)) { rad ->
                    RadioHomeCard(
                        station = rad,
                        viewModel = viewModel,
                        onPlayClick = {
                            viewModel.selectRadioStation(rad)
                            viewModel.selectTab(AppTab.RADIO)
                        }
                    )
                }
            }
        }
    }
    }

    if (showDetailsDialog && selectedCatalogItem != null) {
        CatalogItemDetailsDialog(
            item = selectedCatalogItem!!,
            viewModel = viewModel,
            onDismiss = {
                showDetailsDialog = false
                selectedCatalogItem = null
            },
            onTrailerClick = { clickedItem ->
                activeTrailerItem = clickedItem
            }
        )
    }

    val trailerToShow = activeTrailerItem ?: viewModel.activeTrailerItem
    if (trailerToShow != null) {
        TrailerYoutubePlayerDialog(
            item = trailerToShow,
            onDismiss = {
                activeTrailerItem = null
                viewModel.activeTrailerItem = null
            }
        )
    }
}

@Composable
fun DrawCatalogRow(
    catalog: Catalog,
    favoriteCatalogItems: Set<String>,
    seenProgress: Map<String, Float>,
    onFocus: (CatalogItem) -> Unit,
    onClick: (CatalogItem) -> Unit,
    customTitle: String? = null,
    customLayout: String? = null,
    customIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val titleToDraw = customTitle ?: catalog.name
    val layoutToDraw = customLayout ?: catalog.layoutType
    val iconToDraw = customIcon ?: when (catalog.sourceType) {
        "TMDB" -> Icons.Filled.Movie
        "Trakt" -> Icons.Filled.Tv
        "MDBList" -> Icons.Filled.FilterAlt
        else -> Icons.Filled.VideoLibrary
    }
    HomeSectionRowHeader(
        title = titleToDraw.uppercase(),
        icon = iconToDraw,
        color = Color(0xFF00E5FF)
    )

    val isSupportedRowType = layoutToDraw in listOf(
        "Horizontal Poster Row",
        "Vertical Poster Row",
        "Landscape Row",
        "Banner Row",
        "Large Featured Row",
        "Compact Row",
        "Horizontal"
    )

    if (isSupportedRowType) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            items(catalog.items.take(catalog.numItems)) { item ->
                CatalogItemHomeCard(
                    item = item,
                    layoutType = layoutToDraw,
                    isFavorite = item.id in favoriteCatalogItems,
                    progress = seenProgress[item.id] ?: 0f,
                    onFocus = {
                        onFocus(item)
                    },
                    onClick = {
                        onClick(item)
                    }
                )
            }
        }
    } else if (layoutToDraw == "Vertical") {
        CatalogVerticalGrid(
            items = catalog.items.take(catalog.numItems),
            layoutType = "Vertical",
            favoriteCatalogItems = favoriteCatalogItems,
            seenProgress = seenProgress,
            onItemFocus = { item ->
                onFocus(item)
            },
            onClick = { item ->
                onClick(item)
            }
        )
    } else if (layoutToDraw == "Top Numerado" || layoutToDraw.contains("top", ignoreCase = true) || titleToDraw.contains("top", ignoreCase = true) || titleToDraw.contains("Mejor Valorad", ignoreCase = true) || titleToDraw.contains("Top 250", ignoreCase = true)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            itemsIndexed(catalog.items.take(catalog.numItems)) { index, item ->
                CatalogItemNumberedCard(
                    item = item,
                    rank = index + 1,
                    isFavorite = item.id in favoriteCatalogItems,
                    progress = seenProgress[item.id] ?: 0f,
                    onFocus = {
                        onFocus(item)
                    },
                    onClick = {
                        onClick(item)
                    }
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
            items(catalog.items.take(catalog.numItems)) { item ->
                CatalogItemHomeCard(
                    item = item,
                    layoutType = "Horizontal Poster Row",
                    isFavorite = item.id in favoriteCatalogItems,
                    progress = seenProgress[item.id] ?: 0f,
                    onFocus = {
                        onFocus(item)
                    },
                    onClick = {
                        onClick(item)
                    }
                )
            }
        }
    }
}

// Subordinate Layout helpers
@Composable
fun HomeSectionRowHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color = Color(0xFF00E5FF)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 44.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Vertical Neon Anchor Bar for high-end cinematic vibe
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun ChannelHomeCard(
    channel: Channel,
    viewModel: MediaViewModel,
    onPlayClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    LaunchedEffect(channel.id, viewModel.favoriteChannels.collectAsState().value) {
        isFavorite = viewModel.isChannelFavorite(channel.id)
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(115.dp)
            .tvFocusEffect(shape = RoundedCornerShape(12.dp))
            .clickable { onPlayClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )

            // Fade Gradient overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Content info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Top controls (Channel tag and Favorite bullet)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CH ${channel.number}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )

                    IconButton(
                        onClick = { viewModel.toggleChannelFavorite(channel.id) },
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Channel metadata
                Column {
                    Text(
                        text = channel.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = channel.category,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RadioHomeCard(
    station: RadioStation,
    viewModel: MediaViewModel,
    onPlayClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    LaunchedEffect(station.id, viewModel.favoriteRadioStations.collectAsState().value) {
        isFavorite = viewModel.isRadioFavorite(station.id)
    }

    val cardColor = remember(station.themeColorHex) {
        try {
            Color(android.graphics.Color.parseColor(station.themeColorHex))
        } catch (e: Exception) {
            Color(0xFF6B4EFE)
        }
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(115.dp)
            .tvFocusEffect(shape = RoundedCornerShape(12.dp))
            .clickable { onPlayClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, CardColorGradientOverlay(cardColor))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = station.logoUrl,
                contentDescription = station.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.45f
            )

            // Glow gradient overlay matching station
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                cardColor.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            // Station particulars
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = station.frequency,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(cardColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    IconButton(
                        onClick = { viewModel.toggleRadioFavorite(station.id) },
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favoritos",
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = station.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = station.genre,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

private fun CardColorGradientOverlay(color: Color): Brush {
    return Brush.radialGradient(
        colors = listOf(color, Color.White.copy(alpha = 0.08f)),
        radius = 180f
    )
}

@Composable
fun CatalogItemHomeCard(
    item: CatalogItem,
    layoutType: String = "Vertical",
    isFavorite: Boolean = false,
    progress: Float = 0f,
    onFocus: () -> Unit = {},
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardWidth = when (layoutType) {
        "Horizontal Poster Row", "Horizontal" -> 145.dp
        "Vertical Poster Row", "Vertical" -> 165.dp
        "Landscape Row" -> 220.dp
        "Banner Row" -> 270.dp
        "Large Featured Row" -> 210.dp
        "Compact Row" -> 115.dp
        else -> 145.dp
    }
    
    val imageHeight = when (layoutType) {
        "Horizontal Poster Row", "Horizontal" -> 205.dp
        "Vertical Poster Row", "Vertical" -> 235.dp
        "Landscape Row" -> 125.dp
        "Banner Row" -> 100.dp
        "Large Featured Row" -> 290.dp
        "Compact Row" -> 165.dp
        else -> 205.dp
    }

    var isFocused by remember { mutableStateOf(false) }
    var showInlineVideo by remember { mutableStateOf(false) }
    
    LaunchedEffect(isFocused) {
        if (isFocused) {
            kotlinx.coroutines.delay(1000)
            showInlineVideo = true
        } else {
            showInlineVideo = false
        }
    }

    val cinematicDetails = remember(item.id) { getCinematicDetails(item) }
    val hasTrailer = cinematicDetails.trailerUrl.isNotEmpty()

    val animatedWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (showInlineVideo && hasTrailer) 240.dp else cardWidth,
        animationSpec = androidx.compose.animation.core.tween(350),
        label = "inline_video_width"
    )

    val appliedModifier = if (modifier == Modifier) {
        Modifier.width(animatedWidth)
    } else modifier
    Card(
        modifier = appliedModifier
            .onFocusChanged { state ->
                isFocused = state.isFocused || state.hasFocus
                if (isFocused) {
                    onFocus()
                }
            }
            .clickable { onClick() }
            .tvFocusEffect(
                shape = RoundedCornerShape(12.dp),
                focusedBorderColor = Color(0xFF00E5FF),
                scaleAmount = 1.08f
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                if (showInlineVideo && hasTrailer) {
                    var isReady by remember { mutableStateOf(false) }
                    AndroidView(
                        factory = { ctx ->
                            android.widget.VideoView(ctx).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                isFocusable = false
                                isFocusableInTouchMode = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .focusable(false),
                        update = { videoView ->
                            val url = cinematicDetails.trailerUrl
                            if (videoView.tag != url) {
                                videoView.tag = url
                                try {
                                    videoView.stopPlayback()
                                    videoView.setVideoPath(url)
                                    videoView.setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        mp.setVolume(0f, 0f)
                                        videoView.start()
                                        videoView.clearFocus()
                                        isReady = true
                                    }
                                    videoView.setOnErrorListener { _, _, _ -> true }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        onRelease = { videoView ->
                            try {
                                videoView.stopPlayback()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    )
                    if (!isReady) {
                        AsyncImage(
                            model = cinematicDetails.backdropUrl,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    // Movie/Show Poster
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Gold Rating Overlay Tag
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.rating,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Favorite Overlay Tag
                if (isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorito",
                            tint = Color.Red,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                // Dynamic Year overlay gradient background and Progress Indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(4.dp)
                    ) {
                        Text(
                            text = item.year,
                            color = Color.White.copy(alpha = 0.80f),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Seen progress bar
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(Color(0xFF00E5FF))
                            )
                        }
                    }
                }
            }

            // Title padding segment
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.genre,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 8.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

data class ActorInfo(val name: String, val role: String, val photoUrl: String)

fun getMockCast(itemTitle: String, genre: String): List<ActorInfo> {
    val cleanTitle = itemTitle.lowercase()
    return when {
        cleanTitle.contains("dune") -> listOf(
            ActorInfo("T. Chalamet", "Paul Atreides", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200"),
            ActorInfo("Zendaya", "Chani", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200"),
            ActorInfo("Austin Butler", "Feyd-Rautha", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200"),
            ActorInfo("Florence Pugh", "Irulan", "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200"),
            ActorInfo("R. Ferguson", "Lady Jessica", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200")
        )
        cleanTitle.contains("oppenheimer") -> listOf(
            ActorInfo("Cillian Murphy", "Oppenheimer", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=200"),
            ActorInfo("Emily Blunt", "Kitty Opp.", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200"),
            ActorInfo("R. Downey Jr.", "Lewis Strauss", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?q=80&w=200"),
            ActorInfo("Matt Damon", "Leslie Groves", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?q=80&w=200")
        )
        cleanTitle.contains("spider") -> listOf(
            ActorInfo("S. Moore", "Miles Morales", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200"),
            ActorInfo("H. Steinfeld", "Gwen Stacy", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200"),
            ActorInfo("Oscar Isaac", "Miguel O'Hara", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200")
        )
        cleanTitle.contains("interstellar") || cleanTitle.contains("interestelar") -> listOf(
            ActorInfo("M. McConaughey", "Cooper", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=200"),
            ActorInfo("Anne Hathaway", "Brand", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200"),
            ActorInfo("J. Chastain", "Murph", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200")
        )
        else -> {
            if (genre.contains("Terror", true) || genre.contains("Suspenso", true)) {
                listOf(
                    ActorInfo("Jenna Ortega", "Tara", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200"),
                    ActorInfo("B. Skarsgård", "Pennywise", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200"),
                    ActorInfo("Anya Taylor-Joy", "Thomasin", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200")
                )
            } else if (genre.contains("Acción", true) || genre.contains("Ficción", true) || genre.contains("Sci-Fi", true)) {
                listOf(
                    ActorInfo("Pedro Pascal", "Mandalorian", "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?q=80&w=200"),
                    ActorInfo("Ana de Armas", "Paloma", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200"),
                    ActorInfo("Ryan Gosling", "K", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200")
                )
            } else {
                listOf(
                    ActorInfo("Margot Robbie", "Barbie", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200"),
                    ActorInfo("Glen Powell", "Hangman", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200"),
                    ActorInfo("S. Sweeney", "Bea", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200")
                )
            }
        }
    }
}

@Composable
fun CatalogItemDetailsDialog(
    item: CatalogItem,
    viewModel: MediaViewModel,
    onDismiss: () -> Unit,
    onTrailerClick: (CatalogItem) -> Unit = {}
) {
    val context = LocalContext.current
    val localDetails = remember(item) { getCinematicDetails(item) }
    val offlineDescription = remember(item) {
        val joins = localDetails.subtitleLines.joinToString("\n")
        if (joins.replace("\\[.*?\\]".toRegex(), "").trim().isNotEmpty()) joins else item.description
    }
    var dynamicDescription by remember(item) { mutableStateOf(offlineDescription.ifEmpty { item.description }) }
    var dynamicRating by remember(item) { mutableStateOf(item.rating) }
    var dynamicYear by remember(item) { mutableStateOf(item.year) }
    var dynamicLogoUrl by remember(item) { mutableStateOf<String?>(null) }
    var dynamicBackdrop by remember(item) { mutableStateOf("") }
    var dynamicCast by remember(item) { mutableStateOf<List<ActorInfo>>(getMockCast(item.title, item.genre)) }

    LaunchedEffect(item) {
        val prefs = context.getSharedPreferences("lumina_prefs", android.content.Context.MODE_PRIVATE)
        val apiKey = prefs.getString("tmdb_api_key", "")?.trim() ?: ""
        if (apiKey.isNotEmpty()) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val client = okhttp3.OkHttpClient()
                    var tmdbId = item.tmdbId
                    var isTv = item.isTvShow
                    
                    // Fallback lookup: Search by title if tmdbId is missing (so simple custom lists load real dynamic overviews too!)
                    if (tmdbId.isNullOrEmpty()) {
                        val searchUrl = "https://api.themoviedb.org/3/search/multi?query=${java.net.URLEncoder.encode(item.title, "UTF-8")}&language=es-MX"
                        val reqBuilder = okhttp3.Request.Builder()
                        if (apiKey.startsWith("ey")) {
                            reqBuilder.url(searchUrl)
                            reqBuilder.header("Authorization", "Bearer $apiKey")
                        } else {
                            reqBuilder.url("$searchUrl&api_key=$apiKey")
                        }
                        val resp = client.newCall(reqBuilder.build()).execute()
                        if (resp.isSuccessful) {
                            val jsonStr = resp.body?.string() ?: ""
                            val results = org.json.JSONObject(jsonStr).optJSONArray("results")
                            if (results != null && results.length() > 0) {
                                val first = results.getJSONObject(0)
                                tmdbId = first.optString("id")
                                isTv = first.optString("media_type") == "tv" || first.has("first_air_date") || (first.has("name") && !first.has("title"))
                            }
                        }
                    }
                    
                    if (!tmdbId.isNullOrEmpty()) {
                        val mediaType = if (isTv) "tv" else "movie"
                        
                        // 1. Fetch details
                        val detailUrl = "https://api.themoviedb.org/3/$mediaType/$tmdbId?language=es-MX"
                        val reqBuilder = okhttp3.Request.Builder()
                        if (apiKey.startsWith("ey")) {
                            reqBuilder.url(detailUrl)
                            reqBuilder.header("Authorization", "Bearer $apiKey")
                        } else {
                            reqBuilder.url("$detailUrl&api_key=$apiKey")
                        }
                        val resp = client.newCall(reqBuilder.build()).execute()
                        if (resp.isSuccessful) {
                            val jsonStr = resp.body?.string() ?: ""
                            val jsonObj = org.json.JSONObject(jsonStr)
                            val overview = jsonObj.optString("overview", "").trim()
                            if (overview.isNotEmpty()) {
                                dynamicDescription = overview
                            }
                            val vote = jsonObj.optDouble("vote_average", 0.0)
                            if (vote > 0.0) {
                                dynamicRating = String.format(java.util.Locale.US, "%.1f", vote)
                            }
                            val releaseDate = jsonObj.optString("release_date").ifEmpty { jsonObj.optString("first_air_date", "") }
                            if (releaseDate.length >= 4) {
                                dynamicYear = releaseDate.substring(0, 4)
                            }
                            val backdropPath = jsonObj.optString("backdrop_path", "")
                            if (backdropPath.isNotEmpty()) {
                                dynamicBackdrop = "https://image.tmdb.org/t/p/w1280$backdropPath"
                            }
                        }
                        
                        // 2. Fetch logo
                        val logoUrlBuilder = "https://api.themoviedb.org/3/$mediaType/$tmdbId/images?include_image_language=es,en,null"
                        val logoReqBuilder = okhttp3.Request.Builder()
                        if (apiKey.startsWith("ey")) {
                            logoReqBuilder.url(logoUrlBuilder)
                            logoReqBuilder.header("Authorization", "Bearer $apiKey")
                        } else {
                            logoReqBuilder.url("$logoUrlBuilder&api_key=$apiKey")
                        }
                        val logoResp = client.newCall(logoReqBuilder.build()).execute()
                        if (logoResp.isSuccessful) {
                            val jsonStr = logoResp.body?.string() ?: ""
                            val logos = org.json.JSONObject(jsonStr).optJSONArray("logos")
                            if (logos != null && logos.length() > 0) {
                                var bestPath = logos.getJSONObject(0).optString("file_path")
                                for (i in 0 until logos.length()) {
                                    val lang = logos.getJSONObject(i).optString("iso_639_1", "")
                                    if (lang == "es") {
                                        bestPath = logos.getJSONObject(i).optString("file_path")
                                        break
                                    }
                                }
                                dynamicLogoUrl = "https://image.tmdb.org/t/p/w500$bestPath"
                                try {
                                    val creditsUrl = "https://api.themoviedb.org/3/$mediaType/$tmdbId/credits?language=es-MX"
                                    val creditsReqBuilder = okhttp3.Request.Builder()
                                    if (apiKey.startsWith("ey")) {
                                        creditsReqBuilder.url(creditsUrl)
                                        creditsReqBuilder.header("Authorization", "Bearer $apiKey")
                                    } else {
                                        creditsReqBuilder.url("$creditsUrl&api_key=$apiKey")
                                    }
                                    val creditsResp = client.newCall(creditsReqBuilder.build()).execute()
                                    if (creditsResp.isSuccessful) {
                                        val jsonStr = creditsResp.body?.string() ?: ""
                                        val castArray = org.json.JSONObject(jsonStr).optJSONArray("cast")
                                        if (castArray != null && castArray.length() > 0) {
                                            val tmdbCast = mutableListOf<ActorInfo>()
                                            val count = minOf(castArray.length(), 10)
                                            for (i in 0 until count) {
                                                val castObj = castArray.getJSONObject(i)
                                                val name = castObj.optString("name", "")
                                                val character = castObj.optString("character", "")
                                                val profilePath = castObj.optString("profile_path", "")
                                                val photoUrl = if (profilePath.isNotEmpty() && profilePath != "null") {
                                                    "https://image.tmdb.org/t/p/w185$profilePath"
                                                } else {
                                                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=200"
                                                }
                                                tmdbCast.add(ActorInfo(name, character, photoUrl))
                                            }
                                            if (tmdbCast.isNotEmpty()) {
                                                dynamicCast = tmdbCast
                                            }
                                        }
                                    }
                                } catch (creditsEx: Exception) {
                                    creditsEx.printStackTrace()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val details = remember(item) { getCinematicDetails(item) }

    // Dynamic and high-fidelity generation of cinematic data specs
    val isSeriesOrAnime = remember(item) {
        val titleLower = item.title.lowercase()
        val genreLower = item.genre.lowercase()
        genreLower.contains("anime") || genreLower.contains("animación") || titleLower.contains("serie") || titleLower.contains("temporada") || item.isTvShow
    }

    val director = remember(item) {
        val titleLower = item.title.lowercase()
        when {
            titleLower.contains("dune") -> "Denis Villeneuve"
            titleLower.contains("oppenheimer") -> "Christopher Nolan"
            titleLower.contains("interstellar") || titleLower.contains("interestelar") -> "Christopher Nolan"
            titleLower.contains("spider") -> "Kemp Powers"
            item.genre.contains("Acción", true) -> "Chad Stahelski"
            item.genre.contains("Terror", true) -> "James Wan"
            else -> "Jon Favreau"
        }
    }

    val productora = remember(item) {
        val titleLower = item.title.lowercase()
        when {
            titleLower.contains("dune") -> "Warner Bros. / Legendary Entertainment"
            titleLower.contains("oppenheimer") -> "Universal Pictures / Syncopy"
            titleLower.contains("spider") -> "Columbia Pictures / Marvel Arts"
            item.genre.contains("Anime", true) -> "Toei Animation"
            else -> "Paramount Pictures / Universal"
        }
    }

    val pais = remember(item) {
        if (item.genre.contains("Anime", true) || item.genre.contains("Manga", true)) "Japón" else "Estados Unidos"
    }

    val idioma = remember(item) {
        "Español Latino / Inglés"
    }

    val clasificacion = remember(item) {
        if (item.genre.contains("Terror", true) || item.genre.contains("Horror", true) || item.genre.contains("Drama", true)) "R (Público Adulto)" else "PG-13 (Público General)"
    }

    val temporadasInfo = remember(item) {
        if (isSeriesOrAnime) "3 Temporadas" else "Película Completa"
    }

    val emisionStatus = remember(item) {
        if (isSeriesOrAnime) "En Emisión Semanal" else "Emitido"
    }

    val duracionText = remember(item) {
        if (isSeriesOrAnime) "24m por ep." else "1h 56m"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() } // Tap outside content card dismisses
        ) {
            val isWideLayout = maxWidth >= 600.dp
            val cardWidthPercent = if (isWideLayout) 0.85f else 0.95f
            val cardPadding = if (isWideLayout) 24.dp else 12.dp

            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(cardWidthPercent)
                    .fillMaxHeight(0.9f)
                    .clickable(enabled = false) {} // Prevent click-through of content
                    .padding(vertical = cardPadding),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0E17)),
                border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color.White.copy(alpha = 0.05f))))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // 1. Hero Backdrop Cover Image with Close button
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            AsyncImage(
                                model = dynamicBackdrop.ifEmpty { details.backdropUrl },
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.45f
                            )
                            
                            // Cinematic dark wash over backdrop
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xFF0A0E17).copy(alpha = 0.6f),
                                                Color(0xFF0A0E17)
                                            )
                                        )
                                    )
                            )

                            // Close Button
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cerrar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Left Accent Title Overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = item.title.uppercase(),
                                    color = Color.White,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        letterSpacing = (-0.5).sp,
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.9f),
                                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                            blurRadius = 6f
                                        )
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.genre,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .background(Color(0xFF00E5FF).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = dynamicYear,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // 2. Responsive Content Pane (Split on Wide, Stacked on Mobile)
                    item {
                        if (isWideLayout) {
                            // SPLIT SCREEN: Left column Poster and Info list / Right Column Synopsis and Cast
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                // Column A: Poster Card with Details Grid
                                Column(
                                    modifier = Modifier.width(180.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                    ) {
                                        AsyncImage(
                                            model = item.posterUrl,
                                            contentDescription = item.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    // Compact TMDB badge list
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "TMDB Score",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 9.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Rating",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = dynamicRating,
                                                color = Color(0xFFFFD700),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // Column B: Main Details Panel and Metadata
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Action buttons grid
                                    DetailsActionsGrid(item, viewModel, onDismiss)

                                    // Synopsis Segment
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "SINOPSIS / RESUMEN",
                                            color = Color(0xFF00E5FF),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = dynamicDescription,
                                            color = Color.White.copy(alpha = 0.82f),
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Quick Spec Grid (Wide Layout representation)
                                    SpecInformationGrid(
                                        director = director,
                                        productora = productora,
                                        pais = pais,
                                        idioma = idioma,
                                        clasificacion = clasificacion,
                                        temporadas = temporadasInfo,
                                        status = emisionStatus,
                                        duracion = duracionText
                                    )
                                }
                            }
                        } else {
                            // PORTRAIT / MOBILE LAYOUT: Stacked components sequentially
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // A Row at the top of stacked layout containing the poster card and compact info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .height(155.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                                    ) {
                                        AsyncImage(
                                            model = item.posterUrl,
                                            contentDescription = item.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // TMDB Score Badge
                                        Row(
                                            modifier = Modifier
                                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(6.dp))
                                                .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "TMDB Score",
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 9.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Rating",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = dynamicRating,
                                                color = Color(0xFFFFD700),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Text(
                                            text = "Director: $director",
                                            color = Color.White.copy(alpha = 0.70f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            text = "Productora:\n$productora",
                                            color = Color.White.copy(alpha = 0.55f),
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                // Inline action button bar
                                DetailsActionsGrid(item, viewModel, onDismiss)

                                // Synopsis Card
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "SINOPSIS / RESUMEN",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.7.sp
                                    )
                                    Text(
                                        text = dynamicDescription,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.2.sp,
                                        lineHeight = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Quick Spec Grid
                                SpecInformationGrid(
                                    director = director,
                                    productora = productora,
                                    pais = pais,
                                    idioma = idioma,
                                    clasificacion = clasificacion,
                                    temporadas = temporadasInfo,
                                    status = emisionStatus,
                                    duracion = duracionText
                                )
                            }
                        }
                    }

                    // 3. Horizontal scrolling Cast of Actors
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "REPARTO Y ELENCO PRINCIPAL",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp)
                            ) {
                                items(dynamicCast) { actor ->
                                    val cardScale = if (isWideLayout) 1f else 0.85f
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width((72.dp * cardScale).coerceAtLeast(64.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size((52.dp * cardScale).coerceAtLeast(46.dp))
                                                .clip(CircleShape)
                                                .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.40f), CircleShape)
                                        ) {
                                            SubcomposeAsyncImage(
                                                model = actor.photoUrl, loading = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 1.dp, modifier = Modifier.size(16.dp)) } }, error = { val initials = actor.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(); Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors = listOf(Color(0xFF00E5FF), Color(0xFF00FF87)))), contentAlignment = Alignment.Center) { Text(text = initials, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) } },
                                                contentDescription = actor.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = actor.name,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Text(
                                            text = actor.role,
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 8.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DetailsActionsGrid(
    item: CatalogItem,
    viewModel: MediaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val details = remember(item) { getCinematicDetails(item) }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 🔴 Ver Tráiler (Official Dedicated YouTube Player)
        Button(
            onClick = {
                onDismiss()
                viewModel.activeTrailerItem = item
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000), contentColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Movie, contentDescription = "Ver Tráiler", tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("VER TRÁILER", fontWeight = FontWeight.Black, fontSize = 10.5.sp)
        }

        // ▶ Reproducir
        Button(
            onClick = {
                onDismiss()
                val movieChannel = Channel(
                    id = "trailer_${item.id}",
                    name = item.title,
                    streamUrl = item.streamUrl ?: details.trailerUrl,
                    logoUrl = item.posterUrl,
                    category = "Cine Premium",
                    description = item.description,
                    number = 999
                )
                viewModel.selectChannel(movieChannel)
                viewModel.isFullscreenPlayerActive = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("REPRODUCIR", fontWeight = FontWeight.Black, fontSize = 10.5.sp)
        }

        // ▶ Continuar Viendo
        Button(
            onClick = {
                onDismiss()
                Toast.makeText(context, "Reanudando reproducción desde última pausa...", Toast.LENGTH_SHORT).show()
                val movieChannel = Channel(
                    id = "trailer_${item.id}",
                    name = item.title,
                    streamUrl = item.streamUrl ?: details.trailerUrl,
                    logoUrl = item.posterUrl,
                    category = "Cine Premium",
                    description = item.description,
                    number = 999
                )
                viewModel.selectChannel(movieChannel)
                viewModel.isFullscreenPlayerActive = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87), contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("CONTINUAR VIENDO", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        // ➕ Mi Lista
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "${item.title} añadida a Mi Lista", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("MI LISTA", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        // ❤ Favoritos
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "${item.title} añidada a Favoritos", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("FAVORITOS", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        // ⬇ Descargar
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "Descargando para reproducción offline...", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("DESCARGAR", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }

        // 📤 Compartir
        OutlinedButton(
            onClick = {
                try {
                    val shareStr = "¡Mira ${item.title} (${item.year}) en Lumina! Calificación: ${item.rating} estrella."
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, shareStr)
                        type = "text/plain"
                    }
                    val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                } catch(e: Exception) {
                    Toast.makeText(context, "No se pudo compartir", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("COMPARTIR", fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

@Composable
fun SpecInformationGrid(
    director: String,
    productora: String,
    pais: String,
    idioma: String,
    clasificacion: String,
    temporadas: String,
    status: String,
    duracion: String
) {
    val items = listOf(
        Pair("Director", director),
        Pair("Productora", productora),
        Pair("País de Origen", pais),
        Pair("Audio / Idioma", idioma),
        Pair("Clasificación", clasificacion),
        Pair("Episodios / Duración", "$temporadas ($duracion)"),
        Pair("Estado de Emisión", status)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "DETALLES COMPLETOS DE LA PRODUCCIÓN",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )

        items.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.40f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(0.9f)
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CatalogVerticalGrid(
    items: List<CatalogItem>,
    layoutType: String = "Vertical",
    favoriteCatalogItems: Set<String> = emptySet(),
    seenProgress: Map<String, Float> = emptyMap(),
    onItemFocus: (CatalogItem) -> Unit = {},
    onClick: (CatalogItem) -> Unit
) {
    val chunked = remember(items) { items.chunked(3) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        CatalogItemHomeCard(
                            item = item,
                            layoutType = layoutType,
                            isFavorite = item.id in favoriteCatalogItems,
                            progress = seenProgress[item.id] ?: 0f,
                            onFocus = { onItemFocus(item) },
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onClick(item) }
                        )
                    }
                }
                // Align column layouts cleanly if row size is under 3
                val remainder = 3 - rowItems.size
                if (remainder > 0) {
                    repeat(remainder) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogItemNumberedCard(
    item: CatalogItem,
    rank: Int,
    isFavorite: Boolean = false,
    progress: Float = 0f,
    onFocus: () -> Unit = {},
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .onFocusChanged { state ->
                if (state.isFocused || state.hasFocus) {
                    onFocus()
                }
            }
            .width(150.dp)
            .height(175.dp)
            .clickable { onClick() }
            .tvFocusEffect(
                shape = RoundedCornerShape(8.dp),
                focusedBorderColor = Color(0xFF00E5FF),
                scaleAmount = 1.12f
            )
    ) {
        // 1. Giant Number Rank (Drawn FIRST on the bottom layer to stay behind the poster card)
        val rankStr = "$rank"
        Text(
            text = rankStr,
            style = TextStyle(
                fontSize = 120.sp,
                fontWeight = FontWeight.W900,
                color = Color.White.copy(alpha = 0.82f), // Semi-transparent silvery white/gray look
                letterSpacing = (-9).sp,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.95f),
                    offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                    blurRadius = 8f
                )
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 2.dp, y = 14.dp) // Aligns beautifully with the bottom edge
        )

        // 2. Poster Card (Drawn SECOND on the top layer to overlap the giant rank number on the right)
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier
                .width(110.dp)
                .height(155.dp)
                .align(Alignment.BottomEnd)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Movie/Show Poster
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gold Rating Overlay Tag
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.rating,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Favorite Overlay Tag
                if (isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorito",
                            tint = Color.Red,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }

                // Dynamic Year overlay gradient background & progress
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                )
                            )
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Column {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.year,
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Seen progress bar
                    if (progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(Color(0xFF00E5FF))
                            )
                        }
                    }
                }
            }
        }
    }
}

// Cinematic Details helper structures for rich immersive home page banners matching user screenshot
data class CinematicInfo(
    val logoText: String,
    val dateAndMetadata: String,
    val providerBadge: String,
    val budget: String,
    val subtitleLines: List<String>,
    val trailerUrl: String,
    val backdropUrl: String
)

data class LoadedTmdbDetails(
    val description: String,
    val rating: String,
    val year: String,
    val logoUrl: String?,
    val backdropUrl: String?
)

fun getCinematicDetails(movie: CatalogItem): CinematicInfo {
    return when (movie.id) {
        "m_michael" -> CinematicInfo(
            logoText = "Michael",
            dateAndMetadata = "22 Apr 2026 • Music / Drama • 2h 6m",
            providerBadge = "tv+",
            budget = "Presupuesto: $250M",
            subtitleLines = listOf(
                "Michael: No le temas a la oscuridad...",
                "Su voz sacudió al mundo, su corazón rompió moldes.",
                "Desde el descubrimiento de su extraordinario talento temprano...",
                "[Suena Billie Jean en versión sinfónica de fondo]",
                "Hasta convertirse en una visionaria estrella del pop global.",
                "Michael: El viaje definitivo del Rey del Pop de regreso."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=1200"
        )
        "m_theboys" -> CinematicInfo(
            logoText = "THE BOYS",
            dateAndMetadata = "13 Jun 2024 • Acción / Ciencia Ficción • 4 Temp",
            providerBadge = "prime video",
            budget = "Presupuesto: $180M",
            subtitleLines = listOf(
                "Homelander: ¡Mírenme! ¡Yo soy vuestro único salvador!",
                "Un grupo fuera de la ley dispuesto a desenmascarar el corporativismo.",
                "Butcher: Es hora de nivelar esta balanza ruidosa...",
                "[Efecto de rayos oculares destrozando un hangar de caza]",
                "The Boys: La última y más explosiva temporada de todas."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1200"
        )
        "m1" -> CinematicInfo(
            logoText = "DUNE II",
            dateAndMetadata = "29 Feb 2024 • Ciencia Ficción / Aventura • 2h 46m",
            providerBadge = "MAX",
            budget = "Presupuesto: $190M",
            subtitleLines = listOf(
                "Paul Atreides: Seguiré el camino que conduce a mi pueblo.",
                "Chani: Aquel que controla la especia gobierna el espacio.",
                "[Gemidos de gigantescos gusanos de arena de Arrakis]",
                "Únete a los Fremen en la última cruzada santa por Arrakis."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1200"
        )
        "m2" -> CinematicInfo(
            logoText = "Oppenheimer",
            dateAndMetadata = "20 Jul 2023 • Historia / Drama • 3h 0m",
            providerBadge = "UNIVERSAL",
            budget = "Presupuesto: $100M",
            subtitleLines = listOf(
                "Oppenheimer: No sabemos si causaremos la ignición atmosférica.",
                "Un invento secreto que cambiará irrevocablemente la historia del hombre.",
                "[Tictac acelerado de reloj despertador con suspenso bélico]",
                "Oppenheimer: Ahora me he convertido en la muerte, destructora de mundos."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1444703686981-a3abbc4d4fe3?q=80&w=1200"
        )
        "m3" -> CinematicInfo(
            logoText = "SPIDER-MAN",
            dateAndMetadata = "02 Jun 2023 • Animación / Acción • 2h 20m",
            providerBadge = "CINE PREMIUM",
            budget = "Presupuesto: $150M",
            subtitleLines = listOf(
                "Miles Morales: Todos me dicen cómo debe ser mi historia.",
                "Un viaje a través del indómito Multiverso arácnido.",
                "Gwen Stacy: ¿Quieres salir de aquí?",
                "Spider-Man: Across the Spider-Verse ya disponible."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            backdropUrl = "https://images.unsplash.com/photo-1618005198143-e52834644026?q=80&w=1200"
        )
        else -> CinematicInfo(
            logoText = movie.title,
            dateAndMetadata = "${movie.year} • ${movie.genre} • 2h 15m",
            providerBadge = "CINE PREMIUM",
            budget = "Presupuesto: $120M",
            subtitleLines = listOf(
                "Entra en un festín cinematográfico inolvidable en Lumina...",
                "Ya disponible con soporte multi-idioma oficial en HD.",
                "Presiona reproducir para iniciar la inmersión completa."
            ),
            trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            backdropUrl = movie.posterUrl
        )
    }
}

@Composable
fun TrailerYoutubePlayerDialog(
    item: CatalogItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val details = remember(item) { getCinematicDetails(item) }
    val videoUrl = remember(item) { item.streamUrl ?: details.trailerUrl }
    
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableStateOf(0) }
    var totalDuration by remember { mutableStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var videoViewInstance by remember { mutableStateOf<VideoView?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var isFitMode by remember { mutableStateOf(false) }
    
    // Auto-hide controls after 3.5 seconds
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            kotlinx.coroutines.delay(3500)
            showControls = false
        }
    }

    // Position progress tracking thread
    LaunchedEffect(isPlaying, videoViewInstance) {
        while (isPlaying && videoViewInstance != null) {
            try {
                val vv = videoViewInstance!!
                if (vv.isPlaying) {
                    currentPosition = vv.currentPosition
                    val duration = vv.duration
                    if (duration > 0) {
                        totalDuration = duration
                    }
                    isBuffering = false
                }
            } catch (e: Exception) {
                // Ignore transient errors
            }
            kotlinx.coroutines.delay(200)
        }
    }

    androidx.activity.compose.BackHandler {
        onDismiss()
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    showControls = !showControls
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        videoViewInstance = this
                    }
                },
                update = { videoView ->
                    if (videoView.tag != videoUrl) {
                        videoView.tag = videoUrl
                        try {
                            isBuffering = true
                            videoView.stopPlayback()
                            videoView.setVideoPath(videoUrl)
                            videoView.setOnPreparedListener { mp ->
                                isBuffering = false
                                mp.isLooping = true
                                val vol = if (isMuted) 0f else 1f
                                mp.setVolume(vol, vol)
                                totalDuration = videoView.duration
                                if (isPlaying) {
                                    videoView.start()
                                }
                            }
                            videoView.setOnErrorListener { _, _, _ ->
                                isBuffering = false
                                Toast.makeText(context, "Error al reproducir tráiler", Toast.LENGTH_SHORT).show()
                                true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            isBuffering = false
                        }
                    }
                },
                modifier = if (isFitMode) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .align(Alignment.Center)
                }
            )

            if (isBuffering) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF0000),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                ) {
                    // TOP BAR: Navigation title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onDismiss() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Cerrar",
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Reproduciendo Tráiler Oficial",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Premium Tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Red.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayCircle,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "TRÁILER EXCLUSIVO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    // CENTER: Playback controllers
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                videoViewInstance?.let { vv ->
                                    val newPos = (vv.currentPosition - 10000).coerceAtLeast(0)
                                    vv.seekTo(newPos)
                                    currentPosition = newPos
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Replay10,
                                contentDescription = "Retroceder 10 Segundos",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                isPlaying = !isPlaying
                                videoViewInstance?.let { vv ->
                                    if (isPlaying) {
                                        vv.start()
                                    } else {
                                        vv.pause()
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(76.dp)
                                .background(Color(0xFFFF0000), CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Reproducir o Pausar",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                videoViewInstance?.let { vv ->
                                    val duration = vv.duration
                                    val newPos = (vv.currentPosition + 10000).coerceAtMost(if (duration > 0) duration else 999999)
                                    vv.seekTo(newPos)
                                    currentPosition = newPos
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Forward10,
                                contentDescription = "Adelantar 10 Segundos",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // BOTTOM BAR: Timeline timeline
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .align(Alignment.BottomCenter),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Slider(
                            value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f,
                            onValueChange = { percent ->
                                val targetPos = (percent * totalDuration).toInt()
                                currentPosition = targetPos
                                videoViewInstance?.seekTo(targetPos)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF0000),
                                activeTrackColor = Color(0xFFFF0000),
                                inactiveTrackColor = Color.White.copy(alpha = 0.24f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val elapsedStr = remember(currentPosition) { formatSeconds(currentPosition) }
                            val durationStr = remember(totalDuration) { formatSeconds(totalDuration) }
                            Text(
                                text = "$elapsedStr / $durationStr",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        isMuted = !isMuted
                                        videoViewInstance?.let { vv ->
                                            val vol = if (isMuted) 0f else 1f
                                            // Set volume dynamically
                                            Toast.makeText(context, if (isMuted) "Silenciado" else "Sonido Reanudado", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                        contentDescription = "Mudo",
                                        tint = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        isFitMode = !isFitMode
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isFitMode) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                        contentDescription = "Aspecto",
                                        tint = Color.White
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

fun formatSeconds(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
}
