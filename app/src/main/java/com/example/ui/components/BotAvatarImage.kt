package com.example.ui.components

import android.util.Base64
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/**
 * Immutable Base64 decoder helper for Micro-WebP / legacy data URIs.
 * Returns a fresh [ByteArray] that can safely be wrapped into a new [ByteBuffer]
 * on every Coil request without ever hitting the stateful `position == limit` bug.
 */
internal object BotAvatarCodec {
    fun decodeBase64DataUriBytes(avatarUrl: String): ByteArray? {
        return try {
            val base64Part = avatarUrl.substringAfter(",")
            if (base64Part.isBlank()) null
            else Base64.decode(base64Part, Base64.NO_WRAP)
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
 * Handles 3 cases transparently without ever requesting Size.ORIGINAL:
 *  1. Micro-WebP / Legacy Base64 Data URI ("data:image/webp;base64,...") -> decoded once into an
 *     immutable [ByteArray], wrapped into a fresh [ByteBuffer] per [ImageRequest], cached in
 *     Coil's MemoryCache with `allowRgb565(false)` so purple/gothic gradients never band.
 *  2. file:// or content:// URI -> Coil AsyncImage with `allowRgb565(false)`.
 *  3. http/https URL -> Coil AsyncImage with disk & memory caching and `allowRgb565(false)`.
 *
 * Falls back to [fallback] composable when no valid image is available or decoding fails.
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
    var loadFailed by remember(avatarUrl) { mutableStateOf(false) }

    when {
        avatarUrl.isNullOrBlank() || loadFailed -> fallback()

        ImageStorageManager.isBase64DataUri(avatarUrl) -> {
            // Keep immutable ByteArray across recompositions; wrap a fresh ByteBuffer per ImageRequest
            // so Coil never reads an already-drained ByteBuffer (position == limit) after cache eviction.
            val decodedBytes: ByteArray? = remember(avatarUrl) {
                BotAvatarCodec.decodeBase64DataUriBytes(avatarUrl)
            }
            if (decodedBytes != null && decodedBytes.isNotEmpty()) {
                val request = ImageRequest.Builder(context)
                    .data(BotAvatarCodec.wrapFreshBuffer(decodedBytes))
                    .memoryCacheKey(BotAvatarCodec.buildCacheKey(avatarUrl))
                    .allowRgb565(false)
                    .crossfade(false)
                    .build()
                AsyncImage(
                    model = request,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = modifier,
                    onError = { loadFailed = true }
                )
            } else {
                fallback()
            }
        }

        else -> {
            val isRemote = avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")
            val request = remember(avatarUrl, context) {
                ImageRequest.Builder(context)
                    .data(avatarUrl)
                    .allowRgb565(false)
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
