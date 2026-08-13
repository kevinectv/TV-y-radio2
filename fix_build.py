import sys

file_path = "app/build.gradle.kts"
with open(file_path, "r") as f:
    content = f.read()

target = """            // Clean directories first to prevent incorrect states
            if (destVisibleDir.exists()) destVisibleDir.deleteRecursively()
            if (destHiddenDir.exists()) destHiddenDir.deleteRecursively()
            
            // Create directories if they do not exist"""

replacement = """            // Clean directories first to prevent incorrect states
            if (destVisibleDir.exists()) {
                destVisibleDir.listFiles()?.forEach { if (it.name.endsWith(".apk")) it.delete() }
            }
            if (destHiddenDir.exists()) {
                destHiddenDir.listFiles()?.forEach { if (it.name.endsWith(".apk")) it.delete() }
            }
            
            // Create directories if they do not exist"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Replaced successfully.")
else:
    print("Target not found.")
