package com.example

import com.example.data.local.MessageEntity
import com.example.util.ProviderRetryDelayAdvisor
import com.example.util.StoryDialogueQuoteCounter
import org.junit.Assert.assertEquals
import org.junit.Test

class V84MegaBatchUpgradeTest {

    @Test
    fun storyDialogueQuoteCounter_countsDialoguesAndActionsSeparately() {
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "assistant", text = "*Gülümser.* \"Hoş geldin!\" *Kapıyı aralar.* \"İçeri geç.\"", timestamp = 1L),
            MessageEntity(id = "2", botId = "b1", role = "user", text = "*Başını sallar.* \"Teşekkürler.\"", timestamp = 2L)
        )
        val counts = StoryDialogueQuoteCounter.count(msgs)
        assertEquals(3, counts.dialogueLines)
        assertEquals(3, counts.actionCues)
        assertEquals("3 replik • 3 aksiyon", StoryDialogueQuoteCounter.formatCompact(counts))
    }

    @Test
    fun providerRetryDelayAdvisor_doublesAndCapsCleanly() {
        assertEquals(500L, ProviderRetryDelayAdvisor.computeDelayMs(0, 500L, 8000L))
        assertEquals(1000L, ProviderRetryDelayAdvisor.computeDelayMs(1, 500L, 8000L))
        assertEquals(2000L, ProviderRetryDelayAdvisor.computeDelayMs(2, 500L, 8000L))
        assertEquals(8000L, ProviderRetryDelayAdvisor.computeDelayMs(6, 500L, 8000L))
    }
}
