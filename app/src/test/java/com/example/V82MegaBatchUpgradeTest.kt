package com.example

import com.example.book.BookChapterFilterHelper
import com.example.book.BookReadState
import com.example.data.managers.ProviderFailureType
import com.example.data.managers.ProviderHealthState
import com.example.util.ApiDiagnosticsFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V82MegaBatchUpgradeTest {

    @Test
    fun bookChapterFilterHelper_computesCompletedAndRemainingCounts() {
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 4,
            completedChapters = setOf(1, 2, 3),
            currentNodeId = "ch4_n1",
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
        val counts = BookChapterFilterHelper.computeCounts(state, totalChapters = 10)
        assertEquals(10, counts.total)
        assertEquals(3, counts.completed)
        assertEquals(7, counts.remaining)
        assertEquals("Tümü (10) • Tamamlanan (3) • Kalan (7)", BookChapterFilterHelper.formatCountsLine(counts))
    }

    @Test
    fun apiDiagnosticsFormatter_usesHumanFriendlyTurkishCooldownTimer() {
        val now = 1_000_000L
        val report = ApiDiagnosticsFormatter.formatReport(
            appVersion = "8.2",
            healthMap = mapOf(
                "groq" to ProviderHealthState(
                    providerKey = "groq",
                    cooldownUntilMs = now + 135_000L,
                    lastFailureType = ProviderFailureType.RATE_LIMIT
                )
            ),
            logs = emptyList(),
            nowMs = now
        )
        assertTrue(report.contains("2 dk 15 sn"))
    }
}
