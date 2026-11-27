package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.min

object ImageStorageManager {

    private const val TAG = "ImageStorageManager"
    private const val TARGET_AVATAR_SIZE = 256
    private const val WEBP_QUALITY = 82
    private const val BOT_IMAGES_DIR = "bot_images"

    /**
     * OOM-safe WebP compression and storage into app internal storage (filesDir/bot_images/).
     * Delegates to AvatarOptimizer.processBackgroundToWebPFile for EXIF-safe, RGB_565 WebP output.
     */
    suspend fun compressAndSaveImage(context: Context, inputUri: Uri): String = withContext(Dispatchers.IO) {
        AvatarOptimizer.processBackgroundToWebPFile(context, inputUri)
            .onFailure { Log.e(TAG, "Failed to compress and save WebP image", it) }
            .getOrElse { inputUri.toString() }
    }

    /**
     * Converts a local file path / URI string to a compact Micro-WebP Base64 Data URI
     * ("data:image/webp;base64,...", ~12-18 KB) with OOM-safe inSampleSize & EXIF correction.
     */
    suspend fun toBase64DataUri(context: Context, uriOrPath: String): String? = withContext(Dispatchers.IO) {
        try {
            if (uriOrPath.startsWith("data:image/")) return@withContext uriOrPath

            if (uriOrPath.startsWith("content://") || uriOrPath.startsWith("file://")) {
                val uri = Uri.parse(uriOrPath)
                return@withContext AvatarOptimizer.processAvatarToMicroWebPBase64(context, uri).getOrNull()
            }

            val file = File(uriOrPath)
            if (!file.exists()) return@withContext null

            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return@withContext null

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = AvatarOptimizer.calculateInSampleSize(
                    boundsOptions.outWidth,
                    boundsOptions.outHeight,
                    TARGET_AVATAR_SIZE * 2,
                    TARGET_AVATAR_SIZE * 2
                )
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            var sampled: Bitmap? = null
            var rotated: Bitmap? = null
            var square: Bitmap? = null
            var finalBitmap: Bitmap? = null
            try {
                sampled = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                    ?: return@withContext null

                rotated = AvatarOptimizer.fixExifOrientation(context, Uri.fromFile(file), sampled)
                if (rotated !== sampled && !sampled.isRecycled) {
                    sampled.recycle()
                    sampled = null
                }

                val dimension = min(rotated.width, rotated.height)
                val xOffset = (rotated.width - dimension) / 2
                val yOffset = (rotated.height - dimension) / 2
                square = Bitmap.createBitmap(rotated, xOffset, yOffset, dimension, dimension)
                if (square !== rotated && !rotated.isRecycled) {
                    rotated.recycle()
                    rotated = null
                }

                finalBitmap = Bitmap.createScaledBitmap(square, TARGET_AVATAR_SIZE, TARGET_AVATAR_SIZE, true)
                if (finalBitmap !== square && !square.isRecycled) {
                    square.recycle()
                    square = null
                }

                val out = ByteArrayOutputStream()
                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                finalBitmap.compress(format, WEBP_QUALITY, out)
                val bytes = out.toByteArray()
                if (bytes.isEmpty()) return@withContext null
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                "data:image/webp;base64,$base64"
            } finally {
                listOf(finalBitmap, square, rotated, sampled).distinct().forEach { bmp ->
                    try {
                        if (bmp != null && !bmp.isRecycled) bmp.recycle()
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "toBase64DataUri failed", e)
            null
        }
    }

    /**
     * Decodes a Base64 data URI back to a Bitmap with RGB_565 preference for low memory usage.
     */
    fun base64DataUriToBitmap(dataUri: String): Bitmap? {
        return try {
            if (!dataUri.startsWith("data:image/")) return null
            val base64Part = dataUri.substringAfter(",")
            val bytes = Base64.decode(base64Part, Base64.NO_WRAP)
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
        } catch (e: Exception) {
            Log.e(TAG, "base64DataUriToBitmap failed", e)
            null
        }
    }

    /**
     * Returns true if the given string is a Base64 data URI.
     */
    fun isBase64DataUri(value: String): Boolean = value.startsWith("data:image/")

    /**
     * Safely deletes a locally stored background image ONLY IF it is owned by Emochi
     * inside `context.filesDir/bot_images/`. External URIs, URLs, and Base64 strings are ignored.
     */
    fun deleteLocalBackgroundIfOwned(context: Context, uriStr: String?): Boolean {
        if (uriStr.isNullOrBlank()) return false
        if (uriStr == "USE_AVATAR" || isBase64DataUri(uriStr) ||
            uriStr.startsWith("http://") || uriStr.startsWith("https://") ||
            uriStr.startsWith("content://")
        ) {
            return false
        }
        return try {
            val ownedDir = File(context.filesDir, BOT_IMAGES_DIR).canonicalFile
            val rawPath = if (uriStr.startsWith("file://")) {
                Uri.parse(uriStr).path ?: return false
            } else {
                uriStr
            }
            val target = File(rawPath).canonicalFile
            val isOwned = target.path.startsWith(ownedDir.path + File.separator)
            if (isOwned && target.exists() && target.isFile) {
                target.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "deleteLocalBackgroundIfOwned skipped for $uriStr", e)
            false
        }
    }

    /**
     * Deletes orphaned WebP files inside `filesDir/bot_images/` that are no longer referenced
     * by any active bot's `chatBgUrl` or `avatarUrl`. Returns the number of deleted files.
     */
    fun pruneOrphanBackgrounds(context: Context, activeUrls: Set<String>): Int {
        return try {
            val ownedDir = File(context.filesDir, BOT_IMAGES_DIR)
            if (!ownedDir.exists() || !ownedDir.isDirectory) return 0
            val activeCanonicalPaths = activeUrls
                .filter { it.isNotBlank() && !isBase64DataUri(it) && !it.startsWith("http") && !it.startsWith("content://") }
                .mapNotNull { url ->
                    try {
                        val path = if (url.startsWith("file://")) Uri.parse(url).path else url
                        path?.let { File(it).canonicalPath }
                    } catch (_: Exception) {
                        null
                    }
                }
                .toSet()

            var deletedCount = 0
            ownedDir.listFiles()?.forEach { file ->
                if (file.isFile && file.canonicalPath !in activeCanonicalPaths) {
                    if (file.delete()) deletedCount++
                }
            }
            deletedCount
        } catch (e: Exception) {
            Log.w(TAG, "pruneOrphanBackgrounds failed", e)
            0
        }
    }
}
