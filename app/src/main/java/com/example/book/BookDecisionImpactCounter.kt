package com.example.book

/**
 * Tallies critical, branching, and relationship-shaping decisions in Book Mode.
 */
object BookDecisionImpactCounter {

    data class ImpactCounts(
        val total: Int,
        val branching: Int,
        val relationship: Int
    )

    fun count(state: BookReadState): ImpactCounts {
        val all = state.chapterDecisions.values.flatten()
        return ImpactCounts(
            total = all.size,
            branching = all.count { it.isBranching },
            relationship = all.count { it.isRelationship }
        )
    }

    fun formatCompact(counts: ImpactCounts): String {
        if (counts.total == 0) return "Henüz kritik karar yok"
        return "${counts.total} karar (${counts.branching} dallanma • ${counts.relationship} ilişki)"
    }
}
