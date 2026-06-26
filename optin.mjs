import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf8');

content = content.replace('@Composable\nfun HomeHeroBannerTv(', '@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)\n@Composable\nfun HomeHeroBannerTv(');

content = content.replace('@Composable\nfun HomeHeroBannerMobile(', '@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)\n@Composable\nfun HomeHeroBannerMobile(');

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log("OptIn added");
