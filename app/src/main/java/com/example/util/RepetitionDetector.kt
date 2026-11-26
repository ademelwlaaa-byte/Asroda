package com.example.util

import com.example.data.repository.TurkishTextAnalyzer
import java.util.Locale

/**
 * Detects repetitive phrases, Turkish-stemmed cliché loops, and repetitive turn-opening
 * patterns across recent assistant roleplay turns so the prompt engine can nudge LLMs
 * away from narrative ruts.
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
        val window = recentAssistantReplies.takeLast(6)

        // Track surface phrase keyed by Turkish-stemmed signature so "gözlerini kıstı" and "gözlerini kısarak" group together
        val stemSigFreq = LinkedHashMap<String, Int>()
        val stemSigSurface = LinkedHashMap<String, String>()
        val openingFreq = LinkedHashMap<String, Int>()
        val openingSurface = LinkedHashMap<String, String>()

        for (reply in window) {
            val words = nonWordRegex.replace(reply.lowercase(trLocale), " ")
                .trim()
                .split(spaceRegex)
                .filter { it.length >= 2 }
            if (words.isEmpty()) continue

            // 1. Detect repetitive 2-word and 3-word opening clichés across consecutive turns
            if (words.size >= 2) {
                val open2Words = words.take(2)
                if (open2Words.any { it !in TurkishTextAnalyzer.STOP_WORDS }) {
                    val open2Raw = open2Words.joinToString(" ")
                    val open2Sig = "o2:" + open2Words.joinToString(" ") { TurkishTextAnalyzer.stemToken(it) }
                    openingFreq[open2Sig] = (openingFreq[open2Sig] ?: 0) + 1
                    openingSurface.putIfAbsent(open2Sig, open2Raw)
                }
            }
            if (words.size >= 3) {
                val open3Raw = words.take(3).joinToString(" ")
                val open3Sig = "o3:" + words.take(3).joinToString(" ") { TurkishTextAnalyzer.stemToken(it) }
                openingFreq[open3Sig] = (openingFreq[open3Sig] ?: 0) + 1
                openingSurface.putIfAbsent(open3Sig, open3Raw)
            }

            // 2. Detect repetitive Turkish-stemmed n-gram phrases (4-gram + 3-gram fallback for verb-inflected loops)
            val stems = words.map { TurkishTextAnalyzer.stemToken(it) }
            val effectiveSizes = if (ngramSize >= 4) listOf(ngramSize, 3) else listOf(ngramSize)
            for (sz in effectiveSizes) {
                if (words.size < sz) continue
                val seenSigInThisReply = HashSet<String>()
                for (i in 0..(words.size - sz)) {
                    val sliceWords = words.subList(i, i + sz)
                    // Require at least 2 content words so stopword-only 3-grams never false-trigger
                    if (sliceWords.count { it !in TurkishTextAnalyzer.STOP_WORDS } < 2) continue
                    val sig = "$sz:" + stems.subList(i, i + sz).joinToString(" ")
                    if (seenSigInThisReply.add(sig)) {
                        stemSigFreq[sig] = (stemSigFreq[sig] ?: 0) + 1
                        stemSigSurface.putIfAbsent(sig, sliceWords.joinToString(" "))
                    }
                }
            }
        }

        val combined = LinkedHashMap<String, Int>()
        for ((sig, count) in openingFreq) {
            if (count >= minMessageOccurrences) {
                val surface = openingSurface[sig] ?: continue
                combined[surface] = maxOf(combined[surface] ?: 0, count + 1)
            }
        }
        for ((sig, count) in stemSigFreq) {
            if (count >= minMessageOccurrences) {
                val surface = stemSigSurface[sig] ?: continue
                combined[surface] = maxOf(combined[surface] ?: 0, count)
            }
        }

        return combined.entries
            .sortedByDescending { it.value }
            .take(maxResults)
            .map { it.key }
    }
}
