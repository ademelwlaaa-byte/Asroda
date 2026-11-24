package com.example

import com.example.data.local.MessageEntity
import com.example.util.CustomApiKeyMasker
import com.example.util.StoryTranscriptHeaderHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V98MegaBatchUpgradeTest {

    @Test
    fun customApiKeyMasker_masksLongAndShortKeysSafely() {
        assertEquals("sk-p••••cdef", CustomApiKeyMasker.mask("Bearer sk-proj-1234567890abcdef"))
        assertEquals("••••", CustomApiKeyMasker.mask("shortkey"))
        assertEquals("", CustomApiKeyMasker.mask("   "))
    }

    @Test
    fun storyTranscriptHeaderHelper_buildsSingleLineSummary() {
        val header = StoryTranscriptHeaderHelper.formatHeaderLine(
            title = "Aiden",
            messages = listOf(
                MessageEntity(id = "1", botId = "b1", role = "assistant", text = "Karanlık kulede gizemli bir ışık parladı.", timestamp = 1L)
            )
        )
        assertTrue(header.startsWith("Aiden • 1 mesaj"))
        assertTrue(header.contains("kelime"))
        assertTrue(header.contains("sayfa"))
    }
}
