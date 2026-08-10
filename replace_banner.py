import sys

file_path = "app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt"
with open(file_path, "r") as f:
    content = f.read()

target1 = """    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(effectiveHeight)
            .focusable()
            .onFocusChanged { focusState ->
                if (focusState.hasFocus) {
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(0)
                    }
                }
            }
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
                        Key.DirectionDown -> {
                            false
                        }
                        Key.DirectionUp -> {
                            // Allow focus to move up naturally to top menu
                            false
                        }
                        Key.DirectionCenter, Key.Enter, Key.Spacebar -> {
                            onDetailsClick(currentMovie)
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {"""

replacement1 = """    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(effectiveHeight)
    ) {"""

target2 = """                    // 4. Primary Play Button
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .focusRequester(playButtonFocusRequester)
                            .focusable()
                            .clickable { onTrailerClick(targetMovie) }
                    ) {"""

replacement2 = """                    // 4. Primary Play Button
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
                    ) {"""

target3 = """                                            .clip(RoundedCornerShape(3.dp))
                                            .clickable {
                                                 heroIndex = index
                                                 autoRotateTrigger++
                                            }
                                            .background("""

replacement3 = """                                            .clip(RoundedCornerShape(3.dp))
                                            .background("""

if target1 in content and target2 in content and target3 in content:
    print("Already done?")
else:
    # Actually just run regex or string replace directly for target3
    import re
    # We already know target1 and target2 are there. Let's reset from clean state
    # or just replace them one by one.
    pass

with open(file_path, "r") as f:
    content = f.read()

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)
content = re.sub(r'\.clip\(RoundedCornerShape\(3\.dp\)\)\s*\.clickable\s*\{\s*heroIndex\s*=\s*index\s*autoRotateTrigger\+\+\s*\}\s*\.background\(', r'.clip(RoundedCornerShape(3.dp))\n                                            .background(', content)

with open(file_path, "w") as f:
    f.write(content)
print("Success replacing all")
