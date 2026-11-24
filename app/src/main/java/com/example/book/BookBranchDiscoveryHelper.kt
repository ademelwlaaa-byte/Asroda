package com.example.book

/**
 * Summarizes discovered story branches and world-state flags for the Book Chronicle ribbon.
 */
object BookBranchDiscoveryHelper {

    data class BranchSummary(
        val branchCount: Int,
        val flagCount: Int,
        val explorerBadge: String
    )

    fun compute(state: BookReadState): BranchSummary {
        val branches = state.discoveredBranches.size
        val flags = state.flags.size
        val badge = when {
            branches >= 6 -> "🌟 Çoklu Rota Ustası"
            branches >= 3 -> "🧭 Rota Kaşifi"
            branches >= 1 -> "🌿 İlk Dallanma"
            else -> "🗺️ Ana Rota"
        }
        return BranchSummary(branches, flags, badge)
    }

    fun formatCompact(summary: BranchSummary): String {
        return "${summary.explorerBadge} (${summary.branchCount} rota • ${summary.flagCount} iz)"
    }
}
