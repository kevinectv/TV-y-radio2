import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. Remove focusProperties and focusGroup from the Row
row_target = """                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusProperties {
                            if (viewModel.currentTab == AppTab.HOME) {
                                down = contentFocusRequester
                            }
                        }
                        .focusGroup()
                        .padding("""
row_replacement = """                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding("""
if row_target in content:
    content = content.replace(row_target, row_replacement)
else:
    print("Could not find row target")

# 2. Add focusProperties to the Box for tabs
box_target = """                                Box(
                                    modifier = Modifier
                                        .scale(tabScale)
                                        .onFocusChanged { isTabFocused = it.isFocused || it.hasFocus }
                                        .clip(RoundedCornerShape(10.dp))"""
box_replacement = """                                Box(
                                    modifier = Modifier
                                        .scale(tabScale)
                                        .onFocusChanged { isTabFocused = it.isFocused || it.hasFocus }
                                        .focusProperties {
                                            if (tab == AppTab.HOME) {
                                                down = contentFocusRequester
                                            }
                                        }
                                        .clip(RoundedCornerShape(10.dp))"""
if box_target in content:
    content = content.replace(box_target, box_replacement)
else:
    print("Could not find box target")

with open(file_path, "w") as f:
    f.write(content)
print("Done")
