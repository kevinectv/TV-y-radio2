with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "r") as f:
    content = f.read()

# 1. Add import if not present
if "import com.example.ui.components.responsive" not in content:
    content = "import com.example.ui.components.responsive\n" + content

# 2. Add clock state and LaunchedEffect after `val playButtonFocusRequester = remember { FocusRequester() }`
old_vars = """    val playButtonFocusRequester = remember { FocusRequester() }"""
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

assert old_vars in content, "old_vars not found"
content = content.replace(old_vars, new_vars, 1)

# 3. Add Top Header Row right inside the root Box after the background Crossfade.
# Let's find where the background Crossfade ends or where `// Background Backdrop` starts.
old_box_start = """    ) {
        // Background Backdrop with crossfade (light overlay for vivid backdrop)"""

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

assert old_box_start in content, "old_box_start not found"
content = content.replace(old_box_start, new_box_start, 1)

with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "w") as f:
    f.write(content)
print("Successfully updated HomeHeroBannerTv.kt")
