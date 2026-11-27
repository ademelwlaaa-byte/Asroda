package com.example.book

/**
 * Computes strongest ally & overall bond summary from [BookReadState] for the Chronicle modal.
 */
object BookBondSummaryHelper {

    data class BondHighlight(
        val strongestAlly: BookCharacterId?,
        val strongestScore: Int,
        val unlockedCount: Int,
        val averageScore: Int = 0
    )

    fun compute(state: BookReadState): BondHighlight {
        val unlocked = BookStateRepository.unlockedCharacters(state)
        if (unlocked.isEmpty()) {
            return BondHighlight(null, 0, 0, 0)
        }
        // Deterministic tie-breaking by story introduction order (lower ordinal wins ties)
        val top = unlocked.maxWithOrNull(
            compareBy<BookCharacterId> { state.bondScore(it) }.thenByDescending { it.ordinal }
        )
        val topScore = if (top != null) state.bondScore(top) else 0
        val avgScore = (unlocked.sumOf { state.bondScore(it) } / unlocked.size).coerceIn(0, 100)
        return BondHighlight(
            strongestAlly = top,
            strongestScore = topScore,
            unlockedCount = unlocked.size,
            averageScore = avgScore
        )
    }

    fun formatCompact(highlight: BondHighlight): String {
        val ally = highlight.strongestAlly ?: return "Henüz karakter bağı oluşmadı"
        val sign = if (highlight.strongestScore > 0) "+${highlight.strongestScore}" else "${highlight.strongestScore}"
        val tier = BookBondTierClassifier.classify(highlight.strongestScore)
        val safeUnlocked = highlight.unlockedCount.coerceAtLeast(1)
        return "En Güçlü Bağ: ${ally.displayName} ($sign • $tier) • $safeUnlocked karakter açık"
    }
}
