package com.example.data.managers

import com.example.data.api.LLMAdapterFactory
import com.example.data.api.MemoryToolDefinition
import com.example.data.api.MemoryToolRegistry
import com.example.data.api.ModelResponseResult
import com.example.data.api.RetrofitClient
import com.example.data.local.CustomProviderDao
import com.example.data.local.CustomProviderEntity
import com.example.data.local.MessageEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.EmochiRepository
import com.example.data.repository.ProviderFallbackLogEntry
import com.example.util.AppFeatureFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ProviderFallbackManager(
    private val customProviderDao: CustomProviderDao,
    private val onLogFallback: (ProviderFallbackLogEntry) -> Unit,
    private val customExecutor: (suspend (
        providerKey: String,
        resolvedModel: String,
        systemPrompt: String,
        messages: List<MessageEntity>,
        tools: List<MemoryToolDefinition>
    ) -> ModelResponseResult)? = null
) {
    enum class ExecutionMode {
        NARRATIVE,
        UTILITY
    }

    fun buildProviderChain(
        settings: UserSettingsEntity,
        customProviders: Map<Long, CustomProviderEntity>,
        buildConfigGeminiKey: String
    ): List<String> {
        val freeFirst = ApiPriorityCodec.isFreeFirst(settings.fallbackChainOrder)
        val hasBuildGemini = buildConfigGeminiKey.isNotBlank() && buildConfigGeminiKey != "MY_GEMINI_API_KEY"

        fun isConfigured(providerKey: String): Boolean = when (providerKey) {
            "gemini" -> settings.customApiKey.isNotBlank() || hasBuildGemini
            "groq" -> settings.groqApiKey.isNotBlank()
            "openai" -> settings.openaiApiKey.isNotBlank()
            "claude" -> settings.claudeApiKey.isNotBlank()
            "openrouter" -> settings.openRouterApiKey.isNotBlank()
            "nvidia" -> settings.nvidiaApiKey.isNotBlank()
            "mistral" -> settings.mistralApiKey.isNotBlank()
            "llm7" -> settings.enableLlm7 && settings.llm7Enabled
            "pollinations" -> settings.enablePollinations && settings.pollinationsEnabled
            "ovh" -> settings.enableOvh && settings.ovhEnabled &&
                System.currentTimeMillis() > EmochiRepository.ovhCooldownUntilTimestamp
            else -> if (providerKey.startsWith("custom_")) {
                val id = providerKey.removePrefix("custom_").toLongOrNull()
                id != null && customProviders.containsKey(id)
            } else false
        }

        // 1. User-ordered keyed & custom APIs (#1 = first preference, #2 = second, ...)
        val keyedOrdered = linkedSetOf<String>()
        val resolvedKeyed = ApiPriorityCodec.resolveOrderedConfiguredKeyedProviders(
            settings = settings,
            customProviders = customProviders.values.toList()
        )
        for (k in resolvedKeyed) {
            if (isConfigured(k)) keyedOrdered.add(k)
        }
        // Fallback if BuildConfig provides Gemini key even without user-entered key
        if (keyedOrdered.isEmpty() && hasBuildGemini) {
            keyedOrdered.add("gemini")
        }

        // 2. Enabled free keyless providers
        val freeEnabled = ApiProviderCatalog.FREE_KEYLESS_IDS.filter { isConfigured(it) }

        val finalChain = linkedSetOf<String>()
        if (freeFirst) {
            finalChain.addAll(freeEnabled)
            if (settings.enableAutoFallback || freeEnabled.isEmpty()) {
                finalChain.addAll(keyedOrdered)
            }
        } else {
            finalChain.addAll(keyedOrdered)
            if (settings.enableAutoFallback || keyedOrdered.isEmpty()) {
                finalChain.addAll(freeEnabled)
            }
        }

        if (finalChain.isEmpty()) {
            finalChain.add("pollinations")
            finalChain.add("llm7")
        }
        return finalChain.toList()
    }

    suspend fun executeWithFallback(
        model: String,
        settings: UserSettingsEntity,
        systemPrompt: String,
        messages: List<MessageEntity>,
        buildConfigGeminiKey: String,
        tools: List<MemoryToolDefinition> = emptyList(),
        callType: String = com.example.EmochiApplication.CALL_TYPE_NARRATIVE,
        executionMode: ExecutionMode? = null
    ): ModelResponseResult {
        val resolvedCallType = when (executionMode) {
            ExecutionMode.UTILITY -> com.example.EmochiApplication.CALL_TYPE_UTILITY
            ExecutionMode.NARRATIVE -> com.example.EmochiApplication.CALL_TYPE_NARRATIVE
            null -> callType
        }
        return com.example.EmochiApplication.withCallType(resolvedCallType) {
            withContext(Dispatchers.IO) {
                val customList = try {
                    customProviderDao.getAllProviders()
                } catch (_: Exception) {
                    emptyList()
                }
                val customMap = customList.associateBy { it.id }
                val rawChain = buildProviderChain(settings, customMap, buildConfigGeminiKey)
                val effectiveChain = if (settings.enableAutoFallback) {
                    rawChain
                } else {
                    rawChain.take(1)
                }

                // Circuit breaker ordering: bypass cooled-down providers unless ALL are in cooldown
                val candidates = ProviderHealthTracker.selectOrderedCandidates(effectiveChain)
                val skippedSet = effectiveChain.filter { it !in candidates }
                for (skippedKey in skippedSet) {
                    val remSec = (ProviderHealthTracker.remainingCooldownMs(skippedKey) / 1000L).coerceAtLeast(1L)
                    onLogFallback(
                        ProviderFallbackLogEntry(
                            providerName = skippedKey,
                            status = "COOLDOWN_SKIP",
                            errorMessage = "Devre kesici aktif (${remSec}s kaldı)",
                            layer = effectiveChain.indexOf(skippedKey) + 1
                        )
                    )
                }

                val primarySelected = settings.selectedProvider.lowercase().trim()
                val isUtility = resolvedCallType.equals(com.example.EmochiApplication.CALL_TYPE_UTILITY, ignoreCase = true)
                val refusalGuardEnabled = !isUtility && AppFeatureFlags.isRefusalAutoFallbackEnabled

                var lastError: Exception? = null
                var lastRefusalResult: ModelResponseResult? = null

                for ((idx, providerKey) in candidates.withIndex()) {
                    val layer = idx + 1
                    val customEntity = if (providerKey.startsWith("custom_")) {
                        providerKey.removePrefix("custom_").toLongOrNull()?.let { customMap[it] }
                    } else null

                    val resolvedModel = when {
                        isUtility -> EmochiRepository.sanitizeModelName(
                            EmochiRepository.resolveUtilityModel(providerKey, settings, customEntity)
                        )
                        idx == 0 && providerKey == primarySelected && model.isNotBlank() ->
                            EmochiRepository.sanitizeModelName(model)
                        providerKey == "groq" -> EmochiRepository.sanitizeModelName(settings.groqModel.ifBlank { "openai/gpt-oss-120b" })
                        providerKey == "openai" -> settings.openaiModel.ifBlank { "gpt-4o" }
                        providerKey == "gemini" -> EmochiRepository.sanitizeModelName(settings.geminiModel.ifBlank { "gemini-2.0-flash" })
                        providerKey == "claude" -> settings.claudeModel.ifBlank { "claude-3-5-sonnet-20241022" }
                        providerKey == "openrouter" -> settings.openRouterModel.ifBlank { "deepseek/deepseek-chat" }
                        providerKey == "nvidia" -> settings.nvidiaModel.ifBlank { "meta/llama-3.3-70b-instruct" }
                        providerKey == "mistral" -> settings.mistralModel.ifBlank { "mistral-large-latest" }
                        providerKey == "pollinations" -> settings.pollinationsModel.ifBlank { "openai" }
                        providerKey == "ovh" -> settings.ovhModel.ifBlank { "Meta-Llama-3_3-70B-Instruct" }
                        providerKey == "llm7" -> "default"
                        customEntity != null -> customEntity.modelName
                        else -> "gemini-2.0-flash"
                    }

                    onLogFallback(
                        ProviderFallbackLogEntry(
                            providerName = "$providerKey ($resolvedModel)",
                            status = "TRYING",
                            layer = layer
                        )
                    )

                    val startMs = System.currentTimeMillis()
                    try {
                        val candidateResult = if (customExecutor != null) {
                            customExecutor.invoke(providerKey, resolvedModel, systemPrompt, messages, if (!isUtility) tools else emptyList())
                        } else {
                            val adapter = LLMAdapterFactory.createAdapter(
                                providerKey,
                                resolvedModel,
                                settings,
                                customEntity,
                                buildConfigGeminiKey
                            )
                            val effectiveTools = if (!isUtility && adapter.supportsFunctionCalling()) tools else emptyList()
                            val llmResp = adapter.sendMessage(systemPrompt, messages, effectiveTools)
                            ModelResponseResult(
                                text = llmResp.text,
                                toolCalls = llmResp.toolCalls ?: emptyList(),
                                promptTokens = llmResp.usage?.promptTokens ?: 0L,
                                candidateTokens = llmResp.usage?.candidateTokens ?: 0L,
                                usedProvider = llmResp.usedProvider.ifBlank { providerKey }
                            )
                        }
                        val latencyMs = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                        val cleanText = EmochiRepository.cleanEmotionTags(candidateResult.text)

                        if (cleanText.isBlank() && candidateResult.toolCalls.isEmpty() && candidateResult.text.isBlank()) {
                            ProviderHealthTracker.recordFailure(
                                providerKey = providerKey,
                                failureType = ProviderFailureType.EMPTY_RESPONSE,
                                errorMessage = "Boş yanıt döndü"
                            )
                            throw IllegalStateException("Sağlayıcı ($providerKey) boş yanıt döndürdü.")
                        }

                        // Configurable OOC Censorship / Moralizing Protection
                        if (refusalGuardEnabled && cleanText.isNotBlank()) {
                            val refusalCheck = RefusalDetector.detectOocRefusal(cleanText)
                            if (refusalCheck.isRefusal) {
                                ProviderHealthTracker.recordFailure(
                                    providerKey = providerKey,
                                    failureType = ProviderFailureType.CONTENT_REFUSAL,
                                    errorMessage = refusalCheck.matchedReason
                                )
                                lastRefusalResult = candidateResult
                                val hasNextProvider = idx < candidates.lastIndex
                                onLogFallback(
                                    ProviderFallbackLogEntry(
                                        providerName = "$providerKey ($resolvedModel)",
                                        status = "OOC_REFUSAL_FALLBACK",
                                        errorMessage = refusalCheck.matchedReason,
                                        layer = layer
                                    )
                                )
                                if (hasNextProvider) {
                                    continue
                                } else {
                                    return@withContext candidateResult
                                }
                            }
                        }

                        ProviderHealthTracker.recordSuccess(providerKey, latencyMs)
                        onLogFallback(
                            ProviderFallbackLogEntry(
                                providerName = candidateResult.usedProvider,
                                status = "SUCCESS",
                                layer = layer
                            )
                        )
                        return@withContext candidateResult
                    } catch (e: Exception) {
                        lastError = e
                        val failureType = ProviderHealthTracker.classifyFailure(errorMessage = e.message)
                        if (failureType != ProviderFailureType.EMPTY_RESPONSE) {
                            ProviderHealthTracker.recordFailure(
                                providerKey = providerKey,
                                failureType = failureType,
                                errorMessage = e.message ?: "Bilinmeyen hata"
                            )
                        }
                        if (providerKey == "ovh") {
                            EmochiRepository.ovhCooldownUntilTimestamp = System.currentTimeMillis() + 60_000L
                        }
                        onLogFallback(
                            ProviderFallbackLogEntry(
                                providerName = providerKey,
                                status = "FAILED",
                                errorMessage = e.message ?: "Bilinmeyen hata",
                                layer = layer
                            )
                        )
                    }
                }

                // If every remaining provider errored after an earlier refusal, return the refusal rather than crashing
                lastRefusalResult?.let { return@withContext it }

                throw lastError ?: IllegalStateException("Tüm AI sağlayıcıları yanıt vermekte başarısız oldu.")
            }
        }
    }

    /**
     * Executes a low-token, low-temperature (0.15) utility request strictly routed through
     * EmochiRepository.resolveUtilityModel() with internal header X-Emochi-Call-Type: utility
     * stripped by EmochiApplication.createTemperatureInterceptor() before network transmission.
     * Used by: Co-Piloted AI RAG picker, post-turn Neural Vault Curator, 50-msg Checkpoint summarizer,
     * Baseline Emotion Engine, BackgroundEmotionAuditWorker, and BackgroundMemoryMergeWorker.
     */
    suspend fun executeUtilityRequest(
        settings: UserSettingsEntity,
        systemPrompt: String,
        userMessage: String,
        buildConfigGeminiKey: String,
        maxTokens: Int = 420,
        timeoutMs: Long = 5000L
    ): ModelResponseResult = withContext(Dispatchers.IO) {
        val customList = try {
            customProviderDao.getAllProviders()
        } catch (_: Exception) {
            emptyList()
        }
        val customMap = customList.associateBy { it.id }
        val chain = buildProviderChain(settings, customMap, buildConfigGeminiKey)
        val candidates = ProviderHealthTracker.selectOrderedCandidates(chain)

        val clientBuilder = okhttp3.OkHttpClient.Builder()
            .connectTimeout(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .callTimeout(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addInterceptor(com.example.EmochiApplication.createTemperatureInterceptor())

        val externalInterceptor = RetrofitClient.customInterceptor
        if (externalInterceptor != null && externalInterceptor !== com.example.EmochiApplication.installedTemperatureInterceptor) {
            clientBuilder.addInterceptor(externalInterceptor)
        }
        val utilityClient = clientBuilder.build()

        var lastError: Exception? = null
        for (providerKey in candidates) {
            val customEntity = if (providerKey.startsWith("custom_")) {
                providerKey.removePrefix("custom_").toLongOrNull()?.let { customMap[it] }
            } else null

            val utilityModel = EmochiRepository.sanitizeModelName(
                EmochiRepository.resolveUtilityModel(providerKey, settings, customEntity)
            )

            val startMs = System.currentTimeMillis()
            try {
                val (request, format) = buildUtilityHttpRequest(
                    providerKey = providerKey,
                    model = utilityModel,
                    settings = settings,
                    customEntity = customEntity,
                    buildConfigGeminiKey = buildConfigGeminiKey,
                    systemPrompt = systemPrompt,
                    userMessage = userMessage,
                    maxTokens = maxTokens
                ) ?: continue

                utilityClient.newCall(request).execute().use { resp ->
                    val rawBody = resp.body?.string().orEmpty()
                    val latencyMs = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)
                    if (!resp.isSuccessful) {
                        val retryAfterSec = resp.header("Retry-After")?.trim()?.toLongOrNull()
                        val failureType = ProviderHealthTracker.classifyFailure(
                            httpCode = resp.code,
                            errorMessage = rawBody.take(160)
                        )
                        ProviderHealthTracker.recordFailure(
                            providerKey = providerKey,
                            failureType = failureType,
                            errorMessage = "HTTP ${resp.code}: ${rawBody.take(160)}",
                            retryAfterSeconds = retryAfterSec
                        )
                        throw IllegalStateException("Utility HTTP ${resp.code} ($providerKey): ${rawBody.take(160)}")
                    }
                    val parsed = parseUtilityHttpResponse(rawBody, format, providerKey)
                    if (parsed.text.isNotBlank()) {
                        ProviderHealthTracker.recordSuccess(providerKey, latencyMs)
                        return@withContext parsed
                    }
                }
            } catch (e: Exception) {
                lastError = e
                if (e.message?.startsWith("Utility HTTP ") != true) {
                    val failureType = ProviderHealthTracker.classifyFailure(errorMessage = e.message)
                    ProviderHealthTracker.recordFailure(
                        providerKey = providerKey,
                        failureType = failureType,
                        errorMessage = e.message ?: "Utility call error"
                    )
                }
            }
        }
        throw lastError ?: IllegalStateException("Utility model çağrısı başarısız oldu.")
    }

    private fun buildUtilityHttpRequest(
        providerKey: String,
        model: String,
        settings: UserSettingsEntity,
        customEntity: CustomProviderEntity?,
        buildConfigGeminiKey: String,
        systemPrompt: String,
        userMessage: String,
        maxTokens: Int
    ): Pair<Request, String>? {
        val jsonMedia = "application/json; charset=utf-8".toMediaType()
        val format: String
        val url: String
        val apiKey: String

        when {
            providerKey == "gemini" -> {
                apiKey = settings.customApiKey.ifBlank { buildConfigGeminiKey }.trim()
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") return null
                format = "gemini"
                url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            }
            providerKey == "claude" -> {
                apiKey = settings.claudeApiKey.trim()
                if (apiKey.isBlank()) return null
                format = "anthropic"
                url = "https://api.anthropic.com/v1/messages"
            }
            providerKey == "groq" -> {
                apiKey = settings.groqApiKey.trim()
                if (apiKey.isBlank()) return null
                format = "openai"
                url = "https://api.groq.com/openai/v1/chat/completions"
            }
            providerKey == "openai" -> {
                apiKey = settings.openaiApiKey.trim()
                if (apiKey.isBlank()) return null
                format = "openai"
                url = "https://api.openai.com/v1/chat/completions"
            }
            providerKey == "openrouter" -> {
                apiKey = settings.openRouterApiKey.trim()
                if (apiKey.isBlank()) return null
                format = "openai"
                url = "https://openrouter.ai/api/v1/chat/completions"
            }
            providerKey == "nvidia" -> {
                apiKey = settings.nvidiaApiKey.trim()
                if (apiKey.isBlank()) return null
                format = "openai"
                url = "https://integrate.api.nvidia.com/v1/chat/completions"
            }
            providerKey == "mistral" -> {
                apiKey = settings.mistralApiKey.trim()
                if (apiKey.isBlank()) return null
                format = "openai"
                url = "https://api.mistral.ai/v1/chat/completions"
            }
            providerKey == "llm7" -> {
                apiKey = ""
                format = "openai"
                url = "https://api.llm7.io/v1/chat/completions"
            }
            providerKey == "pollinations" -> {
                apiKey = ""
                format = "openai"
                url = "https://text.pollinations.ai/openai/chat/completions"
            }
            providerKey == "ovh" -> {
                apiKey = ""
                format = "openai"
                url = "https://oai.endpoints.kepler.ai.cloud.ovh.net/v1/chat/completions"
            }
            customEntity != null -> {
                val cleanBase = customEntity.baseUrl.trim().trimEnd('/')
                apiKey = try {
                    com.example.util.KeystoreEncryptionManager.decrypt(customEntity.apiKeyEncrypted).trim()
                } catch (_: Exception) {
                    ""
                }
                val fmt = customEntity.apiFormat.lowercase().trim()
                if (fmt == "anthropic" || cleanBase.contains("anthropic.com")) {
                    format = "anthropic"
                    url = if (cleanBase.endsWith("/messages")) cleanBase else "$cleanBase/v1/messages"
                } else if (fmt == "gemini" || cleanBase.contains("generativelanguage.googleapis.com")) {
                    format = "gemini"
                    url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                } else {
                    format = "openai"
                    url = when {
                        cleanBase.endsWith("/chat/completions") -> cleanBase
                        cleanBase.endsWith("/v1") || cleanBase.endsWith("/v4") || cleanBase.endsWith("/openai") -> "$cleanBase/chat/completions"
                        else -> "$cleanBase/v1/chat/completions"
                    }
                }
            }
            else -> return null
        }

        val reqBuilder = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .header(
                com.example.EmochiApplication.HEADER_CALL_TYPE,
                com.example.EmochiApplication.CALL_TYPE_UTILITY
            )

        if (url.contains("openrouter.ai", ignoreCase = true)) {
            reqBuilder.header("HTTP-Referer", com.example.EmochiApplication.OPENROUTER_REFERER)
            reqBuilder.header("X-Title", com.example.EmochiApplication.OPENROUTER_TITLE)
        }

        val payload = when (format) {
            "gemini" -> JSONObject().apply {
                if (systemPrompt.isNotBlank()) {
                    put(
                        "system_instruction",
                        JSONObject().put(
                            "parts",
                            JSONArray().put(JSONObject().put("text", systemPrompt))
                        )
                    )
                }
                put(
                    "contents",
                    JSONArray().put(
                        JSONObject()
                            .put("role", "user")
                            .put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
                    )
                )
                put(
                    "generationConfig",
                    JSONObject()
                        .put("temperature", com.example.EmochiApplication.UTILITY_TEMPERATURE)
                        .put("topP", com.example.EmochiApplication.UTILITY_TOP_P)
                        .put("maxOutputTokens", maxTokens)
                )
            }
            "anthropic" -> {
                reqBuilder.header("anthropic-version", "2023-06-01")
                if (apiKey.isNotBlank()) reqBuilder.header("x-api-key", apiKey)
                JSONObject().apply {
                    put("model", model)
                    if (systemPrompt.isNotBlank()) put("system", systemPrompt)
                    put("max_tokens", maxTokens)
                    put("temperature", com.example.EmochiApplication.UTILITY_TEMPERATURE)
                    put(
                        "messages",
                        JSONArray().put(
                            JSONObject()
                                .put("role", "user")
                                .put("content", userMessage)
                        )
                    )
                }
            }
            else -> {
                if (apiKey.isNotBlank()) reqBuilder.header("Authorization", "Bearer $apiKey")
                JSONObject().apply {
                    put("model", model)
                    put("temperature", com.example.EmochiApplication.UTILITY_TEMPERATURE)
                    put("top_p", com.example.EmochiApplication.UTILITY_TOP_P)
                    put("max_tokens", maxTokens)
                    val msgsArr = JSONArray()
                    if (systemPrompt.isNotBlank()) {
                        msgsArr.put(JSONObject().put("role", "system").put("content", systemPrompt))
                    }
                    msgsArr.put(JSONObject().put("role", "user").put("content", userMessage))
                    put("messages", msgsArr)
                }
            }
        }

        return Pair(reqBuilder.post(payload.toString().toRequestBody(jsonMedia)).build(), format)
    }

    private fun parseUtilityHttpResponse(
        rawBody: String,
        format: String,
        providerKey: String
    ): ModelResponseResult {
        val root = JSONObject(rawBody)
        return when (format) {
            "gemini" -> {
                val text = root.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.let { parts ->
                        buildString {
                            for (i in 0 until parts.length()) {
                                val t = parts.optJSONObject(i)?.optString("text").orEmpty()
                                if (t.isNotBlank()) append(t)
                            }
                        }
                    }
                    .orEmpty()
                    .trim()
                val usage = root.optJSONObject("usageMetadata")
                ModelResponseResult(
                    text = text,
                    promptTokens = usage?.optLong("promptTokenCount", 0L) ?: 0L,
                    candidateTokens = usage?.optLong("candidatesTokenCount", 0L) ?: 0L,
                    usedProvider = providerKey
                )
            }
            "anthropic" -> {
                val arr = root.optJSONArray("content")
                val text = buildString {
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i) ?: continue
                            if (obj.optString("type") == "text") {
                                append(obj.optString("text"))
                            }
                        }
                    }
                }.trim()
                val usage = root.optJSONObject("usage")
                ModelResponseResult(
                    text = text,
                    promptTokens = usage?.optLong("input_tokens", 0L) ?: 0L,
                    candidateTokens = usage?.optLong("output_tokens", 0L) ?: 0L,
                    usedProvider = providerKey
                )
            }
            else -> {
                val text = root.optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content", "")
                    .orEmpty()
                    .trim()
                val usage = root.optJSONObject("usage")
                ModelResponseResult(
                    text = text,
                    promptTokens = usage?.optLong("prompt_tokens", 0L) ?: 0L,
                    candidateTokens = usage?.optLong("completion_tokens", 0L) ?: 0L,
                    usedProvider = providerKey
                )
            }
        }
    }

    suspend fun testEndpointConnection(
        url: String,
        apiKey: String = "",
        modelName: String = "gpt-4o-mini",
        apiFormat: String = "openai",
        testTools: Boolean = false
    ): EmochiRepository.ProviderTestResult = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        try {
            val cleanUrl = url.trim().trimEnd('/')
            val format = apiFormat.lowercase().trim()

            // 1. Google Gemini native REST test
            if (format == "gemini" || cleanUrl.contains("generativelanguage.googleapis.com")) {
                val safeModel = EmochiRepository.sanitizeModelName(modelName.ifBlank { "gemini-2.0-flash" })
                val geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/$safeModel:generateContent?key=${apiKey.trim()}"
                val payload = JSONObject().apply {
                    put(
                        "contents",
                        JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put(
                                    "parts",
                                    JSONArray().apply {
                                        put(JSONObject().apply {
                                            put("text", "Bağlantı testi için tek kelimeyle 'aktif' yaz.")
                                        })
                                    }
                                )
                            })
                        }
                    )
                    put(
                        "generationConfig",
                        JSONObject().apply { put("maxOutputTokens", 16) }
                    )
                }
                val req = Request.Builder()
                    .url(geminiUrl)
                    .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .header("Content-Type", "application/json")
                    .build()

                RetrofitClient.okHttpClient.newCall(req).execute().use { resp ->
                    val bodyStr = resp.body?.string().orEmpty()
                    val latency = System.currentTimeMillis() - startMs
                    return@withContext if (resp.isSuccessful) {
                        ProviderHealthTracker.recordSuccess("gemini", latency)
                        EmochiRepository.ProviderTestResult(
                            isSuccess = true,
                            supportsFunctionCalling = true,
                            errorMessage = "",
                            rawResponse = bodyStr.take(300),
                            latencyMs = latency
                        )
                    } else {
                        EmochiRepository.ProviderTestResult(
                            isSuccess = false,
                            supportsFunctionCalling = false,
                            errorMessage = "HTTP ${resp.code}: ${bodyStr.take(180)}",
                            rawResponse = bodyStr,
                            latencyMs = latency
                        )
                    }
                }
            }

            // 2. Anthropic Messages API test
            if (format == "anthropic" || cleanUrl.contains("anthropic.com")) {
                val endpoint = if (cleanUrl.endsWith("/messages")) cleanUrl else "$cleanUrl/v1/messages"
                val payload = JSONObject().apply {
                    put("model", modelName.ifBlank { "claude-3-5-sonnet-20241022" })
                    put("max_tokens", 24)
                    put(
                        "messages",
                        JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("content", "Bağlantı testi için tek kelimeyle 'aktif' yaz.")
                            })
                        }
                    )
                }
                val reqBuilder = Request.Builder()
                    .url(endpoint)
                    .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .header("Content-Type", "application/json")
                    .header("anthropic-version", "2023-06-01")
                if (apiKey.isNotBlank()) {
                    reqBuilder.header("x-api-key", apiKey.trim())
                }
                RetrofitClient.okHttpClient.newCall(reqBuilder.build()).execute().use { resp ->
                    val bodyStr = resp.body?.string().orEmpty()
                    val latency = System.currentTimeMillis() - startMs
                    return@withContext if (resp.isSuccessful) {
                        if (cleanUrl.contains("anthropic.com")) {
                            ProviderHealthTracker.recordSuccess("claude", latency)
                        }
                        EmochiRepository.ProviderTestResult(
                            isSuccess = true,
                            supportsFunctionCalling = true,
                            errorMessage = "",
                            rawResponse = bodyStr.take(300),
                            latencyMs = latency
                        )
                    } else {
                        EmochiRepository.ProviderTestResult(
                            isSuccess = false,
                            supportsFunctionCalling = false,
                            errorMessage = "HTTP ${resp.code}: ${bodyStr.take(180)}",
                            rawResponse = bodyStr,
                            latencyMs = latency
                        )
                    }
                }
            }

            // 3. OpenAI-compatible Chat Completions test (OpenAI, Groq, OpenRouter, DeepSeek, OVH, Pollinations, LLM7, Ollama, etc.)
            val endpoint = when {
                cleanUrl.endsWith("/chat/completions") -> cleanUrl
                cleanUrl.contains("pollinations.ai") -> cleanUrl
                cleanUrl.endsWith("/v1") ||
                    cleanUrl.endsWith("/v4") ||
                    cleanUrl.endsWith("/openai") ||
                    cleanUrl.contains("models.inference.ai.azure.com") ||
                    cleanUrl.contains("api.perplexity.ai") -> "$cleanUrl/chat/completions"
                else -> "$cleanUrl/v1/chat/completions"
            }

            val payload = JSONObject().apply {
                put("model", modelName.ifBlank { "openai" })
                put(
                    "messages",
                    JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "Merhaba, bağlantı testi için tek kelimeyle 'aktif' yaz.")
                        })
                    }
                )
                put("max_tokens", 24)
                if (testTools) {
                    put("tools", MemoryToolRegistry.toOpenAiToolsJsonArray())
                }
            }

            val reqBuilder = Request.Builder()
                .url(endpoint)
                .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .header("Content-Type", "application/json")

            if (endpoint.contains("openrouter.ai", ignoreCase = true)) {
                reqBuilder.header("HTTP-Referer", com.example.EmochiApplication.OPENROUTER_REFERER)
                reqBuilder.header("X-Title", com.example.EmochiApplication.OPENROUTER_TITLE)
            }

            if (apiKey.isNotBlank()) {
                reqBuilder.header("Authorization", "Bearer ${apiKey.trim()}")
            }

            RetrofitClient.okHttpClient.newCall(reqBuilder.build()).execute().use { resp ->
                val bodyStr = resp.body?.string().orEmpty()
                val latency = System.currentTimeMillis() - startMs
                if (!resp.isSuccessful) {
                    if (testTools && (resp.code == 400 || resp.code == 422)) {
                        val basicRes = testEndpointConnection(
                            url = url,
                            apiKey = apiKey,
                            modelName = modelName,
                            apiFormat = apiFormat,
                            testTools = false
                        )
                        return@withContext basicRes.copy(supportsFunctionCalling = false)
                    }
                    return@withContext EmochiRepository.ProviderTestResult(
                        isSuccess = false,
                        supportsFunctionCalling = false,
                        errorMessage = "HTTP ${resp.code}: ${bodyStr.take(180)}",
                        rawResponse = bodyStr,
                        latencyMs = latency
                    )
                }
                inferKnownProviderKey(endpoint)?.let { knownKey ->
                    ProviderHealthTracker.recordSuccess(knownKey, latency)
                }
                return@withContext EmochiRepository.ProviderTestResult(
                    isSuccess = true,
                    supportsFunctionCalling = testTools,
                    errorMessage = "",
                    rawResponse = bodyStr.take(300),
                    latencyMs = latency
                )
            }
        } catch (e: Exception) {
            EmochiRepository.ProviderTestResult(
                isSuccess = false,
                supportsFunctionCalling = false,
                errorMessage = e.message ?: "Bağlantı kurulamadı",
                latencyMs = System.currentTimeMillis() - startMs
            )
        }
    }

    private fun inferKnownProviderKey(url: String): String? {
        val lower = url.lowercase()
        return when {
            lower.contains("api.groq.com") -> "groq"
            lower.contains("api.openai.com") -> "openai"
            lower.contains("openrouter.ai") -> "openrouter"
            lower.contains("integrate.api.nvidia.com") -> "nvidia"
            lower.contains("api.mistral.ai") -> "mistral"
            lower.contains("pollinations.ai") -> "pollinations"
            lower.contains("llm7.io") -> "llm7"
            lower.contains("ovh.net") -> "ovh"
            else -> null
        }
    }

    suspend fun fetchModelsFromOpenAiCompatibleEndpoint(
        baseUrl: String,
        apiKey: String
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = baseUrl.trim().trimEnd('/')
            val modelsUrl = when {
                cleanBase.endsWith("/models") -> cleanBase
                cleanBase.endsWith("/v1") || cleanBase.endsWith("/v4") || cleanBase.endsWith("/openai") -> "$cleanBase/models"
                else -> "$cleanBase/v1/models"
            }
            val reqBuilder = Request.Builder().url(modelsUrl).get()
            if (modelsUrl.contains("openrouter.ai", ignoreCase = true)) {
                reqBuilder.header("HTTP-Referer", com.example.EmochiApplication.OPENROUTER_REFERER)
                reqBuilder.header("X-Title", com.example.EmochiApplication.OPENROUTER_TITLE)
            }
            if (apiKey.isNotBlank()) {
                reqBuilder.header("Authorization", "Bearer ${apiKey.trim()}")
            }
            RetrofitClient.okHttpClient.newCall(reqBuilder.build()).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList<String>()
                val body = resp.body?.string().orEmpty()
                val arr = JSONObject(body).optJSONArray("data") ?: return@withContext emptyList<String>()
                val result = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val id = arr.optJSONObject(i)?.optString("id")?.trim().orEmpty()
                    if (id.isNotBlank()) result.add(id)
                }
                result.sorted()
            }
        } catch (_: Exception) {
            emptyList<String>()
        }
    }
}
