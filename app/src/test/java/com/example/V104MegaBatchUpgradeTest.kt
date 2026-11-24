package com.example

import com.example.data.local.BotEntity
import com.example.util.StoryCharacterSynopsisHelper
import com.example.util.TtsActionStripper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V104MegaBatchUpgradeTest {

    @Test
    fun ttsActionStripper_togglesActionBlocksCleanly() {
        val raw = "*Yavaşça gülümser.* \"Hoş geldin!\" *Kapıyı kapatır.*"
        assertEquals("\"Hoş geldin!\"", TtsActionStripper.prepare(raw, includeActions = false))
        assertEquals("Yavaşça gülümser. \"Hoş geldin!\" Kapıyı kapatır.", TtsActionStripper.prepare(raw, includeActions = true))
    }

    @Test
    fun storyCharacterSynopsisHelper_includesPitchGenresAndCompleteness() {
        val bot = BotEntity(
            id = "b1",
            mode = "personal",
            universeName = "",
            keyCharactersJson = "[]",
            aiName = "Aiden",
            aiPersonality = "Kararlı ve gizemli bir büyücü.",
            userCharName = "Sen",
            userCharDesc = "Gezgin",
            scenario = "Kuzey kulesinde fırtına kopar.",
            openingMessage = "Hoş geldin, içeri geç!",
            writingStyle = "Edebi",
            intensity = "Medium"
        )
        val syn = StoryCharacterSynopsisHelper.buildSynopsis(bot)
        assertTrue(syn.contains("Aiden"))
        assertTrue(syn.contains("Fantastik"))
        assertTrue(syn.contains("%"))
    }
}
