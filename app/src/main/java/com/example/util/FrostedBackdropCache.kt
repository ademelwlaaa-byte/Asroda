package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized, memory-bounded (max 4 entries) optical frosted-backdrop cache.
 *
 * All pixel-blurring runs off the UI thread on [Dispatchers.Default] via [getOrGenerate] or
 * [rememberFrostedBackdrop]. While a backdrop is being generated on the first frame, callers
 * render the 0-KB [fallbackDarkGradientBrush] (#090414 -> #1A0B2E) so there is 0 ms main-thread stall.
 *
 * Cleared automatically from [com.example.EmochiApplication.onTrimMemory].
 */
object FrostedBackdropCache {

    private const val MAX_ENTRIES = 4
    private val cache = object : LruCache<String, ImageBitmap>(MAX_ENTRIES) {}
    private val cacheLock = Any()
    private val keyLocks = ConcurrentHashMap<String, Mutex>()

    private fun mutexFor(key: String): Mutex = keyLocks.computeIfAbsent(key) { Mutex() }

    val fallbackDarkGradientBrush: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF090414), Color(0xFF1A0B2E))
    )

    /**
     * Ultra-lightweight 1x2 cosmic dark gradient bitmap (#090414 -> #1A0B2E, <16 bytes) used on the
     * very first frame while [rememberFrostedBackdrop] computes the optical blur on Dispatchers.Default.
     */
    val placeholderImageBitmap: ImageBitmap by lazy {
        val bmp = Bitmap.createBitmap(1, 2, Bitmap.Config.RGB_565)
        bmp.setPixel(0, 0, android.graphics.Color.rgb(9, 4, 20))
        bmp.setPixel(0, 1, android.graphics.Color.rgb(26, 11, 46))
        bmp.asImageBitmap()
    }

    fun get(key: String): ImageBitmap? = synchronized(cacheLock) { cache.get(key) }

    fun put(key: String, bitmap: ImageBitmap) {
        synchronized(cacheLock) {
            cache.put(key, bitmap)
        }
    }

    fun size(): Int = synchronized(cacheLock) { cache.size() }

    fun evictAll() {
        synchronized(cacheLock) {
            cache.evictAll()
        }
    }

    suspend fun getOrGenerate(
        context: Context,
        key: String,
        drawableResId: Int,
        targetWidth: Int = 360,
        maxHeight: Int = 1080,
        blurRadius: Int = 4,
        blurPasses: Int = 2,
        violetShiftWarmPixels: Boolean = false,
        outputRgb565: Boolean = false
    ): ImageBitmap? {
        get(key)?.let { return it }
        val appContext = context.applicationContext ?: context
        // Single-flight per-key lock eliminates duplicate concurrent bitmap allocations & CPU blur stampedes
        return mutexFor(key).withLock {
            get(key)?.let { return@withLock it }
            withContext(Dispatchers.Default) {
                get(key)?.let { return@withContext it }
                val generated = generateFrostedBitmapInternal(
                    context = appContext,
                    drawableResId = drawableResId,
                    targetWidth = targetWidth,
                    maxHeight = maxHeight,
                    blurRadius = blurRadius,
                    blurPasses = blurPasses,
                    violetShiftWarmPixels = violetShiftWarmPixels,
                    outputRgb565 = outputRgb565
                )
                if (generated != null) {
                    put(key, generated)
                }
                generated
            }
        }
    }

    /**
     * Synchronous cache-backed lookup for legacy call sites that require a non-null [ImageBitmap]
     * immediately. Uses the shared LruCache so the same drawable is never blurred twice across screens.
     */
    fun getOrGenerateSync(
        context: Context,
        key: String,
        drawableResId: Int,
        targetWidth: Int = 360,
        maxHeight: Int = 1080,
        blurRadius: Int = 4,
        blurPasses: Int = 2,
        violetShiftWarmPixels: Boolean = false,
        outputRgb565: Boolean = false
    ): ImageBitmap {
        get(key)?.let { return it }
        val appContext = context.applicationContext ?: context
        val generated = generateFrostedBitmapInternal(
            context = appContext,
            drawableResId = drawableResId,
            targetWidth = targetWidth,
            maxHeight = maxHeight,
            blurRadius = blurRadius,
            blurPasses = blurPasses,
            violetShiftWarmPixels = violetShiftWarmPixels,
            outputRgb565 = outputRgb565
        )
        return if (generated != null) {
            put(key, generated)
            generated
        } else {
            placeholderImageBitmap
        }
    }

    private fun generateFrostedBitmapInternal(
        context: Context,
        drawableResId: Int,
        targetWidth: Int,
        maxHeight: Int,
        blurRadius: Int,
        blurPasses: Int,
        violetShiftWarmPixels: Boolean,
        outputRgb565: Boolean
    ): ImageBitmap? {
        return try {
            val opts = BitmapFactory.Options().apply {
                inSampleSize = 2
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val raw = BitmapFactory.decodeResource(context.resources, drawableResId, opts)
                ?: return null

            val w = targetWidth.coerceAtLeast(64)
            val h = ((raw.height.toFloat() / raw.width.toFloat().coerceAtLeast(1f)) * w)
                .toInt()
                .coerceIn(w, maxHeight.coerceAtLeast(w))
            val scaled = Bitmap.createScaledBitmap(raw, w, h, true)
            if (raw !== scaled) raw.recycle()

            val pixels = IntArray(w * h)
            scaled.getPixels(pixels, 0, w, 0, 0, w, h)
            scaled.recycle()

            if (violetShiftWarmPixels) {
                for (i in pixels.indices) {
                    val px = pixels[i]
                    var r = (px ushr 16) and 0xFF
                    var g = (px ushr 8) and 0xFF
                    val bOrig = px and 0xFF
                    var b = bOrig
                    if (r > bOrig + 8) {
                        val warmExcess = r - bOrig
                        b = (bOrig + (warmExcess * 0.95f).toInt()).coerceAtMost(255)
                        r = (r - (warmExcess * 0.22f).toInt()).coerceIn(0, 255)
                        g = (g - (warmExcess * 0.32f).toInt()).coerceIn(0, 255)
                        pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                    }
                }
            }

            val tmp = IntArray(w * h)
            val radius = blurRadius.coerceAtLeast(1)
            val div = radius * 2 + 1
            repeat(blurPasses.coerceAtLeast(1)) {
                // Horizontal separable box-blur pass
                for (y in 0 until h) {
                    val rowOffset = y * w
                    var rSum = 0
                    var gSum = 0
                    var bSum = 0
                    for (dx in -radius..radius) {
                        val px = pixels[rowOffset + dx.coerceIn(0, w - 1)]
                        rSum += (px ushr 16) and 0xFF
                        gSum += (px ushr 8) and 0xFF
                        bSum += px and 0xFF
                    }
                    for (x in 0 until w) {
                        tmp[rowOffset + x] =
                            (0xFF shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                        val nextX = (x + radius + 1).coerceAtMost(w - 1)
                        val prevX = (x - radius).coerceAtLeast(0)
                        val nextPx = pixels[rowOffset + nextX]
                        val prevPx = pixels[rowOffset + prevX]
                        rSum += ((nextPx ushr 16) and 0xFF) - ((prevPx ushr 16) and 0xFF)
                        gSum += ((nextPx ushr 8) and 0xFF) - ((prevPx ushr 8) and 0xFF)
                        bSum += (nextPx and 0xFF) - (prevPx and 0xFF)
                    }
                }
                // Vertical separable box-blur pass
                for (x in 0 until w) {
                    var rSum = 0
                    var gSum = 0
                    var bSum = 0
                    for (dy in -radius..radius) {
                        val px = tmp[dy.coerceIn(0, h - 1) * w + x]
                        rSum += (px ushr 16) and 0xFF
                        gSum += (px ushr 8) and 0xFF
                        bSum += px and 0xFF
                    }
                    for (y in 0 until h) {
                        pixels[y * w + x] =
                            (0xFF shl 24) or ((rSum / div) shl 16) or ((gSum / div) shl 8) or (bSum / div)
                        val nextY = (y + radius + 1).coerceAtMost(h - 1)
                        val prevY = (y - radius).coerceAtLeast(0)
                        val nextPx = tmp[nextY * w + x]
                        val prevPx = tmp[prevY * w + x]
                        rSum += ((nextPx ushr 16) and 0xFF) - ((prevPx ushr 16) and 0xFF)
                        gSum += ((nextPx ushr 8) and 0xFF) - ((prevPx ushr 8) and 0xFF)
                        bSum += (nextPx and 0xFF) - (prevPx and 0xFF)
                    }
                }
            }

            val config = if (outputRgb565) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
            val out = Bitmap.createBitmap(w, h, config)
            out.setPixels(pixels, 0, w, 0, 0, w, h)
            out.asImageBitmap()
        } catch (_: Throwable) {
            null
        }
    }
}

/**
 * Non-blocking Compose helper that returns the cached frosted [ImageBitmap] immediately if
 * warm, or computes it asynchronously on [Dispatchers.Default] without stalling the UI thread.
 * While `value == null`, composables should draw [FrostedBackdropCache.fallbackDarkGradientBrush].
 */
@Composable
fun rememberFrostedBackdrop(
    key: String,
    drawableResId: Int,
    targetWidth: Int = 360,
    maxHeight: Int = 1080,
    blurRadius: Int = 4,
    blurPasses: Int = 2,
    violetShiftWarmPixels: Boolean = false,
    outputRgb565: Boolean = false
): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(
        initialValue = FrostedBackdropCache.get(key),
        key1 = key,
        key2 = drawableResId
    ) {
        if (value == null) {
            value = FrostedBackdropCache.getOrGenerate(
                context = context,
                key = key,
                drawableResId = drawableResId,
                targetWidth = targetWidth,
                maxHeight = maxHeight,
                blurRadius = blurRadius,
                blurPasses = blurPasses,
                violetShiftWarmPixels = violetShiftWarmPixels,
                outputRgb565 = outputRgb565
            )
        }
    }
}
