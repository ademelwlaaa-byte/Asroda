package com.example

import com.example.book.BookFatigueAlertHelper
import com.example.book.BookReadState
import com.example.util.CustomApiModelContextEstimator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V111MegaBatchUpgradeTest {

    @Test
    fun customApiModelContextEstimator_detectsMajorModelContextWindows() {
        assertEquals("📚 1M bağlam", CustomApiModelContextEstimator.estimateContextLabel("gemini-2.5-pro"))
        assertEquals("📚 200K bağlam", CustomApiModelContextEstimator.estimateContextLabel("anthropic/claude-3.7-sonnet"))
        assertEquals("📚 128K bağlam", CustomApiModelContextEstimator.estimateContextLabel("deepseek-chat"))
        assertEquals("📚 32K bağlam", CustomApiModelContextEstimator.estimateContextLabel("gemma-2-9b-it"))
        assertNull(CustomApiModelContextEstimator.estimateContextLabel("my-private-custom-model"))
    }

    @Test
    fun bookFatigueAlertHelper_formatsFatigueAndMysteryTiers() {
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 4,
            completedChapters = setOf(1, 2, 3),
            currentNodeId = "ch4_n2",
            transcript = emptyList(),
            chapterDecisions = emptyMap(),
            bonds = emptyMap(),
            fatigue = 85,
            mystery = 72,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )
        val ribbon = BookFatigueAlertHelper.formatStatusRibbon(state)
        assertTrue(ribbon.contains("Kritik Yorgunluk (85)"))
        assertTrue(ribbon.contains("Yüksek Gizem (72)"))
        assertEquals("⚡ Dinç (10)", BookFatigueAlertHelper.fatigueBadge(10))
        assertEquals("🕯️ Derinleşen Sır (40)", BookFatigueAlertHelper.mysteryBadge(40))
    }
}
