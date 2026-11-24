package com.example

import com.example.util.ProviderRateLimitBudgetHelper
import com.example.util.StorySceneTitleGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V96MegaBatchUpgradeTest {

    @Test
    fun storySceneTitleGenerator_createsConciseTitleAndFallsBackOnBlank() {
        val title = StorySceneTitleGenerator.suggestTitle("*Kuzey kulesinde fırtına koparken* \"Kapıyı aç!\"", 7)
        assertTrue(title.startsWith("Kuzey kulesinde fırtına"))
        assertTrue(title.length <= 37)
        assertEquals("Sahne #7", StorySceneTitleGenerator.suggestTitle("   ", 7))
    }

    @Test
    fun providerRateLimitBudgetHelper_classifiesHeadroomTiers() {
        assertEquals("Sınırsız", ProviderRateLimitBudgetHelper.evaluate(10, 0).statusLabel)
        assertEquals("Bol Kota", ProviderRateLimitBudgetHelper.evaluate(20, 100).statusLabel)
        assertEquals("Orta", ProviderRateLimitBudgetHelper.evaluate(70, 100).statusLabel)
        assertEquals("Kritik", ProviderRateLimitBudgetHelper.evaluate(90, 100).statusLabel)
    }
}
