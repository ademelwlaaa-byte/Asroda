package com.example

import com.example.util.ProviderTimeoutAdvisor
import com.example.util.StoryDialogueSpeakerExtractor
import org.junit.Assert.assertEquals
import org.junit.Test

class V90MegaBatchUpgradeTest {

    @Test
    fun providerTimeoutAdvisor_assignsTieredTimeoutsByModelClass() {
        assertEquals(120_000L, ProviderTimeoutAdvisor.recommendedReadTimeoutMs("deepseek/deepseek-r1"))
        assertEquals(45_000L, ProviderTimeoutAdvisor.recommendedReadTimeoutMs("gemini-2.5-flash"))
        assertEquals(75_000L, ProviderTimeoutAdvisor.recommendedReadTimeoutMs("meta-llama/llama-3.3-70b-instruct"))
    }

    @Test
    fun storyDialogueSpeakerExtractor_extractsDistinctSpeakersAndIgnoresOoc() {
        val text = """
            **Natasha:** Kuleye kuzeyden gireceğiz.
            Wanda: Kalkanı ben tutarım.
            OOC: Sahne devam ediyor.
            **Natasha:** Hazır olun.
        """.trimIndent()
        assertEquals(listOf("Natasha", "Wanda"), StoryDialogueSpeakerExtractor.extractSpeakers(text))
    }
}
