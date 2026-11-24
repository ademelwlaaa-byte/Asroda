package com.example.util

import com.example.data.local.BotEntity

/**
 * Formats a compact Turkish synopsis card for any character or universe bot.
 */
object StoryCharacterSynopsisHelper {

    fun buildSynopsis(bot: BotEntity): String {
        val pitch = StoryCharacterIntroCardHelper.buildPitch(bot)
        val genres = StoryGenreTagClassifier.inferTags(bot).joinToString(" • ")
        val completeness = BotCardCompletenessHelper.evaluate(bot)
        return "$pitch\nTürler: $genres | ${completeness.label} (%${completeness.percent})"
    }
}
