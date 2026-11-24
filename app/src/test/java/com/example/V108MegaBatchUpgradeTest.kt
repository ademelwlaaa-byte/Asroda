package com.example

import com.example.data.local.MessageEntity
import com.example.util.CustomApiTestStatusFormatter
import com.example.util.StoryEmotionalArcHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V108MegaBatchUpgradeTest {

    @Test
    fun storyEmotionalArcHelper_buildsThreeActProgression() {
        val msgs = listOf(
            MessageEntity(id = "1", botId = "b1", role = "assistant", text = "Karanlık kulede gizemli bir gölge vardı.", timestamp = 1L),
            MessageEntity(id = "2", botId = "b1", role = "assistant", text = "Kılıçlar çekildi ve savaş başladı!", timestamp = 2L),
            MessageEntity(id = "3", botId = "b1", role = "assistant", text = "Kalbi sıcacıktı, sevgiyle gülümsedi.", timestamp = 3L)
        )
        val arc = StoryEmotionalArcHelper.formatThreeActArc(msgs)
        assertTrue(arc.contains("→"))
        assertEquals("", StoryEmotionalArcHelper.formatThreeActArc(msgs.take(2)))
    }

    @Test
    fun customApiTestStatusFormatter_formatsSuccessAndErrorWithAdvice() {
        val ok = CustomApiTestStatusFormatter.formatResult(isSuccess = true, latencyMs = 310L)
        assertTrue(ok.contains("✓ Bağlantı başarılı"))
        assertTrue(ok.contains("Çok Hızlı"))

        val err = CustomApiTestStatusFormatter.formatResult(isSuccess = false, httpStatusCode = 401, errorDetail = "Unauthorized")
        assertTrue(err.contains("✕ Hata"))
        assertTrue(err.contains("API anahtarın"))
    }
}
