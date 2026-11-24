package com.example

import com.example.util.ProviderModelFilterHelper
import com.example.util.StoryBookmarkSearchHelper
import com.example.util.StoryBookmarkStore
import org.junit.Assert.assertEquals
import org.junit.Test

class V94MegaBatchUpgradeTest {

    @Test
    fun storyBookmarkSearchHelper_matchesTurkishDiacriticsInTitleAndSnippet() {
        val bms = listOf(
            StoryBookmarkStore.Bookmark("1", "m1", "Işık Kulesi", "Soğuk rüzgar esiyordu", 1L),
            StoryBookmarkStore.Bookmark("2", "m2", "Büyük Savaş", "Kılıçlar çekildi", 2L)
        )
        assertEquals(1, StoryBookmarkSearchHelper.filter(bms, "isik").size)
        assertEquals(1, StoryBookmarkSearchHelper.filter(bms, "kiliclar").size)
        assertEquals(2, StoryBookmarkSearchHelper.filter(bms, "").size)
    }

    @Test
    fun providerModelFilterHelper_matchesMultiTokenQueriesAcrossSeparators() {
        val models = listOf(
            "meta-llama/llama-3.3-70b-instruct:free",
            "deepseek/deepseek-r1:free",
            "openai/gpt-4o-mini"
        )
        assertEquals(
            listOf("meta-llama/llama-3.3-70b-instruct:free"),
            ProviderModelFilterHelper.filterModels(models, "llama 3.3 free")
        )
        assertEquals(
            listOf("deepseek/deepseek-r1:free"),
            ProviderModelFilterHelper.filterModels(models, "deepseek r1")
        )
    }
}
