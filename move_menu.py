import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

old_row = """        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideLayout) {
                TvSideMenu(
                    currentTab = viewModel.currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    contentFocusRequester = contentFocusRequester
                )
            }

        // Main structural Scaffold to support safe edges
        Scaffold(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding(),"""

new_box = """        Box(modifier = Modifier.fillMaxSize()) {
        // Main structural Scaffold to support safe edges
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = if (isWideLayout) 64.dp else 0.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),"""

if old_row in content:
    content = content.replace(old_row, new_box)
    print("Replaced old_row")
else:
    print("Could not find old_row")
    sys.exit(1)

# Now, insert TvSideMenu before the last closing brace of LuminaAppShell
idx = content.find("@Composable\nfun TvSideMenu")
if idx == -1:
    print("Could not find TvSideMenu")
    sys.exit(1)

# Backtrack to find the last `}` before idx
last_brace_idx = content.rfind("}", 0, idx)
if last_brace_idx == -1:
    print("Could not find last brace")
    sys.exit(1)

insert_content = """
            if (isWideLayout) {
                TvSideMenu(
                    currentTab = viewModel.currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    contentFocusRequester = contentFocusRequester
                )
            }
"""
content = content[:last_brace_idx] + insert_content + content[last_brace_idx:]

with open(file_path, "w") as f:
    f.write(content)
print("Done!")
