package com.example.util

/**
 * Curated 1-tap Turkish Director Rule tone presets for [ChatStoryDirectorDialog].
 */
object StoryTonePresets {

    data class TonePreset(
        val id: String,
        val label: String,
        val directive: String
    )

    val presets: List<TonePreset> = listOf(
        TonePreset(
            id = "cinematic",
            label = "🎬 Sinematik",
            directive = "Sinematik, atmosferik ve görsel betimlemeler kullan; mekanı, ışığı ve sesleri canlı hissettir. Kullanıcının yerine asla konuşma."
        ),
        TonePreset(
            id = "romantic",
            label = "💫 Romantik",
            directive = "Duygusal derinliği yüksek, zarif, samimi ve yavaş ilerleyen romantik bir tempo kur; küçük jestlere ve bakışlara odaklan."
        ),
        TonePreset(
            id = "mystery",
            label = "🕯 Gizemli",
            directive = "Gerilimi ve merakı diri tut; ipuçlarını yavaşça açığa çıkar, karanlık ve gizemli bir roman dili kullan."
        ),
        TonePreset(
            id = "action",
            label = "⚡ Aksiyon",
            directive = "Dinamik, tempolu ve keskin cümleler kullan; çatışmayı ve anlık kararları yüksek enerjiyle aktar."
        )
    )

    fun findById(id: String): TonePreset? = presets.firstOrNull { it.id.equals(id.trim(), ignoreCase = true) }
}
