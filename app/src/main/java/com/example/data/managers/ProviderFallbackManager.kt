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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

    data class ResolvedTimeoutConfig(
        val readTimeoutMs: Long,
        val callTimeoutMs: Long,
        val maxFallbackAttempts: Int,
        val strictEnabled: Boolean
    )

    data class AiUtilityModelSelectionResult(
        val providerKey: String,
        val selectedModel: String,
        val source: String, // "ai" | "auto_fallback"
        val availableCount: Int,
        val reasoning: String = ""
    )

    companion object {
        const val STRICT_UTILITY_TIMEOUT_MS = 18_000L
        const val STRICT_UTILITY_MAX_ATTEMPTS = 2
        const val STRICT_NARRATIVE_TIMEOUT_MS = 60_000L
        const val UNLIMITED_SAFETY_CALL_TIMEOUT_MS = 300_000L

        /**
         * Resolves effective per-call timeout & fallback limits based on [AppFeatureFlags.isStrictApiTimeoutEnabled].
         * - When `true`: UTILITY = 18s (max 2 fallback attempts), NARRATIVE = 60s (full chain).
         * - When `false`: readTimeout = 0 (unlimited), generous 300s safety cap so reasoning/local models finish cleanly.
         */
        fun resolveTimeoutConfig(
            mode: ExecutionMode,
            requestedTimeoutMs: Long? = null,
            strictOverride: Boolean? = null
        ): ResolvedTimeoutConfig {
            val strict = strictOverride ?: AppFeatureFlags.isStrictApiTimeoutEnabled
            if (!strict) {
                return ResolvedTimeoutConfig(
                    readTimeoutMs = 0L,
                    callTimeoutMs = UNLIMITED_SAFETY_CALL_TIMEOUT_MS,
                    maxFallbackAttempts = Int.MAX_VALUE,
                    strictEnabled = false
                )
            }
            return when (mode) {
                ExecutionMode.UTILITY -> {
                    val effectiveMs = if (requestedTimeoutMs != null && requestedTimeoutMs > 0L) {
                        minOf(requestedTimeoutMs, STRICT_UTILITY_TIMEOUT_MS)
                    } else {
                        STRICT_UTILITY_TIMEOUT_MS
                    }
                    ResolvedTimeoutConfig(
                        readTimeoutMs = effectiveMs,
                        callTimeoutMs = effectiveMs,
                        maxFallbackAttempts = STRICT_UTILITY_MAX_ATTEMPTS,
                        strictEnabled = true
                    )
                }
                ExecutionMode.NARRATIVE -> {
                    val effectiveMs = if (requestedTimeoutMs != null && requestedTimeoutMs > 0L) {
                        minOf(requestedTimeoutMs, STRICT_NARRATIVE_TIMEOUT_MS)
                    } else {
                        STRICT_NARRATIVE_TIMEOUT_MS
                    }
                    ResolvedTimeoutConfig(
                        readTimeoutMs = effectiveMs,
                        callTimeoutMs = effectiveMs,
                        maxFallbackAttempts = Int.MAX_VALUE,
                        strictEnabled = true
                    )
                }
            }
        }
    }

    /** Coroutine-cooperative OkHttp execution: cancels the underlying socket immediately on timeout/cancellation. */
    private suspend fun Call.awaitResponse(): Response = suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation {
            try {
                cancel()
            } catch (_: Throwable) {}
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
            "ovh" -> settings.enableOvh && settings.ovhEnabled
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
                val effectiveMode = if (isUtility) ExecutionMode.UTILITY else ExecutionMode.NARRATIVE
                val timeoutCfg = resolveTimeoutConfig(effectiveMode)
                val boundedCandidates = if (timeoutCfg.maxFallbackAttempts < candidates.size) {
                    candidates.take(timeoutCfg.maxFallbackAttempts)
                } else {
                    candidates
                }
                val refusalGuardEnabled = !isUtility && AppFeatureFlags.isRefusalAutoFallbackEnabled

                var lastError: Exception? = null
                var lastRefusalResult: ModelResponseResult? = null
                val latestUserText = messages.lastOrNull { it.role == "user" }?.text.orEmpty()
                val isPureOocTurn = !isUtility && com.example.util.UserSentimentAnalyzer.isPureOocMessage(latestUserText)
                var activeSystemPrompt = systemPrompt

                fun isSafetyOrEmptyException(message: String?): Boolean {
                    val m = message.orEmpty().lowercase(Locale.ROOT)
                    return m.contains("candidates boş") ||
                        m.contains("filtre engelledi") ||
                        m.contains("safety") ||
                        m.contains("prohibited_content") ||
                        m.contains("blocklist") ||
                        m.contains("content_filter") ||
                        m.contains("içerik filtresi") ||
                        m.contains("choices boş") ||
                        m.contains("boş yanıt") ||
                        m.contains("malformed_function_call")
                }

                for ((idx, providerKey) in boundedCandidates.withIndex()) {
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
                        suspend fun invokeOnce(
                            requestTools: List<MemoryToolDefinition>,
                            promptToUse: String = activeSystemPrompt
                        ): ModelResponseResult {
                            val callBlock: suspend () -> ModelResponseResult = {
                                if (customExecutor != null) {
                                    customExecutor.invoke(providerKey, resolvedModel, promptToUse, messages, requestTools)
                                } else {
                                    val adapter = LLMAdapterFactory.createAdapter(
                                        providerKey,
                                        resolvedModel,
                                        settings,
                                        customEntity,
                                        buildConfigGeminiKey
                                    )
                                    val effectiveTools = if (!isUtility && adapter.supportsFunctionCalling()) requestTools else emptyList()
                                    val llmResp = adapter.sendMessage(promptToUse, messages, effectiveTools)
                                    ModelResponseResult(
                                        text = llmResp.text,
                                        toolCalls = llmResp.toolCalls ?: emptyList(),
                                        promptTokens = llmResp.usage?.promptTokens ?: 0L,
                                        candidateTokens = llmResp.usage?.candidateTokens ?: 0L,
                                        usedProvider = llmResp.usedProvider.ifBlank { providerKey }
                                    )
                                }
                            }
                            return if (timeoutCfg.strictEnabled && timeoutCfg.callTimeoutMs > 0L) {
                                withTimeout(timeoutCfg.callTimeoutMs) { callBlock() }
                            } else {
                                withTimeout(timeoutCfg.callTimeoutMs) { callBlock() }
                            }
                        }

                        fun extractVisibleNarrative(raw: String): String {
                            val trimmedRaw = raw.trim()
                            if (trimmedRaw.isBlank() || trimmedRaw.equals("null", ignoreCase = true)) return ""
                            val withoutThink = trimmedRaw
                                .replace(Regex("""(?is)<think>.*?</think>"""), "")
                                .replace(Regex("""(?is)<reasoning>.*?</reasoning>"""), "")
                                .replace(Regex("""(?is)<think>.*$"""), "")
                                .replace(Regex("""(?is)<reasoning>.*$"""), "")
                                .trim()
                            if (withoutThink.isBlank()) return ""
                            return EmochiRepository.cleanEmotionTags(withoutThink).trim()
                        }

                        // On pure-OOC setup messages (e.g. long parenthesized worldbuilding blocks),
                        // omit native memory tools during the narrative reply so models don't return
                        // tool-calls-only empty content (post-turn Neural Vault Curator saves all facts).
                        val initialTools = if (!isUtility && !isPureOocTurn) tools else emptyList()
                        var candidateResult: ModelResponseResult? = null
                        var firstCallException: Exception? = null
                        try {
                            candidateResult = invokeOnce(initialTools, activeSystemPrompt)
                        } catch (ce: CancellationException) {
                            throw ce
                        } catch (e: Exception) {
                            if (!isUtility && isSafetyOrEmptyException(e.message)) {
                                firstCallException = e
                            } else {
                                throw e
                            }
                        }

                        var cleanText = candidateResult?.let { extractVisibleNarrative(it.text) }.orEmpty()

                        // Multi-stage recovery:
                        // If Call 1 returned blank/think-only/tag-only/tool-only OR threw a safety/empty filter error,
                        // sanitize the prompt (removing +18/NSFW & ACTIVE_MEMORY_CALL triggers, adding explicit
                        // Turkish narrative recovery instructions) and retry ONCE on the same provider without tools.
                        if (!isUtility && cleanText.isBlank()) {
                            val preservedTools = candidateResult?.toolCalls.orEmpty()
                            val safePrompt = com.example.data.repository.SystemPromptBuilder.sanitizeForSafeRetry(
                                systemPrompt = activeSystemPrompt,
                                isPureOoc = isPureOocTurn
                            )
                            activeSystemPrompt = safePrompt
                            val followUp = try {
                                invokeOnce(emptyList(), safePrompt)
                            } catch (ce: CancellationException) {
                                throw ce
                            } catch (retryErr: Exception) {
                                if (firstCallException == null) firstCallException = retryErr
                                null
                            }
                            if (followUp != null) {
                                val followClean = extractVisibleNarrative(followUp.text)
                                if (followClean.isNotBlank()) {
                                    candidateResult = followUp.copy(
                                        toolCalls = if (followUp.toolCalls.isNotEmpty()) followUp.toolCalls else preservedTools,
                                        promptTokens = (candidateResult?.promptTokens ?: 0L) + followUp.promptTokens,
                                        candidateTokens = (candidateResult?.candidateTokens ?: 0L) + followUp.candidateTokens
                                    )
                                    cleanText = followClean
                                }
                            }
                        }

                        val latencyMs = (System.currentTimeMillis() - startMs).coerceAtLeast(1L)

                        if (cleanText.isBlank() || candidateResult == null) {
                            ProviderHealthTracker.recordFailure(
                                providerKey = providerKey,
                                failureType = ProviderFailureType.EMPTY_RESPONSE,
                                errorMessage = firstCallException?.message ?: "Boş veya yalnızca etiket içeren yanıt döndü"
                            )
                            throw firstCallException ?: IllegalStateException("Sağlayıcı ($providerKey) boş yanıt döndürdü.")
                        }

                        // Configurable OOC Censorship / Moralizing Protection (0s lockout, current-turn only)
                        if (refusalGuardEnabled && cleanText.isNotBlank()) {
                            val refusalCheck = RefusalDetector.detectOocRefusal(cleanText)
                            if (refusalCheck.isRefusal) {
                                ProviderHealthTracker.recordFailure(
                                    providerKey = providerKey,
                                    failureType = ProviderFailureType.CONTENT_REFUSAL,
                                    errorMessage = refusalCheck.matchedReason
                                )
                                lastRefusalResult = candidateResult
                                val hasNextProvider = idx < boundedCandidates.lastIndex
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
                    } catch (ce: CancellationException) {
                        // CRITICAL: User/scope cancellation must NEVER penalize provider health or trigger cooldown
                        throw ce
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
        timeoutMs: Long = STRICT_UTILITY_TIMEOUT_MS
    ): ModelResponseResult = withContext(Dispatchers.IO) {
        val customList = try {
            customProviderDao.getAllProviders()
        } catch (_: Exception) {
            emptyList()
        }
        val customMap = customList.associateBy { it.id }
        val chain = buildProviderChain(settings, customMap, buildConfigGeminiKey)
        val timeoutCfg = resolveTimeoutConfig(ExecutionMode.UTILITY, timeoutMs)
        val rawCandidates = ProviderHealthTracker.selectOrderedCandidates(chain)
        val candidates = if (timeoutCfg.maxFallbackAttempts < rawCandidates.size) {
            rawCandidates.take(timeoutCfg.maxFallbackAttempts)
        } else {
            rawCandidates
        }

        // Share RetrofitClient's singleton ConnectionPool & Dispatcher via newBuilder() — zero pool leaks
        val connectMs = if (timeoutCfg.strictEnabled) timeoutCfg.readTimeoutMs else 30_000L
        val clientBuilder = RetrofitClient.okHttpClient.newBuilder()
            .connectTimeout(connectMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .readTimeout(timeoutCfg.readTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .writeTimeout(connectMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .callTimeout(timeoutCfg.callTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)

        val tempInterceptor = com.example.EmochiApplication.installedTemperatureInterceptor
        if (tempInterceptor == null || clientBuilder.interceptors().none { it === tempInterceptor }) {
            clientBuilder.interceptors().add(0, com.example.EmochiApplication.createTemperatureInterceptor())
        }
        val externalInterceptor = RetrofitClient.customInterceptor
        if (externalInterceptor != null &&
            externalInterceptor !== tempInterceptor &&
            clientBuilder.interceptors().none { it === externalInterceptor }
        ) {
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

                utilityClient.newCall(request).awaitResponse().use { resp ->
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
            } catch (ce: CancellationException) {
                throw ce
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
                url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
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
                    url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
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
            "gemini" -> {
                if (apiKey.isNotBlank()) reqBuilder.header("x-goog-api-key", apiKey)
                JSONObject().apply {
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
                val geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/$safeModel:generateContent"
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
                val reqBuilder = Request.Builder()
                    .url(geminiUrl)
                    .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .header("Content-Type", "application/json")
                if (apiKey.isNotBlank()) {
                    reqBuilder.header("x-goog-api-key", apiKey.trim())
                }
                val req = reqBuilder.build()

                RetrofitClient.okHttpClient.newCall(req).awaitResponse().use { resp ->
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
                RetrofitClient.okHttpClient.newCall(reqBuilder.build()).awaitResponse().use { resp ->
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

            RetrofitClient.okHttpClient.newCall(reqBuilder.build()).awaitResponse().use { resp ->
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
            RetrofitClient.okHttpClient.newCall(reqBuilder.build()).awaitResponse().use { resp ->
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

    /**
     * Fetches the live model list for ANY built-in or custom provider, falling back to curated
     * text/chat models when a provider does not expose a `/models` endpoint (e.g. Anthropic).
     */
    suspend fun fetchModelsForProvider(
        providerKey: String,
        settings: UserSettingsEntity,
        customEntity: CustomProviderEntity?,
        buildConfigGeminiKey: String
    ): List<String> = withContext(Dispatchers.IO) {
        val key = providerKey.lowercase(Locale.ROOT).trim()
        val live: List<String> = when {
            key == "gemini" -> fetchGeminiModels(settings.customApiKey.ifBlank { buildConfigGeminiKey })
            key == "groq" -> fetchModelsFromOpenAiCompatibleEndpoint("https://api.groq.com/openai/v1", settings.groqApiKey)
            key == "openai" -> fetchModelsFromOpenAiCompatibleEndpoint("https://api.openai.com/v1", settings.openaiApiKey)
            key == "openrouter" -> fetchModelsFromOpenAiCompatibleEndpoint("https://openrouter.ai/api/v1", settings.openRouterApiKey)
            key == "nvidia" -> fetchModelsFromOpenAiCompatibleEndpoint("https://integrate.api.nvidia.com/v1", settings.nvidiaApiKey)
            key == "mistral" -> fetchModelsFromOpenAiCompatibleEndpoint("https://api.mistral.ai/v1", settings.mistralApiKey)
            key == "pollinations" -> fetchModelsFromOpenAiCompatibleEndpoint("https://text.pollinations.ai/openai", "")
            key == "ovh" -> fetchModelsFromOpenAiCompatibleEndpoint("https://oai.endpoints.kepler.ai.cloud.ovh.net/v1", "")
            key == "llm7" -> fetchModelsFromOpenAiCompatibleEndpoint("https://api.llm7.io/v1", "")
            customEntity != null -> {
                val decrypted = try {
                    com.example.util.KeystoreEncryptionManager.decrypt(customEntity.apiKeyEncrypted).trim()
                } catch (_: Exception) {
                    com.example.util.SecureApiKeyVault.decrypt(customEntity.apiKeyEncrypted).trim()
                }
                if (customEntity.apiFormat.equals("gemini", ignoreCase = true) ||
                    customEntity.baseUrl.contains("generativelanguage.googleapis.com", ignoreCase = true)
                ) {
                    fetchGeminiModels(decrypted)
                } else {
                    fetchModelsFromOpenAiCompatibleEndpoint(customEntity.baseUrl, decrypted)
                }
            }
            else -> emptyList()
        }

        val cleaned = filterTextChatCandidates(live)
        if (cleaned.isNotEmpty()) return@withContext cleaned

        // Curated catalog fallback so AI still chooses the optimal small model even if /models is restricted
        curatedCatalogForProvider(key, settings, customEntity)
    }

    private suspend fun fetchGeminiModels(apiKey: String): List<String> {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank() || cleanKey == "MY_GEMINI_API_KEY") return emptyList()
        return try {
            val req = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models?pageSize=100")
                .header("x-goog-api-key", cleanKey)
                .get()
                .build()
            RetrofitClient.okHttpClient.newCall(req).awaitResponse().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val body = resp.body?.string().orEmpty()
                val arr = JSONObject(body).optJSONArray("models") ?: return emptyList()
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val methods = obj.optJSONArray("supportedGenerationMethods")
                    var supportsGenerate = false
                    if (methods != null) {
                        for (m in 0 until methods.length()) {
                            if (methods.optString(m).equals("generateContent", ignoreCase = true)) {
                                supportsGenerate = true
                                break
                            }
                        }
                    }
                    if (!supportsGenerate) continue
                    val name = obj.optString("name", "").removePrefix("models/").trim()
                    if (name.isNotBlank()) list.add(name)
                }
                list.sorted()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun filterTextChatCandidates(raw: List<String>): List<String> {
        val excluded = listOf(
            "embed", "embedding", "whisper", "tts", "audio", "transcribe", "moderation",
            "dall-e", "imagen", "flux", "sdxl", "stable-diffusion", "rerank", "bge-",
            "clip", "vision-only", "guard", "shield", "aqa", "gecko", "bison"
        )
        return raw.filter { id ->
            val lower = id.lowercase(Locale.ROOT)
            excluded.none { bad -> lower.contains(bad) }
        }.distinct()
    }

    private fun curatedCatalogForProvider(
        providerKey: String,
        settings: UserSettingsEntity,
        customEntity: CustomProviderEntity?
    ): List<String> = when (providerKey) {
        "gemini" -> listOf("gemini-2.5-flash-lite", "gemini-2.0-flash-lite", "gemini-2.0-flash", "gemini-2.5-flash")
        "groq" -> listOf("llama-3.1-8b-instant", "gemma2-9b-it", "llama-3.3-70b-versatile", "openai/gpt-oss-20b", "openai/gpt-oss-120b")
        "openai" -> listOf("gpt-4.1-nano", "gpt-4o-mini", "gpt-4.1-mini", "gpt-4o")
        "claude" -> listOf("claude-3-5-haiku-latest", "claude-3-5-haiku-20241022", "claude-3-haiku-20240307", "claude-3-5-sonnet-20241022")
        "openrouter" -> listOf(
            "google/gemini-2.0-flash-lite-001",
            "openai/gpt-4o-mini",
            "meta-llama/llama-3.1-8b-instruct:free",
            "mistralai/mistral-small-3.1-24b-instruct:free",
            "deepseek/deepseek-chat"
        )
        "nvidia" -> listOf("meta/llama-3.1-8b-instruct", "google/gemma-2-9b-it", "mistralai/mistral-7b-instruct-v0.3", "meta/llama-3.3-70b-instruct")
        "mistral" -> listOf("ministral-8b-latest", "mistral-small-latest", "open-mistral-nemo", "mistral-large-latest")
        "pollinations" -> listOf("openai-fast", "mistral", "openai", "llama")
        "ovh" -> listOf("Llama-3.2-3B-Instruct", "Meta-Llama-3_1-8B-Instruct", "Mistral-7B-Instruct-v0.3", "Meta-Llama-3_3-70B-Instruct")
        "llm7" -> listOf("gpt-4o-mini", "default", "mistral-small")
        else -> listOfNotNull(customEntity?.modelName?.takeIf { it.isNotBlank() }, settings.selectedModel.takeIf { it.isNotBlank() }).distinct()
    }

    /**
     * Asks the AI itself to choose the best fast, low-cost, JSON-capable small utility/support model
     * for ANY built-in or custom provider, and persists the choice in [AppFeatureFlags] (plus Room for custom providers).
     */
    suspend fun selectUtilityModelWithAi(
        providerKey: String,
        settings: UserSettingsEntity,
        customEntity: CustomProviderEntity?,
        buildConfigGeminiKey: String,
        candidateModelsOverride: List<String>? = null,
        aiChooserOverride: (suspend (systemPrompt: String, userPrompt: String) -> String)? = null
    ): AiUtilityModelSelectionResult = withContext(Dispatchers.IO) {
        val key = providerKey.lowercase(Locale.ROOT).trim()
        val candidates = (candidateModelsOverride ?: fetchModelsForProvider(key, settings, customEntity, buildConfigGeminiKey))
            .filter { it.isNotBlank() }
            .distinct()

        if (candidates.isEmpty()) {
            val fallback = EmochiRepository.resolveUtilityModel(key, settings, customEntity)
            return@withContext AiUtilityModelSelectionResult(
                providerKey = key,
                selectedModel = fallback,
                source = "auto_fallback",
                availableCount = 0,
                reasoning = "Model listesi alınamadı, varsayılan destek modeli kullanıldı."
            )
        }

        // Prioritize small/fast candidates at the top of the prompt list (max 45 models to stay ultra-cheap)
        val promptPool = candidates
            .sortedByDescending { scoreSmallUtilityCandidate(it) }
            .take(45)

        val sysPrompt = """
Sen bir LLM model seçicisisin. Görevin, verilen sağlayıcı model listesi içinden arka plan yardımcı görevleri (hafıza çıkarma, duygu analizi, RAG seçimi, kısa JSON üretimi) için EN İYİ küçük, hızlı, düşük maliyetli ve JSON uyumlu destek modelini seçmek.
KURALLAR:
- Büyük/yavaş/pahalı/reasoning-only modelleri (örn. 70b+, pro, opus, r1, o1, o3, large) mümkünse seçme; varsa flash-lite, mini, nano, haiku, 3b, 7b, 8b, 9b, instant, small, fast gibi modelleri tercih et.
- YALNIZCA listedeki birebir model adını seç.
- SADECE geçerli tek satır JSON döndür: {"model":"<birebir_model_id>","reason":"<kısa Türkçe neden>"}
        """.trimIndent()

        val userPrompt = buildString {
            appendLine("Sağlayıcı: $key")
            appendLine("Mevcut Modeller:")
            promptPool.forEach { appendLine("- $it") }
        }

        var chosenModel: String? = null
        var chosenReason = ""
        var source = "auto_fallback"

        val aiRaw = try {
            if (aiChooserOverride != null) {
                aiChooserOverride(sysPrompt, userPrompt)
            } else {
                // Prefer calling the provider itself first using its main/default model, else fallback chain
                val directResult = callProviderDirectlyForModelPick(
                    providerKey = key,
                    settings = settings,
                    customEntity = customEntity,
                    buildConfigGeminiKey = buildConfigGeminiKey,
                    systemPrompt = sysPrompt,
                    userPrompt = userPrompt
                )
                directResult ?: executeUtilityRequest(
                    settings = settings,
                    systemPrompt = sysPrompt,
                    userMessage = userPrompt,
                    buildConfigGeminiKey = buildConfigGeminiKey,
                    maxTokens = 140,
                    timeoutMs = 12_000L
                ).text
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            null
        }

        if (!aiRaw.isNullOrBlank()) {
            val parsed = parseAiUtilityModelSelection(aiRaw, candidates)
            if (parsed != null) {
                chosenModel = parsed.first
                chosenReason = parsed.second
                source = "ai"
            }
        }

        val finalModel = chosenModel ?: promptPool.first()
        val finalReason = chosenReason.ifBlank {
            if (source == "ai") "AI tarafından hızlı ve düşük maliyetli destek modeli olarak seçildi."
            else "Hızlı küçük model profili otomatik seçildi."
        }

        AppFeatureFlags.setAiUtilityModel(key, finalModel, source)
        if (customEntity != null) {
            try {
                val smallJson = JSONArray(promptPool.take(10)).toString()
                customProviderDao.updateProvider(
                    customEntity.copy(
                        utilityModel = finalModel,
                        utilityModelSource = if (source == "ai") "ai_selected" else "auto_detected",
                        detectedSmallModels = smallJson
                    )
                )
            } catch (_: Exception) {}
        }

        AiUtilityModelSelectionResult(
            providerKey = key,
            selectedModel = finalModel,
            source = source,
            availableCount = candidates.size,
            reasoning = finalReason
        )
    }

    private suspend fun callProviderDirectlyForModelPick(
        providerKey: String,
        settings: UserSettingsEntity,
        customEntity: CustomProviderEntity?,
        buildConfigGeminiKey: String,
        systemPrompt: String,
        userPrompt: String
    ): String? {
        val mainModel = when (providerKey) {
            "gemini" -> EmochiRepository.sanitizeModelName(settings.geminiModel.ifBlank { "gemini-2.0-flash" })
            "groq" -> EmochiRepository.sanitizeModelName(settings.groqModel.ifBlank { "llama-3.1-8b-instant" })
            "openai" -> settings.openaiModel.ifBlank { "gpt-4o-mini" }
            "claude" -> settings.claudeModel.ifBlank { "claude-3-5-haiku-latest" }
            "openrouter" -> settings.openRouterModel.ifBlank { "deepseek/deepseek-chat" }
            "nvidia" -> settings.nvidiaModel.ifBlank { "meta/llama-3.1-8b-instruct" }
            "mistral" -> settings.mistralModel.ifBlank { "mistral-small-latest" }
            "pollinations" -> settings.pollinationsModel.ifBlank { "openai" }
            "ovh" -> settings.ovhModel.ifBlank { "Meta-Llama-3_3-70B-Instruct" }
            "llm7" -> "default"
            else -> customEntity?.modelName.orEmpty().ifBlank { return null }
        }
        val (req, format) = buildUtilityHttpRequest(
            providerKey = providerKey,
            model = mainModel,
            settings = settings,
            customEntity = customEntity,
            buildConfigGeminiKey = buildConfigGeminiKey,
            systemPrompt = systemPrompt,
            userMessage = userPrompt,
            maxTokens = 140
        ) ?: return null

        return try {
            val client = RetrofitClient.okHttpClient.newBuilder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(12, java.util.concurrent.TimeUnit.SECONDS)
                .callTimeout(14, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            client.newCall(req).awaitResponse().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string().orEmpty()
                parseUtilityHttpResponse(body, format, providerKey).text.takeIf { it.isNotBlank() }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Exception) {
            null
        }
    }

    private fun parseAiUtilityModelSelection(
        raw: String,
        candidates: List<String>
    ): Pair<String, String>? {
        val cleaned = raw
            .replace(Regex("```(?:json)?|```", RegexOption.IGNORE_CASE), "")
            .trim()
        val candidateByLower = candidates.associateBy { it.lowercase(Locale.ROOT) }

        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        if (start >= 0 && end > start) {
            try {
                val obj = JSONObject(cleaned.substring(start, end + 1))
                val picked = obj.optString("model", "").trim()
                val reason = obj.optString("reason", "").trim()
                val exact = candidateByLower[picked.lowercase(Locale.ROOT)]
                    ?: candidates.firstOrNull { it.equals(picked.removePrefix("models/"), ignoreCase = true) }
                if (exact != null) return exact to reason
            } catch (_: Exception) {}
        }

        // Plain-text rescue if the model replied with a bare model ID
        val matched = candidates.firstOrNull { cand ->
            cleaned.contains(cand, ignoreCase = true)
        }
        return matched?.let { it to "" }
    }

    private fun scoreSmallUtilityCandidate(modelId: String): Int {
        val m = modelId.lowercase(Locale.ROOT)
        var score = 0
        if (m.contains("flash-lite")) score += 120
        if (m.contains("nano")) score += 110
        if (m.contains("mini")) score += 100
        if (m.contains("haiku")) score += 100
        if (m.contains("instant")) score += 95
        if (m.contains("fast")) score += 90
        if (m.contains("flash")) score += 85
        if (m.contains("small") || m.contains("ministral")) score += 85
        if (m.contains("8b") || m.contains("9b") || m.contains("7b") || m.contains("3b") || m.contains("4b")) score += 80
        if (m.contains(":free")) score += 25
        if (m.contains("70b") || m.contains("120b") || m.contains("405b")) score -= 60
        if (m.contains("opus") || m.contains("pro") || m.contains("large") || m.contains("ultra")) score -= 45
        if (m.contains("thinking") || m.contains("-r1") || m.contains("reasoner") || m.contains("o1") || m.contains("o3")) score -= 70
        return score
    }
}
