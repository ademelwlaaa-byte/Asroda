package com.example

import com.example.book.BookReadState
import com.example.book.RecordedDecision
import com.example.book.StoryChoiceHistoryFormatter
import com.example.util.TtsVoiceLanguageDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V78MegaBatchUpgradeTest {

    @Test
    fun ttsVoiceLanguageDetector_distinguishesTurkishAndEnglishCleanly() {
        assertEquals("tr", TtsVoiceLanguageDetector.detectLanguageCode("Soğuk rüzgâr kuleyi sardı."))
        assertFalse(TtsVoiceLanguageDetector.isLikelyEnglish("Bir zamanlar kadim bir kulede."))
        assertEquals("en", TtsVoiceLanguageDetector.detectLanguageCode("The storm was approaching from the northern tower with cold rain."))
        assertTrue(TtsVoiceLanguageDetector.isLikelyEnglish("Where would they go with this map?"))
    }

    @Test
    fun storyChoiceHistoryFormatter_formatsChronologicalTimeline() {
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 2,
            completedChapters = setOf(1),
            currentNodeId = "ch2_n1",
            transcript = emptyList(),
            chapterDecisions = mapOf(
                1 to listOf(
                    RecordedDecision(
                        chapterNumber = 1,
                        nodeId = "ch1_d1",
                        decisionTitle = "İlk Temas",
                        choiceId = "c1",
                        selectedOption = "Gerçeği paylaş",
                        impactText = "Güven arttı",
                        bondDeltas = emptyMap(),
                        fatigueDelta = 0,
                        mysteryDelta = 0,
                        secretUnlocked = null,
                        branchTag = null,
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "HIGH"
                    )
                )
            ),
            bonds = emptyMap(),
            fatigue = 0,
            mystery = 0,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )
        val timeline = StoryChoiceHistoryFormatter.formatTimeline(state)
        assertTrue(timeline.contains("B1: Gerçeği paylaş → Güven arttı"))
    }
}
