package com.example.data.api

import org.json.JSONArray
import org.json.JSONObject

data class MemoryToolProperty(
    val type: String = "string",
    val description: String,
    val enum: List<String>? = null
)

data class MemoryToolDefinition(
    val name: String,
    val description: String,
    val properties: Map<String, MemoryToolProperty>,
    val required: List<String>
)

data class ParsedMemoryToolCall(
    val name: String,
    val arguments: Map<String, Any>
)

data class ModelResponseResult(
    val text: String,
    val toolCalls: List<ParsedMemoryToolCall> = emptyList(),
    val promptTokens: Long = 0L,
    val candidateTokens: Long = 0L,
    val usedProvider: String = ""
)

object MemoryToolRegistry {
    val CENTRAL_TOOLS: List<MemoryToolDefinition> = listOf(
        MemoryToolDefinition(
            name = "save_memory",
            description = "Konuşma sırasında bağlamsal olarak anlamlı bir bilgi veya geçici/kalıcı olay fark ettiğinde Neural Vault hafızasına kaydeder. 'Elini masaya koydu', 'derin nefes aldı', 'pencereden baktı' gibi sıradan fiziksel hareketler ASLA kaydedilmez; ancak aynı hareket gizli kanıt saklamak veya ilk kez güvenmek gibi olay örgüsünü değiştiriyorsa kaydedilir. Günlük geçici detaylar (yenen yemek, giyilen ceket, baş ağrısı) tier='ephemeral' (ttl_days=2..3) ile kaydedilir.",
            properties = mapOf(
                "content" to MemoryToolProperty(
                    type = "string",
                    description = "Hatırlanacak net bilgi veya olay açıklaması"
                ),
                "category" to MemoryToolProperty(
                    type = "string",
                    description = "Bilgi türü: fact (bilgi/durum/tercih/ilişki) veya event (yaşanan önemli olay/söz)",
                    enum = listOf("fact", "event")
                ),
                "key" to MemoryToolProperty(
                    type = "string",
                    description = "Opsiyonel 1-3 kelimelik kısa Türkçe konu başlığı (ör. 'Akşam Yemeği', 'Gizli Sır', 'Meslek', 'Güven Durumu')"
                ),
                "tier" to MemoryToolProperty(
                    type = "string",
                    description = "Bellek katmanı: ephemeral (2-3 hikâye günü geçici detay: yemek, kıyafet, hafif ağrı), arc (10-14 gün süren görev/yara/küs kalma durumu), core (kalıcı kimlik, sır, kalıcı tercih, ilişki).",
                    enum = listOf("ephemeral", "arc", "core")
                ),
                "ttl_days" to MemoryToolProperty(
                    type = "string",
                    description = "Opsiyonel hikâye günü ömrü: ephemeral için 2-3, arc için 10-14, core için 0."
                ),
                "importance" to MemoryToolProperty(
                    type = "string",
                    description = "Önem derecesi 1-10. 2 ve altı: gürültü (kaydedilmez). 3-5: geçici (ephemeral). 6-7: orta/arc. 8+: kalıcı/çok önemli (core)."
                ),
                "subject" to MemoryToolProperty(
                    type = "string",
                    description = "Bilginin kimin hakkında olduğu: 'kullanıcı', 'karakter', veya sahnedeki karakterin adı (ör. 'Natasha', 'Aiden')."
                )
            ),
            required = listOf("content", "category")
        ),
        MemoryToolDefinition(
            name = "update_memory",
            description = "Var olan bir Neural Vault hafıza kaydını #ID ile günceller (★ kilitli kayıtlar yalnızca kullanıcı açıkça isterse değişir).",
            properties = mapOf(
                "memoryId" to MemoryToolProperty(
                    type = "string",
                    description = "Güncellenecek hafıza kaydının ID'si (ör. '12', '#12', 'F12') veya eşleşen mevcut metni"
                ),
                "newContent" to MemoryToolProperty(
                    type = "string",
                    description = "Güncellenmiş yeni içerik"
                ),
                "reason" to MemoryToolProperty(
                    type = "string",
                    description = "Opsiyonel güncelleme nedeni"
                )
            ),
            required = listOf("memoryId", "newContent")
        ),
        MemoryToolDefinition(
            name = "delete_memory",
            description = "Geçersizleşmiş veya yanlış olduğu belirtilen bir Neural Vault kaydını #ID ile arşive kaldırarak siler.",
            properties = mapOf(
                "memoryId" to MemoryToolProperty(
                    type = "string",
                    description = "Silinecek hafıza kaydının ID'si (ör. '12', '#12', 'F12') veya eşleşen mevcut metni"
                ),
                "reason" to MemoryToolProperty(
                    type = "string",
                    description = "Silme / arşivleme nedeni"
                )
            ),
            required = listOf("memoryId")
        )
    )

    /**
     * Tolerantly extracts a numeric DB ID from model arguments such as "12", "#12", "[#12]", "[#12★]", "F12", "E12".
     * Returns null when the model supplied descriptive text instead of a numeric ID so callers can fall back to semantic lookup.
     */
    fun parseMemoryId(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()
        val match = Regex("""^\[?\s*[#fFeEmM]?\s*(\d+)\s*★?\s*\]?$""").find(trimmed)
        return match?.groupValues?.getOrNull(1)?.toLongOrNull()
    }

    /**
     * Parses text-fallback memory commands emitted by Custom Providers without native function calling:
     * Example: [ACTIVE_MEMORY_CALL: save_memory(content="...", category="fact", key="Meslek", importance=8)]
     * Supports multiple pipe-separated calls inside one tag or multiple tags in the same response.
     */
    fun parseTextFallbackMemoryCalls(responseText: String): List<ParsedMemoryToolCall> {
        if (responseText.isBlank() || !responseText.contains("ACTIVE_MEMORY_CALL", ignoreCase = true)) {
            return emptyList()
        }
        val results = mutableListOf<ParsedMemoryToolCall>()
        val blockRegex = Regex("""(?is)\[ACTIVE_MEMORY_CALL:\s*(.*?)\]""")
        val callRegex = Regex("""(?is)\b(save_memory|update_memory|delete_memory)\s*\(([^)]*)\)""")
        val argRegex = Regex("""(?is)([a-zA-Z_][a-zA-Z0-9_]*)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^,)\s]+))""")

        for (blockMatch in blockRegex.findAll(responseText)) {
            val blockBody = blockMatch.groupValues[1]
            for (callMatch in callRegex.findAll(blockBody)) {
                val fnName = callMatch.groupValues[1].lowercase().trim()
                val argsBody = callMatch.groupValues[2]
                val argsMap = mutableMapOf<String, Any>()
                for (argMatch in argRegex.findAll(argsBody)) {
                    val argName = argMatch.groupValues[1].trim()
                    val argVal = argMatch.groupValues[2].ifEmpty {
                        argMatch.groupValues[3].ifEmpty { argMatch.groupValues[4] }
                    }.trim()
                    argsMap[argName] = argVal
                }
                if (argsMap.isNotEmpty()) {
                    results.add(ParsedMemoryToolCall(name = fnName, arguments = argsMap))
                }
            }
        }
        return results
    }

    // 1) Gemini Mapper (GeminiTool format)
    fun toGeminiTools(definitions: List<MemoryToolDefinition> = CENTRAL_TOOLS): List<GeminiTool> {
        val decls = definitions.map { def ->
            val props = def.properties.mapValues { (_, prop) ->
                GeminiFunctionProperty(
                    type = prop.type.uppercase(),
                    description = prop.description,
                    enum = prop.enum
                )
            }
            GeminiFunctionDeclaration(
                name = def.name,
                description = def.description,
                parameters = GeminiFunctionParameters(
                    type = "OBJECT",
                    properties = props,
                    required = def.required
                )
            )
        }
        return listOf(GeminiTool(functionDeclarations = decls))
    }

    // 2) Claude (Anthropic) Mapper: input_schema + lowercase "object" / "string"
    fun toClaudeToolsJsonArray(definitions: List<MemoryToolDefinition> = CENTRAL_TOOLS): JSONArray {
        val array = JSONArray()
        for (def in definitions) {
            val toolObj = JSONObject()
            toolObj.put("name", def.name)
            toolObj.put("description", def.description)

            val inputSchema = JSONObject()
            inputSchema.put("type", "object")

            val propsObj = JSONObject()
            for ((key, prop) in def.properties) {
                val propObj = JSONObject()
                propObj.put("type", prop.type.lowercase())
                propObj.put("description", prop.description)
                if (!prop.enum.isNullOrEmpty()) {
                    val enumArr = JSONArray()
                    prop.enum.forEach { enumArr.put(it) }
                    propObj.put("enum", enumArr)
                }
                propsObj.put(key, propObj)
            }
            inputSchema.put("properties", propsObj)

            val reqArr = JSONArray()
            def.required.forEach { reqArr.put(it) }
            inputSchema.put("required", reqArr)

            toolObj.put("input_schema", inputSchema)
            array.put(toolObj)
        }
        return array
    }

    // 3) OpenAI / Groq Mapper: type: "function" + parameters: { type: "object", ... }
    fun toOpenAiToolsJsonArray(definitions: List<MemoryToolDefinition> = CENTRAL_TOOLS): JSONArray {
        val array = JSONArray()
        for (def in definitions) {
            val item = JSONObject()
            item.put("type", "function")

            val funcObj = JSONObject()
            funcObj.put("name", def.name)
            funcObj.put("description", def.description)

            val paramsObj = JSONObject()
            paramsObj.put("type", "object")

            val propsObj = JSONObject()
            for ((key, prop) in def.properties) {
                val propObj = JSONObject()
                propObj.put("type", prop.type.lowercase())
                propObj.put("description", prop.description)
                if (!prop.enum.isNullOrEmpty()) {
                    val enumArr = JSONArray()
                    prop.enum.forEach { enumArr.put(it) }
                    propObj.put("enum", enumArr)
                }
                propsObj.put(key, propObj)
            }
            paramsObj.put("properties", propsObj)

            val reqArr = JSONArray()
            def.required.forEach { reqArr.put(it) }
            paramsObj.put("required", reqArr)

            funcObj.put("parameters", paramsObj)
            item.put("function", funcObj)

            array.put(item)
        }
        return array
    }

    // Response Parsers

    // Claude (Anthropic) Response Parser
    fun parseClaudeToolCalls(jsonResponse: JSONObject): Pair<String, List<ParsedMemoryToolCall>> {
        val contentArray = jsonResponse.optJSONArray("content") ?: return Pair("", emptyList())
        val textParts = mutableListOf<String>()
        val toolCalls = mutableListOf<ParsedMemoryToolCall>()

        for (i in 0 until contentArray.length()) {
            val item = contentArray.optJSONObject(i) ?: continue
            val type = item.optString("type")
            if (type == "text") {
                val txt = item.optString("text")
                if (txt.isNotBlank()) textParts.add(txt)
            } else if (type == "tool_use") {
                val name = item.optString("name")
                val inputObj = item.optJSONObject("input")
                val argsMap = mutableMapOf<String, Any>()
                if (inputObj != null) {
                    val keys = inputObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        argsMap[k] = inputObj.opt(k)?.toString() ?: ""
                    }
                }
                if (name.isNotBlank()) {
                    toolCalls.add(ParsedMemoryToolCall(name = name, arguments = argsMap))
                }
            }
        }
        return Pair(textParts.joinToString("\n").trim(), toolCalls)
    }

    // OpenAI / Groq Response Parser
    fun parseOpenAiToolCalls(messageObj: JSONObject): Pair<String, List<ParsedMemoryToolCall>> {
        val text = messageObj.optString("content", "") ?: ""
        val toolCalls = mutableListOf<ParsedMemoryToolCall>()

        val toolCallsArray = messageObj.optJSONArray("tool_calls")
        if (toolCallsArray != null) {
            for (i in 0 until toolCallsArray.length()) {
                val callObj = toolCallsArray.optJSONObject(i) ?: continue
                val funcObj = callObj.optJSONObject("function") ?: continue
                val name = funcObj.optString("name")
                val argsStr = funcObj.optString("arguments", "{}")

                val argsMap = mutableMapOf<String, Any>()
                try {
                    val argsJson = JSONObject(argsStr)
                    val keys = argsJson.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        argsMap[k] = argsJson.opt(k)?.toString() ?: ""
                    }
                } catch (_: Exception) {}

                if (name.isNotBlank()) {
                    toolCalls.add(ParsedMemoryToolCall(name = name, arguments = argsMap))
                }
            }
        }
        return Pair(text.trim(), toolCalls)
    }

    // Gemini Response Parser
    fun parseGeminiToolCalls(candidate: GeminiCandidate?): Pair<String, List<ParsedMemoryToolCall>> {
        val parts = candidate?.content?.parts ?: emptyList()
        val textParts = mutableListOf<String>()
        val toolCalls = mutableListOf<ParsedMemoryToolCall>()

        for (p in parts) {
            val pText = p.text
            if (!pText.isNullOrBlank()) {
                textParts.add(pText)
            }
            val fc = p.functionCall
            if (fc != null) {
                val argsMap = fc.args?.mapValues { it.value as Any } ?: emptyMap()
                toolCalls.add(ParsedMemoryToolCall(name = fc.name, arguments = argsMap))
            }
        }
        return Pair(textParts.joinToString("\n").trim(), toolCalls)
    }
}
