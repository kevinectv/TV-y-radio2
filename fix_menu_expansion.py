import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """    var isMenuFocused by remember { mutableStateOf(false) }
    val menuWidth by animateDpAsState(
        targetValue = if (isMenuFocused) 180.dp else 64.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "menu_width"
    )
    
    var focusedTab by remember { mutableStateOf<AppTab?>(null) }

    Column(
        modifier = Modifier
            .width(menuWidth)
            .fillMaxHeight()
            .background(Color(0xFF030406).copy(alpha = 0.6f))
            .onFocusChanged { isMenuFocused = it.hasFocus }
            .padding(vertical = 24.dp)
            .focusGroup(),"""

replacement = """    var isMenuFocused by remember { mutableStateOf(false) }
    var focusedTab by remember { mutableStateOf<AppTab?>(null) }

    LaunchedEffect(focusedTab) {
        if (focusedTab != null) {
            isMenuFocused = true
        } else {
            delay(150)
            isMenuFocused = false
        }
    }

    val menuWidth by animateDpAsState(
        targetValue = if (isMenuFocused) 180.dp else 64.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "menu_width"
    )

    Column(
        modifier = Modifier
            .width(menuWidth)
            .fillMaxHeight()
            .background(Color(0xFF030406).copy(alpha = 0.6f))
            .padding(vertical = 24.dp)
            .focusGroup(),"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Could not find target block")

