import os

file_path = '/app/applet/app/src/main/java/com/example/ui/screens/HomeHeroBanner.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix literal backslashes from the previous replace
content = content.replace("package com.example.ui.screens\\\\n\\\\nimport androidx.compose.ui.focus.onFocusChanged", "package com.example.ui.screens")
content = content.replace("package com.example.ui.screens\\n\\nimport androidx.compose.ui.focus.onFocusChanged", "package com.example.ui.screens")

lines = content.splitlines()

# Ensure package statement is clean
if lines[0].startswith("package com.example.ui.screens"):
    lines[0] = "package com.example.ui.screens"

# Add import statement
has_import = any("import androidx.compose.ui.focus.onFocusChanged" in l for l in lines)
if not has_import:
    lines.insert(1, "import androidx.compose.ui.focus.onFocusChanged")

# Locate HomeHeroBannerTv
content_clean = "\\n".join(lines)
start_marker = 'fun HomeHeroBannerTv('
end_marker = 'fun HomeHeroBannerMobile('

start_idx = content_clean.find(start_marker)
if start_idx == -1:
    print("ERROR: Start marker not found!")
    exit(1)

end_idx = content_clean.find(end_marker)
if end_idx == -1:
    print("ERROR: End marker not found!")
    exit(1)

search_segment = content_clean[start_idx:end_idx]
last_brace = search_segment.rfind('}')
if last_brace == -1:
    print("ERROR: Last brace not found!")
    exit(1)

absolute_end_idx = start_idx + last_brace + 1

replacement = """fun HomeHeroBannerTv(
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
            android.util.Log.d("LuminaHeroBanner", "Arrived at Hero Banner (TV) - Title: ${targetMovie.title}, Logo: ${richMeta.logoUrl}, Cast: ${targetMovie.castJson}")
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 36.dp.responsive(), end = 48.dp, bottom = 12.dp.responsive(), top = 48.dp.responsive()),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.55f),
                    verticalArrangement = Arrangement.spacedBy(16.dp.responsive()),
                    horizontalAlignment = Alignment.Start
                ) {
                    // 1. Logo or Title (large) with reserved space and elegant positioning
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp.responsive()),
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
                                            fontSize = 32.sp.responsive(),
                                            color = Color.White,
                                            letterSpacing = (-1).sp,
                                            shadow = androidx.compose.ui.graphics.Shadow(
                                                color = Color.Black.copy(alpha = 0.9f),
                                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                                blurRadius = 8f
                                            )
                                        )
                                    )
                                }
                            )
                        } else {
                            Text(
                                text = richMeta.title,
                                style = TextStyle(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp.responsive(),
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

                    // 2. Core Metadata Row: Year • Duration • Genre
                    Row(
                        modifier = Modifier.height(24.dp.responsive()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp.responsive())
                    ) {
                        Text(
                            text = richMeta.year,
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp.responsive()
                        )
                        Text(text = "•", color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp.responsive())
                        Text(
                            text = richMeta.duration,
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp.responsive()
                        )
                        Text(text = "•", color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp.responsive())
                        Text(
                            text = if (richMeta.genres.isNotBlank()) richMeta.genres else "Cine / Drama",
                            color = Color.White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp.responsive()
                        )
                    }

                    // 3. Short Synopsis / Description
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp.responsive()),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = richMeta.description,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp.responsive(),
                            maxLines = 3,
                            lineHeight = 18.sp.responsive(),
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp.responsive()))

                    // 4. Elegant TV-Optimized Capsule Buttons
                    Row(
                        modifier = Modifier.height(44.dp.responsive()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play Button (Reproducir) - White capsule, black text, filled style
                        var isPlayFocused by remember { mutableStateOf(false) }
                        val playBgColor by animateColorAsState(
                            targetValue = if (isPlayFocused) Color(0xFF00E5FF) else Color.White,
                            label = "play_bg"
                        )
                        val playScale by animateFloatAsState(
                            targetValue = if (isPlayFocused) 1.05f else 1.0f,
                            label = "play_scale"
                        )

                        Row(
                            modifier = Modifier
                                .height(44.dp.responsive())
                                .graphicsLayer {
                                    scaleX = playScale
                                    scaleY = playScale
                                }
                                .background(playBgColor, RoundedCornerShape(22.dp))
                                .clickable { onTrailerClick(targetMovie) }
                                .onFocusChanged { isPlayFocused = it.isFocused }
                                .tvFocusEffect(
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .padding(horizontal = 26.dp.responsive()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Reproducir",
                                tint = if (isPlayFocused) Color.White else Color.Black,
                                modifier = Modifier.size(20.dp.responsive())
                            )
                            Spacer(modifier = Modifier.width(8.dp.responsive()))
                            Text(
                                text = "Reproducir",
                                color = if (isPlayFocused) Color.White else Color.Black,
                                fontSize = 13.sp.responsive(),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // My List Button (Mi Lista) - Transparent/outline style with focus border
                        val isFav = targetMovie.id in favoriteCatalogItems
                        var isListFocused by remember { mutableStateOf(false) }
                        val listBgColor by animateColorAsState(
                            targetValue = if (isListFocused) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f),
                            label = "list_bg"
                        )
                        val listBorderColor by animateColorAsState(
                            targetValue = if (isListFocused) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.15f),
                            label = "list_border"
                        )
                        val listScale by animateFloatAsState(
                            targetValue = if (isListFocused) 1.05f else 1.0f,
                            label = "list_scale"
                        )

                        Row(
                            modifier = Modifier
                                .height(44.dp.responsive())
                                .graphicsLayer {
                                    scaleX = listScale
                                    scaleY = listScale
                                }
                                .background(listBgColor, RoundedCornerShape(22.dp))
                                .border(1.dp, listBorderColor, RoundedCornerShape(22.dp))
                                .clickable { viewModel.toggleCatalogItemFavorite(targetMovie.id) }
                                .onFocusChanged { isListFocused = it.isFocused }
                                .tvFocusEffect(
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .padding(horizontal = 26.dp.responsive()),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = "Mi Lista",
                                tint = if (isFav) Color(0xFF00E5FF) else Color.White,
                                modifier = Modifier.size(18.dp.responsive())
                            )
                            Spacer(modifier = Modifier.width(8.dp.responsive()))
                            Text(
                                text = "Mi Lista",
                                color = Color.White,
                                fontSize = 13.sp.responsive(),
                                fontWeight = FontWeight.Bold
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
}"""

new_content = content_clean[:start_idx] + replacement + content_clean[absolute_end_idx:]

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(new_content)

print("SUCCESS: Cleansed package and imports!")
