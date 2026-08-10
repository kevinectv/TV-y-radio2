import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target = "import androidx.compose.ui.focus.focusGroup"
replacement = "import androidx.compose.foundation.focusGroup"

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Fixed import")
else:
    print("Could not find import")
