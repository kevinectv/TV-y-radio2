with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('    outputs.dir(projectDir.dir("../.build-outputs"))\n', '')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
print("Removed .build-outputs from outputs.dir")
