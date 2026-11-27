package com.example.util

import com.example.data.local.MessageEntity

/**
 * Extracts memorable quoted dialogue lines from an interactive story conversation.
 */
object StoryQuoteExtractor {

    private val quoteRegex = Regex("[\"“«]([^\"”»]{16,180})[\"”»]")

    fun extractBestQuote(messages: List<MessageEntity>, botDisplayName: String = ""): String? {
        data class Candidate(val score: Int, val quote: String, val isAssistant: Boolean)
        val candidates = mutableListOf<Candidate>()
        for (m in messages) {
            if (m.role != "assistant" && m.role != "user") continue
            val visible = com.example.data.repository.EmochiRepository.cleanEmotionTags(m.text)
            if (visible.isBlank()) continue
            quoteRegex.findAll(visible).forEach { match ->
                val raw = match.groupValues[1].replace(Regex("\\s+"), " ").trim()
                if (raw.length in 16..180) {
                    var score = raw.length.coerceAtMost(120)
                    if (m.role == "assistant") score += 25
                    if (raw.contains('!') || raw.contains('?') || raw.contains("…") || raw.contains("...")) {
                        score += 20
                    }
                    candidates.add(Candidate(score, raw, m.role == "assistant"))
                }
            }
        }
        val best = candidates.maxByOrNull { it.score } ?: return null
        return if (best.isAssistant && botDisplayName.isNotBlank()) {
            "“${best.quote}” — $botDisplayName"
        } else {
            "“${best.quote}”"
        }
    }
}
