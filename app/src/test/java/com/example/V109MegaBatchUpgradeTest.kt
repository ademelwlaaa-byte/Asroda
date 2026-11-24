package com.example

import com.example.book.BookDecisionImpactCounter
import com.example.book.BookReadState
import com.example.book.RecordedDecision
import com.example.util.StoryWordMilestoneHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V109MegaBatchUpgradeTest {

    @Test
    fun storyWordMilestoneHelper_tracksNextTargetAndLegendTier() {
        assertTrue(StoryWordMilestoneHelper.formatNextMilestone(400).contains("600 kelime kaldı"))
        assertTrue(StoryWordMilestoneHelper.formatNextMilestone(3200).contains("5K"))
        assertTrue(StoryWordMilestoneHelper.formatNextMilestone(120_000).contains("100.000+"))
    }

    @Test
    fun bookDecisionImpactCounter_talliesBranchingAndRelationshipChoices() {
        val d1 = RecordedDecision(
            chapterNumber = 1,
            nodeId = "n1",
            decisionTitle = "T1",
            choiceId = "c1",
            selectedOption = "A",
            impactText = "E1",
            bondDeltas = emptyMap(),
            fatigueDelta = 0,
            mysteryDelta = 0,
            secretUnlocked = null,
            branchTag = null,
            isBranching = true,
            isRelationship = false,
            impactLevel = "HIGH"
        )
        val d2 = RecordedDecision(
            chapterNumber = 2,
            nodeId = "n2",
            decisionTitle = "T2",
            choiceId = "c2",
            selectedOption = "B",
            impactText = "E2",
            bondDeltas = emptyMap(),
            fatigueDelta = 0,
            mysteryDelta = 0,
            secretUnlocked = null,
            branchTag = null,
            isBranching = false,
            isRelationship = true,
            impactLevel = "HIGH"
        )
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 3,
            completedChapters = setOf(1, 2),
            currentNodeId = "ch3_n1",
            transcript = emptyList(),
            chapterDecisions = mapOf(1 to listOf(d1), 2 to listOf(d2)),
            bonds = emptyMap(),
            fatigue = 0,
            mystery = 0,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )
        val counts = BookDecisionImpactCounter.count(state)
        assertEquals(2, counts.total)
        assertEquals(1, counts.branching)
        assertEquals(1, counts.relationship)
        assertEquals("2 karar (1 dallanma • 1 ilişki)", BookDecisionImpactCounter.formatCompact(counts))
    }
}
