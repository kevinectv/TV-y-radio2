import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf8');

const tvStartTag = 'fun HomeHeroBannerTv(';
const tvStartIndex = content.indexOf(tvStartTag);
const tvEndIndex = content.indexOf('fun HomeHeroBannerMobile(', tvStartIndex);

let tvBlock = content.substring(tvStartIndex, tvEndIndex);
// Replace `if (isWideLayout) A else B` with `A`
tvBlock = tvBlock.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$1');
tvBlock = tvBlock.replace(/if\s*\(\s*isWideLayout\s*\)\s*([\d.]+\s*\.?\w*(?:\(\))?)\s*else\s*([\d.]+\s*\.?\w*(?:\(\))?)/g, '$1');

const mobStartTag = 'fun HomeHeroBannerMobile(';
const mobStartIndex = content.indexOf(mobStartTag);
const mobEndIndex = content.indexOf('}\n\n@Composable\nfun DrawCatalogRow(', mobStartIndex) + 2;

let mobBlock = content.substring(mobStartIndex, mobEndIndex);
// Replace `if (isWideLayout) A else B` with `B`
mobBlock = mobBlock.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$2');
mobBlock = mobBlock.replace(/if\s*\(\s*isWideLayout\s*\)\s*([\d.]+\s*\.?\w*(?:\(\))?)\s*else\s*([\d.]+\s*\.?\w*(?:\(\))?)/g, '$2');

content = content.substring(0, tvStartIndex) + tvBlock + mobBlock + content.substring(mobEndIndex);
fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log("Success apply");
