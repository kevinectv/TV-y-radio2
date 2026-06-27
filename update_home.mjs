import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf-8');

// 1. Replace skeletonEffect with shimmerEffect and Skeleton Composables
const skeletonStart = content.indexOf('// --- Skeleton Loading Effect Extension ---');
const skeletonEnd = content.indexOf('@Composable\nfun HomeScreen(');

const newSkeletons = `// --- Skeleton Loading Effect Extension ---
@Composable
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    this
        .androidx.compose.ui.layout.onGloballyPositioned { size = it.size }
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E1E1E),
                    Color(0xFF333333),
                    Color(0xFF1E1E1E)
                ),
                start = androidx.compose.ui.geometry.Offset(startOffsetX, 0f),
                end = androidx.compose.ui.geometry.Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
            ),
            shape = RoundedCornerShape(4.dp)
        )
}

@Composable
fun HomeSkeleton(isWideLayout: Boolean, bannerHeight: androidx.compose.ui.unit.Dp) {
    Column(modifier = Modifier.fillMaxSize()) {
        HeroSkeleton(isWideLayout, bannerHeight)
        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
            repeat(3) {
                CatalogRowSkeleton(isWideLayout)
            }
        }
    }
}

@Composable
fun HeroSkeleton(isWideLayout: Boolean, bannerHeight: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .padding(
                start = if (isWideLayout) 48.dp else 20.dp.responsive(),
                end = if (isWideLayout) 48.dp else 20.dp.responsive(),
                top = if (isWideLayout) 24.dp else 12.dp.responsive(),
                bottom = if (isWideLayout) 24.dp else 12.dp.responsive()
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp.responsive())) {
            Box(modifier = Modifier.width(if (isWideLayout) 240.dp else 140.dp.responsive()).height(if (isWideLayout) 60.dp else 40.dp.responsive()).shimmerEffect())
            Box(modifier = Modifier.width(if (isWideLayout) 300.dp else 200.dp.responsive()).height(14.dp.responsive()).shimmerEffect())
            Box(modifier = Modifier.fillMaxWidth(if (isWideLayout) 0.5f else 0.8f).height(14.dp.responsive()).shimmerEffect())
            Box(modifier = Modifier.fillMaxWidth(if (isWideLayout) 0.4f else 0.6f).height(14.dp.responsive()).shimmerEffect())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp.responsive())) {
                Box(modifier = Modifier.width(60.dp.responsive()).height(22.dp.responsive()).shimmerEffect())
                Box(modifier = Modifier.width(60.dp.responsive()).height(22.dp.responsive()).shimmerEffect())
                Box(modifier = Modifier.width(60.dp.responsive()).height(22.dp.responsive()).shimmerEffect())
            }
        }
    }
}

@Composable
fun CatalogRowSkeleton(isWideLayout: Boolean) {
    Column {
        Box(
            modifier = Modifier
                .padding(start = 16.dp.responsive(), top = 22.dp.responsive(), bottom = 6.dp.responsive())
                .width(150.dp.responsive())
                .height(16.dp.responsive())
                .shimmerEffect()
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp.responsive()),
            horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
            userScrollEnabled = false
        ) {
            items(6) {
                PosterSkeleton()
            }
        }
    }
}

@Composable
fun PosterSkeleton() {
    Box(
        modifier = Modifier
            .width(130.dp.responsive())
            .height(180.dp.responsive())
            .shimmerEffect()
    )
}

`;
content = content.substring(0, skeletonStart) + newSkeletons + content.substring(skeletonEnd);

const oldCurrentMovie = `val currentMovie = activeHeroMovie ?: featuredMovies.firstOrNull() ?: CatalogItem(
        id = "f1", title = "Michael",
        posterUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=1200",
        year = "2026", rating = "7.7", genre = "Música / Drama",
        description = "El viaje de Michael Jackson más allá de la música, desde el descubrimiento de su extraordinario talento como líder de los Jackson Five..."
    )`;
const newCurrentMovie = `val currentMovie = activeHeroMovie ?: featuredMovies.firstOrNull()`;
content = content.replace(oldCurrentMovie, newCurrentMovie);

const layoutStart = content.indexOf('    val isWideLayout = context.resources.configuration.screenWidthDp >= 580');
const layoutEndStr = '    val trailerToShow = activeTrailerItem ?: viewModel.activeTrailerItem';
const layoutEnd = content.indexOf(layoutEndStr);

const newLayout = `    val isWideLayout = context.resources.configuration.screenWidthDp >= 580
    // Height optimizado: -25% aprox para dejar más espacio al catálogo.
    val bannerHeight = if (isWideLayout) 315.dp else 195.dp.responsive()

    // Control de carga (Skeleton)
    val isLoadingData = catalogs.isEmpty() || currentMovie == null

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF030406))) {
        Crossfade(
            targetState = isLoadingData,
            animationSpec = tween(700),
            label = "home_skeleton_fade",
            modifier = Modifier.fillMaxSize()
        ) { isLoading ->
            if (isLoading) {
                HomeSkeleton(isWideLayout, bannerHeight)
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    // --- 1. NETFLIX-STYLE FULL-SCREEN BACKDROP COVERING THE BACKGROUND ---
                    Crossfade(
                        targetState = currentMovie,
                        animationSpec = tween(750),
                        label = "home_full_backdrop",
                        modifier = Modifier.fillMaxSize()
                    ) { movie ->
                        movie?.let { currentSafeMovie ->
                            val movieDetails = getCinematicDetails(currentSafeMovie)
                            val backdropUrlToUse = activeHeroLoadedDetails?.backdropUrl ?: movieDetails.backdropUrl

                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = backdropUrlToUse,
                                    contentDescription = currentSafeMovie.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Cinematic horizontal dark gradient to protect left-aligned text of Hero Banner
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.95f),
                                                    Color.Black.copy(alpha = 0.82f),
                                                    Color.Black.copy(alpha = 0.35f),
                                                    Color.Transparent
                                                ),
                                                endX = 1200f
                                            )
                                        )
                                )

                                // Cinematic vertical dark gradient to smoothly fade to pure black at bottom
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.30f),
                                                    Color.Black.copy(alpha = 0.55f),
                                                    Color(0xFF030406)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }

                    // --- 2. MAIN STRUCTURAL LAYOUT ---
                    Column(modifier = Modifier.fillMaxSize()) {
                        // A) Fixed Hero Banner
                        currentMovie?.let { currentSafeMovie ->
                            HomeHeroBanner(
                                currentMovie = currentSafeMovie,
                                activeHeroLoadedDetails = activeHeroLoadedDetails,
                                featuredMovies = featuredMovies,
                                favoriteCatalogItems = favoriteCatalogItems,
                                bannerHeight = bannerHeight,
                                isWideLayout = isWideLayout,
                                viewModel = viewModel,
                                scrollState = listState,
                                onTrailerClick = { movie ->
                                    activeTrailerItem = movie
                                },
                                onDetailsClick = { movie ->
                                    viewModel.selectedDetailsItem.value = movie
                                }
                            )
                        }

                        // B) Scrollable Content Rows
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            val homeCatalogs = catalogs.filter { it.isVisible && it.showInHome }

                            if (homeCatalogs.isEmpty()) {
                                if (progressItems.isNotEmpty()) {
                                    item {
                                        HomeSectionRowHeader(
                                            title = "⏱️ CONTINUAR VIENDO",
                                            icon = Icons.Filled.PlayCircle,
                                            color = Color(0xFF00FF87)
                                        )
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
                                            contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 6.dp.responsive())
                                        ) {
                                            items(progressItems) { (item, progressVal) ->
                                                CatalogItemHomeCard(
                                                    item = item,
                                                    layoutType = "Landscape Row",
                                                    isFavorite = item.id in favoriteCatalogItems,
                                                    progress = progressVal,
                                                    onFocus = { activeHeroMovie = item },
                                                    onClick = {
                                                        activeHeroMovie = item
                                                        viewModel.selectedDetailsItem.value = item
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
                                            val (displayName, displayIcon) = getCategoryDisplayInfo(catalog.name)
                                            DrawCatalogRow(
                                                catalog = catalog,
                                                favoriteCatalogItems = favoriteCatalogItems,
                                                seenProgress = seenProgress,
                                                customTitle = displayName,
                                                customIcon = displayIcon,
                                                onFocus = { activeHeroMovie = it },
                                                onClick = { clickedItem ->
                                                    activeHeroMovie = clickedItem
                                                    viewModel.selectedDetailsItem.value = clickedItem
                                                }
                                            )
                                        }
                                    }

                                    // Inject Continue Watching under the first dynamic row
                                    if (index == 0 && progressItems.isNotEmpty()) {
                                        item {
                                            HomeSectionRowHeader(
                                                title = "⏱️ CONTINUAR VIENDO",
                                                icon = Icons.Filled.PlayCircle,
                                                color = Color(0xFF00FF87)
                                            )
                                            LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(14.dp.responsive()),
                                                contentPadding = PaddingValues(horizontal = 16.dp.responsive(), vertical = 6.dp.responsive())
                                            ) {
                                                items(progressItems) { (item, progressVal) ->
                                                    CatalogItemHomeCard(
                                                        item = item,
                                                        layoutType = "Landscape Row",
                                                        isFavorite = item.id in favoriteCatalogItems,
                                                        progress = progressVal,
                                                        onFocus = { activeHeroMovie = item },
                                                        onClick = {
                                                            activeHeroMovie = item
                                                            viewModel.selectedDetailsItem.value = item
                                                        }
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
        }
    }

`;

content = content.substring(0, layoutStart) + newLayout + content.substring(layoutEnd);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log('Update complete.');
