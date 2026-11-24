package com.example.util

/**
 * Evaluates a user-authored bot's opening message & scenario and returns a concise Turkish readiness tip.
 */
object StoryOpeningQualityChecker {

    data class QualityAssessment(
        val scorePercent: Int,
        val badge: String,
        val tip: String
    )

    fun evaluate(openingMessage: String, scenario: String): QualityAssessment {
        val openWords = TextWordCounter.countWords(openingMessage)
        val scenWords = TextWordCounter.countWords(scenario)
        val hasDialogue = openingMessage.contains('"') || openingMessage.contains('“') || openingMessage.contains('«')
        val hasAction = openingMessage.contains('*')

        var score = 20
        if (openWords >= 12) score += 25 else if (openWords >= 5) score += 12
        if (scenWords >= 10) score += 25 else if (scenWords >= 4) score += 12
        if (hasDialogue) score += 15
        if (hasAction) score += 15
        score = score.coerceIn(0, 100)

        return when {
            score >= 85 -> QualityAssessment(score, "✦ Roman Kalitesi", "Harika! Diyalog, aksiyon ve sahne dengesi çok güçlü.")
            score >= 60 -> QualityAssessment(score, "✓ İyi Başlangıç", "İpucu: Açılışa kısa bir *eylem* veya \"diyalog\" eklemek tonu daha da güçlendirir.")
            else -> QualityAssessment(score, "🌱 Temel Taslak", "İpucu: Karakterin bulunduğu ortamı ve ilk tepkisini 1-2 cümleyle detaylandır.")
        }
    }
}
