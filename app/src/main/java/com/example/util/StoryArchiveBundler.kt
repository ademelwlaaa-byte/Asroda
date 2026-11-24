package com.example.util

import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity

/**
 * Bundles a character card, chapter summary, and saved bookmarks into a single
 * shareable Turkish Markdown archive.
 */
object StoryArchiveBundler {

    fun buildCompleteArchive(
        bot: BotEntity,
        messages: List<MessageEntity>,
        bookmarks: List<StoryBookmarkStore.Bookmark> = emptyList(),
        nowMs: Long = System.currentTimeMillis()
    ): String {
        val displayName = bot.aiName.ifBlank { bot.universeName.ifBlank { "Asroda" } }
        val userName = bot.userCharName.ifBlank { "Sen" }
        val card = BotProfileExporter.formatCharacterCardMarkdown(bot)
        val recap = ChapterSummaryExporter.formatRecapMarkdown(displayName, userName, messages)
        val bmMd = StoryBookmarkExporter.formatMarkdown(displayName, bookmarks, nowMs)

        return buildString {
            append(card)
            if (recap.isNotBlank()) {
                append("\n\n---\n\n")
                append(recap)
            }
            if (bmMd.isNotBlank()) {
                append("\n\n---\n\n")
                append(bmMd)
            }
        }.trim()
    }
}
