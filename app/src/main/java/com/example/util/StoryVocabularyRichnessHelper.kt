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
        var total = 0L
        for (m in messages) {
            if (!m.role.equals("assistant", ignoreCase = true) || m.text.isBlank()) continue
            val visible = StoryDialogueToNarrationRatioHelper.stripHiddenTags(m.text)
            if (visible.isBlank()) continue
            wordRegex.findAll(visible).forEach { match ->
                val normalized = match.value.lowercase(trLocale).replace("\u0307", "")
                if (normalized.length >= 3) {
                    unique.add(normalized)
                    total++
                }
            }
        }
        if (total <= 0L) return RichnessStats(0, 0, 0, "Henüz veri yok")
        val pct = ((unique.size.toDouble() * 100.0) / total.toDouble()).toInt().coerceIn(1, 100)
        val tier = when {
            pct >= 65 -> "Çok Zengin"
            pct >= 45 -> "Zengin"
            else -> "Dengeli"
        }
        val safeTotal = total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return RichnessStats(unique.size, safeTotal, pct, tier)
    }

    fun formatCompact(stats: RichnessStats): String {
        if (stats.totalWords <= 0) return stats.label
        return "${stats.label} (${stats.uniqueWords} benzersiz • %${stats.richnessPercent.coerceIn(0, 100)})"
    }
}
