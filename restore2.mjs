import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/SettingsScreenTv.kt', 'utf8');

// Undo the changes from split_screen3
content = content.replace(/^private fun /gm, 'fun ');
content = content.replace(/^private data class /gm, 'data class ');
content = content.replace(/^private val /gm, 'val ');
content = content.replace(/^private const val /gm, 'const val ');
content = content.replace(/^private enum class /gm, 'enum class ');
content = content.replace('fun SettingsScreenTv(', 'fun SettingsScreen(');
content = content.replace('val isWideLayout = true', 'val isWideLayout = maxWidth >= 580.dp');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', content);
console.log("Restored accurately");
