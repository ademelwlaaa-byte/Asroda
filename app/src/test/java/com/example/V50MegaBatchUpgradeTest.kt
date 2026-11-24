package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.book.BookChronicleExporter
import com.example.book.BookReadState
import com.example.book.RecordedDecision
import com.example.util.EmotionalTtsProsodyEngine
import com.example.util.StoryBookmarkStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V50MegaBatchUpgradeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun neuralTtsBatching_keepsFirstBatchCompactForFastStartAndModulatesTurkishVocalCues() {
        val story = "*Yavaşça kulağına eğilip fısıldar.* \"Bunu kimse bilmemeli.\" " +
            "*Birden ayağa kalkıp bağırır!* \"Hemen kuleyi terk edin?!\" " +
            (1..8).joinToString(" ") { "Uzun edebi anlatım cümlesi numara $it burada devam ediyor." }

        val segments = EmotionalTtsProsodyEngine.buildProsodySegments(
            rawText = story,
            readActions = true,
            emotionalProsodyEnabled = true
        )
        assertTrue(segments.size >= 4)
        val whisperSeg = segments.first { it.text.contains("fısıldar", ignoreCase = true) }
        val shoutSeg = segments.first { it.text.contains("bağırır", ignoreCase = true) }
        assertTrue("Whisper segment should be slower than shout segment", whisperSeg.ratePercent < shoutSeg.ratePercent)
        assertTrue("Whisper segment should be lower pitch than shout segment", whisperSeg.pitchHz < shoutSeg.pitchHz)

        val batches = EmotionalTtsProsodyEngine.batchSegmentsForNeural(
            segments = segments,
            maxCharsPerBatch = 650,
            firstBatchMaxChars = 220
        )
        assertTrue(batches.size >= 2)
        val firstBatchLen = batches.first().sumOf { it.text.length }
        assertTrue("First batch should stay compact (<=220 chars), got $firstBatchLen", firstBatchLen <= 220)
    }

    @Test
    fun bookChronicleExporter_formatsDecisionsAndSecretsIntoCleanMarkdown() {
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 5,
            completedChapters = setOf(1, 2, 3, 4),
            currentNodeId = "ch5_n1",
            transcript = emptyList(),
            chapterDecisions = mapOf(
                2 to listOf(
                    RecordedDecision(
                        chapterNumber = 2,
                        nodeId = "ch2_n3",
                        decisionTitle = "Güven Testi",
                        choiceId = "trust_natasha",
                        selectedOption = "Natasha'ya gerçeği söyle",
                        impactText = "Güven bağı güçlendi",
                        bondDeltas = emptyMap(),
                        fatigueDelta = 0,
                        mysteryDelta = 0,
                        secretUnlocked = null,
                        branchTag = null,
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "HIGH"
                    )
                )
            ),
            bonds = emptyMap(),
            fatigue = 10,
            mystery = 20,
            flags = emptyMap(),
            discoveredSecrets = setOf("Kızıl Odanın gizli koordinatları"),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )
        val md = BookChronicleExporter.formatDossierMarkdown(state)
        assertTrue(md.contains("Kozmik Sürükleniş"))
        assertTrue(md.contains("Natasha'ya gerçeği söyle"))
        assertTrue(md.contains("Güven bağı güçlendi"))
        assertTrue(md.contains("Kızıl Odanın gizli koordinatları"))
    }

    @Test
    fun storyBookmarkStore_addsDeduplicatesRemovesAndClearsCleanly() {
        val botId = "bot_bm_${System.nanoTime()}"
        try {
            val list1 = StoryBookmarkStore.addBookmark(context, botId, "msg_1", "İlk Karşılaşma", "Yağmur altında...")
            assertEquals(1, list1.size)
            assertEquals("İlk Karşılaşma", list1.first().title)

            // Same messageId updates title instead of duplicating
            val list2 = StoryBookmarkStore.addBookmark(context, botId, "msg_1", "Kulede Karşılaşma", "Yağmur altında...")
            assertEquals(1, list2.size)
            assertEquals("Kulede Karşılaşma", list2.first().title)

            val afterRemove = StoryBookmarkStore.removeBookmark(context, botId, list2.first().id)
            assertTrue(afterRemove.isEmpty())
        } finally {
            StoryBookmarkStore.clearAllForBot(context, botId)
        }
    }
}
