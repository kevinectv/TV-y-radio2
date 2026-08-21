with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "r") as f:
    content = f.read()

target = """            modifier = Modifier.layout { measurable, constraints ->
                val shift = 68.dp.roundToPx()
                val newWidth = constraints.maxWidth + shift
                val placeable = measurable.measure(constraints.copy(
                    minWidth = newWidth,
                    maxWidth = newWidth
                ))
                layout(placeable.width, placeable.height) {
                    placeable.place(-shift, 0)
                }
            }"""

replacement = """            modifier = Modifier.layout { measurable, constraints ->
                val shift = 68.dp.roundToPx()
                // Extend generously to the right to cover any system insets, nav bars, or parent paddings
                val rightExtension = 120.dp.roundToPx() 
                val newWidth = constraints.maxWidth + shift + rightExtension
                val placeable = measurable.measure(constraints.copy(
                    minWidth = newWidth,
                    maxWidth = newWidth
                ))
                layout(placeable.width, placeable.height) {
                    placeable.place(-shift, 0)
                }
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "w") as f:
        f.write(content)
    print("Fixed layout modifier to extend to the right")
else:
    print("Target block not found in HomeHeroBannerTv.kt")
