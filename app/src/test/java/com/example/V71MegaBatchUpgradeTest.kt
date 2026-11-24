package com.example

import com.example.util.ChatSessionTimerHelper
import com.example.util.EmotionalTtsProsodyEngine
import com.example.util.MemoryFactDeduplicator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V71MegaBatchUpgradeTest {

    @Test
    fun emotionalTtsProsodyEngine_preservesTurkishAbbreviationsInSegments() {
        val segments = EmotionalTtsProsodyEngine.buildProsodySegments(
            rawText = "Dr. Voss hızla kapıyı açtı. Prof. Banner sessizce baktı.",
            readActions = true,
            emotionalProsodyEnabled = true
        )
        assertEquals(2, segments.size)
        assertTrue(segments[0].text.startsWith("Dr. Voss"))
        assertTrue(segments[1].text.startsWith("Prof. Banner"))
    }

    @Test
    fun memoryFactDeduplicator_collapsesTurkishDiacriticAndPunctuationDuplicates() {
        assertTrue(MemoryFactDeduplicator.isNearDuplicate("Aiden mavi gözlüdür.", "aiden mavi gozludur!"))
        assertFalse(MemoryFactDeduplicator.isNearDuplicate("Aiden İstanbul'da doğdu", "Aiden Ankara'da doğdu"))

        val deduped = MemoryFactDeduplicator.deduplicate(
            listOf("Soğuk kule kuzeydedir.", "soguk kule kuzeydedir!", "Işık kılıcı gümüştür")
        )
        assertEquals(2, deduped.size)
        assertEquals("Soğuk kule kuzeydedir.", deduped[0])
        assertEquals("Işık kılıcı gümüştür", deduped[1])
    }

    @Test
    fun chatSessionTimerHelper_formatsElapsedTimeAndWpm() {
        assertEquals("45 sn", ChatSessionTimerHelper.formatElapsedTurkish(45_000L))
        assertEquals("12 dk", ChatSessionTimerHelper.formatElapsedTurkish(12 * 60_000L))
        assertEquals("1 sa 25 dk", ChatSessionTimerHelper.formatElapsedTurkish((85 * 60_000L)))
        assertEquals(100, ChatSessionTimerHelper.wordsPerMinute(200, 120_000L))
    }
}
