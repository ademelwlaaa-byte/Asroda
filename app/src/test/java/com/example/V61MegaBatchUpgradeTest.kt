package com.example

import com.example.book.BookProgressSummaryFormatter
import com.example.book.BookReadState
import com.example.book.RecordedDecision
import com.example.data.local.MessageEntity
import com.example.data.managers.ProviderHealthState
import com.example.util.ChatSearchHelper
import com.example.util.ProviderHealthSummaryFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V61MegaBatchUpgradeTest {

    @Test
    fun chatSearchHelper_matchesTurkishDiacriticsTolerantly() {
        val msgs = listOf(
            MessageEntity(id = "m1", botId = "b1", role = "assistant", text = "Soğuk rüzgârda ışık parladı ve özgürlük yankılandı.", timestamp = 1L),
            MessageEntity(id = "m2", botId = "b1", role = "user", text = "Çiçekleri masaya bıraktım.", timestamp = 2L)
        )
        val r1 = ChatSearchHelper.findMatches(msgs, "isik")
        assertEquals(1, r1.size)
        assertEquals("m1", r1.first().messageId)

        val r2 = ChatSearchHelper.findMatches(msgs, "soguk")
        assertEquals(1, r2.size)

        val r3 = ChatSearchHelper.findMatches(msgs, "cicek")
        assertEquals(1, r3.size)
        assertEquals("m2", r3.first().messageId)
    }

    @Test
    fun providerHealthSummaryFormatter_countsHealthyCooldownAndFastest() {
        val now = 1_000_000L
        val states = mapOf(
            "gemini" to ProviderHealthState(providerKey = "gemini", cooldownUntilMs = 0L, lastLatencyMs = 420L),
            "groq" to ProviderHealthState(providerKey = "groq", cooldownUntilMs = 0L, lastLatencyMs = 180L),
            "openrouter" to ProviderHealthState(providerKey = "openrouter", cooldownUntilMs = now + 60_000L, lastLatencyMs = 95L)
        )
        val summary = ProviderHealthSummaryFormatter.summarize(states, nowMs = now)
        assertEquals(3, summary.totalTracked)
        assertEquals(2, summary.healthyCount)
        assertEquals(1, summary.cooldownCount)
        assertEquals("groq", summary.fastestProviderKey)
        assertEquals(180L, summary.fastestLatencyMs)

        val banner = ProviderHealthSummaryFormatter.formatBanner(summary)
        assertTrue(banner.contains("Sağlıklı: 2"))
        assertTrue(banner.contains("Beklemede: 1"))
        assertTrue(banner.contains("groq (180 ms)"))
    }

    @Test
    fun bookProgressSummaryFormatter_computesCompletionAndRibbon() {
        val state = BookReadState(
            bookId = "cosmic_drift",
            activeChapter = 3,
            completedChapters = setOf(1, 2),
            currentNodeId = "ch3_n1",
            transcript = emptyList(),
            chapterDecisions = mapOf(
                1 to listOf(
                    RecordedDecision(
                        chapterNumber = 1,
                        nodeId = "ch1_d1",
                        decisionTitle = "İlk Karar",
                        choiceId = "c1",
                        selectedOption = "Kalkanı aç",
                        impactText = "Natasha etkilendi",
                        bondDeltas = emptyMap(),
                        fatigueDelta = 0,
                        mysteryDelta = 0,
                        secretUnlocked = null,
                        branchTag = null,
                        isBranching = false,
                        isRelationship = true,
                        impactLevel = "HIGH"
                    )
                ),
                2 to listOf(
                    RecordedDecision(
                        chapterNumber = 2,
                        nodeId = "ch2_d1",
                        decisionTitle = "Zaman Kırılması",
                        choiceId = "c2",
                        selectedOption = "Zamanı durdur",
                        impactText = "Sır ortaya çıktı",
                        bondDeltas = emptyMap(),
                        fatigueDelta = 0,
                        mysteryDelta = 0,
                        secretUnlocked = null,
                        branchTag = null,
                        isBranching = true,
                        isRelationship = false,
                        impactLevel = "HIGH"
                    )
                )
            ),
            bonds = emptyMap(),
            fatigue = 0,
            mystery = 0,
            flags = emptyMap(),
            discoveredSecrets = setOf("Mavi Çekirdek", "Yerebatan Şifresi"),
            discoveredBranches = emptySet(),
            isChapterCompleted = false
        )
        val summary = BookProgressSummaryFormatter.compute(state, totalChapters = 10)
        assertEquals(2, summary.completedChapters)
        assertEquals(20, summary.completionPercent)
        assertEquals(2, summary.totalDecisions)
        assertEquals(2, summary.discoveredSecrets)

        val ribbon = BookProgressSummaryFormatter.formatRibbon(summary)
        assertTrue(ribbon.contains("%20"))
        assertTrue(ribbon.contains("2/10 bölüm"))
        assertTrue(ribbon.contains("2 karar"))
        assertTrue(ribbon.contains("2 sır"))
    }
}
