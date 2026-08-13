import sys
import re

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target1 = """                            val tabs = AppTab.values().filter { it != AppTab.SETTINGS && it != AppTab.SEARCH }
                            tabs.forEach { tab ->
                                val isSelected = viewModel.currentTab == tab
                                var isTabFocused by remember { mutableStateOf(false) }
                                
                                val displayLabel = when (tab) {
                                    AppTab.HOME -> "Inicio"
                                    AppTab.WATCHLIST -> "Mi lista"
                                    AppTab.TV -> "IPTV"
                                    AppTab.RADIO -> "Radio"
                                    else -> tab.label
                                }
                                
                                val tabIcon = when (tab) {
                                    AppTab.HOME -> Icons.Filled.Home
                                    AppTab.WATCHLIST -> Icons.Filled.Favorite
                                    AppTab.TV -> Icons.Filled.LiveTv
                                    AppTab.RADIO -> Icons.Filled.Radio
                                    else -> Icons.Filled.Home
                                }"""

replacement1 = """                            val tabs = AppTab.values().filter { it != AppTab.SETTINGS && it != AppTab.SEARCH && it != AppTab.RADIO }
                            tabs.forEach { tab ->
                                val isSelected = viewModel.currentTab == tab
                                var isTabFocused by remember { mutableStateOf(false) }
                                
                                val displayLabel = when (tab) {
                                    AppTab.HOME -> "Inicio"
                                    AppTab.WATCHLIST -> "Mi lista"
                                    AppTab.MOVIES -> "Películas"
                                    AppTab.SERIES -> "Series"
                                    AppTab.TV -> "IPTV"
                                    else -> tab.label
                                }
                                
                                val tabIcon = when (tab) {
                                    AppTab.HOME -> Icons.Filled.Home
                                    AppTab.WATCHLIST -> Icons.Filled.Favorite
                                    AppTab.MOVIES -> Icons.Filled.Movie
                                    AppTab.SERIES -> Icons.Filled.Tv
                                    AppTab.TV -> Icons.Filled.LiveTv
                                    else -> Icons.Filled.Home
                                }"""

target2 = """                        AppTab.values().forEach { tab ->
                            val isSelected = viewModel.currentTab == tab"""

replacement2 = """                        AppTab.values().filter { it != AppTab.RADIO }.forEach { tab ->
                            val isSelected = viewModel.currentTab == tab"""

target3 = """                                        imageVector = when (tab) {
                                            AppTab.HOME -> Icons.Filled.Home
                                            AppTab.WATCHLIST -> Icons.Filled.Favorite
                                            AppTab.TV -> Icons.Filled.LiveTv
                                            AppTab.RADIO -> Icons.Filled.Radio
                                            AppTab.SEARCH -> Icons.Filled.Search
                                            AppTab.SETTINGS -> Icons.Filled.Settings
                                        },"""

replacement3 = """                                        imageVector = when (tab) {
                                            AppTab.HOME -> Icons.Filled.Home
                                            AppTab.WATCHLIST -> Icons.Filled.Favorite
                                            AppTab.MOVIES -> Icons.Filled.Movie
                                            AppTab.SERIES -> Icons.Filled.Tv
                                            AppTab.TV -> Icons.Filled.LiveTv
                                            AppTab.RADIO -> Icons.Filled.Radio
                                            AppTab.SEARCH -> Icons.Filled.Search
                                            AppTab.SETTINGS -> Icons.Filled.Settings
                                        },"""

target4 = """                                    val labelStr = when (tab) {
                                        AppTab.HOME -> "Home"
                                        AppTab.WATCHLIST -> "Favoritos"
                                        AppTab.TV -> "TV"
                                        AppTab.RADIO -> "Radio"
                                        AppTab.SEARCH -> "Buscar"
                                        AppTab.SETTINGS -> "Ajustes"
                                    }"""

replacement4 = """                                    val labelStr = when (tab) {
                                        AppTab.HOME -> "Inicio"
                                        AppTab.WATCHLIST -> "Favoritos"
                                        AppTab.MOVIES -> "Películas"
                                        AppTab.SERIES -> "Series"
                                        AppTab.TV -> "IPTV"
                                        AppTab.RADIO -> "Radio"
                                        AppTab.SEARCH -> "Buscar"
                                        AppTab.SETTINGS -> "Ajustes"
                                    }"""

target5 = """                        AppTab.WATCHLIST -> WatchlistScreen(viewModel = viewModel)
                        AppTab.TV -> TvScreen(viewModel = viewModel)
                        AppTab.RADIO -> RadioScreen(viewModel = viewModel)
                        AppTab.SEARCH -> SearchScreen(viewModel = viewModel)
                        AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)"""

replacement5 = """                        AppTab.WATCHLIST -> WatchlistScreen(viewModel = viewModel)
                        AppTab.MOVIES -> MoviesScreen(viewModel = viewModel)
                        AppTab.SERIES -> SeriesScreen(viewModel = viewModel)
                        AppTab.TV -> TvScreen(viewModel = viewModel)
                        AppTab.RADIO -> RadioScreen(viewModel = viewModel)
                        AppTab.SEARCH -> SearchScreen(viewModel = viewModel)
                        AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)"""

if target1 in content:
    content = content.replace(target1, replacement1)
    content = content.replace(target2, replacement2)
    content = content.replace(target3, replacement3)
    content = content.replace(target4, replacement4)
    content = content.replace(target5, replacement5)
    with open(file_path, "w") as f:
        f.write(content)
    print("Replaced all targets successfully.")
else:
    print("Target1 not found.")

