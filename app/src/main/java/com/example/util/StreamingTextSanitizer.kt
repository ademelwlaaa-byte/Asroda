package com.example.util

/**
 * Sistem #23: Gerçek Zamanlı Gizli Etiket & `<think>` Kalkanı.
 *
 * SSE akışından gelen 1-3 karakterlik parçalanmış token'ları canlı olarak süzer:
 * 1. `<think>...</think>` ve `<reasoning>...</reasoning>` bloklarını 0 karakter sızıntısıyla
 *    bastırır ve aktifken [isThinking] bayrağını `true` tutar.
 * 2. `[[STATE_JSON ...]]`, `[CHARACTER_EMOTION ...]`, `[ACTIVE_MEMORY_CALL ...]` ve
 *    `[EMOTION_UPDATE ...]` etiketlerini ilk `[` karakterinden itibaren lookahead tamponuyla
 *    yakalar; 0 karakter sızıntısıyla yutar ancak ham yanıtı [rawFullText] içinde eksiksiz korur.
 * 3. Normal köşeli parantez ifadelerini (ör. `[gülümseyerek]`, `[fısıldayarak]`) önek eşleşmesi
 *    bozulduğu anda ekrana anında serbest bırakır.
 */
class StreamingTextSanitizer {

    data class SanitizerDelta(
        val visibleDelta: String,
        val isThinking: Boolean
    )

    private enum class Mode {
        NORMAL,
        IN_THINK_TAG,
        IN_HIDDEN_TAG
    }

    private companion object {
        val THINK_OPEN_TAGS = listOf("<think>", "<reasoning>", "<thought>", "<scratchpad>", "<internal>")
        val THINK_CLOSE_BY_OPEN = mapOf(
            "<think>" to "</think>",
            "<reasoning>" to "</reasoning>",
            "<thought>" to "</thought>",
            "<scratchpad>" to "</scratchpad>",
            "<internal>" to "</internal>"
        )
        val HIDDEN_TAG_PREFIXES = listOf(
            "[[state_json",
            "[[state ",
            "[[memory_save",
            "[[character_emotion",
            "[[cast_member",
            "[[world_atmosphere",
            "[character_emotion",
            "[cast_member",
            "[world_atmosphere",
            "[active_memory_call",
            "[emotion_update"
        )
    }

    private val rawBuffer = StringBuilder()
    private val visibleBuffer = StringBuilder()
    private val pending = StringBuilder()

    private var mode: Mode = Mode.NORMAL
    private var expectedThinkClose: String = "</think>"
    private var hiddenDoubleBracket: Boolean = false
    private var hiddenBraceDepth: Int = 0
    private var hiddenInJsonString: Boolean = false
    private var hiddenEscapeNext: Boolean = false
    private var hiddenTrailingBracket: Boolean = false
    private var thinkCloseMatchBuf = StringBuilder()

    val isThinking: Boolean
        get() = mode == Mode.IN_THINK_TAG

    val rawFullText: String
        get() = rawBuffer.toString()

    val visibleText: String
        get() = visibleBuffer.toString()

    /**
     * Yeni gelen akış parçasını işler; kullanıcıya güvenle gösterilebilecek yeni metin parçasını
     * ve güncel düşünme ([isThinking]) durumunu döndürür.
     */
    fun append(chunk: String): SanitizerDelta {
        if (chunk.isEmpty()) {
            return SanitizerDelta(visibleDelta = "", isThinking = isThinking)
        }
        rawBuffer.append(chunk)
        val emitted = StringBuilder()

        for (ch in chunk) {
            when (mode) {
                Mode.IN_THINK_TAG -> processThinkChar(ch)
                Mode.IN_HIDDEN_TAG -> processHiddenTagChar(ch)
                Mode.NORMAL -> processNormalChar(ch, emitted)
            }
        }

        if (emitted.isNotEmpty()) {
            visibleBuffer.append(emitted)
        }
        return SanitizerDelta(
            visibleDelta = emitted.toString(),
            isThinking = isThinking
        )
    }

    /**
     * Akış bittiğinde güvenli tamponda kalmış normal karakterleri (ör. cümle sonundaki tek `[` veya `<`)
     * serbest bırakır; yarım kalmış gizli etiket veya `<think>` bloklarını ise asla sızdırmaz.
     */
    fun finish(): SanitizerDelta {
        val emitted = StringBuilder()
        if (mode == Mode.NORMAL && pending.isNotEmpty()) {
            val remaining = pending.toString()
            pending.setLength(0)
            if (!looksLikeHiddenOrThinkStart(remaining)) {
                emitted.append(remaining)
                visibleBuffer.append(remaining)
            }
        } else {
            pending.setLength(0)
            thinkCloseMatchBuf.setLength(0)
            if (mode == Mode.IN_THINK_TAG) {
                mode = Mode.NORMAL
            }
        }
        return SanitizerDelta(
            visibleDelta = emitted.toString(),
            isThinking = false
        )
    }

    private fun processThinkChar(ch: Char) {
        thinkCloseMatchBuf.append(ch)
        val closeLen = expectedThinkClose.length
        if (thinkCloseMatchBuf.length > closeLen) {
            thinkCloseMatchBuf.delete(0, thinkCloseMatchBuf.length - closeLen)
        }
        if (thinkCloseMatchBuf.length == closeLen &&
            thinkCloseMatchBuf.toString().equals(expectedThinkClose, ignoreCase = true)
        ) {
            thinkCloseMatchBuf.setLength(0)
            mode = Mode.NORMAL
        }
    }

    private fun processHiddenTagChar(ch: Char) {
        if (hiddenDoubleBracket) {
            if (hiddenInJsonString) {
                if (hiddenEscapeNext) {
                    hiddenEscapeNext = false
                } else if (ch == '\\') {
                    hiddenEscapeNext = true
                } else if (ch == '"') {
                    hiddenInJsonString = false
                }
                hiddenTrailingBracket = false
                return
            }

            when (ch) {
                '"' -> {
                    hiddenInJsonString = true
                    hiddenTrailingBracket = false
                }
                '{' -> {
                    hiddenBraceDepth++
                    hiddenTrailingBracket = false
                }
                '}' -> {
                    if (hiddenBraceDepth > 0) hiddenBraceDepth--
                    hiddenTrailingBracket = false
                }
                ']' -> {
                    if (hiddenBraceDepth <= 0) {
                        if (hiddenTrailingBracket) {
                            // Found closing `]]` outside JSON braces
                            hiddenTrailingBracket = false
                            hiddenDoubleBracket = false
                            hiddenBraceDepth = 0
                            mode = Mode.NORMAL
                        } else {
                            hiddenTrailingBracket = true
                        }
                    } else {
                        hiddenTrailingBracket = false
                    }
                }
                else -> {
                    hiddenTrailingBracket = false
                }
            }
        } else {
            if (ch == ']') {
                mode = Mode.NORMAL
            }
        }
    }

    private fun processNormalChar(ch: Char, emitted: StringBuilder) {
        if (pending.isEmpty()) {
            if (ch == '<' || ch == '[') {
                pending.append(ch)
            } else {
                emitted.append(ch)
            }
            return
        }

        pending.append(ch)
        val lower = pending.toString().lowercase(java.util.Locale.ROOT)

        // 1. Check exact think/reasoning open tag match
        for (openTag in THINK_OPEN_TAGS) {
            if (lower == openTag) {
                expectedThinkClose = THINK_CLOSE_BY_OPEN[openTag] ?: "</think>"
                thinkCloseMatchBuf.setLength(0)
                pending.setLength(0)
                mode = Mode.IN_THINK_TAG
                return
            }
        }

        // 2. Check exact hidden tag prefix match
        for (hiddenPrefix in HIDDEN_TAG_PREFIXES) {
            if (lower == hiddenPrefix) {
                hiddenDoubleBracket = hiddenPrefix.startsWith("[[")
                hiddenBraceDepth = 0
                hiddenInJsonString = false
                hiddenEscapeNext = false
                hiddenTrailingBracket = false
                pending.setLength(0)
                mode = Mode.IN_HIDDEN_TAG
                return
            }
        }

        // 3. Still a potential prefix of any think or hidden tag? Keep buffering.
        val couldBeThink = THINK_OPEN_TAGS.any { it.startsWith(lower) }
        val couldBeHidden = HIDDEN_TAG_PREFIXES.any { it.startsWith(lower) }
        if (couldBeThink || couldBeHidden) {
            return
        }

        // 4. Prefix diverged (e.g. `[g` from `[gülümseyerek]` or `<3`):
        // Release the safe prefix, re-checking if the last character itself starts a new tag (`<` or `[`).
        val snapshot = pending.toString()
        pending.setLength(0)
        if (ch == '<' || ch == '[') {
            emitted.append(snapshot, 0, snapshot.length - 1)
            pending.append(ch)
        } else {
            emitted.append(snapshot)
        }
    }

    private fun looksLikeHiddenOrThinkStart(text: String): Boolean {
        val lower = text.lowercase(java.util.Locale.ROOT)
        if (lower.length < 3) return false
        return THINK_OPEN_TAGS.any { it.startsWith(lower) } ||
            HIDDEN_TAG_PREFIXES.any { it.startsWith(lower) }
    }
}
