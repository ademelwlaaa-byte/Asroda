package com.example

import com.example.data.local.MessageEntity
import com.example.util.StoryReadingLevelEstimator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V85MegaBatchUpgradeTest {

    @Test
    fun storyReadingLevelEstimator_classifiesFastBalancedAndRichProse() {
        val fast = StoryReadingLevelEstimator.estimate(
            listOf(MessageEntity(id = "1", botId = "b1", role = "assistant", text = "Kapı açıldı! Işık parladı. Hemen koştu.", timestamp = 1L))
        )
        assertEquals("Akıcı & Hızlı", fast.styleBadge)

        val rich = StoryReadingLevelEstimator.estimate(
            listOf(
                MessageEntity(
                    id = "2",
                    botId = "b1",
                    role = "assistant",
                    text = "Kuzey kulesinin taş merdivenlerinden inerken rüzgârın getirdiği soğuk sis, yüzyıllardır saklanan eski yazıtların üzerindeki gümüş harfleri yavaşça aydınlatıyordu.",
                    timestamp = 2L
                )
            )
        )
        assertEquals("Zengin Roman Dili", rich.styleBadge)
        assertTrue(StoryReadingLevelEstimator.formatCompact(rich).contains("kelime/cümle"))
    }
}
