package com.example

import com.example.data.local.MessageEntity
import com.example.util.CustomApiNameAutoSuggester
import com.example.util.StoryVocabularyRichnessHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V91MegaBatchUpgradeTest {

    @Test
    fun storyVocabularyRichnessHelper_computesUniqueWordsAndRating() {
        val msgs = listOf(
            MessageEntity(
                id = "1",
                botId = "b1",
                role = "assistant",
                text = "Kuzey kulesinde fırtına koparken gümüş fener karanlığı yardı.",
                timestamp = 1L
            )
        )
        val stats = StoryVocabularyRichnessHelper.analyze(msgs)
        assertEquals(8, stats.uniqueWords)
        assertEquals(100, stats.richnessPercent)
        assertEquals("Çok Zengin", stats.label)
        assertTrue(StoryVocabularyRichnessHelper.formatCompact(stats).contains("8 benzersiz"))
    }

    @Test
    fun customApiNameAutoSuggester_detectsKnownAndLocalProviders() {
        assertEquals("OpenRouter", CustomApiNameAutoSuggester.suggestLabel("https://openrouter.ai/api/v1"))
        assertEquals("DeepSeek", CustomApiNameAutoSuggester.suggestLabel("https://api.deepseek.com/v1"))
        assertEquals("Yerel Ollama", CustomApiNameAutoSuggester.suggestLabel("http://10.0.2.2:11434/v1"))
        assertEquals("LM Studio", CustomApiNameAutoSuggester.suggestLabel("http://192.168.1.5:1234/v1"))
        assertEquals("Myproxy", CustomApiNameAutoSuggester.suggestLabel("https://api.myproxy.io/v1"))
    }
}
