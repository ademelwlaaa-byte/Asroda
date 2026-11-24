package com.example

import com.example.data.local.MessageEntity
import com.example.util.ChatStoryStats
import org.junit.Assert.assertEquals
import org.junit.Test

class V43ChatStoryStatsTest {

    @Test
    fun compute_calculatesWordsPagesAndMinutesCorrectly() {
        val longStory = (1..300).joinToString(" ") { "kelime$it" }
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "system", text = "hidden system", timestamp = 100L),
            MessageEntity(id = "2", botId = "b1", role = "user", text = "Kapıyı aralayıp içeri baktım.", timestamp = 1_000L),
            MessageEntity(id = "3", botId = "b1", role = "assistant", text = "Hata", timestamp = 1_500L, status = "error"),
            MessageEntity(id = "4", botId = "b1", role = "assistant", text = longStory, timestamp = 2_000L)
        )

        val s = ChatStoryStats.compute(msgs)
        assertEquals(2, s.totalMessages)
        assertEquals(1, s.userMessages)
        assertEquals(1, s.assistantMessages)
        assertEquals(4, s.userWordCount)
        assertEquals(300, s.assistantWordCount)
        assertEquals(304, s.totalWordCount)
        assertEquals(2, s.estimatedNovelPages)
        assertEquals(2, s.estimatedReadingMinutes)
        assertEquals(1_000L, s.firstTimestampMs)
        assertEquals(2_000L, s.lastTimestampMs)
    }

    @Test
    fun compute_returnsZerosForEmptyOrErrorOnlyChat() {
        val s = ChatStoryStats.compute(
            listOf(MessageEntity(id = "1", botId = "b1", role = "assistant", text = "err", status = "error", timestamp = 1L))
        )
        assertEquals(0, s.totalMessages)
        assertEquals(0, s.totalWordCount)
        assertEquals(0, s.estimatedNovelPages)
    }
}
