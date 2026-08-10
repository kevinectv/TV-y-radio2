import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Add TvSideMenu composable at the bottom
tv_side_menu_code = """
@Composable
fun TvSideMenu(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    contentFocusRequester: FocusRequester
) {
    var isMenuFocused by remember { mutableStateOf(false) }
    val menuWidth by animateDpAsState(
        targetValue = if (isMenuFocused) 180.dp else 64.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "menu_width"
    )
    
    var focusedTab by remember { mutableStateOf<AppTab?>(null) }
    
    LaunchedEffect(focusedTab) {
        isMenuFocused = focusedTab != null
    }

    Column(
        modifier = Modifier
            .width(menuWidth)
            .fillMaxHeight()
            .background(Color(0xFF030406).copy(alpha = 0.6f))
            .padding(vertical = 24.dp)
            .focusGroup(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isMenuFocused) "LUMINA" else "L",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontSize = if (isMenuFocused) 16.sp.responsive() else 18.sp.responsive()
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val tabs = AppTab.values().filter { it != AppTab.SETTINGS && it != AppTab.SEARCH }
        tabs.forEach { tab ->
            val isSelected = currentTab == tab
            val isTabFocused = focusedTab == tab
            
            val displayLabel = when (tab) {
                AppTab.HOME -> "Inicio"
                AppTab.WATCHLIST -> "Mi lista"
                AppTab.TV -> "IPTV"
                AppTab.RADIO -> "Radio"
                else -> tab.label
            }
            
            val icon = when (tab) {
                AppTab.HOME -> Icons.Filled.Home
                AppTab.WATCHLIST -> Icons.Filled.Favorite
                AppTab.RADIO -> Icons.Filled.Radio
                AppTab.TV -> Icons.Filled.LiveTv
                else -> Icons.Filled.Star
            }
            
            val tabBgColor by animateColorAsState(
                targetValue = when {
                    isTabFocused && isSelected -> Color(0xFF151833).copy(alpha = 0.9f)
                    isTabFocused -> Color.White.copy(alpha = 0.12f)
                    isSelected -> Color(0xFF0D0B21).copy(alpha = 0.75f)
                    else -> Color.Transparent
                },
                animationSpec = tween(durationMillis = 200),
                label = "tab_bg"
            )
            
            val contentColor by animateColorAsState(
                targetValue = if (isTabFocused || isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                animationSpec = tween(durationMillis = 200),
                label = "tab_content"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isMenuFocused) Arrangement.Start else Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .onFocusChanged { state ->
                        if (state.isFocused || state.hasFocus) {
                            focusedTab = tab
                        } else if (focusedTab == tab) {
                            focusedTab = null
                        }
                    }
                    .focusProperties {
                        if (tab == AppTab.HOME) {
                            right = contentFocusRequester
                        }
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(tabBgColor)
                    .clickable { onTabSelected(tab) }
                    .tvFocusEffect(
                        shape = RoundedCornerShape(12.dp),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        scaleAmount = 1.05f
                    )
                    .padding(horizontal = if (isMenuFocused) 16.dp else 0.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = displayLabel,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = isMenuFocused,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Row {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = displayLabel,
                            color = contentColor,
                            fontSize = 13.sp.responsive(),
                            fontWeight = if (isSelected || isTabFocused) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
"""

if "fun TvSideMenu" not in content:
    content += "\n" + tv_side_menu_code

# 2. Modify the Scaffold wrapper
target_scaffold = """        // Main structural Scaffold to support safe edges
        Scaffold("""
replacement_scaffold = """        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideLayout) {
                TvSideMenu(
                    currentTab = viewModel.currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    contentFocusRequester = contentFocusRequester
                )
            }

        // Main structural Scaffold to support safe edges
        Scaffold(
            modifier = Modifier.weight(1f),"""

content = content.replace(target_scaffold, replacement_scaffold)

# 3. Update topBar layout
target_topbar = """            topBar = {
                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (isWideLayout) 24.dp else 12.dp,
                            end = if (isWideLayout) 32.dp else 16.dp,
                            top = if (isWideLayout) 16.dp else 10.dp,
                            bottom = if (isWideLayout) 16.dp else 10.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Node: Branded Title "LUMINA" with a beautiful custom electric-blue styled 'A'
                    Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            append("LUMIN")
                            pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF00E5FF)))
                            append("A")
                            pop()
                        },
                        color = Color.White,
                        fontSize = if (isWideLayout) 18.sp.responsive() else 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    // Central Node: Main Navigation Tabs Row - ONLY on Wide Screens
"""

replacement_topbar = """            topBar = {
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
"""

content = content.replace(target_topbar, replacement_topbar)

# 4. Remove Central Node
import re

central_node_start = content.find("if (isWideLayout) {", content.find("Central Node Removed for TV"))
central_node_end = content.find("// Right Node:", central_node_start)

# We need to accurately extract and remove the central node block.
# Since it's exactly between `if (isWideLayout) {` and `// Right Node:`, we can replace that chunk.

chunk_to_remove = content[central_node_start:central_node_end]
content = content.replace(chunk_to_remove, "")

# 5. Fix Scaffold closing brace
target_bottom_bar_end = """                }
            }
        ) { innerPadding ->"""
replacement_bottom_bar_end = """                }
            }
        ) { innerPadding ->"""

# We added a `Row` at the start, we need to close it.
target_end = """                .zIndex(100f)
        ) {
            viewModel.activeVideoUrl?.let { url ->
                // ... VideoPlayer ...
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            }
        }
    }
}"""
# Wait, looking at the end of the file is safer via a direct replace of the last bracket.
last_bracket = content.rfind("}")
if last_bracket != -1:
    content = content[:last_bracket] + "        }\n" + content[last_bracket:]


with open(file_path, "w") as f:
    f.write(content)

print("Done")
