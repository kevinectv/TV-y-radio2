import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/SettingsScreenTv.kt', 'utf8');

content = content.replace(/^private fun /gm, 'fun ');
content = content.replace(/^private data class /gm, 'data class ');
content = content.replace(/^private val /gm, 'val ');
content = content.replace(/^private const val /gm, 'const val ');
content = content.replace(/^private enum class /gm, 'enum class ');
content = content.replace('fun SettingsScreenTv(', 'fun SettingsScreen(');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', content);

// Delete the old files
fs.unlinkSync('app/src/main/java/com/example/ui/screens/SettingsScreenTv.kt');
fs.unlinkSync('app/src/main/java/com/example/ui/screens/SettingsScreenMobile.kt');
console.log("Restored");
