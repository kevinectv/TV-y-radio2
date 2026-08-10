import sys

file_path = "app/src/main/java/com/example/ui/LuminaAppShell.kt"
with open(file_path, "r") as f:
    content = f.read()

# Let's just find the start of TvSideMenu, split the file into two.
# The top part is LuminaAppShell. We need to make sure it has balanced braces.

idx = content.find("@Composable\nfun TvSideMenu")
if idx == -1:
    print("Could not find TvSideMenu")
    sys.exit(1)

lumina_shell = content[:idx]
tv_side_menu = content[idx:]

# Remove the extra brace we mistakenly added at the end of tv_side_menu
# (Actually, let's just re-calculate the braces of lumina_shell)

def balance_braces(text):
    count = 0
    for char in text:
        if char == '{':
            count += 1
        elif char == '}':
            count -= 1
    return count

# Let's remove any trailing closing braces from lumina_shell just in case,
# then add exactly the number needed to balance it.
lumina_shell = lumina_shell.rstrip()
while lumina_shell.endswith('}'):
    lumina_shell = lumina_shell[:-1].rstrip()

bal = balance_braces(lumina_shell)
print("Balance before fixing:", bal)
if bal > 0:
    lumina_shell += "\n" + "}\n" * bal

# Now let's fix tv_side_menu braces.
tv_side_menu = tv_side_menu.rstrip()
while tv_side_menu.endswith('}'):
    tv_side_menu = tv_side_menu[:-1].rstrip()

bal_tv = balance_braces(tv_side_menu)
print("Balance TV before fixing:", bal_tv)
if bal_tv > 0:
    tv_side_menu += "\n" + "}\n" * bal_tv

with open(file_path, "w") as f:
    f.write(lumina_shell + "\n\n" + tv_side_menu)

print("Done")
