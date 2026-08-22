with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.ui.components.responsivepackage com.example.ui.screens", "package com.example.ui.screens\n\nimport com.example.ui.components.responsive")
content = content.replace("package com.example.ui.screensimport", "package com.example.ui.screens\n\nimport")

with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "w") as f:
    f.write(content)
print("Fixed package in HomeHeroBannerTv.kt")
