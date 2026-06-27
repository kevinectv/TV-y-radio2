import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf-8');

content = content.replace(/val offlineDescription = remember\(item\) \{[\s\S]*?\}\n/g, 'val offlineDescription = item.description\n');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log('Fixed final compilation errors.');
