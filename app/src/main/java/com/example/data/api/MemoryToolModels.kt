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
            description = "Konuşma sırasında bağlamsal olarak gerçekten önemli ve kalıcı bir bilgi veya hikâye kırılması fark ettiğinde Neural Vault hafızasına kaydeder. ZORUNLU DİL KURALI: content ve key alanları %100 DOĞAL TÜRKÇE yazılmalıdır; İngilizce açıklama/etiket YASAKTIR. Sıradan sohbet ve fiziksel jestler ASLA kaydedilmez.",
            properties = mapOf(
                "content" to MemoryToolProperty(
                    type = "string",
                    description = "Hatırlanacak net bilgi veya dönüm noktası açıklaması (%100 TÜRKÇE, 3. tekil şahıs, ham diyalog kopyalamadan)"
                ),
                "category" to MemoryToolProperty(
                    type = "string",
                    description = "Bilgi türü: fact (kalıcı bilgi/durum/tercih/ilişki) veya event (yalnızca çok nadir, yüksek etkili hikâye dönüm noktası, importance >= 7)",
                    enum = listOf("fact", "event")
                ),
                "key" to MemoryToolProperty(
                    type = "string",
                    description = "1-3 kelimelik kısa TÜRKÇE konu başlığı (ör. 'Akşam Yemeği', 'Gizli Sır', 'Meslek', 'Güven Durumu' — asla İngilizce değil)"
                ),
                "tier" to MemoryToolProperty(
                    type = "string",
                    description = "Bellek katmanı: ephemeral (2-3 hikâye günü geçici detay), arc (10-14 gün süren görev/yara/küs kalma durumu), core (kalıcı kimlik, sır, kalıcı tercih, ilişki).",
                    enum = listOf("ephemeral", "arc", "core")
                ),
                "ttl_days" to MemoryToolProperty(
                    type = "integer",
                    description = "Opsiyonel hikâye günü ömrü: ephemeral için 2-3, arc için 10-14, core için 0."
                ),
                "importance" to MemoryToolProperty(
                    type = "integer",
                    description = "Önem derecesi 1-10. 1-2: gürültü (kaydedilmez). 3-6: gizli arka plan RAG. 7-10: görünür Neural Vault / Anı."
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
            description = "Var olan bir Neural Vault hafıza kaydını #ID ile günceller (★ kilitli kayıtlar yalnızca kullanıcı açıkça isterse değişir). newContent ve reason %100 TÜRKÇE olmalıdır.",
            properties = mapOf(
                "memoryId" to MemoryToolProperty(
                    type = "string",
                    description = "Güncellenecek hafıza kaydının ID'si (ör. '12', '#12', 'F12') veya eşleşen mevcut metni"
                ),
                "newContent" to MemoryToolProperty(
                    type = "string",
                    description = "Güncellenmiş yeni içerik (%100 TÜRKÇE)"
                ),
                "reason" to MemoryToolProperty(
                    type = "string",
                    description = "Opsiyonel Türkçe güncelleme nedeni"
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
     * Safely parses integer tool arguments whether the LLM/serializer emits an Int, Long,
     * Double (e.g. Gemini JSON numbers like `8.0`), or a numeric String (`"8"` / `"8.0"`).
     */
    fun parseIntArg(raw: Any?, default: Int): Int {
        return when (raw) {
            null -> default
            is Number -> raw.toInt()
            else -> {
                val s = raw.toString().trim()
                s.toIntOrNull() ?: s.toDoubleOrNull()?.toInt() ?: default
            }
        }
    }

    /**
     * Parses text-fallback memory commands emitted by Custom Providers without native function calling:
     * Example: [ACTIVE_MEMORY_CALL: save_memory(content="İstanbul (Kadıköy) [Gizli]", category="fact", importance=8)]
     * Uses a quote-aware, bracket/paren-balanced scanner so values containing `)`, `]`, `,`, or escaped
     * quotes `\"` / `\'` never break parsing.
     */
    fun parseTextFallbackMemoryCalls(responseText: String): List<ParsedMemoryToolCall> {
        if (responseText.isBlank() || !responseText.contains("ACTIVE_MEMORY_CALL", ignoreCase = true)) {
            return emptyList()
        }
        val results = mutableListOf<ParsedMemoryToolCall>()
        val tagHeaderRegex = Regex("""(?i)\[ACTIVE_MEMORY_CALL:\s*""")
        val fnNameSet = setOf("save_memory", "update_memory", "delete_memory")

        var searchFrom = 0
        while (searchFrom < responseText.length) {
            val headerMatch = tagHeaderRegex.find(responseText, searchFrom) ?: break
            val blockStart = headerMatch.range.last + 1
            val blockEnd = findClosingBracketQuoteAware(responseText, blockStart)
            if (blockEnd < 0) break

            val blockBody = responseText.substring(blockStart, blockEnd)
            parseCallsFromBlock(blockBody, fnNameSet, results)
            searchFrom = blockEnd + 1
        }
        return results
    }

    private fun findClosingBracketQuoteAware(text: String, startIndex: Int): Int {
        var i = startIndex
        var inQuote: Char? = null
        var bracketDepth = 0
        while (i < text.length) {
            val ch = text[i]
            if (inQuote != null) {
                if (ch == '\\' && i + 1 < text.length) {
                    i += 2
                    continue
                }
                if (ch == inQuote) inQuote = null
            } else {
                when (ch) {
                    '"', '\'' -> inQuote = ch
                    '[' -> bracketDepth++
                    ']' -> {
                        if (bracketDepth == 0) return i
                        bracketDepth--
                    }
                }
            }
            i++
        }
        return -1
    }

    private fun parseCallsFromBlock(
        blockBody: String,
        validNames: Set<String>,
        out: MutableList<ParsedMemoryToolCall>
    ) {
        val fnStartRegex = Regex("""(?i)\b(save_memory|update_memory|delete_memory)\s*\(""")
        var pos = 0
        while (pos < blockBody.length) {
            val m = fnStartRegex.find(blockBody, pos) ?: break
            val fnName = m.groupValues[1].lowercase().trim()
            val argsStart = m.range.last + 1
            val argsEnd = findClosingParenQuoteAware(blockBody, argsStart)
            if (argsEnd < 0) break

            if (fnName in validNames) {
                val argsMap = parseNamedArguments(blockBody.substring(argsStart, argsEnd))
                if (argsMap.isNotEmpty()) {
                    out.add(ParsedMemoryToolCall(name = fnName, arguments = argsMap))
                }
            }
            pos = argsEnd + 1
        }
    }

    private fun findClosingParenQuoteAware(text: String, startIndex: Int): Int {
        var i = startIndex
        var inQuote: Char? = null
        var parenDepth = 0
        while (i < text.length) {
            val ch = text[i]
            if (inQuote != null) {
                if (ch == '\\' && i + 1 < text.length) {
                    i += 2
                    continue
                }
                if (ch == inQuote) inQuote = null
            } else {
                when (ch) {
                    '"', '\'' -> inQuote = ch
                    '(' -> parenDepth++
                    ')' -> {
                        if (parenDepth == 0) return i
                        parenDepth--
                    }
                }
            }
            i++
        }
        return -1
    }

    private fun parseNamedArguments(argsBody: String): MutableMap<String, Any> {
        val argsMap = mutableMapOf<String, Any>()
        var i = 0
        val len = argsBody.length
        while (i < len) {
            while (i < len && (argsBody[i].isWhitespace() || argsBody[i] == ',')) i++
            if (i >= len) break

            val keyStart = i
            while (i < len && (argsBody[i].isLetterOrDigit() || argsBody[i] == '_')) i++
            val key = argsBody.substring(keyStart, i).trim()
            if (key.isEmpty()) {
                i++
                continue
            }

            while (i < len && argsBody[i].isWhitespace()) i++
            if (i >= len || argsBody[i] != '=') continue
            i++ // skip '='
            while (i < len && argsBody[i].isWhitespace()) i++
            if (i >= len) break

            val value: String = if (argsBody[i] == '"' || argsBody[i] == '\'') {
                val quote = argsBody[i]
                i++
                val sb = StringBuilder()
                while (i < len) {
                    val c = argsBody[i]
                    if (c == '\\' && i + 1 < len) {
                        val next = argsBody[i + 1]
                        sb.append(
                            when (next) {
                                'n' -> '\n'
                                't' -> '\t'
                                else -> next
                            }
                        )
                        i += 2
                        continue
                    }
                    if (c == quote) {
                        i++
                        break
                    }
                    sb.append(c)
                    i++
                }
                sb.toString().trim()
            } else {
                val valStart = i
                while (i < len && argsBody[i] != ',' && !argsBody[i].isWhitespace()) i++
                argsBody.substring(valStart, i).trim()
            }
            argsMap[key] = value
        }
        return argsMap
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
        val rawContentObj = if (messageObj.isNull("content")) null else messageObj.opt("content")
        val text = when (rawContentObj) {
            null -> ""
            is JSONArray -> buildString {
                for (i in 0 until rawContentObj.length()) {
                    val part = rawContentObj.optJSONObject(i)
                    val partText = part?.optString("text", "").orEmpty()
                    if (partText.isNotBlank()) {
                        if (isNotEmpty()) append("\n")
                        append(partText)
                    }
                }
            }
            else -> rawContentObj.toString().let { if (it.equals("null", ignoreCase = true)) "" else it }
        }
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
