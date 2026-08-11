import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """        ElegantBackground(
            modifier = Modifier.fillMaxSize(),
            accentColorHex = backgroundAccent
        )

        Box(modifier = Modifier.fillMaxSize()) {
        // Main structural Scaffold to support safe edges
        Scaffold("""

replacement = """        ElegantBackground(
            modifier = Modifier.fillMaxSize(),
            accentColorHex = backgroundAccent
        )

        // Main structural Scaffold to support safe edges
        Scaffold("""

if target in content:
    content = content.replace(target, replacement)
    
    # We need to remove one trailing brace before the end of the file
    content = content.rstrip()
    if content.endswith('}'):
        content = content[:-1].rstrip()
    
    content += "\n"
    
    with open(file_path, "w") as f:
        f.write(content)
    print("Unnested Box")
else:
    print("Target not found")
