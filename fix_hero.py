import sys

file_path = "app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Update logo sizes
target_logo_box = """                    // 1. Logo or Title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {"""
replacement_logo_box = """                    // 1. Logo or Title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {"""

target_logo_image = """                                modifier = Modifier
                                    .heightIn(max = 72.dp)
                                    .widthIn(max = 340.dp),"""
replacement_logo_image = """                                modifier = Modifier
                                    .heightIn(max = 86.dp)
                                    .widthIn(max = 400.dp),"""

target_logo_text_1 = """                                        style = TextStyle(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 32.sp,"""
replacement_logo_text_1 = """                                        style = TextStyle(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 38.sp,"""

target_logo_text_2 = """                                style = TextStyle(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 32.sp,"""
replacement_logo_text_2 = """                                style = TextStyle(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 38.sp,"""

# 2. Update Play Button and Indicators
target_button_indicators = """                    // 4. Primary Play Button
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .focusRequester(playButtonFocusRequester)
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.DirectionLeft -> {
                                            heroIndex = (heroIndex - 1 + featuredMovies.size) % featuredMovies.size
                                            autoRotateTrigger++
                                            true
                                        }
                                        Key.DirectionRight -> {
                                            heroIndex = (heroIndex + 1) % featuredMovies.size
                                            autoRotateTrigger++
                                            true
                                        }
                                        else -> false
                                    }
                                } else {
                                    false
                                }
                            }
                            .focusable()
                            .clickable { onTrailerClick(targetMovie) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Reproducir",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Reproducir",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    // 5. Generous spacing before indicators (strictly below button, never above/touching)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 6. Carousel Indicators
                    val currentIndex = featuredMovies.indexOfFirst { it.id == currentMovie.id }.coerceAtLeast(0)
                    if (featuredMovies.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                featuredMovies.forEachIndexed { index, _ ->
                                    val isActive = index == currentIndex
                                    Box(
                                        modifier = Modifier
                                            .height(6.dp)
                                            .width(if (isActive) 24.dp else 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                color = if (isActive) Color.White else Color.White.copy(alpha = 0.35f),
                                                shape = RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }"""

replacement_button_indicators = """                    // 4. Primary Play Button & Carousel Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        Surface(
                            color = Color.White,
                            shape = CircleShape,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .size(56.dp)
                                .focusRequester(playButtonFocusRequester)
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.key) {
                                            Key.DirectionLeft -> {
                                                heroIndex = (heroIndex - 1 + featuredMovies.size) % featuredMovies.size
                                                autoRotateTrigger++
                                                true
                                            }
                                            Key.DirectionRight -> {
                                                heroIndex = (heroIndex + 1) % featuredMovies.size
                                                autoRotateTrigger++
                                                true
                                            }
                                            else -> false
                                        }
                                    } else {
                                        false
                                    }
                                }
                                .focusable()
                                .clickable { onTrailerClick(targetMovie) }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Reproducir",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        // 5. Carousel Indicators
                        val currentIndex = featuredMovies.indexOfFirst { it.id == currentMovie.id }.coerceAtLeast(0)
                        if (featuredMovies.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                featuredMovies.forEachIndexed { index, _ ->
                                    val isActive = index == currentIndex
                                    Box(
                                        modifier = Modifier
                                            .height(6.dp)
                                            .width(if (isActive) 24.dp else 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                color = if (isActive) Color.White else Color.White.copy(alpha = 0.35f),
                                                shape = RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }"""

target_floating_button = """        // Floating Play Button on the right (decorative, non-focusable)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 80.dp)
                .focusable(false)
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(68.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }"""

replacement_floating_button = ""

content = content.replace(target_logo_box, replacement_logo_box)
content = content.replace(target_logo_image, replacement_logo_image)
content = content.replace(target_logo_text_1, replacement_logo_text_1)
content = content.replace(target_logo_text_2, replacement_logo_text_2)
content = content.replace(target_button_indicators, replacement_button_indicators)
content = content.replace(target_floating_button, replacement_floating_button)

with open(file_path, "w") as f:
    f.write(content)

print("Replaced successfully.")
