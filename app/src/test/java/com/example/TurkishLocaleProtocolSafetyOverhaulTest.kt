package com.example

import com.example.data.api.MemoryToolRegistry
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.EmochiRepository
import com.example.data.repository.LivingMemoryCodec
import com.example.data.repository.MemoryLifecycleCodec
import com.example.util.CustomApiUrlNormalizer
import com.example.util.CustomApiUrlValidationHelper
import com.example.util.SseStreamParser
import com.example.util.StreamingTextSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

/**
 * Verifies that ALL critical API schemas, SSE error/tag parsers, MemoryEngine codecs,
 * and URL/model resolvers remain 100% immune to Turkish dotted-I ("i"->"İ", "I"->"ı")
 * corruption when the device locale is tr-TR.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TurkishLocaleProtocolSafetyOverhaulTest {

    private inline fun withTurkishLocale(block: () -> Unit) {
        val prev = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            block()
        } finally {
            Locale.setDefault(prev)
        }
    }

    @Test
    fun geminiAndOpenAiToolSchemas_neverEmitTurkishDottedIUnderTrLocale() = withTurkishLocale {
        val geminiTools = MemoryToolRegistry.toGeminiTools()
        val decls = geminiTools.first().functionDeclarations.orEmpty()
        assertTrue(decls.isNotEmpty())
        for (decl in decls) {
            val props = decl.parameters?.properties.orEmpty()
            for ((_, prop) in props) {
                assertFalse("Gemini property type must never contain Turkish İ: ${prop.type}", prop.type.contains('İ'))
                assertTrue("Expected ASCII uppercase type STRING/INTEGER/BOOLEAN, got ${prop.type}", prop.type in setOf("STRING", "INTEGER", "BOOLEAN", "NUMBER", "ARRAY", "OBJECT"))
            }
        }
        val claudeArr = MemoryToolRegistry.toClaudeToolsJsonArray()
        for (i in 0 until claudeArr.length()) {
            val props = claudeArr.getJSONObject(i).getJSONObject("input_schema").getJSONObject("properties")
            val keys = props.keys()
            while (keys.hasNext()) {
                val t = props.getJSONObject(keys.next()).getString("type")
                assertFalse("Claude schema type must be ASCII lowercase, got $t", t.contains('ı') || t.contains('İ'))
                assertTrue(t in setOf("string", "integer", "boolean", "number", "array", "object"))
            }
        }
        val openAiArr = MemoryToolRegistry.toOpenAiToolsJsonArray()
        for (i in 0 until openAiArr.length()) {
            val props = openAiArr.getJSONObject(i).getJSONObject("function").getJSONObject("parameters").getJSONObject("properties")
            val keys = props.keys()
            while (keys.hasNext()) {
                val t = props.getJSONObject(keys.next()).getString("type")
                assertFalse("OpenAI schema type must be ASCII lowercase, got $t", t.contains('ı') || t.contains('İ'))
                assertTrue(t in setOf("string", "integer", "boolean", "number", "array", "object"))
            }
        }
    }

    @Test
    fun streamingTextSanitizer_stripsUppercaseThinkAndInitMemoryTagsUnderTrLocale() = withTurkishLocale {
        val sanitizer = StreamingTextSanitizer()
        sanitizer.append("<THINK>gizli analiz</THINK><INTERNAL>iç</INTERNAL>Merhaba! [ACTIVE_MEMORY_CALL save_memory(content=\"x\")]")
        sanitizer.finish()
        assertEquals("Merhaba!", sanitizer.visibleText.trim())
    }

    @Test
    fun memoryCodecsAndSseErrors_areLocaleRootSafeUnderTrLocale() = withTurkishLocale {
        // LivingMemoryCodec header keys ("title", "weight") must not become "TİTLE"/"WEİGHT"
        val raw = "[[MEM|pov:Aiden|title:Kuledeki Gece|emo:umutlu|weight:9]]\nYağmur altında konuşma."
        val parsed = LivingMemoryCodec.parse(raw)
        assertTrue(parsed.isStructured)
        assertEquals("Kuledeki Gece", parsed.title)
        assertEquals(9, parsed.salience)

        // MemoryLifecycleCodec keys ("INFERRED", "HIT") must not become "ınferred"/"hıt"
        val meta = MemoryLifecycleCodec.parse("INFERRED|T:ARC|CD:2|ED:10|HIT:3")
        assertEquals("inferred", meta.baseConfidence)
        assertEquals(3, meta.accessCount)

        // SseStreamParser inline error parsing for INVALID_ARGUMENT / PERMISSION_DENIED
        val errInvalid = SseStreamParser.parseSseLine(
            """data: {"error":{"status":"INVALID_ARGUMENT","message":"Bad schema"}}""",
            SseStreamParser.SseFormat.GEMINI
        )
        assertNotNull(errInvalid)
        assertEquals(400, errInvalid!!.errorCode)

        val errPerm = SseStreamParser.parseSseLine(
            """data: {"error":{"status":"PERMISSION_DENIED","message":"Denied"}}""",
            SseStreamParser.SseFormat.GEMINI
        )
        assertNotNull(errPerm)
        assertEquals(403, errPerm!!.errorCode)

        // URL normalizer & validator + EmochiRepository provider/context helpers
        assertEquals("https://api.example.com/v1", CustomApiUrlNormalizer.normalizeBaseUrl("HTTPS://api.example.com/v1"))
        assertTrue(CustomApiUrlValidationHelper.isValid("HTTPS://api.example.com/v1"))

        val settings = UserSettingsEntity(selectedProvider = "GEMINI", geminiModel = "gemini-2.5-flash")
        assertEquals("gemini-2.5-flash", EmochiRepository.getEffectiveModelForProvider(settings))
    }
}
