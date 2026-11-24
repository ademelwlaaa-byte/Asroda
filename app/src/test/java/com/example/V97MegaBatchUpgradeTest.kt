package com.example

import com.example.book.BookChapterCompletionBadgeHelper
import com.example.util.StoryDialogueFormatValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V97MegaBatchUpgradeTest {

    @Test
    fun storyDialogueFormatValidator_detectsAndClosesUnbalancedDelimiters() {
        assertFalse(StoryDialogueFormatValidator.hasBalancedQuotes("\"Merhaba"))
        assertFalse(StoryDialogueFormatValidator.hasBalancedAsterisks("*Kılıcını çeker"))
        assertEquals("\"Merhaba\"", StoryDialogueFormatValidator.autoCloseUnbalanced("\"Merhaba"))
        assertEquals("*Kılıcını çeker*", StoryDialogueFormatValidator.autoCloseUnbalanced("*Kılıcını çeker"))
        assertTrue(StoryDialogueFormatValidator.hasBalancedQuotes("\"Tamam\""))
    }

    @Test
    fun bookChapterCompletionBadgeHelper_mapsCompletionTiers() {
        assertEquals("🌱 İlk Adım", BookChapterCompletionBadgeHelper.resolveBadge(0))
        assertEquals("📖 Yolculuk Başladı", BookChapterCompletionBadgeHelper.resolveBadge(10))
        assertEquals("🕯 Sır Avcısı", BookChapterCompletionBadgeHelper.resolveBadge(40))
        assertEquals("✦ Kozmik Kaşif", BookChapterCompletionBadgeHelper.resolveBadge(75))
        assertEquals("👑 Evrenin Hakimi", BookChapterCompletionBadgeHelper.resolveBadge(100))
    }
}
