package com.example.book

/**
 * Formats a player's Book Mode decisions into a compact chronological Turkish timeline.
 */
object StoryChoiceHistoryFormatter {

    fun formatTimeline(state: BookReadState, maxEntries: Int = 10): String {
        val decisions = state.chapterDecisions.entries
            .sortedBy { it.key }
            .flatMap { it.value }
            .filter { it.selectedOption.isNotBlank() }
        if (decisions.isEmpty()) return "Henüz kaydedilmiş bir bölüm kararı yok."
        val safeMax = maxEntries.coerceIn(1, 50)
        return decisions.takeLast(safeMax).joinToString("\n") { d ->
            val chapterNum = d.chapterNumber.coerceAtLeast(1)
            val impact = if (d.impactText.isNotBlank()) " → ${d.impactText.trim()}" else ""
            "• B$chapterNum: ${d.selectedOption.trim()}$impact"
        }
    }
}
