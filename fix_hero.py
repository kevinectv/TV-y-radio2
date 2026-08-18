import sys

file_path = "app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                    // 4. Primary Play Button
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

replacement = """                    // 4. Primary Play Button (Restored Circular) & Carousel Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Circular Play Button Restored
                        Surface(
                            color = Color.White,
                            shape = CircleShape,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .size(68.dp)
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
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                        
                        // 5. Carousel Indicators placed next to the Play Button
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

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Replaced successfully.")
else:
    print("Target not found.")

