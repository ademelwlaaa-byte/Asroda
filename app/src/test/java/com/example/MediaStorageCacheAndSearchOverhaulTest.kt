package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MessageEntity
import com.example.util.ApiKeyFormatValidator
import com.example.util.AppCacheCleaner
import com.example.util.AppUpdateManager
import com.example.util.BackupHealthChecker
import com.example.util.ChatSearchHelper
import com.example.util.ChatStoryStats
import com.example.util.ImageStorageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MediaStorageCacheAndSearchOverhaulTest {

    @Test
    fun test1_appUpdateManager_enforcesHttpsRedirectsAndValidatesApkZipHeader() {
        val validRedirect = AppUpdateManager.resolveAndValidateHttpsRedirect(
            "https://github.com/ademelwlaaa-byte/Asroda-Releases/releases/download/v12/app.apk",
            "/cdn/asroda-v12.apk"
        )
        assertEquals("https://github.com/cdn/asroda-v12.apk", validRedirect)

        var downgradeBlocked = false
        try {
            AppUpdateManager.resolveAndValidateHttpsRedirect(
                "https://github.com/releases/app.apk",
                "http://evil.example.com/fake.apk"
            )
        } catch (_: SecurityException) {
            downgradeBlocked = true
        }
        assertTrue("HTTP downgrade redirect MUST be rejected with SecurityException", downgradeBlocked)

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val validApk = File(ctx.cacheDir, "valid_test.apk").apply {
            writeBytes(byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x00, 0x01))
        }
        val htmlPage = File(ctx.cacheDir, "error_page.apk").apply {
            writeText("<!DOCTYPE html><html><body>502 Bad Gateway</body></html>")
        }
        try {
            assertTrue(AppUpdateManager.hasValidApkZipHeader(validApk))
            assertFalse("HTML error page must fail APK ZIP header validation", AppUpdateManager.hasValidApkZipHeader(htmlPage))
        } finally {
            validApk.delete()
            htmlPage.delete()
        }
    }

    @Test
    fun test2_chatSearchHelperAndStoryStats_stripInternalThinkAndStateJsonTags() {
        val messages = listOf(
            MessageEntity(
                id = "m1",
                botId = "b1",
                role = "assistant",
                text = "<think>secret_internal_token affection trust</think>İstanbul gecesinde yağmur yağıyordu. [[STATE_JSON {\"affection\":88,\"trust\":75}]]",
                timestamp = 1000L
            )
        )

        // Hidden internal tags must NOT match search queries
        assertTrue(ChatSearchHelper.findMatches(messages, "secret_internal").isEmpty())
        assertTrue(ChatSearchHelper.findMatches(messages, "STATE_JSON").isEmpty())

        // Visible Turkish text matches with diacritic folding & clean snippet
        val matches = ChatSearchHelper.findMatches(messages, "yagmur")
        assertEquals(1, matches.size)
        assertFalse(matches.first().snippet.contains("STATE_JSON"))
        assertFalse(matches.first().snippet.contains("secret_internal"))

        // ChatStoryStats counts ONLY visible literary words (4 words: "İstanbul gecesinde yağmur yağıyordu.")
        val stats = ChatStoryStats.compute(messages)
        assertEquals(4, stats.assistantWordCount)
        assertEquals(4, stats.totalWordCount)
    }

    @Test
    fun test3_backupHealthChecker_detectsCustomApiKeyCustomProvidersAndRejectsEmptyObject() {
        assertFalse("Empty JSON object {} is not a valid Asroda backup", BackupHealthChecker.inspectJson("{}").isValidJson)

        val withGeminiKey = """{"version":2,"bots":[{"id":"b1"}],"messages":[],"settings":{"customApiKey":"AIzaSyTest123456789012345"}}"""
        val prev1 = BackupHealthChecker.inspectJson(withGeminiKey)
        assertTrue(prev1.isValidJson)
        assertTrue("customApiKey (Gemini) must be detected as plaintext key", prev1.includesPlaintextKeys)

        val withCustomProviderKey = """{"version":2,"bots":[],"messages":[],"settings":{},"customProviders":[{"label":"Proxy","apiKeyEncrypted":"sk-live-1234567890abcdef"}]}"""
        val prev2 = BackupHealthChecker.inspectJson(withCustomProviderKey)
        assertTrue(prev2.isValidJson)
        assertTrue("Plaintext custom provider key must be detected", prev2.includesPlaintextKeys)

        val maskedBackup = """{"version":2,"bots":[{"id":"b1"}],"messages":[],"settings":{"customApiKey":""},"customProviders":[{"label":"Proxy","apiKeyEncrypted":""}]}"""
        val prev3 = BackupHealthChecker.inspectJson(maskedBackup)
        assertTrue(prev3.isValidJson)
        assertFalse(prev3.includesPlaintextKeys)
    }

    @Test
    fun test4_apiKeyValidatorCacheCleanerAndImageStoragePathGuard() {
        assertEquals(
            "sk-abc1234567890",
            ApiKeyFormatValidator.sanitizeKey("\uFEFF  \"Bearer\u00A0sk-abc\u200B1234567890\" \n")
        )

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        // Path traversal outside filesDir/bot_images MUST NEVER be deleted
        val outsideFile = File(ctx.filesDir, "important_user_file.txt").apply { writeText("keep") }
        try {
            val deleted = ImageStorageManager.deleteLocalBackgroundIfOwned(
                ctx,
                File(File(ctx.filesDir, "bot_images"), "../important_user_file.txt").path
            )
            assertFalse("Path traversal delete must be rejected", deleted)
            assertTrue("Outside file must remain intact", outsideFile.exists())
        } finally {
            outsideFile.delete()
        }

        // AppCacheCleaner inspects & clears cleanly without crashing
        val dummyCache = File(File(ctx.cacheDir, "tts_audio_cache").apply { mkdirs() }, "sample.mp3").apply {
            writeBytes(ByteArray(2048) { 1 })
        }
        val report = AppCacheCleaner.inspectCache(ctx)
        assertTrue(report.ttsBytes >= 2048L)
        val freed = AppCacheCleaner.clearDisposableCaches(ctx)
        assertTrue(freed >= 2048L)
        assertFalse(dummyCache.exists())
    }
}
