package com.example

import com.example.data.local.MessageEntity
import com.example.data.managers.ProviderHealthState
import com.example.util.ProviderQuickHealthBadgeHelper
import com.example.util.StoryUserParticipationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V107MegaBatchUpgradeTest {

    @Test
    fun providerQuickHealthBadgeHelper_classifiesStatesCleanly() {
        val now = 1_000_000L
        assertEquals("Hazır", ProviderQuickHealthBadgeHelper.formatBadge(null, now))
        assertEquals("Beklemede", ProviderQuickHealthBadgeHelper.formatBadge(ProviderHealthState("g", cooldownUntilMs = now + 5000L), now))
        assertEquals("Sorunlu", ProviderQuickHealthBadgeHelper.formatBadge(ProviderHealthState("g", consecutiveFailures = 2), now))
        assertEquals("Hazır", ProviderQuickHealthBadgeHelper.formatBadge(ProviderHealthState("g"), now))
    }

    @Test
    fun storyUserParticipationHelper_analyzesUserTurnStyle() {
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "user", text = "Kapıyı açıp içeri baktım.", timestamp = 1L),
            MessageEntity(id = "2", botId = "b1", role = "assistant", text = "Karanlık oda sessizdi.", timestamp = 2L)
        )
        val stats = StoryUserParticipationHelper.analyze(msgs)
        assertEquals(1, stats.userTurnCount)
        assertEquals("Hızlı & Dinamik", stats.styleLabel)
        assertTrue(StoryUserParticipationHelper.formatCompact(stats).contains("kelime/mesaj"))
    }
}
