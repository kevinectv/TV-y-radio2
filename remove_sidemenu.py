import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

# Remove TvSideMenu Composable
import re
# We need to find @Composable\nfun TvSideMenu and remove everything until the end of the file
idx = content.find("@Composable\nfun TvSideMenu")
if idx != -1:
    content = content[:idx].rstrip() + "\n"
    print("Removed TvSideMenu Composable")
else:
    print("Could not find TvSideMenu Composable")

with open(file_path, "w") as f:
    f.write(content)
