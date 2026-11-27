package com.example.util

import java.util.Locale

/**
 * Generates context-aware Turkish quick-reply / roleplay action chips based on the
 * assistant's latest narrative message so players never face writer's block.
 */
object QuickReplyChipsHelper {

    private val trLocale = Locale.forLanguageTag("tr-TR")
    private val kissWordRegex = Regex("""(?<!\p{L})öp(?:üyor|tü|er|üş|mek|sün|eyim|ücük)?""")

    fun suggestStarters(lastAssistantText: String): List<String> {
        val visible = com.example.data.repository.EmochiRepository.cleanEmotionTags(lastAssistantText).trim()
        val lower = visible.lowercase(trLocale)
        if (lower.isBlank()) {
            return listOf(
                "*Etrafı dikkatle süzer ve ilk adımı atar.*",
                "\"Bana neler olduğunu baştan anlat.\"",
                "*Sessizce dinleyip tepkisini ölçer.*"
            )
        }
        return when {
            lower.contains("?") -> listOf(
                "\"Gerçeği duymaya hazır mısın?\"",
                "*Gözlerinin içine bakıp kısa bir sessizlikten sonra yanıtlar.*",
                "\"Bunu neden şimdi soruyorsun?\""
            )
            lower.contains("saldır") || lower.contains("silah") || lower.contains("tehlike") || lower.contains("alarm") -> listOf(
                "*Anında siper alıp çevreyi tarar.*",
                "\"Arkamı kolla, ben önden gidiyorum!\"",
                "*Soğukkanlılığını koruyup karşı hamle yapar.*"
            )
            lower.contains("fısılda") || kissWordRegex.containsMatchIn(lower) || lower.contains("kalp") || lower.contains("elini") -> listOf(
                "*Bir adım daha yaklaşıp yumuşakça gülümser.*",
                "\"Burada seninle güvendeyim.\"",
                "*Sessizce elini tutup gözlerine bakar.*"
            )
            else -> listOf(
                "*Kaşlarını çatıp daha yakından inceler.*",
                "\"Devam et, tüm detayları bilmek istiyorum.\"",
                "*Derin bir nefes alıp beklenmedik bir hamle yapar.*"
            )
        }
    }
}
