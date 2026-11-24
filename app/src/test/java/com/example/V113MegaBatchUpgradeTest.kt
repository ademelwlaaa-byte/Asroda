package com.example

import com.example.util.ProviderRoutingTierHelper
import com.example.util.StoryDialogueDensityCoach
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V113MegaBatchUpgradeTest {

    @Test
    fun providerRoutingTierHelper_classifiesTiersAndRedundancy() {
        assertEquals("⚡ Ultra Hızlı", ProviderRoutingTierHelper.classifyTier("Groq"))
        assertEquals("🌟 Ana Akıllı", ProviderRoutingTierHelper.classifyTier("Gemini"))
        assertEquals("🔧 Özel API", ProviderRoutingTierHelper.classifyTier("Custom OpenAI"))
        assertEquals("🔄 Yedek Havuz", ProviderRoutingTierHelper.classifyTier("OpenRouter"))

        assertTrue(ProviderRoutingTierHelper.summarizeRedundancy(1).contains("Tek Sağlayıcı"))
        assertTrue(ProviderRoutingTierHelper.summarizeRedundancy(3).contains("Dengeli Yedekli"))
        assertTrue(ProviderRoutingTierHelper.summarizeRedundancy(5).contains("Yüksek Yedekli"))
    }

    @Test
    fun storyDialogueDensityCoach_givesActionableTurkishTips() {
        assertTrue(StoryDialogueDensityCoach.generateTip(1, 30, 20).contains("yeni başlıyor"))
        assertTrue(StoryDialogueDensityCoach.generateTip(4, 120, 35).contains("kısa kalıyor"))
        assertTrue(StoryDialogueDensityCoach.generateTip(4, 1800, 35).contains("çok uzun"))
        assertTrue(StoryDialogueDensityCoach.generateTip(4, 600, 5).contains("Diyalog oranı düşük"))
        assertTrue(StoryDialogueDensityCoach.generateTip(4, 600, 40).contains("ideal roman temposunda"))
    }
}
