import sys

file_path = "app/src/main/java/com/example/ui/screens/HomeTvScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """                    val homeCatalogs = catalogs.filter { it.isVisible && it.showInHome }
                    homeCatalogs.forEachIndexed { index, catalog ->
                        if (catalog.items.isNotEmpty()) {
                            item(key = "catalog_${catalog.name}") {
                                val isFirstRow = progressItems.isEmpty() && index == 0"""

replacement = """                    val homeCatalogs = catalogs.filter { it.isVisible && it.showInHome }
                    val firstNonEmptyCatalogIndex = homeCatalogs.indexOfFirst { it.items.isNotEmpty() }
                    homeCatalogs.forEachIndexed { index, catalog ->
                        if (catalog.items.isNotEmpty()) {
                            item(key = "catalog_${catalog.name}") {
                                val isFirstRow = progressItems.isEmpty() && index == firstNonEmptyCatalogIndex"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
