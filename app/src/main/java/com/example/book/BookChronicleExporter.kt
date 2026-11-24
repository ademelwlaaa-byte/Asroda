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
        return buildString {
            append("# Kozmik Sürükleniş — Aiden'ın Kronik Dosyası\n\n")
            append("- **Aktif Bölüm:** Bölüm ").append(state.activeChapter).append("\n")
            append("- **Tamamlanan Bölümler:** ").append(state.completedChapters.size)
                .append(" / ").append(CosmicDriftBookRegistry.TOTAL_PLAYABLE_CHAPTERS).append("\n")
            append("- **Açılan Karakterler (").append(unlocked.size).append("):** ")
                .append(unlocked.joinToString(", ") { it.displayName }).append("\n")
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

            if (state.discoveredSecrets.isNotEmpty()) {
                append("## Keşfedilen Sırlar\n")
                state.discoveredSecrets.forEach { secret ->
                    append("- ").append(secret.trim()).append("\n")
                }
            }
        }.trim()
    }
}
