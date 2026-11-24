package com.example

import com.example.data.local.MessageEntity
import com.example.util.ProviderRollingLatencyTracker
import com.example.util.StoryParagraphReadabilityHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class V101MegaBatchUpgradeTest {

    @Before
    fun setup() {
        ProviderRollingLatencyTracker.clearAll()
    }

    @Test
    fun providerRollingLatencyTracker_computesRollingWindowMedianAndAverage() {
        listOf(200L, 400L, 600L, 300L, 500L).forEach {
            ProviderRollingLatencyTracker.recordSample("gemini", it)
        }
        val m = ProviderRollingLatencyTracker.getMetrics("gemini")
        assertNotNull(m)
        assertEquals(5, m!!.sampleCount)
        assertEquals(400L, m.averageMs)
        assertEquals(400L, m.medianMs)
        assertEquals(200L, m.minMs)
        assertEquals(600L, m.maxMs)
    }

    @Test
    fun storyParagraphReadabilityHelper_classifiesParagraphDensity() {
        val airy = StoryParagraphReadabilityHelper.evaluate(
            listOf(MessageEntity(id = "1", botId = "b1", role = "assistant", text = "Kısa ilk paragraf.\n\nKısa ikinci paragraf.", timestamp = 1L))
        )
        assertEquals("Çok Rahat", airy.label)
    }
}
