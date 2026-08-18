import sys

file_path = "app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt"
with open(file_path, "r") as f:
    content = f.read()

start_idx = content.find("                    // 4. Primary Play Button")
if start_idx == -1:
    print("Start not found")
    sys.exit(1)

# we want to replace from start_idx to the end.
# Actually, to make it safer, let's just replace from start_idx to the end of the file.
new_tail = """                    // 4. Primary Play Button & Carousel Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Original Rectangular Play Button
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
                    }
                }
            }
        }
        
        // Floating Play Button on the right (decorative, non-focusable)
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
        }
    }
}
"""

new_content = content[:start_idx] + new_tail
with open(file_path, "w") as f:
    f.write(new_content)
print("Success")

