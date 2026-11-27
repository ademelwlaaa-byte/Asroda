package com.example

import com.example.book.BookBondSummaryHelper
import com.example.book.BookBranchDiscoveryHelper
import com.example.book.BookChapterMeta
import com.example.book.BookCharacterId
import com.example.book.BookProgressSummaryFormatter
import com.example.book.BookReadState
import com.example.book.BookReadingEstimator
import com.example.book.BookSessionResumeHelper
import com.example.book.CosmicDriftBookRegistry
import com.example.book.RecordedDecision
import com.example.book.StoryChoiceHistoryFormatter
import com.example.ui.screens.CharacterPortraitKey
import com.example.ui.screens.advanceCinemaProgress
import com.example.ui.screens.computeCinemaBeatIndex
import com.example.ui.screens.computeCinemaClimaxIntensity
import com.example.ui.screens.computeCinemaDirectedZoom
import com.example.ui.screens.cosmic3d.TurkishSpeechVisemeAnalyzer
import com.example.ui.screens.cosmic3d.computePanoramicViewportBounds
import com.example.ui.screens.cosmic3d.safeFiniteFloat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Cinema25DAndBookHelpersOverhaulTest {

    private fun baseBookState(
        activeChapter: Int = 1,
        completedChapters: Set<Int> = emptySet(),
        chapterDecisions: Map<Int, List<RecordedDecision>> = emptyMap(),
        bonds: Map<BookCharacterId, Int> = emptyMap(),
        flags: Map<String, String> = emptyMap(),
        numericFlags: Map<String, Int> = emptyMap(),
        discoveredSecrets: Set<String> = emptySet(),
        discoveredBranches: Set<String> = emptySet(),
        isChapterCompleted: Boolean = false
    ): BookReadState = BookReadState(
        bookId = "cosmic_drift",
        activeChapter = activeChapter,
        completedChapters = completedChapters,
        currentNodeId = "ch${activeChapter}_n1",
        transcript = emptyList(),
        chapterDecisions = chapterDecisions,
        bonds = bonds,
        fatigue = 15,
        mystery = 40,
        flags = flags,
        numericFlags = numericFlags,
        discoveredSecrets = discoveredSecrets,
        discoveredBranches = discoveredBranches,
        isChapterCompleted = isChapterCompleted
    )

    private fun decision(
        chapterNumber: Int,
        selectedOption: String,
        impactText: String = ""
    ): RecordedDecision = RecordedDecision(
        chapterNumber = chapterNumber,
        nodeId = "n_$chapterNumber",
        decisionTitle = "Karar",
        choiceId = "c_$chapterNumber",
        selectedOption = selectedOption,
        impactText = impactText,
        bondDeltas = emptyMap(),
        fatigueDelta = 0,
        mysteryDelta = 0,
        secretUnlocked = null,
        branchTag = null,
        isBranching = false,
        isRelationship = false,
        impactLevel = "orta"
    )

    // -------------------------------------------------------------------------
    // 1. 2.5D Cinema Motion Engine pure helpers (BookCinematicMedia.kt)
    // -------------------------------------------------------------------------

    @Test
    fun cinemaClimaxAndZoom_followFourBeatArcAndSurviveNonFiniteInputs() {
        val beat1 = computeCinemaClimaxIntensity(0.10f)
        val beat2 = computeCinemaClimaxIntensity(0.45f)
        val beat3Peak = computeCinemaClimaxIntensity(0.685f)
        val beat4 = computeCinemaClimaxIntensity(0.95f)

        assertTrue("Beat 2 should build intensity over beat 1", beat2 > beat1)
        assertTrue("Beat 3 should reach climax peak", beat3Peak > beat2 && beat3Peak >= 0.95f)
        assertTrue("Beat 4 should resolve below peak", beat4 < beat3Peak)

        for (bad in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -5f, 99f)) {
            val intensity = computeCinemaClimaxIntensity(bad)
            val zoom = computeCinemaDirectedZoom(bad)
            assertTrue(intensity.isFinite() && intensity in 0f..1f)
            assertTrue(zoom.isFinite() && zoom in 1.0f..1.15f)
        }
    }

    @Test
    fun cinemaBeatIndexAndProgress_wrapCleanlyAndClampFrameSpikes() {
        assertEquals(0, computeCinemaBeatIndex(0f))
        assertEquals(0, computeCinemaBeatIndex(0.24f))
        assertEquals(1, computeCinemaBeatIndex(0.25f))
        assertEquals(2, computeCinemaBeatIndex(0.50f))
        assertEquals(3, computeCinemaBeatIndex(0.75f))
        assertEquals(3, computeCinemaBeatIndex(1.0f))
        assertEquals(0, computeCinemaBeatIndex(Float.NaN))

        // Normal advance: 1.2s out of 12s = +0.10
        val next = advanceCinemaProgress(current = 0.25f, deltaSec = 0.12f, durationSec = 12f)
        assertEquals(0.26f, next, 0.0005f)

        // Wrap-around past 1.0
        val wrapped = advanceCinemaProgress(current = 0.99f, deltaSec = 0.24f, durationSec = 12f)
        assertEquals(0.01f, wrapped, 0.0005f)

        // Huge frame-drop delta (e.g. 5s background pause) clamped to 0.25s max
        val clampedSpike = advanceCinemaProgress(current = 0.0f, deltaSec = 5.0f, durationSec = 12f)
        assertEquals(0.25f / 12f, clampedSpike, 0.0005f)

        // Non-finite inputs stay finite in [0, 1)
        val safeNan = advanceCinemaProgress(Float.NaN, Float.POSITIVE_INFINITY, 0f)
        assertTrue(safeNan.isFinite() && safeNan in 0f..1f)
    }

    // -------------------------------------------------------------------------
    // 2. Living 3D Encounter Mesh Viewport Math & Turkish Viseme Engine
    // -------------------------------------------------------------------------

    @Test
    fun safeFiniteFloat_replacesNonFiniteValues() {
        assertEquals(3.5f, safeFiniteFloat(3.5f, 1f), 0.0001f)
        assertEquals(1f, safeFiniteFloat(Float.NaN, 1f), 0.0001f)
        assertEquals(1f, safeFiniteFloat(Float.POSITIVE_INFINITY, 1f), 0.0001f)
        assertEquals(1f, safeFiniteFloat(Float.NEGATIVE_INFINITY, 1f), 0.0001f)
    }

    @Test
    fun panoramicViewportBounds_guaranteesZeroBlackBarsAcrossAllExtremeAngles() {
        // Invalid canvases safely return null
        assertNull(
            computePanoramicViewportBounds(
                canvasW = 0f, canvasH = 1920f, bmpW = 1920f, bmpH = 1080f,
                camYawDeg = 0f, camPitchDeg = 0f, leanZoom = 1f, turnProgress = 0f,
                charCenterXLookout = 0.5f, charCenterXTurned = 0.5f, headCenterY = 0.3f
            )
        )
        assertNull(
            computePanoramicViewportBounds(
                canvasW = Float.NaN, canvasH = 1920f, bmpW = 1920f, bmpH = 1080f,
                camYawDeg = 0f, camPitchDeg = 0f, leanZoom = 1f, turnProgress = 0f,
                charCenterXLookout = 0.5f, charCenterXTurned = 0.5f, headCenterY = 0.3f
            )
        )

        val viewports = listOf(1080f to 2400f, 1920f to 1080f, 720f to 1280f)
        val yaws = listOf(-120f, -78f, 0f, 78f, 120f, Float.NaN)
        val pitches = listOf(-40f, -18f, 0f, 18f, 40f, Float.POSITIVE_INFINITY)
        val zooms = listOf(0.5f, 1.0f, 1.4f, 3.0f, Float.NaN)

        for ((cw, ch) in viewports) {
            for (yaw in yaws) {
                for (pitch in pitches) {
                    for (zoom in zooms) {
                        val b = computePanoramicViewportBounds(
                            canvasW = cw,
                            canvasH = ch,
                            bmpW = 1920f,
                            bmpH = 1080f,
                            camYawDeg = yaw,
                            camPitchDeg = pitch,
                            leanZoom = zoom,
                            turnProgress = 0.5f,
                            charCenterXLookout = 0.36f,
                            charCenterXTurned = 0.52f,
                            headCenterY = 0.28f
                        )
                        assertNotNull(b)
                        val bounds = b!!
                        assertTrue("originX must be <= 0 (no left black bar)", bounds.originX <= 0.01f)
                        assertTrue("originX + panW must cover canvasW (no right black bar)", bounds.originX + bounds.panW >= cw - 0.01f)
                        assertTrue("originY must be <= 0 (no top black bar)", bounds.originY <= 0.01f)
                        assertTrue("originY + panH must cover canvasH (no bottom black bar)", bounds.originY + bounds.panH >= ch - 0.01f)
                    }
                }
            }
        }
    }

    @Test
    fun turkishSpeechVisemeAnalyzer_handlesTurkishVowelsBilabialsAndInvalidInputs() {
        // Silence / blank / NaN produce exact rest pose
        assertEquals(0f, TurkishSpeechVisemeAnalyzer.sampleViseme("Merhaba", 0.5f, 0.01f).jawDrop, 0.0001f)
        assertEquals(0f, TurkishSpeechVisemeAnalyzer.sampleViseme("   ", 0.5f, 0.8f).jawDrop, 0.0001f)
        assertEquals(0f, TurkishSpeechVisemeAnalyzer.sampleViseme("Merhaba", Float.NaN, 0.8f).jawDrop, 0.0001f)
        assertEquals(0f, TurkishSpeechVisemeAnalyzer.sampleViseme("Merhaba", 0.5f, Float.NaN).jawDrop, 0.0001f)

        // Open Turkish 'a' opens jaw far more than bilabial 'm'
        val openA = TurkishSpeechVisemeAnalyzer.sampleViseme("aaaa", 0.1f, 0.85f)
        val closedM = TurkishSpeechVisemeAnalyzer.sampleViseme("mmmm", 0.1f, 0.85f)
        assertTrue("Open 'a' jawDrop (${openA.jawDrop}) must exceed bilabial 'm' (${closedM.jawDrop})", openA.jawDrop > closedM.jawDrop + 0.5f)

        // Uppercase Turkish 'Ö'/'Ü' round lips (negative lipWidth); uppercase 'İ' widens lips (positive lipWidth)
        val roundO = TurkishSpeechVisemeAnalyzer.sampleViseme("ÖÜÖÜ", 0.05f, 0.85f)
        val wideI = TurkishSpeechVisemeAnalyzer.sampleViseme("İİİİ", 0.05f, 0.85f)
        assertTrue("Rounded Turkish Ö/Ü should yield negative lipWidth", roundO.lipWidth < -0.15f)
        assertTrue("Wide Turkish İ should yield positive lipWidth", wideI.lipWidth > 0.15f)

        // Negative elapsed time never crashes or produces NaN
        val negTime = TurkishSpeechVisemeAnalyzer.sampleViseme("Gök yarıldı", -3.0f, 0.7f)
        assertTrue(negTime.jawDrop.isFinite() && negTime.jawDrop in 0f..1f)
    }

    // -------------------------------------------------------------------------
    // 3. Turkish-Locale-Safe Portrait Lookup (BookCharacterAvatars.kt)
    // -------------------------------------------------------------------------

    @Test
    fun characterPortraitKey_resolvesTurkishLowercaseLabelsCorrectly() {
        assertNull(CharacterPortraitKey.resolveFromLabel("   "))
        assertEquals(CharacterPortraitKey.ARCHITECT, CharacterPortraitKey.resolveFromLabel("asıl mimar"))
        assertEquals(CharacterPortraitKey.VISION, CharacterPortraitKey.resolveFromLabel("vizyon sinyali"))
        assertEquals(CharacterPortraitKey.ZERO, CharacterPortraitKey.resolveFromLabel("yara izli aiden"))
        assertEquals(CharacterPortraitKey.MIRA, CharacterPortraitKey.resolveFromLabel("mira"))
        assertEquals(CharacterPortraitKey.PIETRO, CharacterPortraitKey.resolveFromLabel("pietro"))
        assertEquals(CharacterPortraitKey.VOSS, CharacterPortraitKey.resolveFromLabel("elias voss"))
        assertEquals(CharacterPortraitKey.VAELOR, CharacterPortraitKey.resolveFromLabel("soluk mühür"))
        assertEquals(CharacterPortraitKey.AIDEN, CharacterPortraitKey.resolveFromLabel("aiden blackwood"))
    }

    // -------------------------------------------------------------------------
    // 4. 24-Chapter Book Helpers Overhaul (Season 1 + Season 2)
    // -------------------------------------------------------------------------

    @Test
    fun bookBondSummaryHelper_breaksTiesDeterministicallyAndComputesAverage() {
        val tiedState = baseBookState(
            activeChapter = 2,
            completedChapters = setOf(1),
            bonds = mapOf(
                BookCharacterId.STEVE to 80,
                BookCharacterId.NATASHA to 80,
                BookCharacterId.WANDA to 60
            )
        )
        val highlight = BookBondSummaryHelper.compute(tiedState)
        assertEquals("Lower ordinal (STEVE) must win score ties deterministically", BookCharacterId.STEVE, highlight.strongestAlly)
        assertEquals(80, highlight.strongestScore)
        assertTrue("Average score must stay in 0..100", highlight.averageScore in 1..100)
        assertTrue(BookBondSummaryHelper.formatCompact(highlight).contains("Steve Rogers"))

        val emptyFormatted = BookBondSummaryHelper.formatCompact(BookBondSummaryHelper.BondHighlight(null, 0, 0))
        assertEquals("Henüz karakter bağı oluşmadı", emptyFormatted)
    }

    @Test
    fun bookBranchDiscoveryHelper_countsSeason2NumericFlagsAndTopTierBadge() {
        val state = baseBookState(
            activeChapter = 18,
            flags = mapOf("met_voss" to "true", "  " to "blank_key", "empty_val" to "   "),
            numericFlags = mapOf("story:cosmic_drift" to 4, "char:steve:trust" to 0, "char:wanda:empathy" to 3),
            discoveredBranches = (1..10).map { "branch_$it" }.toSet() + setOf("", "   ")
        )
        val summary = BookBranchDiscoveryHelper.compute(state)
        assertEquals("Blank branches must be ignored", 10, summary.branchCount)
        assertEquals("1 valid string flag + 2 non-zero numeric flags = 3", 3, summary.flagCount)
        assertEquals("🌌 Çoklu Evren Mimarı", summary.explorerBadge)
        assertEquals("🌌 Çoklu Evren Mimarı (10 rota • 3 iz)", BookBranchDiscoveryHelper.formatCompact(summary))
    }

    @Test
    fun bookProgressSummaryFormatter_handles24ChaptersAndSeasonBreakdown() {
        val state = baseBookState(
            activeChapter = 15,
            completedChapters = setOf(-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 99),
            chapterDecisions = mapOf(
                1 to listOf(decision(1, "Kabul et"), decision(1, "   ")),
                11 to listOf(decision(11, "Mührü aç"))
            ),
            discoveredSecrets = setOf("s1", "  ", "s2")
        )
        val summary = BookProgressSummaryFormatter.compute(state, totalChapters = 24)
        assertEquals("Out-of-range chapters (-1, 0, 99) must be excluded", 12, summary.completedChapters)
        assertEquals(24, summary.totalChapters)
        assertEquals(50, summary.completionPercent)
        assertEquals("Blank decisions must be excluded", 2, summary.totalDecisions)
        assertEquals("Blank secrets must be excluded", 2, summary.discoveredSecrets)

        val breakdown = BookProgressSummaryFormatter.formatSeasonBreakdown(state, season1Total = 10, totalChapters = 24)
        assertEquals("1. Sezon: 10/10 • 2. Sezon: 2/14", breakdown)

        val s1Only = BookProgressSummaryFormatter.formatSeasonBreakdown(state, season1Total = 10, totalChapters = 10)
        assertEquals("1. Sezon: 10/10", s1Only)
    }

    @Test
    fun bookSessionResumeHelper_clampsChapterAndReportsSeasonLabels() {
        assertEquals("1. Sezon", BookSessionResumeHelper.seasonLabelForChapter(1))
        assertEquals("1. Sezon", BookSessionResumeHelper.seasonLabelForChapter(10))
        assertEquals("2. Sezon", BookSessionResumeHelper.seasonLabelForChapter(11))
        assertEquals("2. Sezon", BookSessionResumeHelper.seasonLabelForChapter(24))

        val outOfRangeNew = baseBookState(activeChapter = 99)
        assertEquals("Yeni yolculuk • Bölüm 24 seni bekliyor", BookSessionResumeHelper.buildResumeSubtitle(outOfRangeNew, totalChapters = 24))

        val midSeason2 = baseBookState(activeChapter = 13, completedChapters = (1..12).toSet())
        assertEquals("Bölüm 13 • %50 tamamlandı • 12 bölüm kaldı", BookSessionResumeHelper.buildResumeSubtitle(midSeason2, totalChapters = 24))

        val allDone = baseBookState(
            activeChapter = 24,
            completedChapters = (1..24).toSet(),
            chapterDecisions = mapOf(1 to listOf(decision(1, "Seçim"))),
            discoveredSecrets = setOf("secret_final")
        )
        assertEquals("Tamamlandı (%100) • 1 karar • 1 sır", BookSessionResumeHelper.buildResumeSubtitle(allDone, totalChapters = 24))
    }

    @Test
    fun bookReadingEstimator_isOverflowSafeAndEstimates24ChapterBookRemainingMinutes() {
        // Overflow safety with extreme word counts
        val mins = BookReadingEstimator.estimateRemainingMinutes(
            currentSceneOrder = 1,
            totalScenes = 10,
            isChapterCompleted = false,
            observedTranscriptWordCount = Int.MAX_VALUE,
            observedSceneCount = 1
        )
        assertTrue("Overflow-safe estimate must be positive and bounded", mins in 1..60)
        assertEquals(0, BookReadingEstimator.estimateRemainingMinutes(1, 10, isChapterCompleted = true))
        assertEquals("Bitti", BookReadingEstimator.formatBadgeText(0))

        val customChapters = listOf(
            BookChapterMeta(1, "I", "B1", "C1", "", "", "", 8, 0L, "s1", 5, 2),
            BookChapterMeta(2, "II", "B2", "C2", "", "", "", 12, 0L, "s2", 6, 2),
            BookChapterMeta(11, "XI", "B11", "C11", "", "", "", 15, 0L, "s11", 7, 3)
        )
        assertEquals(27, BookReadingEstimator.estimateBookRemainingMinutes(setOf(1), customChapters))
        assertEquals(0, BookReadingEstimator.estimateBookRemainingMinutes(setOf(1, 2, 11), customChapters))
        assertTrue(
            "Default 24-chapter registry must have positive total reading time",
            BookReadingEstimator.estimateBookRemainingMinutes(emptySet(), CosmicDriftBookRegistry.chapters) > 0
        )
    }

    @Test
    fun storyChoiceHistoryFormatter_sortsChronologicallyFiltersBlanksAndClampsBounds() {
        val state = baseBookState(
            activeChapter = 14,
            chapterDecisions = mapOf(
                14 to listOf(decision(14, "  İkinci sezon kararı  ", "  Kozmik mühür açıldı  ")),
                1 to listOf(decision(0, "İlk karar", "Güven +5"), decision(1, "   ", "Boş"))
            )
        )
        val formatted = StoryChoiceHistoryFormatter.formatTimeline(state, maxEntries = 10)
        val lines = formatted.lines()
        assertEquals("Blank decision must be omitted", 2, lines.size)
        assertEquals("• B1: İlk karar → Güven +5", lines[0])
        assertEquals("• B14: İkinci sezon kararı → Kozmik mühür açıldı", lines[1])

        // maxEntries <= 0 clamps to 1 (keeps most recent entry)
        val clamped = StoryChoiceHistoryFormatter.formatTimeline(state, maxEntries = 0)
        assertEquals("• B14: İkinci sezon kararı → Kozmik mühür açıldı", clamped)
    }
}
