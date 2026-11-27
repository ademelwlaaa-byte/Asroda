package com.example.util

import android.content.Context
import coil.Coil
import java.io.File
import java.util.Locale

/**
 * Safely computes and clears ONLY disposable app caches (Edge Neural TTS MP3 cache,
 * Book Mode voice cache, Coil image disk/memory cache, frosted backdrop bitmaps).
 * NEVER touches Room databases, SharedPreferences, API keys, chats, or book progress.
 */
object AppCacheCleaner {

    data class CacheReport(
        val ttsBytes: Long,
        val imageBytes: Long,
        val otherCacheBytes: Long
    ) {
        val totalBytes: Long get() = ttsBytes + imageBytes + otherCacheBytes
    }

    fun inspectCache(context: Context): CacheReport {
        val root = context.applicationContext.cacheDir ?: return CacheReport(0L, 0L, 0L)
        var tts = 0L
        var img = 0L
        var other = 0L
        root.listFiles()?.forEach { child ->
            val size = directorySize(child)
            when (child.name.lowercase(Locale.ROOT)) {
                "tts_audio_cache", "cosmic_voice_cache", "book_voice_cache" -> tts += size
                "image_cache", "coil_cache" -> img += size
                else -> other += size
            }
        }
        return CacheReport(ttsBytes = tts, imageBytes = img, otherCacheBytes = other)
    }

    /**
     * Clears disposable disk & memory caches and returns the freed byte count.
     */
    fun clearDisposableCaches(context: Context): Long {
        val before = inspectCache(context).totalBytes
        try {
            FrostedBackdropCache.evictAll()
        } catch (_: Exception) {}
        try {
            com.example.ui.screens.AetheriaRefGlassCache.evictAll()
        } catch (_: Exception) {}
        try {
            com.example.ui.components.evictGlobalSettingsRefPlatesCache()
        } catch (_: Exception) {}
        try {
            Coil.imageLoader(context.applicationContext).memoryCache?.clear()
        } catch (_: Exception) {}
        try {
            val root = context.applicationContext.cacheDir
            root?.listFiles()?.forEach { child ->
                child.deleteRecursively()
            }
        } catch (_: Exception) {}
        val after = inspectCache(context).totalBytes
        return (before - after).coerceAtLeast(0L)
    }

    fun formatBytes(bytes: Long): String {
        val safe = bytes.coerceAtLeast(0L)
        return when {
            safe < 1024L -> "$safe B"
            safe < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", safe / 1024.0)
            else -> String.format(Locale.US, "%.2f MB", safe / (1024.0 * 1024.0))
        }
    }

    private fun directorySize(file: File, depth: Int = 0): Long {
        if (depth > 12 || !file.exists()) return 0L
        if (file.isFile) return file.length()
        var total = 0L
        file.listFiles()?.forEach { total += directorySize(it, depth + 1) }
        return total
    }
}
