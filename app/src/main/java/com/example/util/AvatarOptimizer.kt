package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.min

object AvatarOptimizer {

    private const val TARGET_AVATAR_SIZE = 256 // 256x256 px: Avatarlar için kristal netlik + minimum boyut
    private const val WEBP_QUALITY = 82        // Görsel kalite kaybı olmadan maksimum sıkıştırma

    private const val MAX_BG_WIDTH = 1080      // Tam ekran sohbet arka planı maksimum genişlik
    private const val MAX_BG_HEIGHT = 2400     // Tam ekran sohbet arka planı maksimum yükseklik
    private const val BG_WEBP_QUALITY = 85     // Arka planlar için sıfır görünür kayıplı WebP kalitesi

    /**
     * Galeriden seçilen yüksek boyutlu fotoğrafı OOM (OutOfMemory) hatası vermeden okur,
     * EXIF yönünü düzeltir, 256x256 kare kırpar, WebP (%82) sıkıştırır ve ~15 KB'lık
     * hafif Base64 Data URI string olarak döndürür.
     */
    suspend fun processAvatarToMicroWebPBase64(
        context: Context,
        imageUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        var sampledBitmap: Bitmap? = null
        var rotatedBitmap: Bitmap? = null
        var croppedSquare: Bitmap? = null
        var finalBitmap: Bitmap? = null
        try {
            // 1. Aşama: Görseli belleğe yüklemeden sadece boyutlarını oku
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return@withContext Result.failure(IllegalArgumentException("Geçersiz görsel dosyası"))
            }

            // 2. Aşama: Belleği korumak için inSampleSize hesapla
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(
                    boundsOptions.outWidth,
                    boundsOptions.outHeight,
                    TARGET_AVATAR_SIZE * 2,
                    TARGET_AVATAR_SIZE * 2
                )
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            sampledBitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext Result.failure(IllegalStateException("Görsel çözümlenemedi"))

            // 3. Aşama: Kamera/Galeri EXIF döndürme açısını düzelt
            rotatedBitmap = fixExifOrientation(context, imageUri, sampledBitmap)
            if (rotatedBitmap !== sampledBitmap && !sampledBitmap.isRecycled) {
                sampledBitmap.recycle()
                sampledBitmap = null
            }

            // 4. Aşama: Merkezden 1:1 kare kırp ve 256x256 boyutuna ölçekle
            croppedSquare = centerCropSquare(rotatedBitmap)
            if (croppedSquare !== rotatedBitmap && !rotatedBitmap.isRecycled) {
                rotatedBitmap.recycle()
                rotatedBitmap = null
            }

            finalBitmap = Bitmap.createScaledBitmap(
                croppedSquare,
                TARGET_AVATAR_SIZE,
                TARGET_AVATAR_SIZE,
                true
            )
            if (finalBitmap !== croppedSquare && !croppedSquare.isRecycled) {
                croppedSquare.recycle()
                croppedSquare = null
            }

            // 5. Aşama: WebP formatında sıkıştır (~12-18 KB)
            val outputStream = ByteArrayOutputStream()
            val compressFormat = webpLossyFormat()

            finalBitmap.compress(compressFormat, WEBP_QUALITY, outputStream)
            val webpBytes = outputStream.toByteArray()
            if (webpBytes.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("Görsel sıkıştırılamadı"))
            }
            val base64String = Base64.encodeToString(webpBytes, Base64.NO_WRAP)

            Result.success("data:image/webp;base64,$base64String")
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            listOf(finalBitmap, croppedSquare, rotatedBitmap, sampledBitmap).distinct().forEach { bmp ->
                try {
                    if (bmp != null && !bmp.isRecycled) bmp.recycle()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Özel sohbet arka planı seçildiğinde OOM korumalı (inJustDecodeBounds + inSampleSize + RGB_565)
     * olarak okur, EXIF yönünü düzeltir, en fazla 1080x2400 boyutuna ölçekler ve WebP (%85) olarak
     * uygulama içi dizine kaydeder.
     */
    suspend fun processBackgroundToWebPFile(
        context: Context,
        imageUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        var sampledBitmap: Bitmap? = null
        var rotatedBitmap: Bitmap? = null
        var finalBitmap: Bitmap? = null
        try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return@withContext Result.failure(IllegalArgumentException("Geçersiz arka plan görseli"))
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(
                    boundsOptions.outWidth,
                    boundsOptions.outHeight,
                    MAX_BG_WIDTH,
                    MAX_BG_HEIGHT
                )
                // Tam ekran opak arka planlar için RGB_565 ile RAM tüketimini %50 azalt
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            sampledBitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext Result.failure(IllegalStateException("Arka plan görseli çözümlenemedi"))

            rotatedBitmap = fixExifOrientation(context, imageUri, sampledBitmap)
            if (rotatedBitmap !== sampledBitmap && !sampledBitmap.isRecycled) {
                sampledBitmap.recycle()
                sampledBitmap = null
            }

            finalBitmap = if (rotatedBitmap.width > MAX_BG_WIDTH || rotatedBitmap.height > MAX_BG_HEIGHT) {
                val scale = min(
                    MAX_BG_WIDTH.toFloat() / rotatedBitmap.width.toFloat(),
                    MAX_BG_HEIGHT.toFloat() / rotatedBitmap.height.toFloat()
                )
                val targetW = (rotatedBitmap.width * scale).toInt().coerceAtLeast(1)
                val targetH = (rotatedBitmap.height * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(rotatedBitmap, targetW, targetH, true).also {
                    if (it !== rotatedBitmap && !rotatedBitmap.isRecycled) {
                        rotatedBitmap.recycle()
                        rotatedBitmap = null
                    }
                }
            } else {
                rotatedBitmap
            }

            val imagesDir = File(context.filesDir, "bot_images").apply {
                if (!exists()) mkdirs()
            }
            val filename = "bg_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.webp"
            val outputFile = File(imagesDir, filename)

            val bmpToSave = finalBitmap
                ?: return@withContext Result.failure(IllegalStateException("Görsel işlenemedi."))
            FileOutputStream(outputFile).use { out ->
                bmpToSave.compress(webpLossyFormat(), BG_WEBP_QUALITY, out)
            }

            Result.success(Uri.fromFile(outputFile).toString())
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            listOf(finalBitmap, rotatedBitmap, sampledBitmap).distinct().forEach { bmp ->
                try {
                    if (bmp != null && !bmp.isRecycled) bmp.recycle()
                } catch (_: Exception) {}
            }
        }
    }

    private fun webpLossyFormat(): Bitmap.CompressFormat {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
    }

    internal fun calculateInSampleSize(
        rawWidth: Int,
        rawHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            val halfHeight = rawHeight / 2
            val halfWidth = rawWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun centerCropSquare(source: Bitmap): Bitmap {
        val dimension = min(source.width, source.height)
        val xOffset = (source.width - dimension) / 2
        val yOffset = (source.height - dimension) / 2
        return Bitmap.createBitmap(source, xOffset, yOffset, dimension, dimension)
    }

    internal fun fixExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (degrees == 0f) {
                bitmap
            } else {
                val matrix = Matrix().apply { postRotate(degrees) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        } catch (_: Exception) {
            bitmap
        }
    }
}
