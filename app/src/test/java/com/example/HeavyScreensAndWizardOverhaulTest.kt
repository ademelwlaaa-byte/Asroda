package com.example

import com.example.data.local.BotEntity
import com.example.data.repository.KeyCharacter
import com.example.ui.screens.WizardSubmitGuard
import com.example.ui.screens.computeGazeTrackingVector
import com.example.ui.screens.computeWindGustEnvelope
import com.example.ui.screens.deserializeWizardKeyCharacters
import com.example.ui.screens.detectChatMoodFromAssistantText
import com.example.ui.screens.filterExploreTemplates
import com.example.ui.screens.filterGyroTiltSample
import com.example.ui.screens.findExistingBotForTemplatePreset
import com.example.ui.screens.formatAffectionDeltaBadge
import com.example.ui.screens.isValidIsoStoryDate
import com.example.ui.screens.normalizeExploreSearchQuery
import com.example.ui.screens.serializeWizardKeyCharacters
import com.example.ui.screens.shouldShowAidenExploreHeroCard
import com.example.ui.screens.smoothRamp
import com.example.ui.screens.validateWizardStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HeavyScreensAndWizardOverhaulTest {

    private fun sampleBot(
        id: String = "bot-1",
        mode: String = "personal",
        aiName: String = "Aria",
        aiPersonality: String = "Zeki ve soğukkanlı dedektif",
        scenario: String = "İstanbul gecesinde siber gizem",
        universeName: String = "",
        isTemplate: Boolean = false
    ): BotEntity = BotEntity(
        id = id,
        mode = mode,
        aiName = aiName,
        aiPersonality = aiPersonality,
        scenario = scenario,
        universeName = universeName,
        keyCharactersJson = "[]",
        userCharName = "Kullanıcı",
        userCharDesc = "",
        openingMessage = "Merhaba.",
        writingStyle = "rp",
        intensity = "normal",
        isTemplate = isTemplate
    )

    // ── Explore Tab Template Filtering & Turkish Normalization ──

    @Test
    fun normalizeExploreSearchQuery_handlesTurkishCaseFoldingAndWhitespace() {
        assertEquals("istanbul", normalizeExploreSearchQuery("  İSTANBUL  "))
        assertEquals("ışık", normalizeExploreSearchQuery(" IŞIK "))
        assertEquals("", normalizeExploreSearchQuery("   "))
    }

    @Test
    fun shouldShowAidenExploreHeroCard_respectsCategoryAndKeywords() {
        assertTrue(shouldShowAidenExploreHeroCard("all", ""))
        assertTrue(shouldShowAidenExploreHeroCard("universe", ""))
        assertFalse(shouldShowAidenExploreHeroCard("personal", ""))
        assertTrue(shouldShowAidenExploreHeroCard("all", "aiden"))
        assertTrue(shouldShowAidenExploreHeroCard("universe", "galatasaray"))
        assertFalse(shouldShowAidenExploreHeroCard("all", "cyberpunk"))
    }

    @Test
    fun filterExploreTemplates_filtersByCategoryAndTurkishNormalizedFields() {
        val presets = listOf(
            sampleBot(id = "p1", mode = "personal", aiName = "Aria", scenario = "İSTANBUL Siber Güvenlik"),
            sampleBot(id = "u1", mode = "universe", aiName = "Narrator", universeName = "Eldoria Krallığı", scenario = "Büyülü krallık"),
            sampleBot(id = "u2", mode = "universe", aiName = "Narrator", universeName = "Neo Tokyo", aiPersonality = "Karanlık noir", scenario = "Neon sokaklar")
        )

        assertEquals(3, filterExploreTemplates(presets, "all", "").size)
        assertEquals(listOf("u1", "u2"), filterExploreTemplates(presets, "universe", "").map { it.id })
        assertEquals(listOf("p1"), filterExploreTemplates(presets, "personal", "").map { it.id })
        assertEquals(listOf("p1"), filterExploreTemplates(presets, "all", normalizeExploreSearchQuery("İstanbul")).map { it.id })
        assertEquals(listOf("u1"), filterExploreTemplates(presets, "all", normalizeExploreSearchQuery("krallığı")).map { it.id })
        assertEquals(listOf("u2"), filterExploreTemplates(presets, "universe", "noir").map { it.id })
    }

    @Test
    fun findExistingBotForTemplatePreset_ignoresTemplatesAndMatchesUniverseOrName() {
        val templatePreset = sampleBot(id = "preset_eldoria", mode = "universe", aiName = "Anlatıcı", universeName = "Eldoria", isTemplate = true)
        val personalPreset = sampleBot(id = "preset_aria", mode = "personal", aiName = "Aria", isTemplate = true)

        val existingList = listOf(
            sampleBot(id = "tpl_copy", mode = "universe", aiName = "Anlatıcı", universeName = "Eldoria", isTemplate = true),
            sampleBot(id = "user_eldoria", mode = "universe", aiName = "Anlatıcı", universeName = "eldoria", isTemplate = false),
            sampleBot(id = "user_aria", mode = "personal", aiName = "ARIA", isTemplate = false)
        )

        assertEquals("user_eldoria", findExistingBotForTemplatePreset(existingList, templatePreset)?.id)
        assertEquals("user_aria", findExistingBotForTemplatePreset(existingList, personalPreset)?.id)
        assertNull(findExistingBotForTemplatePreset(existingList, sampleBot(id = "other", mode = "personal", aiName = "Lina", isTemplate = true)))
    }

    // ── 2.5D Live Encounter Stage Gyro Deadzone, Wind & Gaze Math ──

    @Test
    fun filterGyroTiltSample_suppressesMicroJitterWithinDeadzoneAndClampsExtremes() {
        // Tiny sensor noise while device is stationary should hold previous values
        val (stillYaw, stillPitch) = filterGyroTiltSample(
            prevYaw = 4.0f,
            prevPitch = -2.0f,
            rawYaw = 4.3f,
            rawPitch = -1.8f,
            smoothing = 0.14f,
            deadzoneDeg = 0.15f
        )
        assertEquals(4.0f, stillYaw, 0.0001f)
        assertEquals(-2.0f, stillPitch, 0.0001f)

        // Deliberate movement passes deadzone and moves smoothly toward clamped target
        val (movedYaw, movedPitch) = filterGyroTiltSample(
            prevYaw = 0f,
            prevPitch = 0f,
            rawYaw = 999f,
            rawPitch = -999f,
            smoothing = 0.5f,
            deadzoneDeg = 0.15f
        )
        assertEquals(11.0f, movedYaw, 0.001f)
        assertEquals(-5.0f, movedPitch, 0.001f)
    }

    @Test
    fun computeWindGustEnvelope_staysWithinSafeBoundsAcrossCycle() {
        for (step in 0..200) {
            val t = step * 0.15f
            val gust = computeWindGustEnvelope(t)
            assertTrue("Gust out of bounds at t=$t: $gust", gust in 0.08f..1.0f)
        }
    }

    @Test
    fun computeGazeTrackingVector_invertsHeadTurnAndClampsCorrectly() {
        val (gxCenter, gyCenter) = computeGazeTrackingVector(0f, 0f, 0f, 0f)
        assertEquals(0f, gxCenter, 0.0001f)
        assertEquals(0f, gyCenter, 0.0001f)

        val (gxRight, gyDown) = computeGazeTrackingVector(camYawDeg = 78f, camPitchDeg = 18f, microSaccadeX = 0f, microSaccadeY = 0f)
        assertEquals(-0.65f, gxRight, 0.001f)
        assertEquals(-0.45f, gyDown, 0.001f)

        val (gxClamped, gyClamped) = computeGazeTrackingVector(camYawDeg = -200f, camPitchDeg = -100f, microSaccadeX = 0.5f, microSaccadeY = 0.5f)
        assertEquals(0.95f, gxClamped, 0.0001f)
        assertEquals(0.75f, gyClamped, 0.0001f)
    }

    // ── ChatVisualEffects & ChatOverlays Pure Helpers ──

    @Test
    fun smoothRamp_clampsEdgesAndEvaluatesSmoothstepMidpoint() {
        assertEquals(0f, smoothRamp(5f, 10f, 20f), 0.0001f)
        assertEquals(0.5f, smoothRamp(15f, 10f, 20f), 0.0001f)
        assertEquals(1f, smoothRamp(25f, 10f, 20f), 0.0001f)
        assertEquals(0f, smoothRamp(9f, 10f, 10f), 0.0001f)
        assertEquals(1f, smoothRamp(10f, 10f, 10f), 0.0001f)
    }

    @Test
    fun detectChatMoodFromAssistantText_detectsTurkishMoodsAndDefaultsToCalm() {
        assertEquals("joy", detectChatMoodFromAssistantText("Bu HARİKA bir haber!"))
        assertEquals("warm", detectChatMoodFromAssistantText("Canım benim, sana sarılmak istiyorum."))
        assertEquals("sad", detectChatMoodFromAssistantText("Bugün çok ÜZGÜN hissediyorum."))
        assertEquals("tense", detectChatMoodFromAssistantText("Büyük bir TEHLİKE yaklaşıyor!"))
        assertEquals("curious", detectChatMoodFromAssistantText("İLGİNÇ bir detay yakaladım."))
        assertEquals("calm", detectChatMoodFromAssistantText("Sessizce pencereden dışarı baktı."))
        assertEquals("calm", detectChatMoodFromAssistantText("   "))
    }

    @Test
    fun formatAffectionDeltaBadge_formatsPositiveNegativeAndZero() {
        assertEquals("+4 Sevgi ✦", formatAffectionDeltaBadge(4))
        assertEquals("-2 Sevgi", formatAffectionDeltaBadge(-2))
        assertNull(formatAffectionDeltaBadge(0))
    }

    // ── CreateBotWizard Validation, Serialization & Atomic Submit Guard ──

    @Test
    fun isValidIsoStoryDate_acceptsBlankAndValidDatesRejectsMalformedOrInvalidCalendarDates() {
        assertTrue(isValidIsoStoryDate(""))
        assertTrue(isValidIsoStoryDate("   "))
        assertTrue(isValidIsoStoryDate("2045-06-15"))
        assertTrue(isValidIsoStoryDate("2024-02-29")) // leap year
        assertFalse(isValidIsoStoryDate("2025-02-29")) // non-leap year
        assertFalse(isValidIsoStoryDate("2026-13-01"))
        assertFalse(isValidIsoStoryDate("15-06-2045"))
        assertFalse(isValidIsoStoryDate("2045/06/15"))
    }

    @Test
    fun serializeAndDeserializeWizardKeyCharacters_roundTripsAndDropsBlankWhenRequested() {
        val input = listOf(
            KeyCharacter(id = "c1", name = "  Elena  ", desc = "  Büyücü  "),
            KeyCharacter(id = "c2", name = "   ", desc = "boş isimli karakter"),
            KeyCharacter(id = "c3", name = "Kael", desc = "Muhafız")
        )
        val rawJson = serializeWizardKeyCharacters(input, dropBlank = false)
        assertEquals(3, deserializeWizardKeyCharacters(rawJson).size)

        val cleanedJson = serializeWizardKeyCharacters(input, dropBlank = true)
        val restored = deserializeWizardKeyCharacters(cleanedJson)
        assertEquals(2, restored.size)
        assertEquals("Elena", restored[0].name)
        assertEquals("Büyücü", restored[0].desc)
        assertEquals("Kael", restored[1].name)

        assertTrue(deserializeWizardKeyCharacters("not-valid-json").isEmpty())
        assertTrue(deserializeWizardKeyCharacters("[]").isEmpty())
    }

    @Test
    fun validateWizardStep_enforcesRequiredFieldsAcrossAllSteps() {
        // Step 0 always valid
        assertNull(validateWizardStep(0, "personal", "", "", "", "", "", "", false, ""))

        // Step 1 personal vs universe
        assertNotNull(validateWizardStep(1, "personal", "", "", "", "", "", "", false, ""))
        assertNotNull(validateWizardStep(1, "personal", "Aria", "", "", "", "", "", false, ""))
        assertNull(validateWizardStep(1, "personal", "Aria", "Cesur", "", "", "", "", false, ""))
        assertNotNull(validateWizardStep(1, "universe", "", "", "", "", "", "", false, ""))
        assertNull(validateWizardStep(1, "universe", "", "", "Eldoria", "", "", "", false, ""))

        // Step 2 scenario + userCharName
        assertNotNull(validateWizardStep(2, "personal", "Aria", "Cesur", "", "", "Aiden", "", false, ""))
        assertNotNull(validateWizardStep(2, "personal", "Aria", "Cesur", "", "Gece vakti", "", "", false, ""))
        assertNull(validateWizardStep(2, "personal", "Aria", "Cesur", "", "Gece vakti", "Aiden", "", false, ""))

        // Step 3 openingMessage
        assertNotNull(validateWizardStep(3, "personal", "Aria", "Cesur", "", "Gece vakti", "Aiden", "  ", false, ""))
        assertNull(validateWizardStep(3, "personal", "Aria", "Cesur", "", "Gece vakti", "Aiden", "Merhaba", false, ""))

        // Step 4 analysis lock
        assertNotNull(validateWizardStep(4, "personal", "Aria", "Cesur", "", "Gece vakti", "Aiden", "Merhaba", true, ""))
        assertNull(validateWizardStep(4, "personal", "Aria", "Cesur", "", "Gece vakti", "Aiden", "Merhaba", false, ""))

        // Step 5 story start date
        assertNotNull(validateWizardStep(5, "personal", "Aria", "Cesur", "", "Gece vakti", "Aiden", "Merhaba", false, "2025-02-29"))
        assertNull(validateWizardStep(5, "personal", "Aria", "Cesur", "", "Gece vakti", "Aiden", "Merhaba", false, "2026-05-14"))
    }

    @Test
    fun wizardSubmitGuard_blocksDoubleTapsAndRecoversAfterReleaseOrTimeout() {
        var nowMs = 1_000L
        val guard = WizardSubmitGuard(staleTimeoutMs = 10_000L, clockMs = { nowMs })

        assertTrue(guard.tryAcquire())
        assertTrue(guard.isLocked())

        // Same-frame / immediate second tap MUST be rejected
        assertFalse(guard.tryAcquire())
        nowMs += 5_000L
        assertFalse(guard.tryAcquire())

        // Normal release allows next submission (e.g. after DB error retry)
        guard.release()
        assertFalse(guard.isLocked())
        assertTrue(guard.tryAcquire())

        // Stale timeout self-recovery if upstream callback was unexpectedly dropped
        nowMs += 10_000L
        assertTrue(guard.tryAcquire())
    }
}
