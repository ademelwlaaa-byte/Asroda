package com.example

import com.example.book.BookBranchDiscoveryHelper
import com.example.book.BookReadState
import com.example.util.CustomApiTemperatureAdvisor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V114MegaBatchUpgradeTest {

    @Test
    fun bookBranchDiscoveryHelper_summarizesBranchesAndFlags() {
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 5,
            completedChapters = setOf(1, 2, 3, 4),
            currentNodeId = "ch5_n1",
            transcript = emptyList(),
            chapterDecisions = emptyMap(),
            bonds = emptyMap(),
            fatigue = 20,
            mystery = 50,
            flags = mapOf("met_voss" to "true", "saved_pier" to "yes"),
            discoveredSecrets = emptySet(),
            discoveredBranches = setOf("b1", "b2", "b3"),
            isChapterCompleted = false
        )
        val summary = BookBranchDiscoveryHelper.compute(state)
        assertEquals(3, summary.branchCount)
        assertEquals(2, summary.flagCount)
        assertEquals("🧭 Rota Kaşifi (3 rota • 2 iz)", BookBranchDiscoveryHelper.formatCompact(summary))
    }

    @Test
    fun customApiTemperatureAdvisor_suggestsModelAppropriateTemperature() {
        assertTrue(CustomApiTemperatureAdvisor.recommendBadge("deepseek-r1").orEmpty().contains("0.60"))
        assertTrue(CustomApiTemperatureAdvisor.recommendBadge("gemini-2.5-flash").orEmpty().contains("0.75"))
        assertTrue(CustomApiTemperatureAdvisor.recommendBadge("claude-3.7-sonnet").orEmpty().contains("0.85"))
        assertNull(CustomApiTemperatureAdvisor.recommendBadge("custom-local-v1"))
    }
}
