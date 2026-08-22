with open("app/src/main/java/com/example/ui/LuminaAppShell.kt", "r") as f:
    content = f.read()

old_topbar = """            topBar = {
                // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                Row("""

new_topbar = """            topBar = {
                if (!(isTvDevice && viewModel.currentTab == AppTab.HOME)) {
                    // --- 2. BARRA SUPERIOR PREMIUM (NUEVA APARIENCIA DE ALTO NIVEL) ---
                    Row("""

assert old_topbar in content, "old_topbar not found"
content = content.replace(old_topbar, new_topbar, 1)

old_bottom = """                }
            },
            bottomBar = {"""

new_bottom = """                }
            }
                }
            },
            bottomBar = {"""

assert old_bottom in content, "old_bottom not found"
content = content.replace(old_bottom, new_bottom, 1)

with open("app/src/main/java/com/example/ui/LuminaAppShell.kt", "w") as f:
    f.write(content)
print("Successfully updated LuminaAppShell.kt")
