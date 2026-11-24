package com.example

import com.example.book.BookBondSummaryHelper
import com.example.book.BookCharacterId
import com.example.book.BookReadState
import com.example.util.CustomApiUrlValidationHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V106MegaBatchUpgradeTest {

    @Test
    fun customApiUrlValidationHelper_acceptsValidAndRejectsMalformedUrls() {
        assertTrue(CustomApiUrlValidationHelper.isValid("https://openrouter.ai/api/v1"))
        assertTrue(CustomApiUrlValidationHelper.isValid("http://10.0.2.2:11434/v1"))
        assertNull(CustomApiUrlValidationHelper.validate("api.deepseek.com/v1"))

        assertFalse(CustomApiUrlValidationHelper.isValid(""))
        assertFalse(CustomApiUrlValidationHelper.isValid("https://api example.com/v1"))
        assertNotNull(CustomApiUrlValidationHelper.validate("ftp://example.com"))
    }

    @Test
    fun bookBondSummaryHelper_includesBondTierLabel() {
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 5,
            completedChapters = setOf(1, 2, 3, 4),
            currentNodeId = "ch5_n1",
            transcript = emptyList(),
            chapterDecisions = emptyMap(),
            bonds = mapOf(BookCharacterId.WANDA to 80),
            fatigue = 0,
            mystery = 0,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )
        val summary = BookBondSummaryHelper.formatCompact(BookBondSummaryHelper.compute(state))
        assertTrue(summary.contains("Sarsılmaz Bağ"))
    }
}
