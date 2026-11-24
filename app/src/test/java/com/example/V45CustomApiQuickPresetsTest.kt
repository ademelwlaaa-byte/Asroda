package com.example

import com.example.data.managers.CustomApiQuickPresets
import org.junit.Assert.assertTrue
import org.junit.Test

class V45CustomApiQuickPresetsTest {

    @Test
    fun presets_haveValidUniqueIdsUrlsAndFormats() {
        val presets = CustomApiQuickPresets.presets
        assertTrue(presets.size >= 5)
        assertTrue(presets.map { it.id }.distinct().size == presets.size)
        presets.forEach { p ->
            assertTrue("Preset ${p.id} must have http(s) URL", p.baseUrl.startsWith("http://") || p.baseUrl.startsWith("https://"))
            assertTrue("Preset ${p.id} must use openai or anthropic format", p.apiFormat == "openai" || p.apiFormat == "anthropic")
            assertTrue("Preset ${p.id} must have a suggested model", p.suggestedModel.isNotBlank())
        }
    }
}
