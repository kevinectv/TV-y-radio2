import re

file_path = "app/build.gradle.kts"
with open(file_path, "r") as f:
    content = f.read()

target = """            // Create directories if they do not exist
            destVisibleDir.mkdirs()
            destHiddenDir.mkdirs()
            
            val visApk = File(destVisibleDir, "app-debug.apk")
            val hidApk = File(destHiddenDir, "app-debug.apk")
            
            // Legacy / Cached URL support targets
            val legacyNames = listOf(
                "Lumina_IPTV_Latest.apk",
                "Lumina_IPTV_v2.0.2.apk",
                "Lumina_IPTV_v2.0.1.apk",
                "Lumina_IPTV_v2.0.0.apk"
            )"""

replacement = """            // Clean directories first to prevent incorrect states
            if (destVisibleDir.exists()) destVisibleDir.deleteRecursively()
            if (destHiddenDir.exists()) destHiddenDir.deleteRecursively()
            
            // Create directories if they do not exist
            destVisibleDir.mkdirs()
            destHiddenDir.mkdirs()
            
            val visApk = File(destVisibleDir, "app-debug.apk")
            val hidApk = File(destHiddenDir, "app-debug.apk")
            
            // Dynamic names and Legacy / Cached URL support targets
            val versionApkName = "Lumina_IPTV_v${appVersion}.apk"
            val legacyNames = listOf(
                "Lumina_IPTV_Latest.apk",
                versionApkName,
                "Lumina_IPTV_v2.0.3.apk",
                "Lumina_IPTV_v2.0.2.apk",
                "Lumina_IPTV_v2.0.1.apk",
                "Lumina_IPTV_v2.0.0.apk"
            ).distinct()"""
            
if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Replaced logic")
else:
    print("Target not found")
