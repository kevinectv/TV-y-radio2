import sys

file_path = "app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt"
with open(file_path, "r") as f:
    content = f.read()

old_padding = "padding(bottom = 124.dp, end = 80.dp) // Aligned with synopsis"
new_padding = "padding(bottom = 110.dp, end = 80.dp)"

if old_padding in content:
    content = content.replace(old_padding, new_padding)
    with open(file_path, "w") as f:
        f.write(content)
    print("Successfully updated circular button padding to 110.dp.")
else:
    print("Could not find the old padding string.")

