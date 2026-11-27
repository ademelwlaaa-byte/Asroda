package com.example.data.managers

import com.example.util.SecureApiKeyVault
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
 *  - 429/402 (RATE_LIMIT)       -> Retry-After seconds if provided, else escalating 60s -> 180s -> 300s backoff
 *  - 5xx / 529 / Timeout / SSL  -> 1st failure passes through to next provider; 2+ consecutive failures -> 45s cooldown
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

    private val TR_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
    private val HTTP_CODE_PREFIX_REGEX = Regex("""\b(?:HTTP|code|status)\s*[:=]?\s*(\d{3})\b""", RegexOption.IGNORE_CASE)
    private val HTTP_CODE_BARE_REGEX = Regex("""\b(401|402|403|429|500|502|503|504|529)\b""")
    private val RETRY_AFTER_REGEX = Regex("""retry[-_ ]?after\s*[:=]?\s*(\d+)""", RegexOption.IGNORE_CASE)

    /** Injectable clock for deterministic unit testing. */
    @Volatile
    var clockMs: () -> Long = { System.currentTimeMillis() }

    private val states = ConcurrentHashMap<String, ProviderHealthState>()
    private val _healthSnapshot = MutableStateFlow<Map<String, ProviderHealthState>>(emptyMap())
    val healthSnapshot: StateFlow<Map<String, ProviderHealthState>> = _healthSnapshot.asStateFlow()

    private fun normalizeKey(providerKey: String): String =
        providerKey.trim().lowercase(Locale.ROOT)

    private fun publishSnapshot() {
        _healthSnapshot.value = states.toMap()
    }

    fun getState(providerKey: String): ProviderHealthState? {
        val key = normalizeKey(providerKey)
        if (key.isEmpty()) return null
        return states[key]
    }

    fun isAvailable(providerKey: String): Boolean {
        val key = normalizeKey(providerKey)
        if (key.isEmpty()) return true
        val st = states[key] ?: return true
        return clockMs() >= st.cooldownUntilMs
    }

    fun remainingCooldownMs(providerKey: String): Long {
        val key = normalizeKey(providerKey)
        if (key.isEmpty()) return 0L
        val st = states[key] ?: return 0L
        return (st.cooldownUntilMs - clockMs()).coerceAtLeast(0L)
    }

    fun classifyFailure(httpCode: Int? = null, errorMessage: String? = null): ProviderFailureType {
        val code = httpCode ?: extractHttpCode(errorMessage)
        val raw = errorMessage.orEmpty()
        val msg = raw.lowercase(Locale.ROOT)
        val msgTr = raw.lowercase(TR_LOCALE)
        fun has(token: String): Boolean = msg.contains(token) || msgTr.contains(token)

        return when {
            code == 401 || code == 403 ||
                has("unauthorized") ||
                has("invalid api key") ||
                has("invalid_api_key") ||
                has("authentication") ||
                has("api key not valid") ||
                has("permission_denied") -> ProviderFailureType.AUTH_ERROR

            code == 429 || code == 402 ||
                has("rate limit") ||
                has("rate_limit") ||
                has("too many requests") ||
                has("quota") ||
                has("resource_exhausted") ||
                has("insufficient") ||
                has("billing") ||
                has("credit") -> ProviderFailureType.RATE_LIMIT

            (code != null && (code in 500..599 || code == 529)) ||
                has("overloaded") ||
                has("timeout") ||
                has("timed out") ||
                has("zaman aşımı") ||
                has("zaman asimi") ||
                has("ssl") ||
                has("handshake") ||
                has("eof") ||
                has("unexpected end of stream") ||
                has("stream reset") ||
                has("socket") ||
                has("connection reset") ||
                has("unable to resolve host") ||
                has("failed to connect") ||
                has("service unavailable") ||
                has("bad gateway") -> ProviderFailureType.SERVER_OR_TIMEOUT

            has("ooc_refusal") ||
                has("content_filter") ||
                has("safety") ||
                has("prohibited_content") -> ProviderFailureType.CONTENT_REFUSAL

            has("boş yanıt") || has("empty response") -> ProviderFailureType.EMPTY_RESPONSE

            else -> ProviderFailureType.UNKNOWN
        }
    }

    fun extractHttpCode(errorMessage: String?): Int? {
        if (errorMessage.isNullOrBlank()) return null
        val match = HTTP_CODE_PREFIX_REGEX.find(errorMessage)
            ?: HTTP_CODE_BARE_REGEX.find(errorMessage)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    fun extractRetryAfterSeconds(errorMessage: String?): Long? {
        if (errorMessage.isNullOrBlank()) return null
        val match = RETRY_AFTER_REGEX.find(errorMessage)
        return match?.groupValues?.getOrNull(1)?.toLongOrNull()
    }

    fun recordSuccess(providerKey: String, latencyMs: Long = 0L) {
        val key = normalizeKey(providerKey)
        if (key.isEmpty()) return
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
        val key = normalizeKey(providerKey)
        val safeRedactedError = SecureApiKeyVault.redactSecrets(errorMessage).take(200)
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
                lastErrorMessage = safeRedactedError,
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
            val st = states[normalizeKey(key)]
            st == null || now >= st.cooldownUntilMs
        }
        if (available.isNotEmpty()) return available

        return chain.sortedBy { key ->
            states[normalizeKey(key)]?.cooldownUntilMs ?: 0L
        }
    }

    fun resetHealth(providerKey: String) {
        val key = normalizeKey(providerKey)
        if (key.isEmpty()) return
        states.remove(key)
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
 * Strips internal `<think>` / `<reasoning>` blocks first, then strictly distinguishes true
 * AI-assistant policy refusals ("Bir yapay zeka olarak bu konuda yardımcı olamam") from
 * legitimate in-character dramatic novel dialogue ("Bu doğru değil ama yine de seninle geliyorum.").
 */
object RefusalDetector {

    data class RefusalCheckResult(
        val isRefusal: Boolean,
        val matchedReason: String = ""
    )

    private val TR_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
    private val THINK_BLOCK_REGEX = Regex("(?is)<(?:think|thinking|reasoning)>.*?</(?:think|thinking|reasoning)>")
    private val UNCLOSED_THINK_REGEX = Regex("(?is)<(?:think|thinking|reasoning)>.*$")

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
        "bu sahneye devam edemem",
        "bu rol yapma sahnesine devam edemem",
        "müstehcen içerik üretemem",
        "yetişkin içerik üretemem",
        "bu tür içerikler üretemem",
        "üzgünüm ama bu konuda",
        "üzgünüm, bu isteği",
        "üzgünüm, bu konuda",
        "etik sınırlarım gereği",
        "as an ai",
        "as an ai assistant",
        "as an artificial intelligence",
        "as a language model",
        "i'm sorry, but i can't",
        "i'm sorry, i can't",
        "i'm sorry, i cannot",
        "i apologize, but i cannot",
        "i cannot continue",
        "i cannot comply with",
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

    internal fun stripReasoningBlocks(rawText: String): String {
        if (rawText.isBlank()) return ""
        return rawText
            .replace(THINK_BLOCK_REGEX, " ")
            .replace(UNCLOSED_THINK_REGEX, " ")
            .trim()
    }

    fun detectOocRefusal(rawText: String): RefusalCheckResult {
        val visible = stripReasoningBlocks(rawText)
        if (visible.isEmpty()) return RefusalCheckResult(false)

        val lower = visible.lowercase(TR_LOCALE)
        val lowerRoot = visible.lowercase(Locale.ROOT)

        // 1. Check hard unambiguous AI refusal markers (unless clearly inside deep novel prose with action beats)
        val hasNarrativeProse = visible.contains('*') && visible.contains('"') && visible.length > 180
        for (pattern in HARD_OOC_REFUSAL_PATTERNS) {
            if (lower.contains(pattern) || lowerRoot.contains(pattern)) {
                val isShortOrAssistantStyle = visible.length < 480 || !hasNarrativeProse
                if (isShortOrAssistantStyle) {
                    return RefusalCheckResult(true, "OOC_REFUSAL: $pattern")
                }
            }
        }

        // 2. Check moralizing real-world lecturing — NEVER flag if written as in-character novel dialogue ("..." or *...*)
        val looksLikeInCharacterDialogue =
            visible.contains('"') || visible.contains('“') || visible.contains('*')
        if (!looksLikeInCharacterDialogue && visible.length <= 600) {
            for (lecture in MORALIZING_LECTURE_PATTERNS) {
                if (lower.contains(lecture) || lowerRoot.contains(lecture)) {
                    return RefusalCheckResult(true, "OOC_MORALIZING: $lecture")
                }
            }
        }

        return RefusalCheckResult(false)
    }
}
