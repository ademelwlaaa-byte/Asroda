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
            val sampled = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                ?: return@withContext null

            val rotated = AvatarOptimizer.fixExifOrientation(context, Uri.fromFile(file), sampled)
            if (rotated !== sampled) sampled.recycle()

            val dimension = min(rotated.width, rotated.height)
            val xOffset = (rotated.width - dimension) / 2
            val yOffset = (rotated.height - dimension) / 2
            val square = Bitmap.createBitmap(rotated, xOffset, yOffset, dimension, dimension)
            if (square !== rotated) rotated.recycle()

            val finalBitmap = Bitmap.createScaledBitmap(square, TARGET_AVATAR_SIZE, TARGET_AVATAR_SIZE, true)
            if (finalBitmap !== square) square.recycle()

            val out = ByteArrayOutputStream()
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            finalBitmap.compress(format, WEBP_QUALITY, out)
            finalBitmap.recycle()

            val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            "data:image/webp;base64,$base64"
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
     * Infrastructure Helper: Prepares image for remote Cloud Storage.
     */
    suspend fun uploadToCloudStorage(
        localUriOrPath: String,
        cloudEndpointUrl: String? = null
    ): String = withContext(Dispatchers.IO) {
        if (localUriOrPath.startsWith("http://") || localUriOrPath.startsWith("https://")) {
            return@withContext localUriOrPath
        }
        localUriOrPath
    }
}
