import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target1 = """    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWideLayout = configuration.screenWidthDp >= 580"""
replacement1 = """    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWideLayout = configuration.screenWidthDp >= 580
    val context = androidx.compose.ui.platform.LocalContext.current
    val isTvDevice = remember(context) { com.example.ui.screens.isAndroidTvDevice(context) }"""

target2 = """                    when (tab) {
                        AppTab.HOME -> HomeScreen(viewModel = viewModel)
                        AppTab.WATCHLIST -> WatchlistScreen(viewModel = viewModel)"""
replacement2 = """                    when (tab) {
                        AppTab.HOME -> {
                            if (isTvDevice) {
                                com.example.ui.screens.HomeTvScreen(viewModel = viewModel)
                            } else {
                                HomeScreen(viewModel = viewModel)
                            }
                        }
                        AppTab.WATCHLIST -> WatchlistScreen(viewModel = viewModel)"""

if target1 in content and target2 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
