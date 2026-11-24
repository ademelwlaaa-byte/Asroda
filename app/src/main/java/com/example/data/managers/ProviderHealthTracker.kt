package com.example.data.managers

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

enum class ProviderFailureType {
    AUTH_ERROR,
    RATE_LIMIT,
    SERVER_OR_TIMEOUT,
    CONTENT_REFUSAL,
    EMPTY_RESPONSE,
    UNKNOWN
}

data class ProviderHealthState(
    val providerKey: String,
    val consecutiveFailures: Int = 0,
    val rateLimitStrikes: Int = 0,
    val cooldownUntilMs: Long = 0L,
    val lastFailureType: ProviderFailureType? = null,
    val lastErrorMessage: String = "",
    val lastLatencyMs: Long = 0L,
    val lastSuccessMs: Long = 0L,
    val totalSuccessCount: Int = 0,
    val totalFailureCount: Int = 0
)

/**
 * Thread-safe circuit breaker & live telemetry tracker for all configured AI providers.
 *
 * Rules:
 *  - 401/403 (AUTH_ERROR)       -> 15 min cooldown (cleared immediately via [resetHealth] when key changes or test succeeds)
 *  - 429 (RATE_LIMIT)           -> Retry-After seconds if provided, else escalating 60s -> 180s -> 300s backoff
 *  - 5xx / Timeout              -> 1st failure passes through to next provider; 2+ consecutive failures -> 45s cooldown
 *  - CONTENT_REFUSAL / EMPTY    -> 0s long-term lockout (skipped only for the current turn)
 *  - Emergency Fallback Rule    -> If ALL providers in a chain are in cooldown, returns them sorted by earliest expiry
 */
object ProviderHealthTracker {

    const val AUTH_COOLDOWN_MS = 15L * 60_000L
    const val RATE_LIMIT_STEP_1_MS = 60_000L
    const val RATE_LIMIT_STEP_2_MS = 180_000L
    const val RATE_LIMIT_STEP_3_MS = 300_000L
    const val SERVER_COOLDOWN_MS = 45_000L
    const val UNKNOWN_COOLDOWN_MS = 30_000L

    /** Injectable clock for deterministic unit testing. */
    @Volatile
    var clockMs: () -> Long = { System.currentTimeMillis() }

    private val states = ConcurrentHashMap<String, ProviderHealthState>()
    private val _healthSnapshot = MutableStateFlow<Map<String, ProviderHealthState>>(emptyMap())
    val healthSnapshot: StateFlow<Map<String, ProviderHealthState>> = _healthSnapshot.asStateFlow()

    private fun publishSnapshot() {
        _healthSnapshot.value = states.toMap()
    }

    fun getState(providerKey: String): ProviderHealthState? = states[providerKey.lowercase(Locale.ROOT)]

    fun isAvailable(providerKey: String): Boolean {
        val st = states[providerKey.lowercase(Locale.ROOT)] ?: return true
        return clockMs() >= st.cooldownUntilMs
    }

    fun remainingCooldownMs(providerKey: String): Long {
        val st = states[providerKey.lowercase(Locale.ROOT)] ?: return 0L
        return (st.cooldownUntilMs - clockMs()).coerceAtLeast(0L)
    }

    fun classifyFailure(httpCode: Int? = null, errorMessage: String? = null): ProviderFailureType {
        val code = httpCode ?: extractHttpCode(errorMessage)
        val msg = errorMessage.orEmpty().lowercase(Locale.ROOT)
        return when {
            code == 401 || code == 403 ||
                msg.contains("unauthorized") ||
                msg.contains("invalid api key") ||
                msg.contains("invalid_api_key") ||
                msg.contains("authentication") ||
                msg.contains("api key not valid") ||
                msg.contains("permission_denied") -> ProviderFailureType.AUTH_ERROR

            code == 429 ||
                msg.contains("rate limit") ||
                msg.contains("rate_limit") ||
                msg.contains("too many requests") ||
                msg.contains("quota") ||
                msg.contains("resource_exhausted") -> ProviderFailureType.RATE_LIMIT

            (code != null && code in 500..599) ||
                msg.contains("timeout") ||
                msg.contains("timed out") ||
                msg.contains("socket") ||
                msg.contains("connection reset") ||
                msg.contains("unable to resolve host") ||
                msg.contains("failed to connect") ||
                msg.contains("service unavailable") ||
                msg.contains("bad gateway") -> ProviderFailureType.SERVER_OR_TIMEOUT

            msg.contains("ooc_refusal") ||
                msg.contains("content_filter") ||
                msg.contains("safety") ||
                msg.contains("prohibited_content") -> ProviderFailureType.CONTENT_REFUSAL

            msg.contains("boş yanıt") || msg.contains("empty response") -> ProviderFailureType.EMPTY_RESPONSE

            else -> ProviderFailureType.UNKNOWN
        }
    }

    fun extractHttpCode(errorMessage: String?): Int? {
        if (errorMessage.isNullOrBlank()) return null
        val match = Regex("""\b(?:HTTP|code|status)\s*[:=]?\s*(\d{3})\b""", RegexOption.IGNORE_CASE)
            .find(errorMessage)
            ?: Regex("""\b(401|403|429|500|502|503|504)\b""").find(errorMessage)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    fun extractRetryAfterSeconds(errorMessage: String?): Long? {
        if (errorMessage.isNullOrBlank()) return null
        val match = Regex("""retry[-_ ]?after\s*[:=]?\s*(\d+)""", RegexOption.IGNORE_CASE)
            .find(errorMessage)
        return match?.groupValues?.getOrNull(1)?.toLongOrNull()
    }

    fun recordSuccess(providerKey: String, latencyMs: Long = 0L) {
        val key = providerKey.lowercase(Locale.ROOT)
        if (latencyMs > 0L) {
            com.example.util.ProviderRollingLatencyTracker.recordSample(key, latencyMs)
        }
        val now = clockMs()
        states.compute(key) { _, prev ->
            val current = prev ?: ProviderHealthState(providerKey = key)
            current.copy(
                consecutiveFailures = 0,
                rateLimitStrikes = 0,
                cooldownUntilMs = 0L,
                lastFailureType = null,
                lastErrorMessage = "",
                lastLatencyMs = if (latencyMs > 0L) latencyMs else current.lastLatencyMs,
                lastSuccessMs = now,
                totalSuccessCount = current.totalSuccessCount + 1
            )
        }
        publishSnapshot()
    }

    fun recordFailure(
        providerKey: String,
        failureType: ProviderFailureType,
        errorMessage: String = "",
        retryAfterSeconds: Long? = null
    ): ProviderHealthState {
        val key = providerKey.lowercase(Locale.ROOT)
        val now = clockMs()
        val updated = states.compute(key) { _, prev ->
            val current = prev ?: ProviderHealthState(providerKey = key)
            val nextConsecutive = current.consecutiveFailures + 1
            val nextRateStrikes = if (failureType == ProviderFailureType.RATE_LIMIT) {
                current.rateLimitStrikes + 1
            } else {
                current.rateLimitStrikes
            }

            val cooldownMs = when (failureType) {
                ProviderFailureType.AUTH_ERROR -> AUTH_COOLDOWN_MS
                ProviderFailureType.RATE_LIMIT -> {
                    val parsedRetry = retryAfterSeconds ?: extractRetryAfterSeconds(errorMessage)
                    if (parsedRetry != null && parsedRetry > 0L) {
                        parsedRetry.coerceIn(5L, 600L) * 1000L
                    } else {
                        when (nextRateStrikes) {
                            1 -> RATE_LIMIT_STEP_1_MS
                            2 -> RATE_LIMIT_STEP_2_MS
                            else -> RATE_LIMIT_STEP_3_MS
                        }
                    }
                }
                ProviderFailureType.SERVER_OR_TIMEOUT -> {
                    if (nextConsecutive >= 2) SERVER_COOLDOWN_MS else 0L
                }
                ProviderFailureType.CONTENT_REFUSAL,
                ProviderFailureType.EMPTY_RESPONSE -> 0L
                ProviderFailureType.UNKNOWN -> {
                    if (nextConsecutive >= 2) UNKNOWN_COOLDOWN_MS else 0L
                }
            }

            current.copy(
                consecutiveFailures = if (failureType == ProviderFailureType.CONTENT_REFUSAL || failureType == ProviderFailureType.EMPTY_RESPONSE) {
                    current.consecutiveFailures
                } else {
                    nextConsecutive
                },
                rateLimitStrikes = nextRateStrikes,
                cooldownUntilMs = if (cooldownMs > 0L) maxOf(current.cooldownUntilMs, now + cooldownMs) else current.cooldownUntilMs,
                lastFailureType = failureType,
                lastErrorMessage = errorMessage.take(200),
                totalFailureCount = current.totalFailureCount + 1
            )
        }!!
        publishSnapshot()
        return updated
    }

    /**
     * Orders candidate providers for execution:
     *  - Available (non-cooled-down) providers stay in exact user priority order.
     *  - Cooled-down providers are skipped.
     *  - Emergency Rule: if EVERY provider in [chain] is in cooldown, returns them sorted by
     *    earliest `cooldownUntilMs` so the user still gets a last-resort attempt.
     */
    fun selectOrderedCandidates(chain: List<String>): List<String> {
        if (chain.isEmpty()) return emptyList()
        val now = clockMs()
        val available = chain.filter { key ->
            val st = states[key.lowercase(Locale.ROOT)]
            st == null || now >= st.cooldownUntilMs
        }
        if (available.isNotEmpty()) return available

        return chain.sortedBy { key ->
            states[key.lowercase(Locale.ROOT)]?.cooldownUntilMs ?: 0L
        }
    }

    fun resetHealth(providerKey: String) {
        states.remove(providerKey.lowercase(Locale.ROOT))
        publishSnapshot()
    }

    fun resetAll() {
        states.clear()
        publishSnapshot()
    }
}

/**
 * False-positive-safe detector for Out-of-Character AI assistant refusals & moralizing lectures.
 *
 * Strictly distinguishes true AI-assistant policy refusals ("Bir yapay zeka olarak bu konuda yardımcı olamam")
 * from legitimate in-character dramatic novel dialogue ("Bu doğru değil ama yine de seninle geliyorum.").
 */
object RefusalDetector {

    data class RefusalCheckResult(
        val isRefusal: Boolean,
        val matchedReason: String = ""
    )

    // Unambiguous AI-assistant self-identification / hard policy refusal phrases
    private val HARD_OOC_REFUSAL_PATTERNS = listOf(
        "bir yapay zeka olarak",
        "yapay zeka modeli olarak",
        "dil modeli olarak",
        "yapay zeka asistanı olarak",
        "etik kurallarım gereği",
        "etik ilkelerim gereği",
        "güvenlik politikam gereği",
        "güvenlik yönergelerim",
        "kullanım politikaları gereği",
        "topluluk kurallarına aykırı",
        "bu içeriği üretemem",
        "böyle bir içerik üretemem",
        "bu isteği yerine getiremem",
        "bu talebi yerine getiremem",
        "bu konuda yardımcı olamam",
        "bu konuda sana yardımcı olamam",
        "bu senaryoya devam edemem",
        "bu rol yapma sahnesine devam edemem",
        "müstehcen içerik üretemem",
        "yetişkin içerik üretemem",
        "as an ai",
        "as an artificial intelligence",
        "as a language model",
        "i cannot fulfill this request",
        "i can't fulfill this request",
        "i cannot assist with this",
        "i'm unable to generate",
        "i cannot generate sexually explicit",
        "i cannot generate explicit",
        "against my ethical guidelines",
        "against my safety guidelines",
        "violates my usage policy",
        "violates content policy",
        "i must decline this request"
    )

    // Preachy / real-world advice patterns that indicate OOC lecturing when outside narrative prose
    private val MORALIZING_LECTURE_PATTERNS = listOf(
        "polise başvurmalısın",
        "polise haber vermelisin",
        "yetkililere başvurmalısın",
        "yetkililere haber vermelisin",
        "sağlıklı iletişim kurmalısın",
        "profesyonel destek almalısın",
        "bir uzmandan yardım almalısın",
        "yasalara uymak önemlidir",
        "suç işlemek doğru değildir",
        "şiddet asla çözüm değildir",
        "contact the authorities",
        "report this to the police",
        "seek professional help",
        "it's important to respect boundaries and follow the law"
    )

    fun detectOocRefusal(rawText: String): RefusalCheckResult {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return RefusalCheckResult(false)

        val lower = trimmed.lowercase(Locale.forLanguageTag("tr-TR"))
        val lowerRoot = trimmed.lowercase(Locale.ROOT)

        // 1. Check hard unambiguous AI refusal markers (unless clearly inside deep novel prose with action beats)
        val hasNarrativeProse = trimmed.contains('*') && trimmed.contains('"') && trimmed.length > 180
        for (pattern in HARD_OOC_REFUSAL_PATTERNS) {
            if (lower.contains(pattern) || lowerRoot.contains(pattern)) {
                // Only ignore if buried inside a long multi-paragraph roleplay where the character actually talks about an AI
                val isShortOrAssistantStyle = trimmed.length < 480 || !hasNarrativeProse
                if (isShortOrAssistantStyle) {
                    return RefusalCheckResult(true, "OOC_REFUSAL: $pattern")
                }
            }
        }

        // 2. Check moralizing real-world lecturing — NEVER flag if written as in-character novel dialogue ("..." or *...*)
        val looksLikeInCharacterDialogue =
            (trimmed.contains('"') || trimmed.contains('“') || trimmed.contains('*'))
        if (!looksLikeInCharacterDialogue && trimmed.length <= 600) {
            for (lecture in MORALIZING_LECTURE_PATTERNS) {
                if (lower.contains(lecture) || lowerRoot.contains(lecture)) {
                    return RefusalCheckResult(true, "OOC_MORALIZING: $lecture")
                }
            }
        }

        return RefusalCheckResult(false)
    }
}
