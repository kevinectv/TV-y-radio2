import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add imports
imports_target = "import androidx.compose.ui.focus.focusRequester"
imports_replacement = "import androidx.compose.ui.focus.focusRequester\nimport androidx.compose.ui.focus.focusProperties\nimport androidx.compose.ui.focus.focusGroup"
if imports_target in content:
    content = content.replace(imports_target, imports_replacement)
else:
    print("Could not find imports target")

# Add FocusRequester
fr_target = "    val isTvDevice = remember(context) { com.example.ui.screens.isAndroidTvDevice(context) }"
fr_replacement = "    val isTvDevice = remember(context) { com.example.ui.screens.isAndroidTvDevice(context) }\n    val contentFocusRequester = remember { FocusRequester() }"
if fr_target in content:
    content = content.replace(fr_target, fr_replacement)
else:
    print("Could not find fr target")

# Modify Row
row_target = """                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding("""
row_replacement = """                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusProperties {
                            if (viewModel.currentTab == AppTab.HOME) {
                                down = contentFocusRequester
                            }
                        }
                        .focusGroup()
                        .padding("""
if row_target in content:
    content = content.replace(row_target, row_replacement)
else:
    print("Could not find row target")

# Modify HomeTvScreen call
home_target = """                        AppTab.HOME -> {
                            if (isTvDevice) {
                                com.example.ui.screens.HomeTvScreen(viewModel = viewModel)
                            }"""
home_replacement = """                        AppTab.HOME -> {
                            if (isTvDevice) {
                                Box(
                                    modifier = Modifier
                                        .focusRequester(contentFocusRequester)
                                        .focusGroup()
                                ) {
                                    com.example.ui.screens.HomeTvScreen(viewModel = viewModel)
                                }
                            }"""
if home_target in content:
    content = content.replace(home_target, home_replacement)
else:
    print("Could not find home target")

with open(file_path, "w") as f:
    f.write(content)
print("Done")
