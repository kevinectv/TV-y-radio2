import sys
import re

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

imports_to_add = """
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
"""

if "import androidx.compose.ui.draw.drawBehind" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier" + imports_to_add)
    with open(file_path, "w") as f:
        f.write(content)
    print("Added imports")
else:
    print("Imports already present")

