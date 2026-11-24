package com.example

import com.example.data.local.MessageEntity
import com.example.util.ChatTranscriptFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V41ChatTranscriptFormatterTest {

    @Test
    fun formatMarkdown_filtersErrorsAndSystemMessagesAndFormatsCleanly() {
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "system", text = "Internal prompt", timestamp = 1_700_000_000_000L),
            MessageEntity(id = "2", botId = "b1", role = "user", text = "Kapıyı yavaşça araladım.", timestamp = 1_700_000_010_000L),
            MessageEntity(id = "3", botId = "b1", role = "assistant", text = "Hata oluştu", timestamp = 1_700_000_020_000L, status = "error"),
            MessageEntity(id = "4", botId = "b1", role = "assistant", text = "*Loş ışıkta sana döndü.* \"Geç kaldın.\"", timestamp = 1_700_000_030_000L)
        )

        val md = ChatTranscriptFormatter.formatMarkdown(
            botDisplayName = "Natasha",
            userDisplayName = "Aiden",
            messages = msgs
        )

        assertTrue(md.startsWith("# Natasha — Hikaye Kaydı"))
        assertTrue(md.contains("**Aiden**"))
        assertTrue(md.contains("Kapıyı yavaşça araladım."))
        assertTrue(md.contains("**Natasha**"))
        assertTrue(md.contains("*Loş ışıkta sana döndü.* \"Geç kaldın.\""))
        assertFalse(md.contains("Internal prompt"))
        assertFalse(md.contains("Hata oluştu"))
    }

    @Test
    fun formatMarkdown_returnsEmptyWhenNoValidMessages() {
        val md = ChatTranscriptFormatter.formatMarkdown(
            botDisplayName = "Natasha",
            userDisplayName = "Aiden",
            messages = listOf(
                MessageEntity(id = "1", botId = "b1", role = "assistant", text = "   ", timestamp = 1L)
            )
        )
        assertEquals("", md)
    }
}
