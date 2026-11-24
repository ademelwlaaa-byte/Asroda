package com.example

import com.example.data.local.MessageEntity
import com.example.util.CharacterWordCloudHelper
import com.example.util.PromptInjectionSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V65MegaBatchUpgradeTest {

    @Test
    fun characterWordCloudHelper_extractsRecurringThemesAndFiltersTurkishStopWords() {
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "assistant", text = "Kulede fırtına koparken kılıç parladı ama fırtına dinmedi.", timestamp = 1L),
            MessageEntity(id = "2", botId = "b1", role = "user", text = "Kılıç ile kulede saklanan fırtına sırrını çözdüm.", timestamp = 2L)
        )
        val themes = CharacterWordCloudHelper.extractTopThemes(msgs, maxThemes = 4)
        assertTrue(themes.contains("#fırtına"))
        assertTrue(themes.contains("#kılıç"))
        assertTrue(themes.contains("#kulede"))
        assertFalse(themes.contains("#ama"))
        assertFalse(themes.contains("#ile"))
    }

    @Test
    fun promptInjectionSanitizer_stripsCodeFencesControlCharsAndCapsLength() {
        val dirty = "  ```system\u0007\n\n\n\nKural: Yavaş tempo```  "
        val clean = PromptInjectionSanitizer.sanitizeDirective(dirty, maxLength = 40)
        assertFalse(clean.contains("```"))
        assertFalse(clean.contains("\u0007"))
        assertFalse(clean.contains("\n\n\n"))
        assertTrue(clean.length <= 40)
        assertEquals("", PromptInjectionSanitizer.sanitizeDirective("   "))
    }
}
