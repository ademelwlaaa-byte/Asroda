package com.example.util

import com.example.data.local.MessageEntity

/**
 * Exports a compact, shareable Turkish chapter recap for the active interactive story.
 */
object ChapterSummaryExporter {

    fun formatRecapMarkdown(
        title: String,
        userName: String,
        messages: List<MessageEntity>,
        recentTurnCount: Int = 4
    ): String {
        val storyMsgs = messages.filter { it.role == "user" || it.role == "assistant" }
        val stats = ChatStoryStats.compute(storyMsgs)
        val atmosphere = StoryMoodAnalytics.detectDominantAtmosphere(storyMsgs)
        val pacing = StoryPacingAnalyzer.analyze(storyMsgs)
        val prose = StoryReadingLevelEstimator.estimate(storyMsgs)
        val themes = CharacterWordCloudHelper.extractTopThemes(storyMsgs, maxThemes = 5)
        val bestQuote = StoryQuoteExtractor.extractBestQuote(storyMsgs, title)

        return buildString {
            append("# ").append(title.ifBlank { "Asroda Hikayesi" }).append(" — Bölüm Özeti\n\n")
            append("- **Seviye:** ").append(StoryDepthTierClassifier.formatLabel(stats.totalWordCount)).append("\n")
            append("- **Atmosfer:** ").append(atmosphere).append("\n")
            append("- **Anlatım Dengesi:** ").append(StoryPacingAnalyzer.formatCompact(pacing)).append("\n")
            append("- **Edebi Üslup:** ").append(StoryReadingLevelEstimator.formatCompact(prose)).append("\n")
            if (themes.isNotEmpty()) {
                append("- **Temalar:** ").append(themes.joinToString(" ")).append("\n")
            }
            val arc = StoryEmotionalArcHelper.formatThreeActArc(storyMsgs)
            if (arc.isNotBlank()) {
                append("- **Duygu Akışı:** ").append(arc).append("\n")
            }
            append("- **Hacim:** ").append(stats.totalMessages).append(" mesaj • ")
                .append(stats.totalWordCount).append(" kelime (~")
                .append(stats.estimatedNovelPages).append(" sayfa)\n")
            if (bestQuote != null) {
                append("\n> ").append(bestQuote).append("\n")
            }
            val recent = storyMsgs.takeLast(recentTurnCount.coerceIn(1, 8))
            if (recent.isNotEmpty()) {
                append("\n## Son Sahneler\n")
                recent.forEach { msg ->
                    val speaker = if (msg.role == "user") userName.ifBlank { "Sen" } else title.ifBlank { "Karakter" }
                    val clean = msg.text.replace(Regex("\\s+"), " ").trim().take(220)
                    append("- **").append(speaker).append(":** ").append(clean)
                    if (msg.text.trim().length > 220) append("…")
                    append("\n")
                }
            }
        }.trim()
    }
}
