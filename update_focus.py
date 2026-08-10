import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

old_focus_logic = """    var focusedTab by remember { mutableStateOf<AppTab?>(null) }
    
    LaunchedEffect(focusedTab) {
        isMenuFocused = focusedTab != null
    }

    Column(
        modifier = Modifier
            .width(menuWidth)
            .fillMaxHeight()
            .background(Color(0xFF030406).copy(alpha = 0.6f))
            .padding(vertical = 24.dp)
            .focusGroup(),"""

new_focus_logic = """    var focusedTab by remember { mutableStateOf<AppTab?>(null) }

    Column(
        modifier = Modifier
            .width(menuWidth)
            .fillMaxHeight()
            .background(Color(0xFF030406).copy(alpha = 0.6f))
            .onFocusChanged { isMenuFocused = it.hasFocus }
            .padding(vertical = 24.dp)
            .focusGroup(),"""

if old_focus_logic in content:
    content = content.replace(old_focus_logic, new_focus_logic)
else:
    print("Could not find old focus logic")

with open(file_path, "w") as f:
    f.write(content)

print("Done")
