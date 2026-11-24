package com.example

import com.example.book.BookBondSummaryHelper
import com.example.book.BookCharacterId
import com.example.book.BookReadState
import com.example.util.ResponseLengthTargetHelper
import com.example.util.TextWordCounter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V72MegaBatchUpgradeTest {

    @Test
    fun responseLengthTargetHelper_returnsExpectedTurkishTargets() {
        assertTrue(ResponseLengthTargetHelper.describeTarget("short").contains("60-120"))
        assertTrue(ResponseLengthTargetHelper.describeTarget("standard").contains("150-250"))
        assertTrue(ResponseLengthTargetHelper.describeTarget("detailed").contains("300-450"))
        assertTrue(ResponseLengthTargetHelper.describeTarget("novel").contains("500+"))
    }

    @Test
    fun bookBondSummaryHelper_findsStrongestUnlockedAlly() {
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 3,
            completedChapters = setOf(1, 2),
            currentNodeId = "ch3_n1",
            transcript = emptyList(),
            chapterDecisions = emptyMap(),
            bonds = mapOf(
                BookCharacterId.NATASHA to 62,
                BookCharacterId.STEVE to 35
            ),
            fatigue = 0,
            mystery = 0,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )
        val highlight = BookBondSummaryHelper.compute(state)
        assertEquals(BookCharacterId.NATASHA, highlight.strongestAlly)
        assertEquals(62, highlight.strongestScore)
        val label = BookBondSummaryHelper.formatCompact(highlight)
        assertTrue(label.contains("+62"))
    }

    @Test
    fun textWordCounter_countsWordsAndParagraphsAccurately() {
        val text = "Birinci paragraf burada başlar.\n\nİkinci paragraf burada devam eder."
        assertEquals(9, TextWordCounter.countWords(text))
        assertEquals(2, TextWordCounter.countParagraphs(text))
        assertTrue(TextWordCounter.formatCompact(text).startsWith("9 kelime"))
    }
}
