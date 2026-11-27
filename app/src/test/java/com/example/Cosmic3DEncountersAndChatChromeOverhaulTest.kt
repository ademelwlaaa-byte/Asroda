package com.example

import com.example.ui.components.emotionHistoryEmoji
import com.example.ui.components.formatTurkishEmotionLabel
import com.example.ui.components.relationshipTierLabel
import com.example.ui.components.sanitizeCustomEmotionRange
import com.example.ui.screens.cosmic3d.CosmicBossDuelCalculator
import com.example.ui.screens.cosmic3d.CosmicDuelBossId
import com.example.ui.screens.cosmic3d.StoryMiniGameId
import com.example.ui.screens.cosmic3d.StoryMiniGameScorer
import com.example.ui.screens.formatSearchMatchCounter
import com.example.ui.screens.resolveBotCardDescription
import com.example.ui.screens.resolveBotCardDisplayName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class Cosmic3DEncountersAndChatChromeOverhaulTest {

    // -------------------------------------------------------------------------
    // 1. BookStoryMiniGames pure logic & Turkish-locale tag safety
    // -------------------------------------------------------------------------

    @Test
    fun storyMiniGameIdAndScorer_handleTurkishLocaleAndBounds() {
        val prevLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            // Under tr-TR locale, naive .uppercase() turns 'i' into 'İ', breaking tag matching!
            assertEquals(StoryMiniGameId.RED_EYE_WILLPOWER, StoryMiniGameId.fromTag("willpower"))
            assertEquals(StoryMiniGameId.BOSPHORUS_CORE_BALANCE, StoryMiniGameId.fromTag("bosphorus_core"))
            assertEquals(StoryMiniGameId.WHITE_EYE_RESONANCE, StoryMiniGameId.fromTag("white_eye_resonance"))
            assertEquals(StoryMiniGameId.WHITE_EYE_RESONANCE, StoryMiniGameId.fromTag("unknown_tag"))
        } finally {
            Locale.setDefault(prevLocale)
        }

        assertEquals("S", StoryMiniGameScorer.computeRank(900, 1000))
        assertEquals("A", StoryMiniGameScorer.computeRank(600, 1000))
        assertEquals("B", StoryMiniGameScorer.computeRank(400, 1000))
        assertEquals("B", StoryMiniGameScorer.computeRank(-50, 1000))
        assertEquals("B", StoryMiniGameScorer.computeRank(500, 0))

        assertEquals(0.5f, StoryMiniGameScorer.computeTimerFraction(4000, 8000), 0.0001f)
        assertEquals(0f, StoryMiniGameScorer.computeTimerFraction(-200, 8000), 0.0001f)
        assertEquals(1f, StoryMiniGameScorer.computeTimerFraction(12000, 8000), 0.0001f)
        assertEquals(0f, StoryMiniGameScorer.computeTimerFraction(4000, 0), 0.0001f)

        assertEquals(64, StoryMiniGameScorer.computeWillpowerAfterChoice(40, 24))
        assertEquals(100, StoryMiniGameScorer.computeWillpowerAfterChoice(90, 24))
        assertEquals(0, StoryMiniGameScorer.computeWillpowerAfterChoice(10, -50))
    }

    // -------------------------------------------------------------------------
    // 2. CosmicRooftopBossGame tag parsing & duel math
    // -------------------------------------------------------------------------

    @Test
    fun cosmicBossDuel_handlesTurkishLocaleAndShieldRankMath() {
        val prevLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals(CosmicDuelBossId.MIRROR_ZERO, CosmicDuelBossId.fromTag("mirror_zero"))
            assertEquals(CosmicDuelBossId.ULTRON_PRIME, CosmicDuelBossId.fromTag("ultron_prime"))
            assertEquals(CosmicDuelBossId.VAELOR_ROOFTOP, CosmicDuelBossId.fromTag("vaelor_rooftop"))
        } finally {
            Locale.setDefault(prevLocale)
        }

        // 2 required orders -> stepDrop = 35 -> 100 -> 65 -> 30 -> clamped at 25
        val s1 = CosmicBossDuelCalculator.computeShieldAfterOrder(100, 2)
        val s2 = CosmicBossDuelCalculator.computeShieldAfterOrder(s1, 2)
        val s3 = CosmicBossDuelCalculator.computeShieldAfterOrder(s2, 2)
        assertEquals(65, s1)
        assertEquals(30, s2)
        assertEquals(25, s3)

        // Division-by-zero protection
        assertEquals(30, CosmicBossDuelCalculator.computeShieldAfterOrder(100, 0))

        assertEquals("S", CosmicBossDuelCalculator.computeDuelRank(1500))
        assertEquals("A", CosmicBossDuelCalculator.computeDuelRank(1050))
        assertEquals("B", CosmicBossDuelCalculator.computeDuelRank(800))

        assertEquals(0f, CosmicBossDuelCalculator.shieldProgressFraction(-10), 0.0001f)
        assertEquals(1f, CosmicBossDuelCalculator.shieldProgressFraction(150), 0.0001f)
        assertEquals(0.45f, CosmicBossDuelCalculator.shieldProgressFraction(45), 0.0001f)
    }

    // -------------------------------------------------------------------------
    // 3. CharacterEmotionControlModal range guard & Turkish formatting
    // -------------------------------------------------------------------------

    @Test
    fun emotionControlModal_preventsInvertedRangeCrashAndFormatsTurkishLabels() {
        // Inverted min > max must NOT throw IllegalArgumentException!
        val (min1, max1, cur1) = sanitizeCustomEmotionRange(rawMin = "80", rawMax = "50", rawCurrent = "40")
        assertEquals(80, min1)
        assertEquals(81, max1)
        assertEquals(80, cur1)

        // Non-numeric strings recover gracefully to defaults
        val (min2, max2, cur2) = sanitizeCustomEmotionRange("", "abc", "250")
        assertEquals(0, min2)
        assertEquals(100, max2)
        assertEquals(100, cur2)

        assertEquals("Düşman", relationshipTierLabel(-5))
        assertEquals("Arkadaş", relationshipTierLabel(62))
        assertEquals("Sevgili", relationshipTierLabel(90))
        assertEquals("Ruh Eşi", relationshipTierLabel(120))

        assertEquals("💕", emotionHistoryEmoji("FLÖRTÖZ"))
        assertEquals("😄", emotionHistoryEmoji("NEŞELİ"))
        assertEquals("😡", emotionHistoryEmoji("ÖFKELİ"))
        assertEquals("😢", emotionHistoryEmoji("KIRGIN"))
        assertEquals("😌", emotionHistoryEmoji("SAKİN"))
        assertEquals("✦", emotionHistoryEmoji("gizemli"))

        assertEquals("İstekli", formatTurkishEmotionLabel("istekli"))
        assertEquals("Nötr", formatTurkishEmotionLabel("   "))
    }

    // -------------------------------------------------------------------------
    // 4. BotCardItem & ChatTopBar pure helpers
    // -------------------------------------------------------------------------

    @Test
    fun botCardAndTopBarHelpers_sanitizeAndFormatEdgeCases() {
        assertEquals("Aetheria", resolveBotCardDisplayName("starter_aetheria", "universe", "Aetheria: Neon Kuleler", "Bot"))
        assertEquals("Evren", resolveBotCardDisplayName("u_1", "universe", "   ", "  "))
        assertEquals("Karakter", resolveBotCardDisplayName("c_1", "character", "", "   "))

        val dirtyScenario = "<think>gizli</think>Sisli limanda eski bir dost seni bekliyor. [[STATE_JSON]]{\"x\":1}[[/STATE_JSON]]"
        val cleanDesc = resolveBotCardDescription("custom_1", dirtyScenario, "fallback")
        assertEquals("Sisli limanda eski bir dost seni bekliyor.", cleanDesc)
        assertFalse(cleanDesc.contains("think"))
        assertFalse(cleanDesc.contains("STATE_JSON"))

        assertTrue(resolveBotCardDescription("starter_ayla", "", "").contains("dostun"))

        assertEquals("Eşleşme bulunamadı", formatSearchMatchCounter(0, 0))
        assertEquals("1 / 5 eşleşme", formatSearchMatchCounter(-3, 5))
        assertEquals("3 / 5 eşleşme", formatSearchMatchCounter(2, 5))
        assertEquals("5 / 5 eşleşme", formatSearchMatchCounter(99, 5))
    }
}
