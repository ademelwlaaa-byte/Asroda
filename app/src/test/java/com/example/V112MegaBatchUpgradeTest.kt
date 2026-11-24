package com.example

import com.example.util.StorySessionFreshnessHelper
import com.example.util.TtsProsodyStyleRecommender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V112MegaBatchUpgradeTest {

    @Test
    fun storySessionFreshnessHelper_formatsRecentAndAgedSessions() {
        val now = 1_700_000_000_000L
        assertEquals("🆕 Henüz başlamadı", StorySessionFreshnessHelper.formatFreshness(0L, now))
        assertEquals("🟢 Az önce aktif", StorySessionFreshnessHelper.formatFreshness(now - 5 * 60_000L, now))
        assertTrue(StorySessionFreshnessHelper.formatFreshness(now - 4 * 3_600_000L, now).contains("4 sa önce"))
        assertEquals("📅 3 gün önce", StorySessionFreshnessHelper.formatFreshness(now - 3 * 86_400_000L, now))
        assertEquals("🕸️ Uzun süredir bekliyor", StorySessionFreshnessHelper.formatFreshness(now - 45 * 86_400_000L, now))
    }

    @Test
    fun ttsProsodyStyleRecommender_mapsAtmospheresToSpeechRates() {
        val action = TtsProsodyStyleRecommender.recommendForAtmosphere("⚔️ Aksiyon & Gerilim")
        assertEquals(1.08f, action.recommendedRate, 0.001f)
        assertTrue(action.label.contains("1.08x"))

        val mystery = TtsProsodyStyleRecommender.recommendForAtmosphere("🕯️ Gizemli")
        assertEquals(0.92f, mystery.recommendedRate, 0.001f)

        val neutral = TtsProsodyStyleRecommender.recommendForAtmosphere("Normal")
        assertEquals(1.00f, neutral.recommendedRate, 0.001f)
    }
}
