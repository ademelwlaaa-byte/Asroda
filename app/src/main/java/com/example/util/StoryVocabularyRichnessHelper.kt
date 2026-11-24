package com.example.util

import com.example.data.local.MessageEntity
import java.util.Locale

/**
 * Computes lexical diversity (unique word richness) across assistant story turns.
 */
object StoryVocabularyRichnessHelper {

    private val trLocale = Locale.forLanguageTag("tr-TR")
    private val wordRegex = Regex("[\\p{L}]{3,}")

    data class RichnessStats(
        val uniqueWords: Int,
        val totalWords: Int,
        val richnessPercent: Int,
        val label: String
    )

    fun analyze(messages: List<MessageEntity>): RichnessStats {
        val unique = HashSet<String>()
        var total = 0
        for (m in messages) {
            if (m.role != "assistant") continue
            wordRegex.findAll(m.text).forEach { match ->
                unique.add(match.value.lowercase(trLocale))
                total++
            }
        }
        if (total == 0) return RichnessStats(0, 0, 0, "Henüz veri yok")
        val pct = ((unique.size * 100f) / total).toInt().coerceIn(1, 100)
        val tier = when {
            pct >= 65 -> "Çok Zengin"
            pct >= 45 -> "Zengin"
            else -> "Dengeli"
        }
        return RichnessStats(unique.size, total, pct, tier)
    }

    fun formatCompact(stats: RichnessStats): String {
        if (stats.totalWords == 0) return stats.label
        return "${stats.label} (${stats.uniqueWords} benzersiz • %${stats.richnessPercent})"
    }
}
