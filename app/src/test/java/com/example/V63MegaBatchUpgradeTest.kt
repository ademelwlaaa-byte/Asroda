package com.example

import com.example.data.local.MessageEntity
import com.example.util.StoryQuoteExtractor
import com.example.util.TtsSpeechEstimator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V63MegaBatchUpgradeTest {

    @Test
    fun ttsSpeechEstimator_scalesWithSpeedAndFormatsTurkishDurations() {
        val normalSecs = TtsSpeechEstimator.estimateSeconds(wordCount = 270, speedMultiplier = 1.0f)
        val fastSecs = TtsSpeechEstimator.estimateSeconds(wordCount = 270, speedMultiplier = 1.5f)
        assertEquals(120, normalSecs)
        assertEquals(80, fastSecs)
        assertEquals("~2 dk", TtsSpeechEstimator.formatDurationTurkish(120))
        assertEquals("~1 dk 20 sn", TtsSpeechEstimator.formatDurationTurkish(80))
        assertEquals("~45 sn", TtsSpeechEstimator.formatDurationTurkish(45))
        assertEquals("0 sn", TtsSpeechEstimator.formatDurationTurkish(0))
    }

    @Test
    fun storyQuoteExtractor_picksMostDramaticDialogueLine() {
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "user", text = "Kapıyı çaldım.", timestamp = 1L),
            MessageEntity(id = "2", botId = "b1", role = "assistant", text = "*Derin bir nefes alır.* \"Karanlık çöktüğünde yalnızca kendi ışığına güvenebilirsin!\" dedi.", timestamp = 2L)
        )
        val quote = StoryQuoteExtractor.extractBestQuote(msgs, "Aiden")
        assertNotNull(quote)
        assertTrue(quote!!.contains("Karanlık çöktüğünde"))
        assertTrue(quote.contains("— Aiden"))

        val noQuote = StoryQuoteExtractor.extractBestQuote(
            listOf(MessageEntity(id = "3", botId = "b1", role = "assistant", text = "Sessizce uzaklaştı.", timestamp = 3L))
        )
        assertNull(noQuote)
    }
}
