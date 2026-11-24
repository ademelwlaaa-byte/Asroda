package com.example

import com.example.data.local.BotEntity
import com.example.util.StoryCharacterIntroCardHelper
import org.junit.Assert.assertTrue
import org.junit.Test

class V95MegaBatchUpgradeTest {

    @Test
    fun storyCharacterIntroCardHelper_buildsCleanPitchForPersonalAndUniverseBots() {
        val personal = BotEntity(
            id = "b1",
            mode = "personal",
            universeName = "",
            keyCharactersJson = "[]",
            aiName = "Aiden",
            aiPersonality = "Soğuk ama sadık bir koruyucu.",
            userCharName = "Sen",
            userCharDesc = "",
            scenario = "",
            openingMessage = "Hoş geldin.",
            writingStyle = "Edebi",
            intensity = "Medium"
        )
        val pitch = StoryCharacterIntroCardHelper.buildPitch(personal)
        assertTrue(pitch.contains("Aiden (Karakter"))
        assertTrue(pitch.contains("Soğuk ama sadık"))
    }
}
