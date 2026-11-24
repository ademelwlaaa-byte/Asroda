package com.example

import com.example.data.local.MessageEntity
import com.example.util.StoryTurnCadenceAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V80MegaBatchUpgradeTest {

    @Test
    fun storyTurnCadenceAnalyzer_computesLongestTurnAndAiToUserRatio() {
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "user", text = "Kapıyı yavaşça açtım ve içeri baktım.", timestamp = 1L),
            MessageEntity(
                id = "2",
                botId = "b1",
                role = "assistant",
                text = "Karanlık koridorun sonunda titrek bir mum ışığı yanıyordu. Taş duvarlardan yankılanan soğuk rüzgâr tüm odayı doldururken gölgeler kıpırdandı.",
                timestamp = 2L
            )
        )
        val stats = StoryTurnCadenceAnalyzer.analyze(msgs)
        assertEquals(18, stats.longestTurnWords)
        assertEquals(3.0, stats.userToAiRatio, 0.01)

        val formatted = StoryTurnCadenceAnalyzer.formatCompact(stats)
        assertTrue(formatted.contains("18 kelime"))
        assertTrue(formatted.contains("3.0x"))
    }
}
