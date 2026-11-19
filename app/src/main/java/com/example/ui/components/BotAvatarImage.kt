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
 * Unified, memory-efficient bot avatar composable powered by Coil.
 *
 * Handles 3 cases transparently without ever requesting Size.ORIGINAL:
 *  1. Micro-WebP / Legacy Base64 Data URI ("data:image/webp;base64,...") -> decoded to ByteBuffer
 *     and rendered through Coil's MemoryCache with automatic target-box downsampling & RGB_565.
 *  2. file:// or content:// URI -> Coil AsyncImage with allowRgb565(true) & crossfade(300).
 *  3. http/https URL -> Coil AsyncImage with disk & memory caching.
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
            val byteBuffer: ByteBuffer? = remember(avatarUrl) {
                try {
                    val base64Part = avatarUrl.substringAfter(",")
                    if (base64Part.isBlank()) null
                    else ByteBuffer.wrap(Base64.decode(base64Part, Base64.NO_WRAP))
                } catch (_: Exception) {
                    null
                }
            }
            if (byteBuffer != null) {
                val request = remember(avatarUrl, context) {
                    ImageRequest.Builder(context)
                        .data(byteBuffer)
                        .memoryCacheKey("avatar_b64_${avatarUrl.hashCode()}_${avatarUrl.length}")
                        .allowRgb565(true)
                        .crossfade(false)
                        .build()
                }
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
