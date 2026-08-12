import re

file_path = "app/build.gradle.kts"
with open(file_path, "r") as f:
    content = f.read()

target = """afterEvaluate {
    tasks.findByName("assembleDebug")?.finalizedBy("copyApkToOutputFolders")
    tasks.findByName("assemble")?.finalizedBy("copyApkToOutputFolders")
}"""

replacement = """afterEvaluate {
    tasks.findByName("assembleDebug")?.finalizedBy("copyApkToOutputFolders")
    tasks.findByName("assembleRelease")?.finalizedBy("copyApkToOutputFolders")
    tasks.findByName("assemble")?.finalizedBy("copyApkToOutputFolders")
    tasks.findByName("build")?.finalizedBy("copyApkToOutputFolders")
}"""

if target in content:
    content = content.replace(target, replacement)
    with open(file_path, "w") as f:
        f.write(content)
    print("Added hooks")
else:
    print("Target not found")
