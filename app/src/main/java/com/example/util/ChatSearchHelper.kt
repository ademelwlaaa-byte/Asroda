package com.example.util

import com.example.data.local.MessageEntity
import java.util.Locale

/**
 * Turkish-locale & diacritic-tolerant in-chat message search & context snippet extractor.
 */
object ChatSearchHelper {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    data class SearchMatch(
        val messageId: String,
        val snippet: String,
        val matchCountInMessage: Int
    )

    /**
     * Folds Turkish diacritics (ç, ğ, ı, ö, ş, ü) to ASCII equivalents after Turkish-locale
     * lowercasing so queries like "isik" match "Işık" and "soguk" matches "soğuk".
     */
    fun foldTurkish(text: String): String {
        val lower = text.lowercase(trLocale)
        val sb = StringBuilder(lower.length)
        for (ch in lower) {
            sb.append(
                when (ch) {
                    'ç' -> 'c'
                    'ğ' -> 'g'
                    'ı', 'İ' -> 'i'
                    'ö' -> 'o'
                    'ş' -> 's'
                    'ü' -> 'u'
                    else -> ch
                }
            )
        }
        return sb.toString()
    }

    fun findMatches(messages: List<MessageEntity>, rawQuery: String): List<SearchMatch> {
        val q = foldTurkish(rawQuery.trim())
        if (q.length < 2) return emptyList()
        val out = mutableListOf<SearchMatch>()
        for (m in messages) {
            if (m.role != "user" && m.role != "assistant") continue
            val visibleText = com.example.data.repository.EmochiRepository.cleanEmotionTags(m.text)
            if (visibleText.isBlank()) continue
            val folded = foldTurkish(visibleText)
            var idx = folded.indexOf(q)
            if (idx < 0) continue
            val firstIdx = idx
            var count = 0
            while (idx >= 0) {
                count++
                idx = folded.indexOf(q, idx + q.length)
            }
            val start = (firstIdx - 32).coerceAtLeast(0)
            val end = (firstIdx + q.length + 48).coerceAtMost(visibleText.length)
            val prefix = if (start > 0) "…" else ""
            val suffix = if (end < visibleText.length) "…" else ""
            val snippet = prefix + visibleText.substring(start, end).replace(Regex("\\s+"), " ").trim() + suffix
            out.add(SearchMatch(messageId = m.id, snippet = snippet, matchCountInMessage = count))
        }
        return out
    }
}
