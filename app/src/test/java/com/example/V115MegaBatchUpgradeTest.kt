package com.example

import com.example.data.local.MessageEntity
import com.example.util.ProviderLatencyConsistencyHelper
import com.example.util.StoryOpeningHookAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V115MegaBatchUpgradeTest {

    @Test
    fun storyOpeningHookAnalyzer_detectsOpeningStyles() {
        val cinematic = listOf(
            MessageEntity(
                id = "m1",
                botId = "b1",
                role = "assistant",
                text = "*Yağmur pencereye vururken yavaşça döndü ve ışığı kıstı.* \"Bu gece kimse güvende değil, hazır mısın?\" diye fısıldadı karanlığın içinden bakarak."
            )
        )
        assertEquals("🎬 Sinematik Açılış", StoryOpeningHookAnalyzer.classifyOpening(cinematic))

        val dialogue = listOf(
            MessageEntity(id = "m2", botId = "b1", role = "assistant", text = "\"Merhaba, seni bekliyordum.\"")
        )
        assertEquals("💬 Diyalogla Giriş", StoryOpeningHookAnalyzer.classifyOpening(dialogue))

        assertEquals("🌱 Açılış Bekleniyor", StoryOpeningHookAnalyzer.classifyOpening(emptyList()))
    }

    @Test
    fun providerLatencyConsistencyHelper_evaluatesJitterSpread() {
        val stable = ProviderLatencyConsistencyHelper.evaluate(listOf(320L, 410L, 500L))
        assertEquals(180L, stable.spreadMs)
        assertTrue(stable.badge.contains("Çok Stabil"))

        val jittery = ProviderLatencyConsistencyHelper.evaluate(listOf(300L, 2600L))
        assertEquals(2300L, jittery.spreadMs)
        assertTrue(jittery.badge.contains("Dalgalı"))

        assertEquals("📊 Veri Toplanıyor", ProviderLatencyConsistencyHelper.evaluate(listOf(400L)).badge)
    }
}
