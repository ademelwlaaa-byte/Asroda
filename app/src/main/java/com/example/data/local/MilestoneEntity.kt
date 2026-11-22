package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "milestones",
    indices = [Index(value = ["botId"])]
)
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val botId: String,
    val type: String,       // Normalized unique key: "trust_25", "affection_60", "story_first_secret", etc.
    val title: String,      // Encodes display title + optional " — <desc>" + optional " · Gün X • <date>"
    val emoji: String,
    val affectionAtTime: Int,
    val trustAtTime: Int,
    val reachedAt: Long = System.currentTimeMillis()
) {
    val isNarrative: Boolean
        get() = type.startsWith("story_", ignoreCase = true)

    val displayTitle: String
        get() = title.substringBefore(" · Gün ").substringBefore(" — ").trim().ifBlank { title }

    val narrativeDescription: String
        get() = title.substringBefore(" · Gün ").let { body ->
            if (" — " in body) body.substringAfter(" — ").trim() else ""
        }

    val storyStamp: String
        get() = if (" · Gün " in title) "Gün " + title.substringAfter(" · Gün ").trim() else ""
}

object MilestoneDefinitions {
    data class MilestoneDef(
        val type: String,
        val threshold: Int,
        val field: String,
        val title: String,
        val emoji: String
    )

    // System #18 Channel 1: Deterministic numeric thresholds (trust: 25/50/75/95, affection: 30/60/85/95)
    val all = listOf(
        MilestoneDef("trust_25",     25, "trust",     "İlk Güven",       "🤝"),
        MilestoneDef("trust_50",     50, "trust",     "Güven Eşiği",     "🛡️"),
        MilestoneDef("trust_75",     75, "trust",     "Sarsılmaz Güven", "⚔️"),
        MilestoneDef("trust_95",     95, "trust",     "Mutlak Güven",    "👑"),
        MilestoneDef("affection_30", 30, "affection", "İlk Kıvılcım",    "💫"),
        MilestoneDef("affection_60", 60, "affection", "Derin Bağ",       "💜"),
        MilestoneDef("affection_85", 85, "affection", "Kalp Mührü",      "❤️"),
        MilestoneDef("affection_95", 95, "affection", "Sonsuz Bağ",      "💖")
    )

    fun normalizeMilestoneKey(rawKey: String, narrativePrefix: Boolean = true): String {
        val cleaned = rawKey
            .trim()
            .lowercase(java.util.Locale.ROOT)
            .replace('ç', 'c').replace('ğ', 'g').replace('ı', 'i')
            .replace('ö', 'o').replace('ş', 's').replace('ü', 'u')
            .replace(Regex("[^a-z0-9_]+"), "_")
            .trim('_')
            .take(48)
        if (cleaned.isBlank()) return ""
        return if (narrativePrefix && !cleaned.startsWith("story_")) "story_$cleaned" else cleaned
    }

    fun formatStoryStamp(storyDayCounter: Long, storyCalendarDate: String): String {
        val day = storyDayCounter.coerceAtLeast(1L)
        val cal = storyCalendarDate.trim()
        return if (cal.isNotBlank()) "Gün $day • $cal" else "Gün $day"
    }

    fun buildStampedTitle(
        baseTitle: String,
        description: String = "",
        storyDayCounter: Long = 1L,
        storyCalendarDate: String = ""
    ): String {
        val cleanTitle = baseTitle.trim().take(72)
        val cleanDesc = description.trim().take(180)
        val stamp = formatStoryStamp(storyDayCounter, storyCalendarDate)
        return buildString {
            append(cleanTitle)
            if (cleanDesc.isNotBlank()) {
                append(" — ")
                append(cleanDesc)
            }
            append(" · ")
            append(stamp)
        }
    }
}
