import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target_start = """            topBar = {
                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (isWideLayout) 0.dp else 12.dp,
                            end = if (isWideLayout) 32.dp else 16.dp,
                            top = if (isWideLayout) 16.dp else 10.dp,
                            bottom = if (isWideLayout) 16.dp else 10.dp
                        ),
                    horizontalArrangement = if (isWideLayout) Arrangement.End else Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isWideLayout) {
                        // Left Node: Branded Title "LUMINA" with a beautiful custom electric-blue styled 'A'
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                append("LUMIN")
                                pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF00E5FF)))
                                append("A")
                                pop()
                            },
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }

                    // Central Node Removed for TV (now in TvSideMenu)
                    // Right Node: Live Clock, Search Icon, Profile Avatar, and optional Settings Button
                    Row("""

replacement_start = """            topBar = {
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

                                val tabBgColor by animateColorAsState(
                                    targetValue = when {
                                        isTabFocused && isSelected -> Color.White
                                        isTabFocused -> Color.White.copy(alpha = 0.15f)
                                        isSelected -> Color.White.copy(alpha = 0.1f)
                                        else -> Color.Transparent
                                    },
                                    animationSpec = tween(durationMillis = 200),
                                    label = "tab_bg"
                                )
                                
                                val contentColor by animateColorAsState(
                                    targetValue = when {
                                        isTabFocused && isSelected -> Color.Black
                                        isTabFocused || isSelected -> Color.White
                                        else -> Color.White.copy(alpha = 0.65f)
                                    },
                                    animationSpec = tween(durationMillis = 200),
                                    label = "tab_content"
                                )

                                Box(
                                    modifier = Modifier
                                        .onFocusChanged { isTabFocused = it.isFocused || it.hasFocus }
                                        .focusProperties {
                                            if (tab == AppTab.HOME) {
                                                down = contentFocusRequester
                                            }
                                        }
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(tabBgColor)
                                        .clickable { viewModel.selectTab(tab) }
                                        .tvFocusEffect(
                                            shape = RoundedCornerShape(20.dp),
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            scaleAmount = 1.05f
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = displayLabel,
                                        color = contentColor,
                                        fontSize = 14.sp.responsive(),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Right Node: Live Clock, Search Icon, Profile Avatar, and optional Settings Button
                    Row("""

if target_start in content:
    content = content.replace(target_start, replacement_start)
    print("Replaced topbar start")
else:
    print("Could not find topbar start")

with open(file_path, "w") as f:
    f.write(content)
