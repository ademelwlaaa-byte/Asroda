package com.example.book

import java.text.Collator
import java.util.Locale

/**
 * Sorts and formats Book Mode discovered secrets cleanly using Turkish collation.
 */
object BookSecretProgressHelper {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    fun sortedSecrets(secrets: Set<String>): List<String> {
        val collator = Collator.getInstance(trLocale)
        return secrets
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedWith { a, b -> collator.compare(a, b) }
    }

    fun formatSummary(secrets: Set<String>): String {
        val count = sortedSecrets(secrets).size
        return when (count) {
            0 -> "Henüz gizli sır keşfedilmedi"
            1 -> "1 gizli sır keşfedildi"
            else -> "$count gizli sır keşfedildi"
        }
    }
}
