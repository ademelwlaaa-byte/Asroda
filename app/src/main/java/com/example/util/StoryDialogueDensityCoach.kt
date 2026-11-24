package com.example.util

/**
 * Produces a single concise Turkish coaching recommendation for the Story Stats dialog
 * based on assistant reply length, dialogue proportion, and total turns.
 */
object StoryDialogueDensityCoach {

    fun generateTip(
        assistantMessages: Int,
        assistantWordCount: Int,
        dialoguePercent: Int
    ): String {
        if (assistantMessages < 2) {
            return "🌱 Hikaye yeni başlıyor — birkaç tur sonra kişiselleştirilmiş öneriler görünecek."
        }
        val avgAssistantWords = assistantWordCount / assistantMessages.coerceAtLeast(1)
        return when {
            avgAssistantWords < 45 ->
                "💡 Yanıtlar kısa kalıyor — Hikaye Yönetmeni'nden 'Uzun' veya 'Roman' modunu deneyebilirsin."
            avgAssistantWords > 380 ->
                "💡 Sahne blokları çok uzun — daha hızlı tempo için 'Dengeli' yanıt uzunluğunu seçebilirsin."
            dialoguePercent < 12 ->
                "💡 Diyalog oranı düşük — doğrudan alıntılı repliklerle karakter etkileşimini artırabilirsin."
            dialoguePercent > 75 ->
                "💡 Sahne neredeyse tamamen diyalog — ortam betimlemesi isteyerek atmosferi derinleştirebilirsin."
            else ->
                "✨ Anlatım uzunluğu ve diyalog dengesi ideal roman temposunda."
        }
    }
}
