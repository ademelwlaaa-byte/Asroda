package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.book.BookContentPackManager
import com.example.book.BookPackKind
import com.example.data.local.MessageEntity
import com.example.ui.components.resolvePreviousEmotionStateOnSettingsSave
import com.example.ui.screens.computeLastAssistantReplyIndex
import com.example.ui.screens.formatBubbleClockTime
import com.example.ui.screens.isMessageAutoCollapsible
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ComposePerfAndBookDlcOverhaulTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        BookContentPackManager.resetForTesting()
        File(context.filesDir, "book_packs").deleteRecursively()
    }

    @After
    fun tearDown() {
        BookContentPackManager.resetForTesting()
        File(context.filesDir, "book_packs").deleteRecursively()
    }

    private fun buildZipBytes(entries: List<Pair<String, ByteArray>>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            for ((name, data) in entries) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(data)
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    private fun assertNoStagingOrBackupDirsLeft(context: Context) {
        val root = BookContentPackManager.packsRootDir(context)
        val leftovers = root.listFiles()?.filter {
            it.name.contains("_staging_") || it.name.contains("_backup_")
        }.orEmpty()
        assertTrue("Expected zero leftover staging/backup dirs, found: ${leftovers.map { it.name }}", leftovers.isEmpty())
    }

    // =========================================================================
    // 1. Atomic staging swap: corrupt update MUST preserve existing working pack
    // =========================================================================
    @Test
    fun bookContentPackManager_atomicStagingSwap_preservesOldPackOnCorruptZipAndSwapsCleanlyOnValidUpdate() {
        val packId = "test_voice_pack_atomic"
        val v1VoiceBytes = ByteArray(1024) { 0x11 }
        val v1Zip = buildZipBytes(
            listOf(
                "manifest.json" to """{"title":"V1 Dublaj","subtitle":"Sürüm 1","kind":"voice"}""".toByteArray(),
                "voices/hero_v1.mp3" to v1VoiceBytes
            )
        )

        val installedV1 = BookContentPackManager.extractZipIntoPackInternal(
            context = context,
            packId = packId,
            fallbackTitle = "Fallback",
            fallbackSubtitle = "Fallback",
            kind = BookPackKind.VOICE_DUB_PACK,
            sourceUrl = null,
            rawZipStream = ByteArrayInputStream(v1Zip)
        )
        assertEquals("V1 Dublaj", installedV1.title)
        assertNotNull(BookContentPackManager.findDownloadedVoiceFile(context, "hero_v1"))
        assertNoStagingOrBackupDirsLeft(context)

        // Attempt to update the same pack with corrupt/non-ZIP bytes: MUST fail AND keep V1 intact!
        val corruptBytes = "NOT_A_VALID_ZIP_STREAM_CORRUPTED_DATA".toByteArray()
        try {
            BookContentPackManager.extractZipIntoPackInternal(
                context = context,
                packId = packId,
                fallbackTitle = "Corrupt",
                fallbackSubtitle = "Corrupt",
                kind = BookPackKind.VOICE_DUB_PACK,
                sourceUrl = null,
                rawZipStream = ByteArrayInputStream(corruptBytes)
            )
            fail("Expected IllegalArgumentException for corrupt/empty ZIP")
        } catch (_: IllegalArgumentException) {
            // Expected
        }

        val survivingV1 = BookContentPackManager.findDownloadedVoiceFile(context, "hero_v1")
        assertNotNull("Existing working V1 voice file MUST survive a failed/corrupt update!", survivingV1)
        assertEquals(1024L, survivingV1!!.length())
        assertNoStagingOrBackupDirsLeft(context)

        // Now install a valid V2 pack: atomic swap replaces V1 cleanly
        val v2VoiceBytes = ByteArray(1536) { 0x22 }
        val v2Zip = buildZipBytes(
            listOf(
                "manifest.json" to """{"title":"V2 Dublaj","subtitle":"Sürüm 2","kind":"voice"}""".toByteArray(),
                "voices/hero_v2.mp3" to v2VoiceBytes
            )
        )
        val installedV2 = BookContentPackManager.extractZipIntoPackInternal(
            context = context,
            packId = packId,
            fallbackTitle = "Fallback",
            fallbackSubtitle = "Fallback",
            kind = BookPackKind.VOICE_DUB_PACK,
            sourceUrl = null,
            rawZipStream = ByteArrayInputStream(v2Zip)
        )
        assertEquals("V2 Dublaj", installedV2.title)
        assertNull("Old V1 file should be replaced after atomic V2 swap", BookContentPackManager.findDownloadedVoiceFile(context, "hero_v1"))
        val newV2 = BookContentPackManager.findDownloadedVoiceFile(context, "hero_v2")
        assertNotNull(newV2)
        assertEquals(1536L, newV2!!.length())
        assertNoStagingOrBackupDirsLeft(context)
    }

    // =========================================================================
    // 2. ZIP-slip path traversal rejection
    // =========================================================================
    @Test
    fun bookContentPackManager_rejectsZipSlipTraversalWithoutWritingOutsideStaging() {
        val maliciousZip = buildZipBytes(
            listOf(
                "../../escaped_evil.txt" to "pwned".toByteArray()
            )
        )
        try {
            BookContentPackManager.extractZipIntoPackInternal(
                context = context,
                packId = "zipslip_pack",
                fallbackTitle = "Evil",
                fallbackSubtitle = "Evil",
                kind = BookPackKind.STORY_SEASON,
                sourceUrl = null,
                rawZipStream = ByteArrayInputStream(maliciousZip)
            )
            fail("Expected SecurityException on ZIP-slip traversal")
        } catch (_: SecurityException) {
            // Expected
        }
        assertFalse(File(context.filesDir, "escaped_evil.txt").exists())
        assertNoStagingOrBackupDirsLeft(context)
    }

    // =========================================================================
    // 3. ZIP-bomb entry count, single-entry byte cap, and total byte cap
    // =========================================================================
    @Test
    fun bookContentPackManager_enforcesZipBombEntryAndSizeCaps() {
        // A) Entry count cap
        val tooManyEntriesZip = buildZipBytes(
            (1..6).map { idx -> "file_$idx.txt" to "hello".toByteArray() }
        )
        try {
            BookContentPackManager.extractZipIntoPackInternal(
                context = context,
                packId = "bomb_entries",
                fallbackTitle = "Bomb",
                fallbackSubtitle = "Bomb",
                kind = BookPackKind.STORY_SEASON,
                sourceUrl = null,
                rawZipStream = ByteArrayInputStream(tooManyEntriesZip),
                maxEntries = 3
            )
            fail("Expected SecurityException when ZIP exceeds maxEntries")
        } catch (_: SecurityException) {
            // Expected
        }
        assertNoStagingOrBackupDirsLeft(context)

        // B) Single-entry byte cap
        val singleBigEntryZip = buildZipBytes(
            listOf("huge.bin" to ByteArray(4096) { 0x7A })
        )
        try {
            BookContentPackManager.extractZipIntoPackInternal(
                context = context,
                packId = "bomb_single",
                fallbackTitle = "Bomb",
                fallbackSubtitle = "Bomb",
                kind = BookPackKind.STORY_SEASON,
                sourceUrl = null,
                rawZipStream = ByteArrayInputStream(singleBigEntryZip),
                maxSingleEntryBytes = 1024L
            )
            fail("Expected SecurityException when entry exceeds maxSingleEntryBytes")
        } catch (_: SecurityException) {
            // Expected
        }
        assertNoStagingOrBackupDirsLeft(context)

        // C) Total extracted byte cap
        val totalBigZip = buildZipBytes(
            listOf(
                "a.bin" to ByteArray(600) { 1 },
                "b.bin" to ByteArray(600) { 2 },
                "c.bin" to ByteArray(600) { 3 }
            )
        )
        try {
            BookContentPackManager.extractZipIntoPackInternal(
                context = context,
                packId = "bomb_total",
                fallbackTitle = "Bomb",
                fallbackSubtitle = "Bomb",
                kind = BookPackKind.STORY_SEASON,
                sourceUrl = null,
                rawZipStream = ByteArrayInputStream(totalBigZip),
                maxTotalBytes = 1200L
            )
            fail("Expected SecurityException when ZIP exceeds maxTotalBytes")
        } catch (_: SecurityException) {
            // Expected
        }
        assertNoStagingOrBackupDirsLeft(context)
    }

    // =========================================================================
    // 4. Per-pack Mutex serialization
    // =========================================================================
    @Test
    fun bookContentPackManager_perPackMutexSerializesSamePackAndIsolatesDistinctPacks() = runBlocking {
        val m1 = BookContentPackManager.mutexForPack("Voice_Pack_1")
        val m1Alias = BookContentPackManager.mutexForPack("voice_pack_1")
        val m2 = BookContentPackManager.mutexForPack("voice_pack_2")
        assertSame("Same canonical pack ID must share one Mutex", m1, m1Alias)
        assertNotSame("Distinct packs must not block each other", m1, m2)

        val inCriticalSection = AtomicInteger(0)
        val maxConcurrent = AtomicInteger(0)

        val jobA = async {
            m1.withLock {
                val cur = inCriticalSection.incrementAndGet()
                maxConcurrent.accumulateAndGet(cur, ::maxOf)
                delay(40)
                inCriticalSection.decrementAndGet()
            }
        }
        val jobB = async {
            m1Alias.withLock {
                val cur = inCriticalSection.incrementAndGet()
                maxConcurrent.accumulateAndGet(cur, ::maxOf)
                delay(40)
                inCriticalSection.decrementAndGet()
            }
        }
        jobA.await()
        jobB.await()
        assertEquals("Per-pack Mutex must strictly prevent concurrent overlap", 1, maxConcurrent.get())
    }

    // =========================================================================
    // 5. O(1) cached file index & invalidation on install/delete
    // =========================================================================
    @Test
    fun bookContentPackManager_cachedFileIndexServesO1LookupsAndInvalidatesOnDelete() = runBlocking {
        val packId = "indexed_pack_1"
        val zipBytes = buildZipBytes(
            listOf(
                "voices/natasha_line_1.mp3" to ByteArray(1024) { 0x33 },
                "media/amb_bosphorus.webp" to ByteArray(1024) { 0x44 }
            )
        )
        BookContentPackManager.extractZipIntoPackInternal(
            context = context,
            packId = packId,
            fallbackTitle = "Index Pack",
            fallbackSubtitle = "Index Test",
            kind = BookPackKind.VOICE_DUB_PACK,
            sourceUrl = null,
            rawZipStream = ByteArrayInputStream(zipBytes)
        )

        val buildsBefore = BookContentPackManager.indexBuildCountForTest()
        repeat(20) {
            assertNotNull(BookContentPackManager.findDownloadedVoiceFile(context, "natasha_line_1"))
            assertNotNull(BookContentPackManager.findDownloadedMediaFile(context, "amb_bosphorus"))
        }
        val buildsAfter = BookContentPackManager.indexBuildCountForTest()
        assertEquals("40 repeated lookups must trigger exactly 1 index build (O(1) cache)", buildsBefore + 1, buildsAfter)

        // Delete pack -> must invalidate index so subsequent lookups return null cleanly
        val deleted = BookContentPackManager.deletePack(context, packId)
        assertTrue(deleted)
        assertNull(BookContentPackManager.findDownloadedVoiceFile(context, "natasha_line_1"))
        assertNull(BookContentPackManager.findDownloadedMediaFile(context, "amb_bosphorus"))
    }

    // =========================================================================
    // 6. BotSettingsModal previousEmotionState preservation helper
    // =========================================================================
    @Test
    fun botSettingsModal_resolvePreviousEmotionState_preservesHistoryUnlessEmotionChanged() {
        val oldPrev = """{"mood":"temkinli","affection":42}"""
        val current = """{"mood":"romantik","affection":78}"""
        val edited = """{"mood":"öfkeli","affection":30}"""

        // Saving unrelated metadata (name/scenario/avatar) must NOT clobber previousEmotionState
        val preserved = resolvePreviousEmotionStateOnSettingsSave(
            originalEmotionJson = current,
            originalPreviousEmotionJson = oldPrev,
            updatedEmotionJson = current
        )
        assertEquals(oldPrev, preserved)

        // Explicitly changing emotion state records the pre-edit emotionState as previousEmotionState
        val updatedPrev = resolvePreviousEmotionStateOnSettingsSave(
            originalEmotionJson = current,
            originalPreviousEmotionJson = oldPrev,
            updatedEmotionJson = edited
        )
        assertEquals(current, updatedPrev)
    }

    // =========================================================================
    // 7. ChatMessageBubbles O(1) assistant-reply lookup, auto-collapse & clock format helpers
    // =========================================================================
    @Test
    fun chatMessageBubbles_precomputedAssistantReplyIndexMatchesLinearScanAndHelpersWork() {
        val messages = listOf(
            MessageEntity(id = "1", botId = "b1", role = "user", text = "Selam", timestamp = 1_000L),
            MessageEntity(id = "2", botId = "b1", role = "assistant", text = "Hoş geldin.", timestamp = 2_000L),
            MessageEntity(id = "3", botId = "b1", role = "user", text = "Nasılsın?", timestamp = 3_000L),
            MessageEntity(id = "4", botId = "b1", role = "assistant", text = "   ", timestamp = 4_000L), // blank placeholder
            MessageEntity(id = "5", botId = "b1", role = "user", text = "Cevap bekliyorum", timestamp = 5_000L)
        )

        val lastNonBlankAssistantIdx = computeLastAssistantReplyIndex(messages)
        assertEquals(1, lastNonBlankAssistantIdx)

        // O(1) check `index < lastNonBlankAssistantIdx` must match legacy O(N) `drop(index + 1).any(...)` for every index
        for (idx in messages.indices) {
            val expected = messages.drop(idx + 1).any { it.role == "assistant" && it.text.isNotBlank() }
            assertEquals("Mismatch at index $idx", expected, idx < lastNonBlankAssistantIdx)
        }

        // Auto-collapse helper
        val longText = "a".repeat(185)
        val multiLineText = "1\n2\n3\n4\n5"
        assertFalse(isMessageAutoCollapsible(isOldMessage = false, cleanText = longText))
        assertFalse(isMessageAutoCollapsible(isOldMessage = true, cleanText = "Kısa mesaj"))
        assertTrue(isMessageAutoCollapsible(isOldMessage = true, cleanText = longText))
        assertTrue(isMessageAutoCollapsible(isOldMessage = true, cleanText = multiLineText))

        // Cached clock formatter
        assertEquals("", formatBubbleClockTime(0L))
        val formatted = formatBubbleClockTime(1_700_000_000_000L)
        assertEquals(5, formatted.length)
        assertEquals(':', formatted[2])
    }
}
