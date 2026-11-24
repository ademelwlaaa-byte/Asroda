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
