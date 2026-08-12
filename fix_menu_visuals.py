import sys
import re

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """            topBar = {
                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF030406).copy(alpha = 0.85f),
                                    Color(0xFF030406).copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(
                            start = if (isWideLayout) 32.dp else 12.dp,
                            end = if (isWideLayout) 32.dp else 16.dp,
                            top = if (isWideLayout) 24.dp else 10.dp,
                            bottom = if (isWideLayout) 24.dp else 10.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Node + Central Node (Logo and Tabs)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Logo
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                append("LUMIN")
                                pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF00E5FF)))
                                append("A")
                                pop()
                            },
                            color = Color.White,
                            fontSize = 16.sp.responsive(),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )

                        // If wide layout, display tabs here (Central Node)
                        if (isWideLayout) {
                            val tabs = AppTab.values().filter { it != AppTab.SETTINGS && it != AppTab.SEARCH }
                            tabs.forEach { tab ->
                                val isSelected = viewModel.currentTab == tab
                                var isTabFocused by remember { mutableStateOf(false) }
                                
                                val displayLabel = when (tab) {
                                    AppTab.HOME -> "Inicio"
                                    AppTab.WATCHLIST -> "Mi lista"
                                    AppTab.TV -> "IPTV"
                                    AppTab.RADIO -> "Radio"
                                    else -> tab.label
                                }
                                
                                val tabIcon = when (tab) {
                                    AppTab.HOME -> Icons.Filled.Home
                                    AppTab.WATCHLIST -> Icons.Filled.Favorite
                                    AppTab.TV -> Icons.Filled.LiveTv
                                    AppTab.RADIO -> Icons.Filled.Radio
                                    else -> Icons.Filled.Home
                                }

                                val tabBgColor by animateColorAsState(
                                    targetValue = when {
                                        isTabFocused -> Color.White.copy(alpha = 0.15f)
                                        isSelected -> Color.White.copy(alpha = 0.08f)
                                        else -> Color.Transparent
                                    },
                                    animationSpec = tween(durationMillis = 200),
                                    label = "tab_bg"
                                )
                                
                                val contentColor by animateColorAsState(
                                    targetValue = when {
                                        isTabFocused || isSelected -> Color.White
                                        else -> Color.White.copy(alpha = 0.65f)
                                    },
                                    animationSpec = tween(durationMillis = 200),
                                    label = "tab_content"
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .onFocusChanged { isTabFocused = it.isFocused || it.hasFocus }
                                            .focusProperties {
                                                if (tab == AppTab.HOME) {
                                                    down = contentFocusRequester
                                                }
                                            }
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(tabBgColor)
                                            .clickable { viewModel.selectTab(tab) }
                                            .tvFocusEffect(
                                                shape = RoundedCornerShape(16.dp),
                                                focusedBorderColor = if (isTabFocused) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                borderWidth = 1.dp,
                                                scaleAmount = 1.05f
                                            )
                                            .padding(horizontal = 18.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = tabIcon,
                                                contentDescription = displayLabel,
                                                tint = contentColor,
                                                modifier = Modifier.size(16.dp.responsive())
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = displayLabel,
                                                color = contentColor,
                                                fontSize = 13.sp.responsive(),
                                                fontWeight = if (isSelected || isTabFocused) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                    
                                    // Subtle indicator for selected tab
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(3.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) {
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color(0xFF00E5FF).copy(alpha = 0.8f),
                                                            Color(0xFF00E5FF)
                                                        )
                                                    )
                                                } else {
                                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                                }
                                            )
                                            .shadow(if (isSelected) 4.dp else 0.dp, CircleShape, ambientColor = Color(0xFF00E5FF), spotColor = Color(0xFF00E5FF))
                                    )
                                }
                            }
                        }"""

replacement = """            topBar = {
                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 1. Sombra muy discreta y suave como requested
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF030406).copy(alpha = 0.90f),
                                    Color(0xFF030406).copy(alpha = 0.65f),
                                    Color(0xFF030406).copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(
                            start = if (isWideLayout) 32.dp else 12.dp,
                            end = if (isWideLayout) 32.dp else 16.dp,
                            top = if (isWideLayout) 24.dp else 10.dp,
                            bottom = if (isWideLayout) 24.dp else 10.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Node + Central Node (Logo and Tabs)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp) // Reducido para que el menú sea más compacto
                    ) {
                        // Logo
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                append("LUMIN")
                                pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF00E5FF)))
                                append("A")
                                pop()
                            },
                            color = Color.White,
                            fontSize = 16.sp.responsive(),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(end = 20.dp) // Padding mantenido para separar el Logo de las opciones
                        )

                        // If wide layout, display tabs here (Central Node)
                        if (isWideLayout) {
                            val tabs = AppTab.values().filter { it != AppTab.SETTINGS && it != AppTab.SEARCH }
                            tabs.forEach { tab ->
                                val isSelected = viewModel.currentTab == tab
                                var isTabFocused by remember { mutableStateOf(false) }
                                
                                val displayLabel = when (tab) {
                                    AppTab.HOME -> "Inicio"
                                    AppTab.WATCHLIST -> "Mi lista"
                                    AppTab.TV -> "IPTV"
                                    AppTab.RADIO -> "Radio"
                                    else -> tab.label
                                }
                                
                                val tabIcon = when (tab) {
                                    AppTab.HOME -> Icons.Filled.Home
                                    AppTab.WATCHLIST -> Icons.Filled.Favorite
                                    AppTab.TV -> Icons.Filled.LiveTv
                                    AppTab.RADIO -> Icons.Filled.Radio
                                    else -> Icons.Filled.Home
                                }

                                val tabBgColor by animateColorAsState(
                                    targetValue = when {
                                        isTabFocused -> Color.White.copy(alpha = 0.15f)
                                        isSelected -> Color.White.copy(alpha = 0.12f) // Un poco más visible
                                        else -> Color.Transparent
                                    },
                                    animationSpec = tween(durationMillis = 200),
                                    label = "tab_bg"
                                )
                                
                                val contentColor by animateColorAsState(
                                    targetValue = when {
                                        isTabFocused || isSelected -> Color.White
                                        else -> Color.White.copy(alpha = 0.65f)
                                    },
                                    animationSpec = tween(durationMillis = 200),
                                    label = "tab_content"
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .onFocusChanged { isTabFocused = it.isFocused || it.hasFocus }
                                            .focusProperties {
                                                if (tab == AppTab.HOME) {
                                                    down = contentFocusRequester
                                                }
                                            }
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(tabBgColor)
                                            .clickable { viewModel.selectTab(tab) }
                                            .tvFocusEffect(
                                                shape = RoundedCornerShape(16.dp),
                                                focusedBorderColor = if (isTabFocused) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                borderWidth = 1.dp,
                                                scaleAmount = 1.05f
                                            )
                                            .padding(horizontal = 14.dp, vertical = 8.dp), // Reducido horizontalmente
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = tabIcon,
                                                contentDescription = displayLabel,
                                                tint = contentColor,
                                                modifier = Modifier.size(15.dp.responsive())
                                            )
                                            Spacer(modifier = Modifier.width(4.dp)) // Más compacto
                                            Text(
                                                text = displayLabel,
                                                color = contentColor,
                                                fontSize = 13.sp.responsive(),
                                                fontWeight = if (isSelected || isTabFocused) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                    
                                    // Subtle indicator for selected tab
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(3.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) {
                                                    Brush.horizontalGradient(
                                                        colors = listOf(
                                                            Color(0xFF00E5FF).copy(alpha = 0.8f),
                                                            Color(0xFF00E5FF)
                                                        )
                                                    )
                                                } else {
                                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                                }
                                            )
                                            .shadow(if (isSelected) 4.dp else 0.dp, CircleShape, ambientColor = Color(0xFF00E5FF), spotColor = Color(0xFF00E5FF))
                                    )
                                }
                            }
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Updated visual styling")
else:
    print("Could not find target")

