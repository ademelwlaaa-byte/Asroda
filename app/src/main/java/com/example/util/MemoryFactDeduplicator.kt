package com.example.util

/**
 * Turkish diacritic- and punctuation-insensitive deduplicator for Neural Vault memory facts.
 */
object MemoryFactDeduplicator {

    private val punctSpaceRegex = Regex("[^a-z0-9]+")

    fun normalizeForKey(text: String): String {
        return ChatSearchHelper.foldTurkish(text.trim())
            .replace(punctSpaceRegex, " ")
            .trim()
    }

    fun isNearDuplicate(first: String, second: String): Boolean {
        val a = normalizeForKey(first)
        val b = normalizeForKey(second)
        if (a.isEmpty() || b.isEmpty()) return false
        return a == b
    }

    /**
     * Turkish morphological stem-aware semantic near-duplicate check for long memory facts.
     */
    fun isSemanticNearDuplicate(first: String, second: String, threshold: Float = 0.78f): Boolean {
        if (isNearDuplicate(first, second)) return true
        val stemsA = com.example.data.repository.TurkishTextAnalyzer.extractStems(first).toSet()
        val stemsB = com.example.data.repository.TurkishTextAnalyzer.extractStems(second).toSet()
        if (stemsA.size < 2 || stemsB.size < 2) return false
        val (small, large) = if (stemsA.size <= stemsB.size) stemsA to stemsB else stemsB to stemsA
        var matched = 0
        for (s in small) {
            if (s in large || (s.length >= 4 && large.any { l -> l.length >= 4 && (l.startsWith(s) || s.startsWith(l)) })) {
                matched++
            }
        }
        return (matched.toFloat() / small.size.toFloat()) >= threshold
    }

    fun deduplicate(facts: List<String>): List<String> {
        val seen = HashSet<String>()
        val result = mutableListOf<String>()
        for (fact in facts) {
            val trimmed = fact.trim()
            if (trimmed.isEmpty()) continue
            val key = normalizeForKey(trimmed)
            if (key.isNotEmpty() && seen.add(key)) {
                result.add(trimmed)
            }
        }
        return result
    }
}
