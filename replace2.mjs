import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf8');

const tvStartTag = '@Composable\nfun HomeHeroBannerTv(';
let tvStartIndex = content.indexOf(tvStartTag);
if (tvStartIndex === -1) {
   tvStartIndex = content.indexOf('fun HomeHeroBannerTv(') - 12; 
}

const mobStartTag = 'fun HomeHeroBannerMobile(';
const mobStartIndex = content.indexOf(mobStartTag);
const mobEndIndex = content.indexOf('}\n\n@Composable\nfun DrawCatalogRow(', mobStartIndex) + 2;

const tvCode = fs.readFileSync('tv_code.txt', 'utf8');
const mobileCode = fs.readFileSync('mobile_code.txt', 'utf8');

content = content.substring(0, tvStartIndex) + tvCode + '\n\n' + mobileCode + content.substring(mobEndIndex);
fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log("Success replacement");
