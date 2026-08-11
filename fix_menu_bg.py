import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """    Column(
        modifier = Modifier
            .width(menuWidth)
            .fillMaxHeight()
            .background(Color(0xFF030406).copy(alpha = 0.6f))
            .padding(vertical = 24.dp)
            .focusGroup(),"""

replacement = """    val menuBackground = remember(isMenuFocused) {
        if (isMenuFocused) {
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF030406).copy(alpha = 0.95f),
                    Color(0xFF030406).copy(alpha = 0.85f),
                    Color(0xFF030406).copy(alpha = 0.0f)
                )
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF030406).copy(alpha = 0.6f),
                    Color(0xFF030406).copy(alpha = 0.6f)
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .width(menuWidth)
            .fillMaxHeight()
            .background(brush = menuBackground)
            .padding(vertical = 24.dp)
            .focusGroup(),"""

if target in content:
    content = content.replace(target, replacement)
    print("Replaced background")
else:
    print("Could not find target block")

with open(file_path, "w") as f:
    f.write(content)

