package com.example.util

import com.example.data.local.MessageEntity

/**
 * Extracts memorable quoted dialogue lines from an interactive story conversation.
 */
object StoryQuoteExtractor {

    private val quoteRegex = Regex("[\"“«]([^\"”»]{16,180})[\"”»]")

    fun extractBestQuote(messages: List<MessageEntity>, botDisplayName: String = ""): String? {
        val candidates = mutableListOf<Pair<Int, String>>()
        for (m in messages) {
            if (m.role != "assistant" && m.role != "user") continue
            quoteRegex.findAll(m.text).forEach { match ->
                val raw = match.groupValues[1].replace(Regex("\\s+"), " ").trim()
                if (raw.length in 16..180) {
                    var score = raw.length.coerceAtMost(120)
                    if (m.role == "assistant") score += 25
                    if (raw.contains('!') || raw.contains('?') || raw.contains("…") || raw.contains("...")) {
                        score += 20
                    }
                    candidates.add(score to raw)
                }
            }
        }
        val best = candidates.maxByOrNull { it.first }?.second ?: return null
        return if (botDisplayName.isNotBlank()) {
            "“$best” — $botDisplayName"
        } else {
            "“$best”"
        }
    }
}
