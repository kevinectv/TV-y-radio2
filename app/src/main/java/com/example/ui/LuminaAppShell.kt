package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.ElegantBackground
import com.example.ui.components.tvFocusEffect
import com.example.ui.components.responsive
import com.example.ui.components.getResponsiveScale
import com.example.ui.components.ProfileAvatar
import com.example.ui.screens.ProfileSelectionScreen
import com.example.ui.screens.*
import com.example.data.model.CatalogItem
import kotlinx.coroutines.delay
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LuminaAppShell(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWideLayout = configuration.screenWidthDp >= 580

    // Current live Clock time string
    var timeString by remember { mutableStateOf("12:00 PM") }
    LaunchedEffect(Unit) {
        while (true) {
            val calendar = Calendar.getInstance()
            val rawHour = calendar.get(Calendar.HOUR)
            val min = calendar.get(Calendar.MINUTE)
            val hour = if (rawHour == 0) 12 else rawHour
            val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            timeString = String.format("%d:%02d %s", hour, min, amPm)
            delay(1000)
        }
    }

    val selectedDetailsItem by viewModel.selectedDetailsItem.collectAsState()



    // Premium obsidian & cyan gradient background preserved globally
    val backgroundAccent: String? = null

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // 1. DYNAMIC ATMOSPHERIC VERTICAL-LINE LIGHT GRID BACKGROUND
        ElegantBackground(
            modifier = Modifier.fillMaxSize(),
            accentColorHex = backgroundAccent
        )

        // Main structural Scaffold to support safe edges
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            topBar = {
                // --- 2. BARRA SUPERIOR PREMIUM (GLASSMORPHISM NAV) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Node: Avatar Profile + Search Item expander
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // User Profile Circle (Clicking switches/selects profile)
                        val activeColorHex = viewModel.activeProfile?.profileColor ?: "#00E5FF"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, Color(android.graphics.Color.parseColor(activeColorHex)), RoundedCornerShape(8.dp))
                                .clickable { viewModel.logoutProfile() }
                                .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                        ) {
                            viewModel.activeProfile?.let { profile ->
                                ProfileAvatar(
                                    profile = profile,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // App Title always shown as a clean branded anchor
                        Text(
                            text = "LUMINA",
                            color = Color.White,
                            fontSize = 14.sp.responsive(),
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Central Node: Main Navigation Tabs Row - ONLY on Wide Screens
                    if (isWideLayout) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppTab.values().filter { it != AppTab.SETTINGS }.forEach { tab ->
                                val isSelected = viewModel.currentTab == tab
                                var isTabFocused by remember { mutableStateOf(false) }
                                
                                val tabAlpha by animateFloatAsState(if (isSelected || isTabFocused) 1f else 0.55f, label = "tab_alpha")
                                val tabScale by animateFloatAsState(if (isTabFocused) 1.1f else (if (isSelected) 1.05f else 1f), label = "tab_scale")

                                Box(
                                    modifier = Modifier
                                        .scale(tabScale)
                                        .onFocusChanged { isTabFocused = it.isFocused || it.hasFocus }
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isTabFocused -> Color.White
                                                isSelected -> Color.White.copy(alpha = 0.15f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isTabFocused) 0.dp else if (isSelected) 1.dp else 0.dp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.selectTab(tab) }
                                        .tvFocusEffect(
                                            shape = RoundedCornerShape(10.dp),
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = Color.Transparent,
                                            scaleAmount = 1.03f
                                        )
                                        .padding(horizontal = 14.dp.responsive(), vertical = 8.dp.responsive()),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (tab) {
                                                AppTab.HOME -> Icons.Filled.Home
                                                AppTab.WATCHLIST -> Icons.Filled.Favorite
                                                AppTab.TV -> Icons.Filled.LiveTv
                                                AppTab.RADIO -> Icons.Filled.Radio
                                                AppTab.SEARCH -> Icons.Filled.Search
                                                AppTab.SETTINGS -> Icons.Filled.Settings
                                            },
                                            contentDescription = tab.label,
                                            tint = if (isTabFocused) Color.Black else if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp.responsive())
                                        )

                                        Text(
                                            text = tab.label,
                                            color = if (isTabFocused) Color.Black else if (isSelected) Color.White else Color.White.copy(alpha = tabAlpha),
                                            fontSize = 11.sp.responsive(),
                                            fontWeight = if (isSelected || isTabFocused) FontWeight.ExtraBold else FontWeight.Medium,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Right Node: Live Clock and Configuration quick icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Settings icon in top right - ONLY on Wide Screens (on Mobile it is in the Bottom Bar!)
                        if (isWideLayout) {
                            var isSettingsFocused by remember { mutableStateOf(false) }
                            val settingsRotation by animateFloatAsState(
                                targetValue = if (isSettingsFocused) 180f else 0f,
                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 350, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                label = "settings_rotation"
                            )

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .onFocusChanged { isSettingsFocused = it.isFocused || it.hasFocus }
                                    .focusable()
                                    .background(
                                        color = if (isSettingsFocused) Color.White.copy(alpha = 0.22f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isSettingsFocused) 1.5.dp else 0.dp,
                                        color = if (isSettingsFocused) Color.White.copy(alpha = 0.8f) else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.selectTab(AppTab.SETTINGS) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings Icon Toggle",
                                    tint = if (isSettingsFocused) Color.White else Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .graphicsLayer {
                                            rotationZ = settingsRotation
                                        }
                                )
                            }
                        }

                        // Digital Clock displaying 12-hour AM/PM format
                        Text(
                            text = timeString,
                            color = Color.White,
                            fontSize = if (isWideLayout) 12.sp else 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            bottomBar = {
                if (!isWideLayout) {
                    NavigationBar(
                        containerColor = Color.Black.copy(alpha = 0.85f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
                        tonalElevation = 8.dp
                    ) {
                        AppTab.values().forEach { tab ->
                            val isSelected = viewModel.currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = Color.White,
                                    indicatorColor = Color.White,
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                ),
                                icon = {
                                    Icon(
                                        imageVector = when (tab) {
                                            AppTab.HOME -> Icons.Filled.Home
                                            AppTab.WATCHLIST -> Icons.Filled.Favorite
                                            AppTab.TV -> Icons.Filled.LiveTv
                                            AppTab.RADIO -> Icons.Filled.Radio
                                            AppTab.SEARCH -> Icons.Filled.Search
                                            AppTab.SETTINGS -> Icons.Filled.Settings
                                        },
                                        contentDescription = tab.label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    val labelStr = when (tab) {
                                        AppTab.HOME -> "Home"
                                        AppTab.WATCHLIST -> "Favoritos"
                                        AppTab.TV -> "TV"
                                        AppTab.RADIO -> "Radio"
                                        AppTab.SEARCH -> "Buscar"
                                        AppTab.SETTINGS -> "Ajustes"
                                    }
                                    Text(
                                        text = labelStr,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            // --- 3. SCENE CONTENT WITH MODERN CROSSFADE SLIDE ANIMATIONS ---
            AnimatedContent(
                targetState = viewModel.currentTab,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { x -> if (targetState.ordinal > initialState.ordinal) x else -x }
                    ) + fadeIn() with slideOutHorizontally(
                        targetOffsetX = { x -> if (targetState.ordinal > initialState.ordinal) -x else x }
                    ) + fadeOut()
                },
                modifier = Modifier
                    .fillMaxSize()
            ) { tab ->
                val tabPadding = if (isWideLayout && tab == AppTab.HOME) {
                    PaddingValues(top = 0.dp, bottom = innerPadding.calculateBottomPadding())
                } else {
                    innerPadding
                }
                Box(modifier = Modifier.fillMaxSize().padding(tabPadding)) {
                    when (tab) {
                        AppTab.HOME -> HomeScreen(viewModel = viewModel)
                        AppTab.WATCHLIST -> WatchlistScreen(viewModel = viewModel)
                        AppTab.TV -> TvScreen(viewModel = viewModel)
                        AppTab.RADIO -> RadioScreen(viewModel = viewModel)
                        AppTab.SEARCH -> SearchScreen(viewModel = viewModel)
                        AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }

        // 4. TRANSICIÓN ULTRA-ELEGANTE AL REPRODUCTOR EN PANTALLA COMPLETA INTEGRADO
        AnimatedVisibility(
            visible = viewModel.isFullscreenPlayerActive,
            enter = fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.95f),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.95f),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f)
        ) {
            FullscreenPlayerScreen(viewModel = viewModel)
        }

        // 5. NUEVA PANTALLA DE DETALLES A PANTALLA COMPLETA (NETFLIX STYLE)
        AnimatedVisibility(
            visible = selectedDetailsItem != null,
            enter = fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.95f),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.95f),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(98f)
        ) {
            selectedDetailsItem?.let { item ->
                CatalogItemFullScreenDetails(
                    item = item,
                    viewModel = viewModel,
                    onDismiss = { viewModel.selectedDetailsItem.value = null },
                    onNavigateToSimilar = { newItem ->
                        viewModel.selectedDetailsItem.value = newItem
                    }
                )
            }
        }


    }
}

@Composable
fun SearchCenterOverlay(
    viewModel: MediaViewModel,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val allChannels by viewModel.allChannels.collectAsState()
    val allRadioStations by viewModel.allRadioStations.collectAsState()
    val favoriteChannels by viewModel.favoriteChannels.collectAsState()
    val favoriteRadioStations by viewModel.favoriteRadioStations.collectAsState()

    // Filters Configuration
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Canales de TV", "Emisoras de Radio", "Favoritos")

    val categories = remember {
        listOf("Deportes", "Cine", "Noticias", "Entretenimiento", "Música")
    }
    var activeCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Unified Match Logic
    val matchedChannels = remember(query, allChannels, selectedFilter, activeCategoryFilter, favoriteChannels) {
        allChannels.filter { channel ->
            val matchesQuery = query.isEmpty() || channel.name.contains(query, ignoreCase = true) || channel.description.contains(query, ignoreCase = true)
            val matchesFilter = selectedFilter == "Todos" || selectedFilter == "Canales de TV" ||
                    (selectedFilter == "Favoritos" && favoriteChannels.any { it.id == channel.id })
            val matchesCategory = activeCategoryFilter == null || channel.category.contains(activeCategoryFilter!!, ignoreCase = true) || channel.description.contains(activeCategoryFilter!!, ignoreCase = true)

            matchesQuery && matchesFilter && matchesCategory
        }
    }

    val matchedRadioStations = remember(query, allRadioStations, selectedFilter, activeCategoryFilter, favoriteRadioStations) {
        if (selectedFilter == "Canales de TV" || activeCategoryFilter != null) emptyList() else {
            allRadioStations.filter { radio ->
                val matchesQuery = query.isEmpty() || radio.name.contains(query, ignoreCase = true) || radio.genre.contains(query, ignoreCase = true)
                val matchesFilter = selectedFilter == "Todos" || selectedFilter == "Emisoras de Radio" ||
                        (selectedFilter == "Favoritos" && favoriteRadioStations.any { it.id == radio.id })
                matchesQuery && matchesFilter
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismiss() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxHeight(0.9f)
                .clickable(enabled = true, onClick = {}) // prevent dismiss click propagation
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C101B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "BUSCADOR INTELIGENTE MULTIRED",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input field
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            "Escribe para buscar películas, deportes, canales o radios...",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4A89FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filters.forEach { filterOpt ->
                        val isSel = selectedFilter == filterOpt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isSel) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable { selectedFilter = filterOpt }
                                .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filterOpt,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                // Theme Filters (only relevant for Channels/Todos)
                if (selectedFilter == "Todos" || selectedFilter == "Canales de TV") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isNone = activeCategoryFilter == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNone) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                                .clickable { activeCategoryFilter = null }
                                .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                "Todo-Género",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        categories.forEach { cat ->
                            val isSel = activeCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF4A89FF).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, if (isSel) Color(0xFF4A89FF) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { activeCategoryFilter = cat }
                                    .tvFocusEffect(shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    cat,
                                    color = if (isSel) Color(0xFF4A89FF) else Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results list
                Box(modifier = Modifier.weight(1f)) {
                    if (matchedChannels.isEmpty() && matchedRadioStations.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Sin resultados coincidentes",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Intente otra búsqueda temática o cambie de filtro.",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (matchedChannels.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "CANALES DE TELEVISIÓN (${matchedChannels.size})",
                                        color = Color(0xFF4A89FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(bottom = 4.dp, top = 4.dp)
                                    )
                                }

                                items(matchedChannels) { chan ->
                                    val isFav = favoriteChannels.any { it.id == chan.id }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                                            .clickable {
                                                viewModel.selectChannel(chan)
                                                onDismiss()
                                            }
                                            .tvFocusEffect(shape = RoundedCornerShape(10.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = chan.logoUrl,
                                            contentDescription = chan.name,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black),
                                            contentScale = ContentScale.Fit
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "CH ${chan.number}",
                                                    color = Color(0xFF4A89FF),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = chan.name,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (isFav) {
                                                    Icon(
                                                        imageVector = Icons.Default.Favorite,
                                                        contentDescription = "Favorito",
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = chan.description,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.toggleChannelFavorite(chan.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorito",
                                                tint = if (isFav) Color.Red else Color.White.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Ver",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (matchedRadioStations.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "EMISORAS DE RADIO (${matchedRadioStations.size})",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                items(matchedRadioStations) { rad ->
                                    val isRadioFav = favoriteRadioStations.any { it.id == rad.id }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.03f))
                                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                                            .clickable {
                                                viewModel.selectRadioStation(rad)
                                                if (!viewModel.isRadioPlaying) {
                                                    viewModel.toggleRadioPlay()
                                                }
                                                viewModel.selectTab(AppTab.RADIO)
                                                onDismiss()
                                            }
                                            .tvFocusEffect(shape = RoundedCornerShape(10.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = rad.logoUrl,
                                            contentDescription = rad.name,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = rad.frequency,
                                                    color = Color(0xFF00E5FF),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = rad.name,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (isRadioFav) {
                                                    Icon(
                                                        imageVector = Icons.Default.Favorite,
                                                        contentDescription = "Favorito",
                                                        tint = Color.Red,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = rad.genre.uppercase(),
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 10.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = { viewModel.toggleRadioFavorite(rad.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isRadioFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorito",
                                                tint = if (isRadioFav) Color.Red else Color.White.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Escuchar",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
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

