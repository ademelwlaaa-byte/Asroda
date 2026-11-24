package com.example

import com.example.util.CustomApiModelRecommendationHelper
import com.example.util.StoryMoodEmojiMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class V79MegaBatchUpgradeTest {

    @Test
    fun customApiModelRecommendationHelper_prefersLightweightFastUtilityModels() {
        val models = listOf(
            "anthropic/claude-opus-4",
            "deepseek/deepseek-r1",
            "google/gemini-2.5-flash-lite:free",
            "meta-llama/llama-3.3-70b-instruct"
        )
        val picked = CustomApiModelRecommendationHelper.pickFastestUtilityModel(
            models = models,
            excludePrimary = "meta-llama/llama-3.3-70b-instruct"
        )
        assertEquals("google/gemini-2.5-flash-lite:free", picked)
    }

    @Test
    fun storyMoodEmojiMapper_mapsTurkishAndEnglishMoods() {
        assertEquals("💫 Romantik", StoryMoodEmojiMapper.mapToBadge("Romantik ve şefkatli"))
        assertEquals("⚡ Gerilim", StoryMoodEmojiMapper.mapToBadge("Yüksek gerilim"))
        assertEquals("🕯 Gizemli", StoryMoodEmojiMapper.mapToBadge("Karanlık gizem"))
        assertEquals("🌧 Melankolik", StoryMoodEmojiMapper.mapToBadge("Hüzünlü veda"))
        assertEquals("⚔ Aksiyon", StoryMoodEmojiMapper.mapToBadge("Hızlı aksiyon"))
        assertEquals("✦ Dengeli", StoryMoodEmojiMapper.mapToBadge("Sıradan"))
    }
}
