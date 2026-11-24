package com.example

import com.example.data.local.BotEntity
import com.example.util.BotCategoryCountHelper
import com.example.util.ResponseSanitizer
import com.example.util.StoryDialoguePunctuationNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V75MegaBatchUpgradeTest {

    @Test
    fun storyDialoguePunctuationNormalizer_cleansSpacesAndExcessPunctuation() {
        val messy = "Bekle , ne oluyor ???? Kapıyı aç !!!!!! Lütfen......"
        val clean = StoryDialoguePunctuationNormalizer.normalize(messy)
        assertEquals("Bekle, ne oluyor? Kapıyı aç! Lütfen...", clean)

        val viaSanitizer = ResponseSanitizer.sanitizeForDisplay("Assistant: Merhaba , dünya !!!!")
        assertEquals("Merhaba, dünya!", viaSanitizer)
    }

    @Test
    fun botCategoryCountHelper_countsCategoriesAndFormatsSummary() {
        fun bot(id: String, mode: String) = BotEntity(
            id = id,
            mode = mode,
            universeName = "",
            keyCharactersJson = "[]",
            aiName = id,
            aiPersonality = "",
            userCharName = "Sen",
            userCharDesc = "",
            scenario = "",
            openingMessage = "",
            writingStyle = "",
            intensity = "Medium"
        )
        val counts = BotCategoryCountHelper.compute(
            bots = listOf(bot("b1", "personal"), bot("b2", "universe"), bot("b3", "universe")),
            favoriteIds = setOf("b1", "b2"),
            draftBotIds = setOf("b3")
        )
        assertEquals(3, counts.total)
        assertEquals(1, counts.personal)
        assertEquals(2, counts.universe)
        assertEquals(2, counts.favorites)
        assertEquals(1, counts.withDrafts)

        val summary = BotCategoryCountHelper.formatSummary(counts)
        assertTrue(summary.contains("3 hikaye"))
        assertTrue(summary.contains("2 evren"))
        assertTrue(summary.contains("2 favori"))
        assertTrue(summary.contains("1 taslak"))
    }
}
