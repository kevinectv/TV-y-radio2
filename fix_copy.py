with open("app/build.gradle.kts", "r") as f:
    content = f.read()

import re

# Match the old doLast block inside copyApkToOutputFolders
pattern = r"doLast \{.*?println\(\"--- APK COPY FAILED: Source file not found ---\"\)\n        \}\n    \}"

new_doLast = """doLast {
        val debugApkSource = buildDir.file("outputs/apk/debug/app-debug.apk").get().asFile
        val releaseApkSource = buildDir.file("outputs/apk/release/app-release.apk").get().asFile
        
        val apkSource = if (releaseApkSource.exists() && (!debugApkSource.exists() || releaseApkSource.lastModified() >= debugApkSource.lastModified())) {
            releaseApkSource
        } else if (debugApkSource.exists()) {
            debugApkSource
        } else {
            null
        }

        if (apkSource != null && apkSource.exists()) {
            val destVisibleDir = projectDir.dir("../build-outputs").asFile
            
            // Clean directory first
            if (destVisibleDir.exists()) {
                destVisibleDir.listFiles()?.forEach { if (it.name.endsWith(".apk")) it.delete() }
            } else {
                destVisibleDir.mkdirs()
            }
            
            // Only output a single app-debug.apk to avoid zip timeouts and platform confusion
            val visApk = File(destVisibleDir, "app-debug.apk")
            apkSource.copyTo(visApk, overwrite = true)
            
            println("--- APK COPY SUCCESSFUL ---")
            println("Copied APK to: ${visApk.absolutePath} (${visApk.length()} bytes)")
        } else {
            println("--- APK COPY FAILED: Source file not found ---")
        }
    }"""

content = re.sub(pattern, new_doLast, content, flags=re.DOTALL)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
print("Updated app/build.gradle.kts")
