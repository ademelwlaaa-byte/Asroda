package com.example.util

import com.example.data.local.MessageEntity

/**
 * Computes average user turn word count and participation summary for Story Stats.
 */
object StoryUserParticipationHelper {

    data class ParticipationStats(
        val userTurnCount: Int,
        val avgUserWords: Int,
        val styleLabel: String
    )

    fun analyze(messages: List<MessageEntity>): ParticipationStats {
        val userMsgs = messages.filter { it.role == "user" && it.text.isNotBlank() }
        if (userMsgs.isEmpty()) return ParticipationStats(0, 0, "Henüz mesaj yok")
        val totalWords = userMsgs.sumOf { TextWordCounter.countWords(it.text) }
        val avg = (totalWords / userMsgs.size).coerceAtLeast(1)
        val style = when {
            avg >= 40 -> "Roman Yazarı"
            avg >= 18 -> "Detaylı Oyuncu"
            else -> "Hızlı & Dinamik"
        }
        return ParticipationStats(userTurnCount = userMsgs.size, avgUserWords = avg, styleLabel = style)
    }

    fun formatCompact(stats: ParticipationStats): String {
        if (stats.userTurnCount == 0) return stats.styleLabel
        return "${stats.styleLabel} (Ort. ${stats.avgUserWords} kelime/mesaj)"
    }
}
