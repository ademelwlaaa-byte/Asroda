package com.example

import com.example.book.BookReadState
import com.example.book.BookTranscriptStatsHelper
import com.example.util.CustomApiHeaderSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V89MegaBatchUpgradeTest {

    @Test
    fun customApiHeaderSanitizer_blocksCrLfInjectionAndStripsInvalidNameChars() {
        val safe = CustomApiHeaderSanitizer.buildSafeHeaderMap(
            mapOf(
                "X-Custom Header!" to "val1\r\nEvil: true",
                "HTTP-Referer" to "  https://asroda.app  ",
                "   " to "ignored"
            )
        )
        assertEquals(2, safe.size)
        assertTrue(safe.containsKey("X-CustomHeader"))
        assertFalse(safe["X-CustomHeader"]!!.contains("\n"))
        assertEquals("https://asroda.app", safe["HTTP-Referer"])
    }

    @Test
    fun bookTranscriptStatsHelper_handlesEmptyStateGracefully() {
        val emptyState = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 1,
            completedChapters = emptySet(),
            currentNodeId = "ch1_n1",
            transcript = emptyList(),
            chapterDecisions = emptyMap(),
            bonds = emptyMap(),
            fatigue = 0,
            mystery = 0,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )
        val stats = BookTranscriptStatsHelper.compute(emptyState)
        assertEquals(0, stats.totalWords)
        assertEquals("Henüz okuma kaydı yok", BookTranscriptStatsHelper.formatCompact(stats))
    }
}
