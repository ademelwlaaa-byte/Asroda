package com.example.util

import com.example.data.local.MessageEntity
import java.util.Locale

/**
 * Extracts top recurring meaningful Turkish story themes/keywords from a conversation,
 * filtering out common Turkish stop words.
 */
object CharacterWordCloudHelper {

    private val trLocale = Locale.forLanguageTag("tr-TR")
    private val wordRegex = Regex("[\\p{L}]{4,}")

    private val turkishStopWords = setOf(
        "ama", "ancak", "artık", "aslında", "ayrıca", "bana", "bazı", "belki", "beni", "benim",
        "beri", "bile", "biraz", "biri", "birkaç", "birlikte", "bize", "bizim", "böyle", "bunu",
        "bunun", "burada", "bütün", "çünkü", "daha", "dahi", "dedi", "değil", "demek", "diğer",
        "diye", "doğru", "dolayı", "eder", "eğer", "etmek", "fakat", "fazla", "falan", "gibi",
        "göre", "hala", "hâlâ", "hangi", "hatta", "hemen", "hem", "hepsi", "herkes", "hiçbir",
        "için", "içinde", "ilgili", "ise", "işte", "kadar", "karşı", "kendi", "kendini", "kendisi",
        "kime", "kimi", "kimse", "lakin", "nasıl", "neden", "neler", "nerede", "nereye", "niçin",
        "niye", "olan", "olarak", "oldu", "olduğu", "olmak", "olsa", "olsun", "olur", "onlar",
        "onları", "onun", "orada", "oysa", "öyle", "pek", "rağmen", "sadece", "sana", "sanki",
        "seni", "senin", "şey", "şeyler", "şimdi", "şöyle", "şunu", "tabi", "tabii", "tüm",
        "üzere", "vardı", "vardır", "veya", "yahut", "yalnız", "yani", "yapar", "yaptı", "yine",
        "yoksa", "zaten", "zaman"
    )

    fun extractTopThemes(messages: List<MessageEntity>, maxThemes: Int = 5): List<String> {
        val counts = HashMap<String, Int>()
        for (m in messages) {
            if (m.role != "user" && m.role != "assistant") continue
            wordRegex.findAll(m.text).forEach { match ->
                val w = match.value.lowercase(trLocale)
                if (w !in turkishStopWords) {
                    counts[w] = (counts[w] ?: 0) + 1
                }
            }
        }
        return counts.entries
            .filter { it.value >= 2 }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(maxThemes.coerceIn(1, 10))
            .map { "#${it.key}" }
    }
}
