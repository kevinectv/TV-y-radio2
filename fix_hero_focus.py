import sys

file_path = "app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Modify the outer Box
outer_box_old = """    val effectiveHeight = bannerHeight + 220.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(effectiveHeight)
    ) {"""

outer_box_new = """    val effectiveHeight = bannerHeight + 220.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(effectiveHeight)
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
            .clickable { onDetailsClick(currentMovie) }
    ) {"""

# 2. Modify the Surface
surface_old = """                        // Original Rectangular Play Button
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

surface_new = """                        // Original Rectangular Play Button
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { onTrailerClick(targetMovie) }
                        ) {"""

if outer_box_old in content:
    content = content.replace(outer_box_old, outer_box_new)
else:
    print("Outer Box not found")

if surface_old in content:
    content = content.replace(surface_old, surface_new)
else:
    print("Surface not found")

with open(file_path, "w") as f:
    f.write(content)
print("Success")

