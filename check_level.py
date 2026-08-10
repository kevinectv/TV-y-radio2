import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    lines = f.readlines()

count = 0
for i, line in enumerate(lines):
    for char in line:
        if char == '{':
            count += 1
        elif char == '}':
            count -= 1
    if "fun TvSideMenu" in line:
        print("Brace level at TvSideMenu:", count)
        break

