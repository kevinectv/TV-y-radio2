import re

file_path = "app/build.gradle.kts"
with open(file_path, "r") as f:
    content = f.read()

target = """    inputs.file(buildDir.file("outputs/apk/debug/app-debug.apk"))
    outputs.dir(projectDir.dir("../build-outputs"))
    outputs.dir(projectDir.dir("../.build-outputs"))
    outputs.upToDateWhen { false }
    
    doLast {
        val apkSource = buildDir.file("outputs/apk/debug/app-debug.apk").get().asFile
        if (apkSource.exists()) {"""

replacement = """    outputs.dir(projectDir.dir("../build-outputs"))
    outputs.dir(projectDir.dir("../.build-outputs"))
    outputs.upToDateWhen { false }
    
    doLast {
        val debugApkSource = buildDir.file("outputs/apk/debug/app-debug.apk").get().asFile
        val releaseApkSource = buildDir.file("outputs/apk/release/app-release.apk").get().asFile
        
        // Pick the most recently modified APK, or release if both exist
        val apkSource = if (releaseApkSource.exists() && (!debugApkSource.exists() || releaseApkSource.lastModified() >= debugApkSource.lastModified())) {
            releaseApkSource
        } else if (debugApkSource.exists()) {
            debugApkSource
        } else {
            null
        }

        if (apkSource != null && apkSource.exists()) {"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Fixed release logic")
else:
    print("Target not found")
