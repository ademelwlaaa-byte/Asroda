package com.example

import com.example.data.local.MessageEntity
import com.example.util.CharacterMentionTracker
import com.example.util.TtsSentenceChunker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V68MegaBatchUpgradeTest {

    @Test
    fun characterMentionTracker_parsesKeyCharactersAndRanksMentions() {
        val json = """[{"name":"Natasha"},{"name":"Wanda"},{"name":"Voss"}]"""
        val names = CharacterMentionTracker.extractCastNames(json)
        assertEquals(listOf("Natasha", "Wanda", "Voss"), names)

        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "assistant", text = "Wanda ve Natasha kuleye girdi. Wanda etrafı taradı.", timestamp = 1L),
            MessageEntity(id = "2", botId = "b1", role = "user", text = "Wanda'ya işaret verdim.", timestamp = 2L)
        )
        val mentions = CharacterMentionTracker.countMentions(msgs, names)
        assertEquals(2, mentions.size)
        assertEquals("Wanda", mentions[0].name)
        assertEquals(3, mentions[0].count)
        assertEquals("Natasha", mentions[1].name)
        assertEquals(1, mentions[1].count)
    }

    @Test
    fun ttsSentenceChunker_preservesTurkishAbbreviationsWithoutPrematureSplits() {
        val text = "Dr. Voss hızla kapıyı açtı! Prof. Banner ise sessizce bekliyordu. Ne olacak şimdi?"
        val sentences = TtsSentenceChunker.splitSentences(text)
        assertEquals(3, sentences.size)
        assertTrue(sentences[0].startsWith("Dr. Voss"))
        assertTrue(sentences[1].startsWith("Prof. Banner"))
        assertEquals("Ne olacak şimdi?", sentences[2])
    }
}
