package com.example.util

import com.example.data.local.MessageEntity
import java.util.Locale

/**
 * Turkish-locale-aware in-chat message search & context snippet extractor.
 */
object ChatSearchHelper {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    data class SearchMatch(
        val messageId: String,
        val snippet: String,
        val matchCountInMessage: Int
    )

    fun findMatches(messages: List<MessageEntity>, rawQuery: String): List<SearchMatch> {
        val q = rawQuery.trim().lowercase(trLocale)
        if (q.length < 2) return emptyList()
        val out = mutableListOf<SearchMatch>()
        for (m in messages) {
            if (m.role != "user" && m.role != "assistant") continue
            val lower = m.text.lowercase(trLocale)
            var idx = lower.indexOf(q)
            if (idx < 0) continue
            val firstIdx = idx
            var count = 0
            while (idx >= 0) {
                count++
                idx = lower.indexOf(q, idx + q.length)
            }
            val start = (firstIdx - 32).coerceAtLeast(0)
            val end = (firstIdx + q.length + 48).coerceAtMost(m.text.length)
            val prefix = if (start > 0) "…" else ""
            val suffix = if (end < m.text.length) "…" else ""
            val snippet = prefix + m.text.substring(start, end).replace(Regex("\\s+"), " ").trim() + suffix
            out.add(SearchMatch(messageId = m.id, snippet = snippet, matchCountInMessage = count))
        }
        return out
    }
}
