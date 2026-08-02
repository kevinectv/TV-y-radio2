package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache

class LuminaApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Allocate 25% of available RAM to image caching
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.10) // Allocate up to 10% of disk space to cache poster assets
                    .build()
            }
            .crossfade(true) // Smooth 100ms fade-in transition
            .crossfade(150)
            .respectCacheHeaders(false) // Cache images aggressively regardless of server headers
            .build()
    }
}
