package com.example.util

import com.example.data.local.MessageEntity

/**
 * Formats a concise single-line Turkish story header for shares, exports, and summary cards.
 */
object StoryTranscriptHeaderHelper {

    fun formatHeaderLine(
        title: String,
        messages: List<MessageEntity>
    ): String {
        val stats = ChatStoryStats.compute(messages)
        val mood = StoryMoodAnalytics.detectDominantAtmosphere(messages)
        val cleanTitle = title.ifBlank { "Asroda Hikayesi" }
        return "$cleanTitle • ${stats.totalMessages} mesaj • ${stats.totalWordCount} kelime • ~${stats.estimatedNovelPages} sayfa • $mood"
    }
}
