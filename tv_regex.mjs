import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf8');

// Find the boundaries of HomeHeroBannerTv
const tvStartTag = 'fun HomeHeroBannerTv(';
const tvStartIndex = content.indexOf(tvStartTag);
const tvEndIndex = content.indexOf('fun HomeHeroBannerMobile(', tvStartIndex);

if (tvStartIndex === -1 || tvEndIndex === -1) {
    console.error("Tags not found");
    process.exit(1);
}

let tvBlock = content.substring(tvStartIndex, tvEndIndex);

// Regex to match: if (isWideLayout) {X} else {Y} or if (isWideLayout) X else Y
// It's a bit tricky to parse nested brackets, but we can do simple ones.
// Example: if (isWideLayout) 600.dp.responsive() else 400.dp.responsive()

const regex = /if\s*\(\s*isWideLayout\s*\)\s*([^\s]+(?:\s+[^\s]+)*?)\s+else\s+([^\s,)]+(?:\s+[^\s,)]+)*)/g;
// Actually, simple regex:
tvBlock = tvBlock.replace(/if\s*\(\s*isWideLayout\s*\)\s*([a-zA-Z0-9_.()]+)\s*else\s*([a-zA-Z0-9_.()]+)/g, '$1');

// specifically for strings that have spaces, like `110.dp`
tvBlock = tvBlock.replace(/if\s*\(\s*isWideLayout\s*\)\s*([\d.]+\s*\.?\w*(?:\(\))?)\s*else\s*([\d.]+\s*\.?\w*(?:\(\))?)/g, '$1');

content = content.substring(0, tvStartIndex) + tvBlock + content.substring(tvEndIndex);
fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', content);
console.log("Success TV regex apply");
