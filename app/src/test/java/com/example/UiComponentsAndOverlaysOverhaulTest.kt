package com.example

import com.example.ui.components.BotAvatarCodec
import com.example.ui.components.BotAvatarSourceKind
import com.example.ui.components.ModalInputValidators
import com.example.ui.components.MoodColors
import com.example.ui.components.StoryOverlayTextSanitizer
import com.example.ui.components.computeBackgroundLayerAlphas
import com.example.ui.components.computeDustEdgeFade
import com.example.ui.components.computeTypingDotSample
import com.example.ui.components.normalizeOrbHue
import com.example.ui.components.partitionFeatureToggles
import com.example.util.AppFeatureFlags
import com.example.util.StoryBookmarkStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class UiComponentsAndOverlaysOverhaulTest {

    // -------------------------------------------------------------------------
    // 1. AppBackground draw-phase math helpers
    // -------------------------------------------------------------------------

    @Test
    fun appBackgroundAlphasAndDustFade_clampAndSurviveNonFiniteInputs() {
        val (cosmic0, tpl0) = computeBackgroundLayerAlphas(0f)
        assertEquals(1f, cosmic0, 0.0001f)
        assertEquals(0f, tpl0, 0.0001f)

        val (cosmicMid, tplMid) = computeBackgroundLayerAlphas(0.35f)
        assertEquals(0.65f, cosmicMid, 0.0001f)
        assertEquals(0.35f, tplMid, 0.0001f)

        for (bad in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -2f, 5f)) {
            val (c, t) = computeBackgroundLayerAlphas(bad)
            assertTrue(c.isFinite() && c in 0f..1f)
            assertTrue(t.isFinite() && t in 0f..1f)
            assertEquals(1f, c + t, 0.0001f)
        }

        assertEquals(1f, computeDustEdgeFade(0.5f), 0.0001f)
        assertEquals(0.5f, computeDustEdgeFade(0.04f), 0.001f)
        assertEquals(0.5f, computeDustEdgeFade(0.96f), 0.001f)
        assertEquals(1f, computeDustEdgeFade(Float.NaN), 0.0001f)
    }

    // -------------------------------------------------------------------------
    // 2. OrbView hue normalization & Turkish MoodColors
    // -------------------------------------------------------------------------

    @Test
    fun orbHueAndMoodColors_handleTurkishLocaleAndWrapAround() {
        assertEquals(0f, normalizeOrbHue(360f), 0.0001f)
        assertEquals(330f, normalizeOrbHue(-30f), 0.0001f)
        assertEquals(200f, normalizeOrbHue(Float.NaN), 0.0001f)
        assertEquals(200f, normalizeOrbHue(Float.POSITIVE_INFINITY), 0.0001f)

        assertEquals(38f, MoodColors.getMoodHue("  NEŞELİ "), 0.0001f)
        assertEquals(340f, MoodColors.getMoodHue("SICAK"), 0.0001f)
        assertEquals(250f, MoodColors.getMoodHue("HÜZÜNLÜ"), 0.0001f)
        assertEquals(8f, MoodColors.getMoodHue("GERGİN"), 0.0001f)
        assertEquals(275f, MoodColors.getMoodHue("MERAKLI"), 0.0001f)
        assertEquals(200f, MoodColors.getMoodHue("bilinmeyen"), 0.0001f)

        assertEquals("sıcak", MoodColors.getMoodLabel("WARM"))
        assertEquals("gergin", MoodColors.getMoodLabel("GERGİN"))
        assertEquals("sakin", MoodColors.getMoodLabel(""))
    }

    // -------------------------------------------------------------------------
    // 3. TypingDots harmonic wave math
    // -------------------------------------------------------------------------

    @Test
    fun typingDotSample_respectsReducedMotionAndNonFinitePhases() {
        val peak = computeTypingDotSample(wavePhase = (PI / 2.0).toFloat(), dotIndex = 0, reducedMotion = false)
        assertTrue("Active dot at sin=+1 should lift upward (negative Y)", peak.liftFactor < -0.95f)
        assertTrue("Active dot scale should exceed 1.0", peak.scale > 1.0f)
        assertTrue(peak.glowAlpha in 0.85f..1f)

        val reduced = computeTypingDotSample(wavePhase = (PI / 2.0).toFloat(), dotIndex = 0, reducedMotion = true)
        assertEquals("Reduced motion must disable vertical bounce", 0f, reduced.liftFactor, 0.0001f)
        assertTrue(reduced.scale in 0.8f..1.05f)

        val bad = computeTypingDotSample(wavePhase = Float.NaN, dotIndex = -4, reducedMotion = false)
        assertTrue(bad.liftFactor.isFinite() && bad.scale.isFinite() && bad.glowAlpha in 0f..1f)
    }

    // -------------------------------------------------------------------------
    // 4. BotAvatarImage source classifier & fresh ByteBuffer codec
    // -------------------------------------------------------------------------

    @Test
    fun botAvatarCodec_classifiesAllUriKindsAndRejectsUnsupportedSchemes() {
        assertNull(BotAvatarCodec.normalizeUrl("   "))
        assertEquals(BotAvatarSourceKind.EMPTY, BotAvatarCodec.classifySource(null))
        assertEquals(BotAvatarSourceKind.EMPTY, BotAvatarCodec.classifySource("   "))

        assertEquals(
            BotAvatarSourceKind.BASE64_DATA_URI,
            BotAvatarCodec.classifySource("  data:image/webp;base64,UklGRh4AAABXRUJQVlA4 ")
        )
        assertEquals(
            BotAvatarSourceKind.REMOTE_URL,
            BotAvatarCodec.classifySource("https://cdn.example.com/avatar.webp")
        )
        assertEquals(
            BotAvatarSourceKind.REMOTE_URL,
            BotAvatarCodec.classifySource("HTTP://example.com/pic.jpg")
        )
        assertEquals(
            BotAvatarSourceKind.LOCAL_URI_OR_PATH,
            BotAvatarCodec.classifySource("file:///data/user/0/com.example/files/bot_images/a.webp")
        )
        assertEquals(
            BotAvatarSourceKind.LOCAL_URI_OR_PATH,
            BotAvatarCodec.classifySource("content://media/external/images/media/42")
        )
        assertEquals(
            BotAvatarSourceKind.LOCAL_URI_OR_PATH,
            BotAvatarCodec.classifySource("/data/user/0/com.example/files/a.webp")
        )
        assertEquals(
            BotAvatarSourceKind.UNSUPPORTED,
            BotAvatarCodec.classifySource("javascript:alert(1)")
        )
        assertEquals(
            BotAvatarSourceKind.UNSUPPORTED,
            BotAvatarCodec.classifySource("not a valid url")
        )

        val raw = byteArrayOf(1, 2, 3, 4)
        val b1 = BotAvatarCodec.wrapFreshBuffer(raw)
        b1.position(b1.limit())
        val b2 = BotAvatarCodec.wrapFreshBuffer(raw)
        assertEquals(0, b2.position())
        assertEquals(4, b2.remaining())
    }

    // -------------------------------------------------------------------------
    // 5. ChatStoryOverlays internal tag sanitization & bookmark/macro guards
    // -------------------------------------------------------------------------

    @Test
    fun storyOverlaySanitizer_stripsThinkStateMediaAndOocTags() {
        val dirty = """
            <think>Internal chain of thought that must never appear in UI</think>
            "Fırtına kapıya dayandı," dedi Aiden.
            [[ANIM|MUTLAK_UYANIS|Çatı|Yağmur]]
            [OOC_META]debug=1[/OOC_META]
            [[STATE_JSON]]{"affection":85,"mood":"tense"}[[/STATE_JSON]]
        """.trimIndent()

        val cleaned = StoryOverlayTextSanitizer.sanitizePreview(dirty)
        assertEquals("\"Fırtına kapıya dayandı,\" dedi Aiden.", cleaned)
        assertFalse(cleaned.contains("think", ignoreCase = true))
        assertFalse(cleaned.contains("STATE_JSON"))
        assertFalse(cleaned.contains("ANIM"))

        // Unclosed <think> or [[MEMORY_JSON]] truncated cleanly
        val unclosed = "Başlangıç satırı. <think>yarım kalan düşünce..."
        assertEquals("Başlangıç satırı.", StoryOverlayTextSanitizer.stripInternalTags(unclosed))

        // Max length ellipsis truncation
        val longText = "A".repeat(80)
        val preview = StoryOverlayTextSanitizer.sanitizePreview(longText, maxLength = 20)
        assertEquals(20, preview.length)
        assertTrue(preview.endsWith("…"))
    }

    @Test
    fun storyOverlayValidation_blocksDuplicateBookmarksAndMacros() {
        val existingBookmarks = listOf(
            StoryBookmarkStore.Bookmark(
                id = "bm_1",
                messageId = "msg_42",
                title = "Kulede İhanet",
                snippet = "Kısa özet",
                createdAtMs = 1000L
            )
        )
        assertTrue(
            "Same messageId + case-insensitive Turkish title must be treated as duplicate",
            StoryOverlayTextSanitizer.shouldSkipDuplicateBookmark(existingBookmarks, "msg_42", "kulede ihanet")
        )
        assertFalse(
            "Different title on same message should be allowed",
            StoryOverlayTextSanitizer.shouldSkipDuplicateBookmark(existingBookmarks, "msg_42", "Farklı Başlık")
        )
        assertFalse(
            "Same title on different message should be allowed",
            StoryOverlayTextSanitizer.shouldSkipDuplicateBookmark(existingBookmarks, "msg_99", "Kulede İhanet")
        )

        assertNull(StoryOverlayTextSanitizer.validateNewMacro("  ", "text", emptyList()))
        assertNull(StoryOverlayTextSanitizer.validateNewMacro("Savunma", "  ", emptyList()))
        assertNull(StoryOverlayTextSanitizer.validateNewMacro("savunma", "*bekler*", listOf("Savunma")))

        val valid = StoryOverlayTextSanitizer.validateNewMacro("  İleri Atıl ", " *Hızla koşar.* ", listOf("Savunma"))
        assertNotNull(valid)
        assertEquals("İleri Atıl", valid!!.first)
        assertEquals("*Hızla koşar.*", valid.second)
    }

    // -------------------------------------------------------------------------
    // 6. Modals validation & FeatureFlagsPanel partitioning
    // -------------------------------------------------------------------------

    @Test
    fun modalInputValidators_validateTemperaturesAndTurkishInitials() {
        assertEquals(0.85f, ModalInputValidators.parseClampedTemperature("0.85")!!, 0.0001f)
        assertEquals(0.75f, ModalInputValidators.parseClampedTemperature(" 0,75 ")!!, 0.0001f)
        assertEquals(0.1f, ModalInputValidators.parseClampedTemperature("-3.0")!!, 0.0001f)
        assertEquals(1.2f, ModalInputValidators.parseClampedTemperature("9.5")!!, 0.0001f)
        assertNull(ModalInputValidators.parseClampedTemperature(""))
        assertNull(ModalInputValidators.parseClampedTemperature("abc"))
        assertNull(ModalInputValidators.parseClampedTemperature("NaN"))
        assertNull(ModalInputValidators.parseClampedTemperature("Infinity"))

        assertEquals("İ", ModalInputValidators.extractAvatarInitial("istanbul"))
        assertEquals("Ş", ModalInputValidators.extractAvatarInitial(" şafak"))
        assertEquals("A", ModalInputValidators.extractAvatarInitial("   "))
    }

    @Test
    fun featureFlagsPartition_deduplicatesAndSeparatesStableFromExperimental() {
        val sample = listOf(
            AppFeatureFlags.ToggleSpec("k1", "Stable 1", "d1", isExperimental = false, getter = { true }, setter = {}),
            AppFeatureFlags.ToggleSpec("k1", "Duplicate 1", "d1b", isExperimental = true, getter = { false }, setter = {}),
            AppFeatureFlags.ToggleSpec("k2", "Beta 1", "d2", isExperimental = true, getter = { false }, setter = {}),
            AppFeatureFlags.ToggleSpec("k3", "Stable 2", "d3", isExperimental = false, getter = { true }, setter = {})
        )
        val (stable, experimental) = partitionFeatureToggles(sample)
        assertEquals(listOf("k1", "k3"), stable.map { it.key })
        assertEquals(listOf("k2"), experimental.map { it.key })
    }
}
