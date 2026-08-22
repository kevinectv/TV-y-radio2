with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "r") as f:
    text = f.read()

# Fix package/imports header
text = text.replace("import com.example.ui.components.responsivepackage com.example.ui.screens", "package com.example.ui.screens\n\nimport com.example.ui.components.responsive")

# Add clock state if not present
if "timeString" not in text:
    old_target = "    val playButtonFocusRequester = remember { FocusRequester() }"
    new_replacement = """    val playButtonFocusRequester = remember { FocusRequester() }

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
    assert old_target in text, "playButtonFocusRequester not found"
    text = text.replace(old_target, new_replacement, 1)

# Add Top Header Row inside root Box if not present
if "Lumina Logo" not in text:
    old_box = "    ) {\n        // Background Backdrop with crossfade (light overlay for vivid backdrop)"
    new_box = """    ) {
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
    assert old_box in text, "old_box not found"
    text = text.replace(old_box, new_box, 1)

with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "w") as f:
    f.write(text)
print("Successfully fixed HomeHeroBannerTv.kt completely")
