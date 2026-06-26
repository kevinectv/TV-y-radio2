import fs from 'fs';

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'utf8');

const startTag = '@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)\n@Composable\nfun HomeHeroBanner(';
const endTag = '}\n\n@Composable\nfun DrawCatalogRow(';

const startIndex = content.indexOf(startTag);
const endIndex = content.indexOf(endTag) + 2; // includes }\n

const heroBlock = content.substring(startIndex, endIndex);

// Delegator
const delegator = `@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HomeHeroBanner(
    currentMovie: CatalogItem,
    activeHeroLoadedDetails: LoadedTmdbDetails?,
    featuredMovies: List<CatalogItem>,
    favoriteCatalogItems: Set<String>,
    bannerHeight: androidx.compose.ui.unit.Dp,
    isWideLayout: Boolean,
    viewModel: MediaViewModel,
    scrollState: LazyListState,
    onTrailerClick: (CatalogItem) -> Unit,
    onDetailsClick: (CatalogItem) -> Unit
) {
    if (isWideLayout) {
        HomeHeroBannerTv(currentMovie, activeHeroLoadedDetails, featuredMovies, favoriteCatalogItems, bannerHeight, viewModel, scrollState, onTrailerClick, onDetailsClick)
    } else {
        HomeHeroBannerMobile(currentMovie, activeHeroLoadedDetails, featuredMovies, favoriteCatalogItems, bannerHeight, viewModel, scrollState, onTrailerClick, onDetailsClick)
    }
}
`;

// Tv
let tvBlock = heroBlock.replace('fun HomeHeroBanner(', 'fun HomeHeroBannerTv(');
tvBlock = tvBlock.replace('    isWideLayout: Boolean,\n', '');
tvBlock = tvBlock.replace('    val parallaxOffset = 0f\n', '    val parallaxOffset = 0f\n    val isWideLayout = true\n');

// Mobile
let mobileBlock = heroBlock.replace('fun HomeHeroBanner(', 'fun HomeHeroBannerMobile(');
mobileBlock = mobileBlock.replace('    isWideLayout: Boolean,\n', '');
mobileBlock = mobileBlock.replace('    val parallaxOffset = 0f\n', '    val parallaxOffset = 0f\n    val isWideLayout = false\n');

const newContent = content.substring(0, startIndex) + delegator + '\n' + tvBlock + '\n\n' + mobileBlock + '\n' + content.substring(endIndex);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/HomeScreen.kt', newContent);
console.log("Success");
