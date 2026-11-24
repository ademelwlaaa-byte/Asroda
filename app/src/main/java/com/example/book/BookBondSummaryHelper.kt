package com.example.book

/**
 * Computes strongest ally & overall bond summary from [BookReadState] for the Chronicle modal.
 */
object BookBondSummaryHelper {

    data class BondHighlight(
        val strongestAlly: BookCharacterId?,
        val strongestScore: Int,
        val unlockedCount: Int
    )

    fun compute(state: BookReadState): BondHighlight {
        val unlocked = BookStateRepository.unlockedCharacters(state)
        if (unlocked.isEmpty()) {
            return BondHighlight(null, 0, 0)
        }
        val top = unlocked.maxByOrNull { state.bondScore(it) }
        val topScore = if (top != null) state.bondScore(top) else 0
        return BondHighlight(
            strongestAlly = top,
            strongestScore = topScore,
            unlockedCount = unlocked.size
        )
    }

    fun formatCompact(highlight: BondHighlight): String {
        val ally = highlight.strongestAlly ?: return "Henüz karakter bağı oluşmadı"
        val sign = if (highlight.strongestScore > 0) "+${highlight.strongestScore}" else "${highlight.strongestScore}"
        val tier = BookBondTierClassifier.classify(highlight.strongestScore)
        return "En Güçlü Bağ: ${ally.displayName} ($sign • $tier) • ${highlight.unlockedCount} karakter açık"
    }
}
