import os

file_path = '/app/applet/app/src/main/java/com/example/ui/screens/HomeHeroBanner.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Convert literal "\\n" and "\n" escapes to real newlines
content = content.replace("\\\\n", "\\n").replace("\\n", "\n")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("SUCCESS: Converted literal backslash-n escapes to actual newlines!")
