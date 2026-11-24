package com.example.util

import java.util.Locale

/**
 * Detects repetitive 4-word phrases across recent assistant roleplay turns so the prompt
 * engine can nudge LLMs away from cliché loops (common on open-source / free models).
 */
object RepetitionDetector {

    private val trLocale = Locale.forLanguageTag("tr-TR")
    private val nonWordRegex = Regex("[^\\p{L}\\p{Nd}\\s]+")
    private val spaceRegex = Regex("\\s+")

    fun detectLoopingPhrases(
        recentAssistantReplies: List<String>,
        ngramSize: Int = 4,
        minMessageOccurrences: Int = 2,
        maxResults: Int = 5
    ): List<String> {
        if (recentAssistantReplies.size < minMessageOccurrences || ngramSize < 2) return emptyList()
        val phraseDocFreq = LinkedHashMap<String, Int>()
        for (reply in recentAssistantReplies.takeLast(5)) {
            val words = nonWordRegex.replace(reply.lowercase(trLocale), " ")
                .trim()
                .split(spaceRegex)
                .filter { it.length >= 2 }
            if (words.size < ngramSize) continue
            val seenInThisReply = HashSet<String>()
            for (i in 0..(words.size - ngramSize)) {
                val gram = words.subList(i, i + ngramSize).joinToString(" ")
                if (seenInThisReply.add(gram)) {
                    phraseDocFreq[gram] = (phraseDocFreq[gram] ?: 0) + 1
                }
            }
        }
        return phraseDocFreq.entries
            .filter { it.value >= minMessageOccurrences }
            .sortedByDescending { it.value }
            .take(maxResults)
            .map { it.key }
    }
}
