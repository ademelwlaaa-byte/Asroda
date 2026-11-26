package com.example.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Sistem #23: Çoklu Format Gerçek Zamanlı Server-Sent Events (SSE) Ayrıştırıcı.
 *
 * Desteklenen formatlar:
 * - **OpenAI-Compatible SSE** (OpenAI, OpenRouter, Groq, DeepSeek, Nvidia, Mistral, Together, Ollama, Custom):
 *   `data: {"choices":[{"delta":{"content":"..."}}]}` + `delta.reasoning_content` + `data: [DONE]`
 * - **Google Gemini SSE** (`streamGenerateContent?alt=sse`):
 *   `data: {"candidates":[{"content":{"parts":[{"text":"..."}]}}]}`
 * - Yorum/heartbeat satırlarını (`: OPENROUTER PROCESSING`, `: keep-alive`) ve yarım/bozuk JSON'ları
 *   çökmeden güvenle atlar; coroutine iptalinde OkHttp soketini anında kapatır.
 */
object SseStreamParser {

    enum class SseFormat {
        AUTO,
        OPENAI,
        GEMINI,
        CLAUDE
    }

    data class StreamChunk(
        val token: String,
        val isDone: Boolean = false,
        val reasoningToken: String = "",
        val promptTokens: Long = 0L,
        val candidateTokens: Long = 0L,
        val finishReason: String = "",
        val errorMessage: String = "",
        val errorCode: Int = 0
    )

    class SseHttpException(
        val httpCode: Int,
        val errorBody: String,
        val retryAfterSeconds: Long? = null
    ) : IllegalStateException("SSE HTTP $httpCode: ${errorBody.take(240)}")

    /**
     * Tek bir ham SSE satırını ayrıştırır. Yorum (`: ...`), boş satır veya bozuk JSON'da `null` döner.
     */
    fun parseSseLine(
        rawLine: String,
        format: SseFormat = SseFormat.AUTO
    ): StreamChunk? {
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty() || trimmed.startsWith(":")) return null
        if (!trimmed.startsWith("data:")) return null

        val payload = trimmed.removePrefix("data:").trim()
        if (payload.isEmpty()) return null
        if (payload == "[DONE]") {
            return StreamChunk(token = "", isDone = true)
        }

        val json = try {
            JSONObject(payload)
        } catch (_: Exception) {
            return null
        }

        // Inline HTTP-200 SSE error payload (OpenRouter, Groq, Anthropic, Gemini proxies)
        parseInlineSseError(json)?.let { return it }

        // OpenAI-compatible SSE chunk
        if (format == SseFormat.OPENAI || (format == SseFormat.AUTO && json.has("choices"))) {
            val usage = json.optJSONObject("usage")
            val pTok = usage?.optLong("prompt_tokens", 0L) ?: 0L
            val cTok = usage?.optLong("completion_tokens", 0L) ?: 0L

            val choices = json.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                return if (pTok > 0L || cTok > 0L) {
                    StreamChunk(token = "", promptTokens = pTok, candidateTokens = cTok)
                } else null
            }

            val firstChoice = choices.optJSONObject(0) ?: return null
            val finishReason = extractSafeJsonString(firstChoice, "finish_reason")
            val delta = firstChoice.optJSONObject("delta") ?: firstChoice.optJSONObject("message")
            val content = extractSafeJsonString(delta, "content")
            val reasoning = extractSafeJsonString(delta, "reasoning_content")
                .ifEmpty { extractSafeJsonString(delta, "reasoning") }

            if (content.isNotEmpty() || reasoning.isNotEmpty() || pTok > 0L || cTok > 0L || finishReason.isNotEmpty()) {
                return StreamChunk(
                    token = content,
                    isDone = false,
                    reasoningToken = reasoning,
                    promptTokens = pTok,
                    candidateTokens = cTok,
                    finishReason = finishReason
                )
            }
            return null
        }

        // Google Gemini SSE chunk
        if (format == SseFormat.GEMINI || (format == SseFormat.AUTO && json.has("candidates"))) {
            val usage = json.optJSONObject("usageMetadata")
            val pTok = usage?.optLong("promptTokenCount", 0L) ?: 0L
            val cTok = usage?.optLong("candidatesTokenCount", 0L) ?: 0L

            val candidates = json.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.optJSONObject(0) ?: return null
            val finishReason = extractSafeJsonString(firstCandidate, "finishReason")
            val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts")
            val tokenBuilder = StringBuilder()
            val reasoningBuilder = StringBuilder()

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i) ?: continue
                    val text = extractSafeJsonString(part, "text")
                    if (text.isEmpty()) continue
                    if (part.optBoolean("thought", false)) {
                        reasoningBuilder.append(text)
                    } else {
                        tokenBuilder.append(text)
                    }
                }
            }

            if (tokenBuilder.isNotEmpty() || reasoningBuilder.isNotEmpty() || pTok > 0L || cTok > 0L || finishReason.isNotEmpty()) {
                return StreamChunk(
                    token = tokenBuilder.toString(),
                    isDone = false,
                    reasoningToken = reasoningBuilder.toString(),
                    promptTokens = pTok,
                    candidateTokens = cTok,
                    finishReason = finishReason
                )
            }
            return null
        }

        // Anthropic / Claude SSE chunk (`message_start`, `content_block_delta`, `message_delta`, `message_stop`)
        if (format == SseFormat.CLAUDE || (format == SseFormat.AUTO && json.has("type"))) {
            return when (json.optString("type")) {
                "message_start" -> {
                    val inTok = json.optJSONObject("message")
                        ?.optJSONObject("usage")
                        ?.optLong("input_tokens", 0L) ?: 0L
                    if (inTok > 0L) StreamChunk(token = "", promptTokens = inTok) else null
                }
                "content_block_delta" -> {
                    val delta = json.optJSONObject("delta") ?: return null
                    val deltaType = delta.optString("type")
                    when (deltaType) {
                        "thinking_delta" -> {
                            val think = extractSafeJsonString(delta, "thinking")
                            if (think.isNotEmpty()) StreamChunk(token = "", reasoningToken = think) else null
                        }
                        else -> {
                            val text = extractSafeJsonString(delta, "text")
                            if (text.isNotEmpty()) StreamChunk(token = text) else null
                        }
                    }
                }
                "message_delta" -> {
                    val delta = json.optJSONObject("delta")
                    val stopReason = extractSafeJsonString(delta, "stop_reason")
                    val usage = json.optJSONObject("usage")
                    val outTok = usage?.optLong("output_tokens", 0L) ?: 0L
                    if (stopReason.isNotEmpty() || outTok > 0L) {
                        StreamChunk(token = "", candidateTokens = outTok, finishReason = stopReason)
                    } else null
                }
                "message_stop" -> StreamChunk(token = "", isDone = true)
                else -> null
            }
        }

        return null
    }

    private fun parseInlineSseError(json: JSONObject): StreamChunk? {
        val isExplicitErrorType = json.optString("type").equals("error", ignoreCase = true)
        if (!isExplicitErrorType && (!json.has("error") || json.isNull("error"))) return null

        val errObj = json.optJSONObject("error")
        val rawErrStr = if (errObj == null) extractSafeJsonString(json, "error") else ""
        val message = errObj?.let {
            extractSafeJsonString(it, "message")
                .ifBlank { extractSafeJsonString(it, "type") }
                .ifBlank { it.toString() }
        }?.ifBlank { rawErrStr }
            ?: rawErrStr.ifBlank { extractSafeJsonString(json, "message") }

        if (message.isBlank()) return null

        val numericCode = errObj?.optInt("code", 0) ?: json.optInt("code", 0)
        val errType = errObj?.optString("type", "").orEmpty().lowercase()
        val statusStr = errObj?.optString("status", "").orEmpty().uppercase()
        val resolvedCode = when {
            numericCode in 400..599 -> numericCode
            errType.contains("overloaded") || statusStr == "UNAVAILABLE" -> 529
            errType.contains("rate_limit") || statusStr == "RESOURCE_EXHAUSTED" -> 429
            errType.contains("authentication") || statusStr == "UNAUTHENTICATED" -> 401
            errType.contains("permission") || statusStr == "PERMISSION_DENIED" -> 403
            errType.contains("not_found") || statusStr == "NOT_FOUND" -> 404
            errType.contains("invalid_request") || statusStr == "INVALID_ARGUMENT" -> 400
            else -> 500
        }
        return StreamChunk(
            token = "",
            isDone = true,
            errorMessage = message,
            errorCode = resolvedCode
        )
    }

    /**
     * Bir [BufferedReader] akışından tüm SSE satırlarını sıfır bellek şişirmesiyle satır satır okur.
     */
    suspend fun parseSseReader(
        reader: BufferedReader,
        format: SseFormat = SseFormat.AUTO,
        onChunk: suspend (StreamChunk) -> Unit
    ) {
        var line: String?
        var sawDataPrefix = false
        val nonSseJsonPreview = StringBuilder()
        while (reader.readLine().also { line = it } != null) {
            currentCoroutineContext().ensureActive()
            val trimmed = line!!.trim()
            if (trimmed.startsWith("data:")) {
                sawDataPrefix = true
            } else if (!sawDataPrefix && trimmed.isNotEmpty() && !trimmed.startsWith(":") && !trimmed.startsWith("event:")) {
                if (nonSseJsonPreview.length < 2048) {
                    nonSseJsonPreview.append(trimmed)
                }
            }
            val chunk = parseSseLine(line!!, format) ?: continue
            onChunk(chunk)
            if (chunk.isDone) return
        }
        // Some OpenAI/Gemini proxies reply HTTP 200 with plain JSON {"error":{...}} instead of SSE
        if (!sawDataPrefix && nonSseJsonPreview.isNotEmpty()) {
            val preview = nonSseJsonPreview.toString().trim()
            if (preview.startsWith("{")) {
                try {
                    parseInlineSseError(JSONObject(preview))?.let { errChunk ->
                        onChunk(errChunk)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Birim testleri ve çevrimdışı ayrıştırma için saf satır listesi ayrıştırıcısı.
     */
    fun parseSseLines(
        lines: Iterable<String>,
        format: SseFormat = SseFormat.AUTO
    ): List<StreamChunk> {
        val result = mutableListOf<StreamChunk>()
        for (line in lines) {
            val chunk = parseSseLine(line, format) ?: continue
            result.add(chunk)
            if (chunk.isDone) break
        }
        return result
    }

    /**
     * Coroutine iptal edildiğinde OkHttp çağrısını anında kapatan canlı SSE akışı.
     */
    fun stream(
        client: OkHttpClient,
        request: Request,
        format: SseFormat = SseFormat.AUTO
    ): Flow<StreamChunk> = flow {
        val call = client.newCall(request)
        val response = call.awaitResponseCancellable()
        response.use { resp ->
            if (!resp.isSuccessful) {
                val errBody = try { resp.body?.string().orEmpty() } catch (_: Exception) { "" }
                val retryAfter = resp.header("Retry-After")?.trim()?.toLongOrNull()
                throw SseHttpException(resp.code, errBody.ifBlank { "HTTP ${resp.code}" }, retryAfter)
            }

            val body = resp.body ?: throw IllegalStateException("SSE yanıt gövdesi boş.")
            BufferedReader(body.charStream()).use { reader ->
                var sawDone = false
                try {
                    parseSseReader(reader, format) { chunk ->
                        if (chunk.errorMessage.isNotEmpty()) {
                            val code = if (chunk.errorCode in 400..599) chunk.errorCode else 500
                            throw SseHttpException(code, chunk.errorMessage)
                        }
                        if (chunk.isDone) sawDone = true
                        emit(chunk)
                    }
                    if (!sawDone) {
                        emit(StreamChunk(token = "", isDone = true))
                    }
                } catch (ce: kotlinx.coroutines.CancellationException) {
                    try { call.cancel() } catch (_: Throwable) {}
                    throw ce
                }
            }
        }
    }

    private suspend fun Call.awaitResponseCancellable(): Response = suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation {
            try { cancel() } catch (_: Throwable) {}
        }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (cont.isActive) {
                    cont.resume(response)
                } else {
                    response.close()
                }
            }
        })
    }

    private fun extractSafeJsonString(obj: JSONObject?, key: String): String {
        if (obj == null || !obj.has(key) || obj.isNull(key)) return ""
        val value = obj.opt(key) ?: return ""
        if (value === JSONObject.NULL) return ""
        return value.toString()
    }

    /**
     * OpenAI uyumlu bir istek oluşturur (`stream = true`, `Accept: text/event-stream`, `Cache-Control: no-cache`).
     */
    fun buildStreamRequestBody(
        endpointUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        messagesJson: JSONArray
    ): Request {
        val effectiveMessages = JSONArray()
        var hasSystem = false
        for (i in 0 until messagesJson.length()) {
            val item = messagesJson.optJSONObject(i)
            if (item?.optString("role") == "system") hasSystem = true
        }
        if (!hasSystem && systemPrompt.isNotBlank()) {
            effectiveMessages.put(JSONObject().put("role", "system").put("content", systemPrompt))
        }
        for (i in 0 until messagesJson.length()) {
            effectiveMessages.put(messagesJson.get(i))
        }

        val body = JSONObject().apply {
            put("model", model)
            put("messages", effectiveMessages)
            put("temperature", 0.90)
            put("top_p", 0.95)
            put("stream", true)
        }

        val reqBody = body.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val builder = Request.Builder()
            .url(endpointUrl)
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .post(reqBody)

        if (apiKey.isNotBlank()) {
            builder.header("Authorization", "Bearer $apiKey")
        }
        return builder.build()
    }
}