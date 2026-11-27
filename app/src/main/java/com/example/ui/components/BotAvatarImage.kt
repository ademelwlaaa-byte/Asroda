package com.example.ui.components

import android.util.Base64
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.util.ImageStorageManager
import java.nio.ByteBuffer
import java.util.Locale

internal enum class BotAvatarSourceKind {
    EMPTY,
    BASE64_DATA_URI,
    REMOTE_URL,
    LOCAL_URI_OR_PATH,
    UNSUPPORTED
}

/**
 * Immutable Base64 decoder & source classifier for bot avatars.
 * Returns a fresh [ByteArray] that can safely be wrapped into a new [ByteBuffer]
 * on every Coil request without ever hitting the stateful `position == limit` bug.
 */
internal object BotAvatarCodec {
    fun normalizeUrl(avatarUrl: String?): String? {
        val trimmed = avatarUrl?.trim()
        return if (trimmed.isNullOrEmpty()) null else trimmed
    }

    fun classifySource(avatarUrl: String?): BotAvatarSourceKind {
        val normalized = normalizeUrl(avatarUrl) ?: return BotAvatarSourceKind.EMPTY
        if (ImageStorageManager.isBase64DataUri(normalized)) {
            return BotAvatarSourceKind.BASE64_DATA_URI
        }
        val lower = normalized.lowercase(Locale.ROOT)
        return when {
            lower.startsWith("https://") || lower.startsWith("http://") -> BotAvatarSourceKind.REMOTE_URL
            lower.startsWith("file://") ||
                lower.startsWith("content://") ||
                lower.startsWith("android.resource://") ||
                normalized.startsWith("/") -> BotAvatarSourceKind.LOCAL_URI_OR_PATH
            else -> BotAvatarSourceKind.UNSUPPORTED
        }
    }

    fun decodeBase64DataUriBytes(avatarUrl: String): ByteArray? {
        return try {
            val base64Part = avatarUrl.substringAfter(",", missingDelimiterValue = "").trim()
            if (base64Part.isBlank()) null
            else Base64.decode(base64Part, Base64.DEFAULT)
        } catch (_: Exception) {
            null
        }
    }

    fun wrapFreshBuffer(bytes: ByteArray): ByteBuffer = ByteBuffer.wrap(bytes)

    fun buildCacheKey(avatarUrl: String): String =
        "avatar_b64_${avatarUrl.hashCode()}_${avatarUrl.length}"
}

/**
 * Unified, memory-efficient bot avatar composable powered by Coil.
 *
 * Handles 3 cases transparently without main-thread file I/O or Size.ORIGINAL allocations:
 *  1. Micro-WebP / Legacy Base64 Data URI ("data:image/webp;base64,...") -> decoded once into an
 *     immutable [ByteArray], wrapped into a remembered [ByteBuffer] [ImageRequest] (with one-shot
 *     fresh-buffer retry on eviction), cached in Coil's MemoryCache.
 *  2. file://, content://, android.resource://, or absolute path -> Coil AsyncImage with `allowRgb565(true)`.
 *  3. http/https URL -> Coil AsyncImage with disk & memory caching and `allowRgb565(true)`.
 *
 * Falls back cleanly to [fallback] when empty, unsupported, corrupt, or decode fails.
 */
@Composable
fun BotAvatarImage(
    avatarUrl: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val normalizedUrl = remember(avatarUrl) { BotAvatarCodec.normalizeUrl(avatarUrl) }
    val sourceKind = remember(normalizedUrl) { BotAvatarCodec.classifySource(normalizedUrl) }
    var loadFailed by remember(normalizedUrl) { mutableStateOf(false) }

    when {
        normalizedUrl == null ||
            sourceKind == BotAvatarSourceKind.EMPTY ||
            sourceKind == BotAvatarSourceKind.UNSUPPORTED ||
            loadFailed -> fallback()

        sourceKind == BotAvatarSourceKind.BASE64_DATA_URI -> {
            // Keep immutable ByteArray across recompositions; wrap a fresh ByteBuffer when building
            // the remembered request (and retry once with a fresh ByteBuffer if evicted/drained).
            val decodedBytes: ByteArray? = remember(normalizedUrl) {
                BotAvatarCodec.decodeBase64DataUriBytes(normalizedUrl)
            }
            var bufferRetryCount by remember(normalizedUrl) { mutableIntStateOf(0) }
            if (decodedBytes != null && decodedBytes.isNotEmpty()) {
                val request = remember(normalizedUrl, context, bufferRetryCount) {
                    ImageRequest.Builder(context)
                        .data(BotAvatarCodec.wrapFreshBuffer(decodedBytes))
                        .memoryCacheKey(BotAvatarCodec.buildCacheKey(normalizedUrl))
                        .allowRgb565(false)
                        .crossfade(false)
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = modifier,
                    onError = {
                        if (bufferRetryCount == 0) {
                            bufferRetryCount = 1
                        } else {
                            loadFailed = true
                        }
                    }
                )
            } else {
                fallback()
            }
        }

        else -> {
            val isRemote = sourceKind == BotAvatarSourceKind.REMOTE_URL
            val request = remember(normalizedUrl, context, isRemote) {
                ImageRequest.Builder(context)
                    .data(normalizedUrl)
                    .allowRgb565(true)
                    .crossfade(if (isRemote) 250 else 0)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier,
                onError = { loadFailed = true }
            )
        }
    }
}
