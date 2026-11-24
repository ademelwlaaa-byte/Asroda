package com.example.book

/**
 * Formats a player's Book Mode decisions into a compact chronological Turkish timeline.
 */
object StoryChoiceHistoryFormatter {

    fun formatTimeline(state: BookReadState, maxEntries: Int = 10): String {
        val decisions = state.chapterDecisions.entries
            .sortedBy { it.key }
            .flatMap { it.value }
        if (decisions.isEmpty()) return "Henüz kaydedilmiş bir bölüm kararı yok."
        return decisions.takeLast(maxEntries.coerceIn(1, 50)).joinToString("\n") { d ->
            val impact = if (d.impactText.isNotBlank()) " → ${d.impactText.trim()}" else ""
            "• B${d.chapterNumber}: ${d.selectedOption.trim()}$impact"
        }
    }
}
