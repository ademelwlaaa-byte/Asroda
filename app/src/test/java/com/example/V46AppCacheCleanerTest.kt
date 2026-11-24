package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.AppCacheCleaner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V46AppCacheCleanerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun inspectAndClearCaches_measuresAndFreesDisposableFiles() {
        val ttsDir = File(context.cacheDir, "tts_audio_cache").apply { mkdirs() }
        val imgDir = File(context.cacheDir, "image_cache").apply { mkdirs() }
        File(ttsDir, "sample.mp3").writeBytes(ByteArray(2048))
        File(imgDir, "thumb.webp").writeBytes(ByteArray(4096))

        val before = AppCacheCleaner.inspectCache(context)
        assertTrue(before.ttsBytes >= 2048L)
        assertTrue(before.imageBytes >= 4096L)

        val freed = AppCacheCleaner.clearDisposableCaches(context)
        assertTrue("Clearing caches should free at least 6144 bytes", freed >= 6144L)
        assertEquals(0L, AppCacheCleaner.inspectCache(context).totalBytes)
    }

    @Test
    fun formatBytes_formatsUnitsCleanly() {
        assertEquals("512 B", AppCacheCleaner.formatBytes(512L))
        assertEquals("2.0 KB", AppCacheCleaner.formatBytes(2048L))
        assertEquals("1.50 MB", AppCacheCleaner.formatBytes((1.5 * 1024 * 1024).toLong()))
    }
}
