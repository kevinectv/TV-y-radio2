import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/SettingsScreenTv.kt', 'utf8');

// Undo the changes from split_screen3
content = content.replace(/^private fun /gm, 'fun ');
content = content.replace(/^private data class /gm, 'data class ');
content = content.replace(/^private val /gm, 'val ');
content = content.replace(/^private const val /gm, 'const val ');
content = content.replace(/^private enum class /gm, 'enum class ');
content = content.replace('fun SettingsScreenTv(', 'fun SettingsScreen(');

// Actually, wait, `split_screen3.mjs` also did:
// tvContent = tvContent.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$1');
// tvContent = tvContent.replace(/val isWideLayout = .*/g, 'val isWideLayout = true');
// Oh no! It applied the regex to remove `else` blocks and hardcoded `isWideLayout = true`!
// So it is ALREADY purely the TV code! It's not the original `SettingsScreen.kt`!
