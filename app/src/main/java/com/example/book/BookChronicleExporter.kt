package com.example.book

/**
 * Formats the player's Book Mode Chronicle (active chapter, completed chapters, key decisions,
 * discovered secrets, and unlocked characters) as a shareable Markdown dossier.
 */
object BookChronicleExporter {

    fun formatDossierMarkdown(state: BookReadState): String {
        val unlocked = BookStateRepository.unlockedCharacters(state)
        val allDecisions = state.chapterDecisions.entries
            .sortedBy { it.key }
            .flatMap { it.value }
        val totalPlayable = CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS
        val completedCount = state.completedChapters.count { it in 1..totalPlayable }
        return buildString {
            append("# Kozmik Sürükleniş — Aiden'ın Kronik Dosyası\n\n")
            append("- **Aktif Bölüm:** Bölüm ").append(state.activeChapter.coerceIn(1, totalPlayable)).append("\n")
            append("- **Tamamlanan Bölümler:** ").append(completedCount)
                .append(" / ").append(totalPlayable).append("\n")
            append("- **Açılan Karakterler (").append(unlocked.size).append("):** ")
                .append(if (unlocked.isEmpty()) "Henüz yok" else unlocked.joinToString(", ") { it.displayName }).append("\n")
            val bondHighlight = BookBondSummaryHelper.compute(state)
            append("- **İlişki Özeti:** ").append(BookBondSummaryHelper.formatCompact(bondHighlight)).append("\n")
            append("- **Keşfedilen Sırlar:** ").append(state.discoveredSecrets.size).append("\n\n")

            if (allDecisions.isNotEmpty()) {
                append("## Kritik Kararlar\n")
                allDecisions.forEach { d ->
                    append("- **Bölüm ").append(d.chapterNumber).append(":** ")
                        .append(d.selectedOption.trim())
                    if (d.impactText.isNotBlank()) {
                        append(" — _").append(d.impactText.trim()).append("_")
                    }
                    append("\n")
                }
                append("\n")
            }

            val sortedSecrets = BookSecretProgressHelper.sortedSecrets(state.discoveredSecrets)
            if (sortedSecrets.isNotEmpty()) {
                append("## Keşfedilen Sırlar\n")
                sortedSecrets.forEach { secret ->
                    append("- ").append(secret).append("\n")
                }
            }
        }.trim()
    }
}
