package com.example.util

import com.example.data.local.MessageEntity
import java.util.Locale

/**
 * Classifies the dominant narrative atmosphere of a roleplay chat across Turkish story turns.
 */
object StoryMoodAnalytics {

    private val trLocale = Locale.forLanguageTag("tr-TR")

    private val actionKeywords = listOf("saldır", "silah", "savaş", "patlama", "tehlike", "alarm", "kılıç", "dövüş")
    private val actionWordPrefixRegex = Regex("""(?<!\p{L})(?:kaç(?:ıyor|tı|ar|ın|mak|ış|sın)?|kan(?:lı|lar|ım|ın|ı|a|dan)?)(?!\p{L})""")
    private val romanceKeywords = listOf("kalp", "fısılda", "sarıl", "gözlerinin", "tutku", "sevgili", "elini", "dokun")
    private val romanceWordPrefixRegex = Regex("""(?<!\p{L})(?:öp(?:üyor|tü|er|üş|mek|sün|eyim|ücük)?|aşk(?:ım|ın|ı|la|ta|tan|ımız|lar|a|ımsın)?)(?!\p{L})""")
    private val mysteryKeywords = listOf("gizem", "ipucu", "gölge", "karanlık", "şifre", "kayıp", "dosya", "ihanet", "gizli")
    private val mysteryWordPrefixRegex = Regex("""(?<!\p{L})sır(?:lar|rın|rı|ra|dan)?(?!\p{L})""")

    fun detectDominantAtmosphere(messages: List<MessageEntity>): String {
        val valid = messages.filter {
            (it.role == "user" || it.role == "assistant") && it.text.isNotBlank()
        }
        if (valid.isEmpty()) return "Henüz belirlenmedi"

        var actionScore = 0
        var romanceScore = 0
        var mysteryScore = 0

        for (m in valid.takeLast(60)) {
            val visible = com.example.data.repository.EmochiRepository.cleanEmotionTags(m.text)
            if (visible.isBlank()) continue
            val lower = visible.lowercase(trLocale)
            actionScore += actionKeywords.count { lower.contains(it) }
            if (actionWordPrefixRegex.containsMatchIn(lower)) actionScore++
            romanceScore += romanceKeywords.count { lower.contains(it) }
            if (romanceWordPrefixRegex.containsMatchIn(lower)) romanceScore++
            mysteryScore += mysteryKeywords.count { lower.contains(it) }
            if (mysteryWordPrefixRegex.containsMatchIn(lower)) mysteryScore++
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
