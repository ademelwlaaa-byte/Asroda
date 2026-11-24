package com.example

import com.example.data.local.MessageEntity
import com.example.util.StoryMoodAnalytics
import org.junit.Assert.assertEquals
import org.junit.Test

class V60MegaBatchUpgradeTest {

    @Test
    fun storyMoodAnalytics_detectsDominantAtmosphereAccurately() {
        val actionMsgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "assistant", text = "Kırmızı alarm çaldı, silah sesleri ve patlama kuleyi sarstı!", timestamp = 1L)
        )
        assertEquals("Aksiyon & Gerilim", StoryMoodAnalytics.detectDominantAtmosphere(actionMsgs))

        val romMsgs = listOf(
            MessageEntity(id = "2", botId = "b1", role = "assistant", text = "Elini tutup kalp atışlarını dinledi ve kulağına fısıldadı.", timestamp = 2L)
        )
        assertEquals("Romantik & Duygusal", StoryMoodAnalytics.detectDominantAtmosphere(romMsgs))

        val mystMsgs = listOf(
            MessageEntity(id = "3", botId = "b1", role = "assistant", text = "Kayıp dosyanın içindeki gizli şifre büyük bir ihaneti ortaya çıkardı.", timestamp = 3L)
        )
        assertEquals("Gizem & Entrika", StoryMoodAnalytics.detectDominantAtmosphere(mystMsgs))
    }
}
