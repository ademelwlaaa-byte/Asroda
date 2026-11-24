package com.example

import com.example.util.MessageQuoteSnippetHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V87MegaBatchUpgradeTest {

    @Test
    fun messageQuoteSnippetHelper_formatsSpeakerAndClipsLongText() {
        val shortQuote = MessageQuoteSnippetHelper.formatBlockquote("Karanlıkta bile yolunu bulabilirsin.", "Natasha")
        assertEquals("> Karanlıkta bile yolunu bulabilirsin.\n— *Natasha*", shortQuote)

        val longText = "Kelime ".repeat(60)
        val clipped = MessageQuoteSnippetHelper.formatBlockquote(longText, maxChars = 40)
        assertTrue(clipped.startsWith("> "))
        assertTrue(clipped.endsWith("…"))
        assertEquals("", MessageQuoteSnippetHelper.formatBlockquote("   "))
    }
}
