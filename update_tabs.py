import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                        if (isWideLayout) {
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
                        }"""

replacement = """                        if (isWideLayout) {
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
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(tabBgColor)
                                            .clickable { viewModel.selectTab(tab) }
                                            .tvFocusEffect(
                                                shape = RoundedCornerShape(12.dp),
                                                focusedBorderColor = Color.Transparent,
                                                unfocusedBorderColor = Color.Transparent,
                                                scaleAmount = 1.05f
                                            )
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                                                modifier = Modifier.size(18.dp.responsive())
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = displayLabel,
                                                color = contentColor,
                                                fontSize = 14.sp.responsive(),
                                                fontWeight = if (isSelected || isTabFocused) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                    
                                    // Subtle indicator for selected tab
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(3.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF00E5FF) else Color.Transparent)
                                    )
                                }
                            }
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Updated tabs successfully.")
else:
    print("Target string not found in LuminaAppShell.kt")
