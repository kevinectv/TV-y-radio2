# 1. Fix LuminaAppShell.kt
with open("app/src/main/java/com/example/ui/LuminaAppShell.kt", "r") as f:
    code = f.read()

start_marker = "            topBar = {"
start_idx = code.find(start_marker)
assert start_idx != -1, "start_marker not found"

end_marker = "            bottomBar = {"
end_idx = code.find(end_marker)
assert end_idx != -1, "end_marker not found"

new_topbar_block = """            topBar = {
                if (!(isTvDevice && viewModel.currentTab == AppTab.HOME)) {
                    // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                    Row(
                        modifier = Modifier
                            .padding(start = if (isTvDevice) 68.dp else 0.dp)
                            .fillMaxWidth()
                            // 1. Sombra muy sutil dibujada detrás sin afectar el layout (sin Spacer ni height extra)
                            .drawBehind {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.15f),
                                            Color.Black.copy(alpha = 0.05f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = size.height + 120.dp.toPx()
                                    ),
                                    size = Size(size.width, size.height + 120.dp.toPx())
                                )
                            }
                            .padding(
                                start = if (isWideLayout) 24.dp else 12.dp,
                                end = if (isWideLayout) 32.dp else 16.dp,
                                top = if (isWideLayout) 24.dp else 10.dp,
                                bottom = if (isWideLayout) 20.dp else 10.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Node (Logo)
                        Text(
                            text = androidx.compose.ui.text.buildAnnotatedString {
                                append("LUMIN")
                                pushStyle(androidx.compose.ui.text.SpanStyle(color = Color(0xFF00E5FF)))
                                append("A")
                                pop()
                            },
                            color = Color.White,
                            fontSize = 18.sp.responsive(), // Slightly larger as requested
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        // Right Node: Live Clock ONLY
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 4. Digital Clock displaying 12-hour AM/PM format
                            Text(
                                text = timeString,
                                color = Color.White,
                                fontSize = if (isWideLayout) 13.sp.responsive() else 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            },
"""

code = code[:start_idx] + new_topbar_block + code[end_idx:]

with open("app/src/main/java/com/example/ui/LuminaAppShell.kt", "w") as f:
    f.write(code)
print("Fixed LuminaAppShell.kt")

# 2. Fix HomeHeroBannerTv.kt
with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "r") as f:
    tv_code = f.read()

# Fix package and imports at top
if "import com.example.ui.components.responsivepackage" in tv_code:
    tv_code = tv_code.replace("import com.example.ui.components.responsivepackage", "package")

if "package com.example.ui.screens" in tv_code:
    if "import com.example.ui.components.responsive" not in tv_code:
        tv_code = tv_code.replace("package com.example.ui.screens", "package com.example.ui.screens\n\nimport com.example.ui.components.responsive")

# Check if clock state & ticker are already in HomeHeroBannerTv
if "timeString" not in tv_code:
    old_vars = "    val playButtonFocusRequester = remember { FocusRequester() }"
    new_vars = """    val playButtonFocusRequester = remember { FocusRequester() }

    // Live Clock ticker for the Hero header
    var timeString by remember { mutableStateOf("12:00 PM") }
    LaunchedEffect(Unit) {
        while (true) {
            val calendar = java.util.Calendar.getInstance()
            val rawHour = calendar.get(java.util.Calendar.HOUR)
            val min = calendar.get(java.util.Calendar.MINUTE)
            val hour = if (rawHour == 0) 12 else rawHour
            val amPm = if (calendar.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM) "AM" else "PM"
            timeString = String.format("%d:%02d %s", hour, min, amPm)
            kotlinx.coroutines.delay(1000)
        }
    }"""
    assert old_vars in tv_code, "playButtonFocusRequester not found in HomeHeroBannerTv"
    tv_code = tv_code.replace(old_vars, new_vars, 1)

# Check if Top Header Row is already in HomeHeroBannerTv
if "Lumina Logo" not in tv_code:
    old_box_start = "    ) {\n        // Background Backdrop with crossfade (light overlay for vivid backdrop)"
    new_box_start = """    ) {
        // Top Header Row (Logo LUMINA + Clock) integrated into the top of the Hero banner scroll area
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isWideLayout = configuration.screenWidthDp >= 580
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isWideLayout) 24.dp else 12.dp,
                    end = if (isWideLayout) 32.dp else 16.dp,
                    top = if (isWideLayout) 24.dp else 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prepared Logo image using R.drawable.img_lumina_logo_user_v2 (fully prepared for user's logo file replacement)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_lumina_logo_user_v2),
                contentDescription = "Lumina Logo",
                modifier = Modifier
                    .height(32.dp)
                    .widthIn(max = 160.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.weight(1f))

            // Live Clock
            Text(
                text = timeString,
                color = Color.White,
                fontSize = if (isWideLayout) 13.sp.responsive() else 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Background Backdrop with crossfade (light overlay for vivid backdrop)"""
    assert old_box_start in tv_code, "old_box_start not found in HomeHeroBannerTv"
    tv_code = tv_code.replace(old_box_start, new_box_start, 1)

with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "w") as f:
    f.write(tv_code)
print("Fixed HomeHeroBannerTv.kt")
