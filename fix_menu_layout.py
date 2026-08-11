import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target_logo = """        // Logo
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {"""

replacement_logo = """        // Logo
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(start = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {"""

target_tab = """            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isMenuFocused) Arrangement.Start else Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .onFocusChanged { state ->
                        if (state.isFocused || state.hasFocus) {
                            focusedTab = tab
                        } else if (focusedTab == tab) {
                            focusedTab = null
                        }
                    }
                    .focusProperties {
                        if (tab == AppTab.HOME) {
                            right = contentFocusRequester
                        }
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(tabBgColor)
                    .clickable { onTabSelected(tab) }
                    .tvFocusEffect(
                        shape = RoundedCornerShape(12.dp),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        scaleAmount = 1.05f
                    )
                    .padding(horizontal = if (isMenuFocused) 16.dp else 0.dp, vertical = 12.dp)
            ) {"""

replacement_tab = """            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .onFocusChanged { state ->
                        if (state.isFocused || state.hasFocus) {
                            focusedTab = tab
                        } else if (focusedTab == tab) {
                            focusedTab = null
                        }
                    }
                    .focusProperties {
                        if (tab == AppTab.HOME) {
                            right = contentFocusRequester
                        }
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(tabBgColor)
                    .clickable { onTabSelected(tab) }
                    .tvFocusEffect(
                        shape = RoundedCornerShape(12.dp),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        scaleAmount = 1.05f
                    )
                    .padding(start = 14.dp, end = 16.dp, vertical = 12.dp)
            ) {"""

if target_logo in content:
    content = content.replace(target_logo, replacement_logo)
    print("Replaced logo")

if target_tab in content:
    content = content.replace(target_tab, replacement_tab)
    print("Replaced tab")

with open(file_path, "w") as f:
    f.write(content)

