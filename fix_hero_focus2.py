import sys
import re

file_path = "app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt"
with open(file_path, "r") as f:
    content = f.read()

outer_box_pattern = r"    val effectiveHeight = bannerHeight \+ 220\.dp\s+Box\(\s+modifier = Modifier\s+\.fillMaxWidth\(\)\s+\.height\(effectiveHeight\)\s+\) \{"

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

if re.search(outer_box_pattern, content):
    content = re.sub(outer_box_pattern, outer_box_new, content)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success replacing Box")
else:
    print("Outer Box not found")
