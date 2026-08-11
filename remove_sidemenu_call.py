import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """            if (isWideLayout) {
                TvSideMenu(
                    currentTab = viewModel.currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    contentFocusRequester = contentFocusRequester
                )
            }"""

if target in content:
    content = content.replace(target, "")
    print("Removed TvSideMenu call")
else:
    print("Could not find TvSideMenu call")

with open(file_path, "w") as f:
    f.write(content)
