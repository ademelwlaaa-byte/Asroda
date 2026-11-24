package com.example.util

import com.example.data.local.MessageEntity

/**
 * Computes a 3-act emotional arc (Opening -> Middle -> Current) for long interactive stories.
 */
object StoryEmotionalArcHelper {

    fun formatThreeActArc(messages: List<MessageEntity>): String {
        val story = messages.filter { (it.role == "user" || it.role == "assistant") && it.text.isNotBlank() }
        if (story.size < 3) return ""
        val chunk = (story.size / 3).coerceAtLeast(1)
        val act1 = StoryMoodAnalytics.detectDominantAtmosphere(story.subList(0, chunk))
        val act2 = StoryMoodAnalytics.detectDominantAtmosphere(story.subList(chunk, (chunk * 2).coerceAtMost(story.size)))
        val act3 = StoryMoodAnalytics.detectDominantAtmosphere(story.subList((chunk * 2).coerceAtMost(story.size - 1), story.size))
        return "$act1 → $act2 → $act3"
    }
}
