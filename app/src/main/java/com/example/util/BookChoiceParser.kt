package com.example.util

/**
 * Deterministic parser for interactive book choices:
 * - Parses user text inputs ("1", "birinci", "2️⃣", longest-common-substring) into 1-based indices.
 * - Extracts structured UI metadata (badges like `[Cesur]`, `[Gizli]`, `[Riskli]`, `[İlişki: ...]`,
 *   locked gates `[Kilitli: ...]`) for both deterministic story nodes and custom AI book responses.
 * - Strictly crash-safe on malformed, empty, or unclosed bracket inputs.
 */
object BookChoiceParser {

    enum class ChoiceBadgeKind {
        TONE,
        SECRET,
        RISK,
        RELATIONSHIP,
        BRANCH,
        LOCKED
    }

    data class ParsedChoiceBadge(
        val label: String,
        val kind: ChoiceBadgeKind
    )

    data class ParsedBookChoice(
        val displayText: String,
        val rawLine: String,
        val badges: List<ParsedChoiceBadge> = emptyList(),
        val relationshipHint: String? = null,
        val isLocked: Boolean = false,
        val lockReason: String? = null,
        val indexNumber: Int = 0
    )

    private val numberWords = mapOf(
        "bir" to 1, "birinci" to 1, "birincisini" to 1, "birinci seçenek" to 1,
        "iki" to 2, "ikinci" to 2, "ikincisini" to 2, "ikinci seçenek" to 2,
        "üç" to 3, "üçüncü" to 3, "üçüncüsünü" to 3, "üçüncü seçenek" to 3,
        "dört" to 4, "dördüncü" to 4, "dördüncüsünü" to 4,
        "one" to 1, "first" to 1, "two" to 2, "second" to 2,
        "three" to 3, "third" to 3, "four" to 4, "fourth" to 4
    )

    private val emojiNumbers = mapOf(
        "1️⃣" to 1, "2️⃣" to 2, "3️⃣" to 3, "4️⃣" to 4,
        "5️⃣" to 5, "6️⃣" to 6
    )

    // Matches single-level well-formed [...] tags without crossing nested brackets
    private val bracketTagRegex = Regex("\\[([^\\[\\]]{1,80})\\]")

    // Matches leading choice markers like "1.", "2)", "1️⃣", "A)", "- ", "• "
    private val choiceLinePrefixRegex = Regex(
        "^\\s*(?:([1-6])\\s*[\\.)\\-:]|([1-6])️⃣|([A-Fa-f])\\s*[\\.):])\\s+(.+)$"
    )

    /**
     * Parses a single choice label (deterministic or AI-generated) into clean display text,
     * semantic badges, relationship hints, and conditional lock metadata.
     * Never throws on malformed, blank, or unclosed bracket strings.
     */
    fun parseChoiceDisplay(
        rawLabel: String,
        toneHint: String = "",
        isBranching: Boolean = false,
        isRelationship: Boolean = false,
        isLockedExternal: Boolean = false,
        externalLockReason: String? = null,
        indexNumber: Int = 0
    ): ParsedBookChoice {
        val safeRaw = rawLabel.trim()
        if (safeRaw.isBlank()) {
            val fallbackBadges = buildList {
                if (isLockedExternal) {
                    add(ParsedChoiceBadge(externalLockReason?.takeIf { it.isNotBlank() } ?: "Kilitli", ChoiceBadgeKind.LOCKED))
                }
                if (toneHint.isNotBlank()) {
                    add(ParsedChoiceBadge(toneHint.trim(), if (isBranching) ChoiceBadgeKind.BRANCH else ChoiceBadgeKind.TONE))
                }
            }
            return ParsedBookChoice(
                displayText = "",
                rawLine = rawLabel,
                badges = fallbackBadges,
                relationshipHint = null,
                isLocked = isLockedExternal,
                lockReason = externalLockReason?.takeIf { it.isNotBlank() },
                indexNumber = indexNumber
            )
        }

        val badges = mutableListOf<ParsedChoiceBadge>()
        val seenBadgeLabels = mutableSetOf<String>()
        var relationshipHint: String? = null
        var isLocked = isLockedExternal
        var lockReason: String? = externalLockReason?.takeIf { it.isNotBlank() }

        fun addBadge(label: String, kind: ChoiceBadgeKind) {
            val clean = label.trim()
            if (clean.isBlank()) return
            val key = "${kind.name}:${clean.lowercase()}"
            if (seenBadgeLabels.add(key)) {
                badges.add(ParsedChoiceBadge(clean, kind))
            }
        }

        val matches = bracketTagRegex.findAll(safeRaw).toList()
        for (m in matches) {
            val inner = m.groupValues.getOrNull(1)?.trim().orEmpty()
            if (inner.isBlank()) continue
            val lower = inner.lowercase()

            when {
                lower.startsWith("kilitli:") || lower.startsWith("locked:") || lower.startsWith("koşul:") || lower.startsWith("şart:") -> {
                    val reason = inner.substringAfter(':').trim().ifBlank { "Koşul sağlanmadı" }
                    isLocked = true
                    lockReason = reason
                    addBadge("Kilitli: $reason", ChoiceBadgeKind.LOCKED)
                }
                lower == "kilitli" || lower == "locked" -> {
                    isLocked = true
                    if (lockReason == null) lockReason = "Koşul sağlanmadı"
                    addBadge("Kilitli", ChoiceBadgeKind.LOCKED)
                }
                lower.startsWith("ilişki:") || lower.startsWith("bağ:") || lower.startsWith("bond:") -> {
                    val hint = inner.substringAfter(':').trim()
                    if (hint.isNotBlank()) {
                        relationshipHint = hint
                        addBadge(hint, ChoiceBadgeKind.RELATIONSHIP)
                    }
                }
                lower.startsWith("gizli") || lower.startsWith("sır") || lower.startsWith("secret") -> {
                    val label = inner.substringAfter(':', inner).trim().ifBlank { "Gizli" }
                    addBadge(if (lower.contains(':')) "Gizli: $label" else inner, ChoiceBadgeKind.SECRET)
                }
                lower.startsWith("risk") || lower.startsWith("tehlike") || lower.startsWith("kritik") -> {
                    addBadge(inner, ChoiceBadgeKind.RISK)
                }
                lower.startsWith("dallanma") || lower.startsWith("kader") || lower.startsWith("yol ayrımı") -> {
                    addBadge(inner, ChoiceBadgeKind.BRANCH)
                }
                else -> {
                    addBadge(inner, ChoiceBadgeKind.TONE)
                }
            }
        }

        if (isLocked && lockReason != null && badges.none { it.kind == ChoiceBadgeKind.LOCKED }) {
            addBadge("Kilitli: $lockReason", ChoiceBadgeKind.LOCKED)
        }

        val cleanHint = toneHint.trim()
        if (cleanHint.isNotBlank()) {
            val hintLower = cleanHint.lowercase()
            val kind = when {
                isBranching -> ChoiceBadgeKind.BRANCH
                isRelationship -> ChoiceBadgeKind.RELATIONSHIP
                hintLower.contains("gizli") || hintLower.contains("sır") -> ChoiceBadgeKind.SECRET
                hintLower.contains("risk") || hintLower.contains("tehlike") || hintLower.contains("kırmızı göz") -> ChoiceBadgeKind.RISK
                else -> ChoiceBadgeKind.TONE
            }
            if (badges.none { it.label.equals(cleanHint, ignoreCase = true) }) {
                addBadge(cleanHint, kind)
            }
        }

        val stripped = bracketTagRegex.replace(safeRaw, " ")
            .replace(Regex("\\s{2,}"), " ")
            .replace(Regex("^[-–—:•·]+\\s*"), "")
            .trim()

        val finalDisplay = stripped.ifBlank { safeRaw }

        return ParsedBookChoice(
            displayText = finalDisplay,
            rawLine = rawLabel,
            badges = badges,
            relationshipHint = relationshipHint,
            isLocked = isLocked,
            lockReason = lockReason,
            indexNumber = indexNumber
        )
    }

    /**
     * Parses structured interactive book choices from an AI assistant message.
     * Supports `1. ...`, `1) ...`, `1️⃣ ...`, `A) ...`, and optional `[Cesur]`, `[Kilitli: ...]`, `[İlişki: ...]` tags.
     * Strictly crash-safe on empty, prose-only, or malformed inputs.
     */
    fun parseAiBookChoices(rawAiText: String): List<ParsedBookChoice> {
        if (rawAiText.isBlank()) return emptyList()
        val lines = rawAiText.lines()
        val results = mutableListOf<ParsedBookChoice>()
        var inChoiceSection = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue
            if (trimmed.startsWith("🔀") ||
                trimmed.contains("NE YAPACAKSIN", ignoreCase = true) ||
                trimmed.contains("KARAR NOKTASI", ignoreCase = true) ||
                trimmed.contains("SEÇENEKLER", ignoreCase = true)
            ) {
                inChoiceSection = true
                continue
            }

            val m = choiceLinePrefixRegex.matchEntire(trimmed)
            if (m != null) {
                val digitNum = m.groupValues[1].toIntOrNull()
                    ?: m.groupValues[2].toIntOrNull()
                    ?: m.groupValues[3].firstOrNull()?.uppercaseChar()?.let { it - 'A' + 1 }
                    ?: (results.size + 1)
                val body = m.groupValues[4].trim()
                if (body.isNotBlank()) {
                    results.add(
                        parseChoiceDisplay(
                            rawLabel = body,
                            indexNumber = digitNum
                        ).copy(rawLine = trimmed)
                    )
                }
            } else if (inChoiceSection && (trimmed.startsWith("- ") || trimmed.startsWith("• "))) {
                val body = trimmed.drop(2).trim()
                if (body.isNotBlank()) {
                    results.add(
                        parseChoiceDisplay(
                            rawLabel = body,
                            indexNumber = results.size + 1
                        ).copy(rawLine = trimmed)
                    )
                }
            }
        }
        return results
    }

    /**
     * Given user input and a list of choice texts, returns the 1-based choice index
     * or null if ambiguous / no match.
     */
    fun parseChoice(userInput: String, choices: List<String>): Int? {
        if (userInput.isBlank() || choices.isEmpty()) return null
        val normalized = userInput.trim().lowercase()

        // 1. Emoji number match
        for ((emoji, idx) in emojiNumbers) {
            if (userInput.contains(emoji) && idx <= choices.size) return idx
        }

        // 2. Bare digit match ("1", "2", "3")
        val bareDigit = Regex("^\\s*([1-6])\\s*\$").find(normalized)
        if (bareDigit != null) {
            val n = bareDigit.groupValues[1].toIntOrNull()
            if (n != null && n <= choices.size) return n
        }

        // 3. "1." / "1)" / "#1" prefix
        val prefixDigit = Regex("^[#]?([1-6])[.):]").find(normalized)
        if (prefixDigit != null) {
            val n = prefixDigit.groupValues[1].toIntOrNull()
            if (n != null && n <= choices.size) return n
        }

        // 4. Word-based number ("birinci", "ikinci" …)
        for ((word, idx) in numberWords) {
            if (normalized.contains(word) && idx <= choices.size) return idx
        }

        // 5. Keyword / longest-common-substring match against choice texts
        var bestIdx: Int? = null
        var bestScore = 0
        choices.forEachIndexed { i, choice ->
            val score = longestCommonSubstringLength(normalized, choice.lowercase())
            if (score > bestScore && score >= 4) {
                bestScore = score
                bestIdx = i + 1
            }
        }
        if (bestIdx != null) return bestIdx

        // 6. No match → caller should ask "Hangi seçeneği kastediyorsun?"
        return null
    }

    private fun longestCommonSubstringLength(a: String, b: String): Int {
        if (a.isEmpty() || b.isEmpty()) return 0
        var max = 0
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                if (a[i - 1] == b[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                    if (dp[i][j] > max) max = dp[i][j]
                }
            }
        }
        return max
    }
}
