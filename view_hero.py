with open("app/src/main/java/com/example/ui/screens/HomeHeroBannerTv.kt", "r") as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if "4. Primary Play Button" in line or "5. Generous spacing" in line or "6. Carousel Indicators" in line or "Floating Play Button" in line:
        print(f"{i}: {line.strip()}")
