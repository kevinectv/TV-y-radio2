package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.ElegantBackground
import com.example.ui.components.tvFocusEffect
import com.example.ui.components.CharacterAvatar
import com.example.ui.screens.ProfileSelectionScreen
import com.example.ui.screens.*
import kotlinx.coroutines.delay
import java.util.*

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

    // Toggle Search Field
    var searchExpanded by remember { mutableStateOf(false) }

    // Dynamic background matching the current channel or radio selection color hex
    val backgroundAccent = remember(viewModel.currentTab, viewModel.selectedRadioStation) {
        if (viewModel.currentTab == AppTab.RADIO) {
            viewModel.selectedRadioStation.themeColorHex
        } else {
            null
        }
    }

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
                                CharacterAvatar(
                                    style = profile.avatarStyle,
                                    skinColorHex = profile.avatarSkinColor,
                                    hairColorHex = profile.avatarHairColor,
                                    accessory = profile.avatarAccessory,
                                    expression = profile.avatarExpression,
                                    profileColorHex = profile.profileColor,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // App Title only on mobile to look elegant
                        if (!isWideLayout) {
                            Text(
                                text = "LUMINA",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        // Search expander icon + input box
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { searchExpanded = !searchExpanded },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (searchExpanded) Color(0xFF4A89FF).copy(alpha = 0.15f) else Color.Transparent,
                                        CircleShape
                                    )
                                    .tvFocusEffect(shape = CircleShape)
                                    .semantics { testTag = "search_button" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Trigger",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = searchExpanded,
                                enter = expandHorizontally() + fadeIn(),
                                exit = shrinkHorizontally() + fadeOut()
                            ) {
                                OutlinedTextField(
                                    value = viewModel.searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    placeholder = { Text("Buscar...", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f)) },
                                    modifier = Modifier
                                        .width(if (isWideLayout) 140.dp else 100.dp)
                                        .height(34.dp)
                                        .semantics { testTag = "search_input" },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF4A89FF),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedContainerColor = Color.Black.copy(alpha = 0.6f),
                                        unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    singleLine = true
                                )
                            }
                        }
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
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
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
                                                AppTab.TV -> Icons.Filled.Tv
                                                AppTab.RADIO -> Icons.Filled.Radio
                                                AppTab.SETTINGS -> Icons.Filled.Settings
                                            },
                                            contentDescription = tab.label,
                                            tint = if (isTabFocused) Color.Black else if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Text(
                                            text = tab.label,
                                            color = if (isTabFocused) Color.Black else if (isSelected) Color.White else Color.White.copy(alpha = tabAlpha),
                                            fontSize = 11.sp,
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
                                            AppTab.TV -> Icons.Filled.Tv
                                            AppTab.RADIO -> Icons.Filled.Radio
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
                    .padding(innerPadding)
            ) { tab ->
                when (tab) {
                    AppTab.HOME -> HomeScreen(viewModel = viewModel)
                    AppTab.WATCHLIST -> WatchlistScreen(viewModel = viewModel)
                    AppTab.TV -> TvScreen(viewModel = viewModel)
                    AppTab.RADIO -> RadioScreen(viewModel = viewModel)
                    AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
