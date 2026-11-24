package com.example.util

import com.example.data.local.MessageEntity
import java.util.Locale

/**
 * Classifies the dominant narrative atmosphere of a roleplay chat across Turkish story turns.
 */
object StoryMoodAnalytics {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    private val actionKeywords = listOf("saldır", "silah", "savaş", "patlama", "tehlike", "alarm", "kaç", "kılıç", "dövüş", "kan")
    private val romanceKeywords = listOf("öp", "kalp", "aşk", "fısılda", "sarıl", "gözlerinin", "tutku", "sevgili", "elini", "dokun")
    private val mysteryKeywords = listOf("gizem", "sır", "ipucu", "gölge", "karanlık", "şifre", "kayıp", "dosya", "ihanet", "gizli")

    fun detectDominantAtmosphere(messages: List<MessageEntity>): String {
        val valid = messages.filter {
            (it.role == "user" || it.role == "assistant") && it.text.isNotBlank()
        }
        if (valid.isEmpty()) return "Henüz belirlenmedi"

        var actionScore = 0
        var romanceScore = 0
        var mysteryScore = 0

        for (m in valid.takeLast(60)) {
            val lower = m.text.lowercase(trLocale)
            actionScore += actionKeywords.count { lower.contains(it) }
            romanceScore += romanceKeywords.count { lower.contains(it) }
            mysteryScore += mysteryKeywords.count { lower.contains(it) }
        }

        val maxScore = maxOf(actionScore, romanceScore, mysteryScore)
        if (maxScore == 0) return "Sakin & Edebi"
        return when (maxScore) {
            actionScore -> "Aksiyon & Gerilim"
            romanceScore -> "Romantik & Duygusal"
            else -> "Gizem & Entrika"
        }
    }
}
