package com.example

import com.example.data.local.BotEntity
import com.example.data.managers.ProviderHealthTracker
import com.example.util.BotCardCompletenessHelper
import com.example.util.ProviderRollingLatencyTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class V102MegaBatchUpgradeTest {

    @Before
    fun setup() {
        ProviderRollingLatencyTracker.clearAll()
    }

    @Test
    fun providerHealthTracker_recordsSuccessIntoRollingLatencyTracker() {
        ProviderHealthTracker.recordSuccess("gemini", 320L)
        ProviderHealthTracker.recordSuccess("gemini", 480L)
        val metrics = ProviderRollingLatencyTracker.getMetrics("gemini")
        assertNotNull(metrics)
        assertEquals(2, metrics!!.sampleCount)
        assertEquals(400L, metrics.averageMs)
    }

    @Test
    fun botCardCompletenessHelper_scoresFullVsMinimalCards() {
        val full = BotEntity(
            id = "b1",
            mode = "personal",
            universeName = "",
            keyCharactersJson = "[]",
            aiName = "Aiden",
            aiPersonality = "Kararlı, zeki ve sadık bir koruyucu.",
            userCharName = "Kaan",
            userCharDesc = "Gezgin",
            scenario = "Sisli bir limanda gizli buluşma gerçekleşir.",
            openingMessage = "*Liman fenerine bakar.* \"Geç kaldın ama hâlâ vaktimiz var.\"",
            writingStyle = "Edebi",
            intensity = "Medium",
            pinnedMemory = "Gizli harita kulede."
        )
        val c = BotCardCompletenessHelper.evaluate(full)
        assertEquals(100, c.percent)
        assertEquals("✦ Eksiksiz Kart", c.label)

        val sparse = full.copy(aiPersonality = "", scenario = "", openingMessage = "", pinnedMemory = "")
        assertTrue(BotCardCompletenessHelper.evaluate(sparse).percent < 50)
    }
}
