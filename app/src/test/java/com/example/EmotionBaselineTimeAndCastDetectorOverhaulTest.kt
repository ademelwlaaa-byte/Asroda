package com.example

import com.example.data.local.EmotionState
import com.example.data.local.PrimaryEmotions
import com.example.data.local.RelationshipAxes
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.repository.BaselineEmotionEngine
import com.example.data.repository.CastDetectorEngine
import com.example.data.repository.EmotionEngine
import com.example.data.repository.TimePerceptionEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
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
@Config(sdk = [33])
class EmotionBaselineTimeAndCastDetectorOverhaulTest {

    // ── 1. EmotionEngine: JSON null-safety, English->Turkish normalization, bounds & NaN ──

    @Test
    fun emotionEngine_parseStateJsonSafelyIgnoresJsonNullAndNormalizesEnglishDominant() {
        val prev = EmotionState(
            primaryEmotions = PrimaryEmotions(joy = 20, trust = 60, anger = 10),
            relationshipAxes = RelationshipAxes(affectionScore = 55, respectScore = 60, comfortScore = 55, resentmentScore = 5),
            dominantEmotion = "sakin",
            suppressedEmotion = "merak"
        )

        val nullJson = """{"dominant_emotion": null, "suppressed_emotion": null, "why": null, "delta": {"axis": null, "value": 3, "reason": null}}"""
        val afterNull = EmotionEngine.parseStateJsonSafely(nullJson, prev)
        assertEquals("sakin", afterNull.dominantEmotion)
        assertEquals("merak", afterNull.suppressedEmotion)
        assertEquals("", afterNull.deltaReason)
        assertEquals("affectionScore", afterNull.deltaAxis)
        assertEquals(58, afterNull.affection)

        val englishContradictionJson = """{"dom": "Happy", "pe": {"anger": 85, "joy": 10, "trust": 20}}"""
        val corrected = EmotionEngine.parseStateJsonSafely(englishContradictionJson, prev)
        assertEquals(
            "Leaked English 'Happy' with anger=85 must normalize to Turkish and trigger consistency fix",
            "gergin ve öfkeli",
            corrected.dominantEmotion
        )

        val englishValidJson = """{"dom": "Protective", "pe": {"anger": 5, "trust": 75, "joy": 40}}"""
        val normalized = EmotionEngine.parseStateJsonSafely(englishValidJson, prev)
        assertEquals("koruyucu", normalized.dominantEmotion)
    }

    @Test
    fun emotionEngine_regulationPassiveDecayAndClampingGuardOutOfRangeAndNaN() {
        val regHighCap = EmotionEngine.calculateEmotionalRegulation(rawDelta = -40, capacity = 300)
        assertTrue(regHighCap.isRegulationActive)
        assertTrue("Clamped capacity=100 must keep outwardExpressedDelta negative, not invert sign", regHighCap.outwardExpressedDelta < 0)
        assertEquals(-22, regHighCap.outwardExpressedDelta)

        val clampedNaN = EmotionEngine.calculateClampedAffectionDelta(
            prevAffection = 50,
            rawDelta = 10,
            totalMsgCount = 12,
            baseMultiplier = Double.NaN,
            combinedContextMult = Double.POSITIVE_INFINITY,
            personalityAffectionMult = -4.0
        )
        assertTrue("Non-finite/negative multipliers must fall back safely into 0..15", clampedNaN in 1..15)

        val simpleNaN = EmotionEngine.calculateClampedDelta(
            currentScore = 50,
            rawDelta = 6,
            difficultyMultiplier = Double.NaN
        )
        assertEquals(6, simpleNaN)

        val angryState = EmotionState(
            primaryEmotions = PrimaryEmotions(anger = 45),
            relationshipAxes = RelationshipAxes(resentmentScore = 40)
        )
        val atZero = EmotionEngine.applyPassiveDecayPerMessage(angryState, messageIndex = 0)
        assertEquals("Message index 0 must NOT trigger passive decay", 45, atZero.primaryEmotions.anger)
        val atTen = EmotionEngine.applyPassiveDecayPerMessage(angryState, messageIndex = 10)
        assertEquals(44, atTen.primaryEmotions.anger)
        assertEquals(39, atTen.relationshipAxes.resentmentScore)
    }

    // ── 2. BaselineEmotionEngine: JSONObject.NULL safety, Turkish I/ı protocol keys & clamping ──

    @Test
    fun baselineEmotion_parseAnalysisJsonGuardsJsonNullAcrossAllStringAndTriggerFields() {
        val nullHeavyJson = """
            {
              "relationshipType": null,
              "relationshipNote": null,
              "attachmentStyle": null,
              "dominantEmotion": null,
              "speechStyle": null,
              "sensitiveTriggers": [
                {"topic": null, "reaction": null, "intensityMultiplier": 9.9},
                {"topic": "eski ihanet", "reaction": null, "intensityMultiplier": 0.2}
              ],
              "worldAtmosphere": {
                "currentLocation": null,
                "timeOfDay": null,
                "sceneMood": null
              }
            }
        """.trimIndent()

        val parsed = BaselineEmotionEngine.parseAnalysisJson(
            jsonStr = nullHeavyJson,
            botId = "bot_null_baseline",
            aiName = "Aiden",
            personality = "Temkinli",
            scenario = "Yağmurlu gece"
        )
        assertEquals("other", parsed.relationshipType)
        assertEquals("", parsed.relationshipNote)
        assertEquals("secure", parsed.personalityProfile.attachmentStyle)
        assertFalse(parsed.baselineEmotionState.dominantEmotion.equals("null", ignoreCase = true))
        assertEquals("Doğal ve samimi", parsed.speechPattern)
        assertNull("All-null worldAtmosphere fields must yield null UniverseAtmosphereSeed", parsed.universeAtmosphere)
        assertTrue("No trigger topic may be literal 'null'", parsed.sensitiveTriggers.none { it.triggerTopic.equals("null", ignoreCase = true) })
        assertTrue("No trigger reaction may be literal 'null'", parsed.sensitiveTriggers.none { it.sourceDescription.equals("null", ignoreCase = true) })
        val ihanetTrig = parsed.sensitiveTriggers.firstOrNull { it.triggerTopic == "eski ihanet" }
        assertNotNull(ihanetTrig)
        assertEquals(1.2, ihanetTrig!!.intensityMultiplier, 1e-6)
        assertTrue(parsed.sensitiveTriggers.all { it.intensityMultiplier in 1.2..2.5 })
    }

    @Test
    fun baselineEmotion_turkishLocaleSafeProtocolKeysAndEnglishDominantNormalization() {
        // Uppercase English 'I' must NOT break under tr-TR locale (I -> ı)
        assertEquals("rival", BaselineEmotionEngine.normalizeRelType("RIVAL"))
        assertEquals("sibling", BaselineEmotionEngine.normalizeRelType("SIBLING"))
        assertEquals("friend", BaselineEmotionEngine.normalizeRelType("FRIEND"))
        assertEquals("authority", BaselineEmotionEngine.normalizeRelType("AUTHORITY"))
        assertEquals("anxious", BaselineEmotionEngine.normalizeAttachment("ANXIOUS"))
        assertEquals("avoidant", BaselineEmotionEngine.normalizeAttachment("AVOIDANT"))
        assertEquals("fearful_avoidant", BaselineEmotionEngine.normalizeAttachment("DISORGANIZED"))

        // English dominant emotions normalize cleanly to Turkish instead of dropping to "nötr"
        assertEquals("koruyucu", BaselineEmotionEngine.sanitizeDominantEmotion("protective", PrimaryEmotions()))
        assertEquals("şüpheci", BaselineEmotionEngine.sanitizeDominantEmotion("Suspicious", PrimaryEmotions()))

        // Canonical JSON serialization clamps trigger multiplier & normalizes dominant emotion
        val fallback = BaselineEmotionEngine.calculateDeterministicFallback("b1", "Aiden", "Sakin", "Ev")
        val mutated = fallback.copy(
            difficultyMultiplier = Double.NaN,
            baselineEmotionState = fallback.baselineEmotionState.copy(dominantEmotion = "protective"),
            sensitiveTriggers = listOf(
                SensitiveTriggerEntity(botId = "b1", triggerTopic = "sır", sourceDescription = "gerginlik", intensityMultiplier = 5.0)
            )
        )
        val canonicalObj = JSONObject(mutated.toCanonicalJson())
        assertEquals("koruyucu", canonicalObj.getString("dominantEmotion"))
        assertEquals(1.0, canonicalObj.getDouble("affectionDifficultyMultiplier"), 1e-6)
        val trigObj = canonicalObj.getJSONArray("sensitiveTriggers").getJSONObject(0)
        assertEquals(2.5, trigObj.getDouble("intensityMultiplier"), 1e-6)
    }

    // ── 3. TimePerceptionEngine: Retrospective guards, leap years, month boundary & JSON null ──

    @Test
    fun timePerception_retrospectivePastReferencesNeverFalselyJumpCurrentClock() {
        val flashback1 = TimePerceptionEngine.parseStoryTimeJump("5 yıl önce tanışmıştık, ertesi sabah şehirden ayrılmıştık.")
        assertEquals(0, flashback1.daysToAdvance)
        assertNull(flashback1.newSceneTime)

        val flashback2 = TimePerceptionEngine.parseStoryTimeJump("3 gün önce olmuştu her şey, hatırladı ve derin bir nefes aldı.")
        assertEquals(0, flashback2.daysToAdvance)

        val flashback3 = TimePerceptionEngine.parseStoryTimeJump("Eskiden burada yaşardık, ertesi gün yine sahile inerdik.")
        assertEquals(0, flashback3.daysToAdvance)

        // Genuine forward jumps remain unaffected (including ASCII 'gun')
        val forwardMorning = TimePerceptionEngine.parseStoryTimeJump("Ertesi sabah güneş odayı aydınlattı.")
        assertEquals(1, forwardMorning.daysToAdvance)
        assertEquals("sabah", forwardMorning.newSceneTime)

        val forwardAsciiDay = TimePerceptionEngine.parseStoryTimeJump("3 gun sonra limanda tekrar buluştular.")
        assertEquals(3, forwardAsciiDay.daysToAdvance)
    }

    @Test
    fun timePerception_strictCalendarLeapYearAndMonthBoundaryArithmetic() {
        // Month-boundary overshoot regression: 15 days before 2024-03-01 is 2 weeks 1 day, NOT 1 month!
        assertEquals("2 hafta 1 gün", TimePerceptionEngine.formatStoryDuration(15L, "2024-03-01"))
        assertEquals("1 ay", TimePerceptionEngine.formatStoryDuration(29L, "2024-03-01"))
        assertEquals("1 yıl", TimePerceptionEngine.formatStoryDuration(366L, "2024-03-01"))

        // Leap day advance & birthday boundary
        val (leapDate, leapDayCounter, leapAge) = TimePerceptionEngine.advanceCalendarDateAndCheckAge(
            currentCalendarDateStr = "2024-02-28",
            currentDayCounter = 1L,
            elapsedDaysToAdd = 1,
            birthDateStr = "2000-02-29",
            initialAge = 23
        )
        assertEquals("2024-02-29", leapDate)
        assertEquals(2L, leapDayCounter)
        assertEquals(24, leapAge)

        // Invalid non-leap calendar date (2025-02-29) must NOT silently roll over to 2025-03-01
        val (invalidDate, _, _) = TimePerceptionEngine.advanceCalendarDateAndCheckAge(
            currentCalendarDateStr = "2025-02-29",
            currentDayCounter = 1L,
            elapsedDaysToAdd = 1,
            birthDateStr = "",
            initialAge = 20
        )
        assertEquals("", invalidDate)
    }

    @Test
    fun timePerception_parseHybridSkipIgnoresJsonNullAndLiteralNull() {
        val legacyWithNulls = JSONObject("""{"detected": true, "days": 0, "amount": null, "scene_time": null}""")
        val res1 = TimePerceptionEngine.parseHybridSkip(
            rawSkipJson = JSONObject.NULL,
            legacyStorySkipObj = legacyWithNulls,
            narrativeText = "Sakin bir akşam yemeği yediler."
        )
        assertEquals(0, res1.daysToAdvance)
        assertNull(res1.newSceneTime)

        val res2 = TimePerceptionEngine.parseHybridSkip(rawSkipJson = "null")
        assertEquals(0, res2.daysToAdvance)
        assertNull(res2.newSceneTime)
    }

    // ── 4. CastDetectorEngine: JSONObject.NULL, Turkish locale & CancellationException ──

    @Test
    fun castDetector_parseMicroAiCastJsonGuardsJsonNullAndFencedUpperJson() {
        val raw = """
            ```JSON
            {
              "cast": [
                {
                  "name": "Kadir",
                  "is_person": true,
                  "aliases": [null, "null", "yaşlı saatçi"],
                  "role": null,
                  "relation": null,
                  "age": null,
                  "in_scene": true
                },
                {
                  "name": null,
                  "is_person": true
                }
              ],
              "rejected": [null, "null", "Galata"]
            }
            ```
        """.trimIndent()

        val parsed = CastDetectorEngine.parseMicroAiCastResponse(raw)
        assertNotNull(parsed)
        assertEquals(1, parsed!!.cast.size)
        val kadir = parsed.cast.first()
        assertEquals("Kadir", kadir.name)
        assertEquals("Yan Karakter", kadir.role)
        assertEquals("", kadir.relation)
        assertEquals(listOf("yaşlı saatçi"), kadir.aliases)
        assertNull(kadir.age)
        assertEquals(listOf("Galata"), parsed.rejected)
    }

    @Test
    fun castDetector_allCapsEnglishNonNamesAndPunctuationRejectedUnderTurkishLocale() {
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("SHIELD"))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("CITY"))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("SILENCE"))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("OFFICE"))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("..."))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("---"))
        assertTrue(CastDetectorEngine.isGenericBackgroundExtra("KID"))
        assertTrue(CastDetectorEngine.isGenericBackgroundExtra("GIRL"))

        assertFalse(CastDetectorEngine.isNonPersonPlaceOrOrganization("İrem Kaya"))
        assertFalse(CastDetectorEngine.isNonPersonPlaceOrOrganization("Kadir"))
    }

    @Test
    fun castDetector_turkishAliasMergingAndCancellationPropagation() = runBlocking {
        assertTrue(CastDetectorEngine.isLikelyAlias("İrem", "irem"))
        assertTrue(CastDetectorEngine.isLikelyAlias("Irmak", "ırmak"))

        val mergedJson = CastDetectorEngine.mergeStoredAliases(
            existingMultiRoleJson = """["Yakın Dost","alias:İrem",null]""",
            newAliases = listOf("irem", "null", "Kızıl Ajan"),
            canonicalName = "İrem Kaya"
        )
        val aliases = CastDetectorEngine.extractStoredAliases(mergedJson)
        assertEquals("Turkish İ/i duplicate alias must not be stored twice", 2, aliases.size)
        assertTrue(aliases.contains("İrem"))
        assertTrue(aliases.contains("Kızıl Ajan"))
        assertFalse(aliases.any { it.equals("null", ignoreCase = true) })

        var cancelled = false
        try {
            CastDetectorEngine.extractAndValidateCastWithMicroAi(
                recentTwoMessagesText = "Yaşlı saatçi Kadir kapıyı açtı ve sessizce bize doğru yürüdü.",
                botName = "Aiden",
                userCharName = "Eren",
                forceMicroAi = true,
                utilityCaller = { _, _, _ -> throw CancellationException("micro-ai cancelled") }
            )
        } catch (_: CancellationException) {
            cancelled = true
        }
        assertTrue("CancellationException from utilityCaller must propagate", cancelled)
    }
}
