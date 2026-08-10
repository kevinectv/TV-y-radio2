import sys

file_path = "app/src/main/java/com/example/ui/screens/HomeTvScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Precalculate homeCatalogs and firstNonEmptyCatalogIndex before the Box
target1 = """    val progressItems = remember(seenProgress, catalogs) {"""
replacement1 = """    val homeCatalogs = remember(catalogs) { catalogs.filter { it.isVisible && it.showInHome } }
    val firstNonEmptyCatalogIndex = remember(homeCatalogs) { homeCatalogs.indexOfFirst { it.items.isNotEmpty() } }
    
    val progressItems = remember(seenProgress, catalogs) {"""

if target1 in content:
    content = content.replace(target1, replacement1)
else:
    print("Could not find target1")

# 2. Conditionally set down focus in hero banner
target2 = """                                .focusProperties {
                                    down = firstRowFocusRequester
                                }"""
replacement2 = """                                .focusProperties {
                                    if (progressItems.isNotEmpty() || firstNonEmptyCatalogIndex != -1) {
                                        down = firstRowFocusRequester
                                    }
                                }"""

if target2 in content:
    content = content.replace(target2, replacement2)
else:
    print("Could not find target2")

# 3. Use precalculated variables in the LazyColumn
target3 = """                    val homeCatalogs = catalogs.filter { it.isVisible && it.showInHome }
                    val firstNonEmptyCatalogIndex = homeCatalogs.indexOfFirst { it.items.isNotEmpty() }
                    homeCatalogs.forEachIndexed { index, catalog ->"""
replacement3 = """                    homeCatalogs.forEachIndexed { index, catalog ->"""

if target3 in content:
    content = content.replace(target3, replacement3)
else:
    print("Could not find target3")

with open(file_path, "w") as f:
    f.write(content)
print("Success")
