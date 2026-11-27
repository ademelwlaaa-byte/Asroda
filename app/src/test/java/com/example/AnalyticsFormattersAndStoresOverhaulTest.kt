package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.book.BookAudioPreferences
import com.example.book.BookChronicleExporter
import com.example.book.BookFatigueAlertHelper
import com.example.book.BookReadState
import com.example.book.StoryCompletionCertificateHelper
import com.example.data.local.MessageEntity
import com.example.data.managers.ProviderHealthState
import com.example.data.repository.ProviderFallbackLogEntry
import com.example.util.ApiDiagnosticsFormatter
import com.example.util.AppFeatureFlags
import com.example.util.ChapterSummaryExporter
import com.example.util.CharacterMentionTracker
import com.example.util.CharacterWordCloudHelper
import com.example.util.ChatTypographyStore
import com.example.util.CustomApiHeaderSanitizer
import com.example.util.MarkdownEmphasisCleaner
import com.example.util.NarrativeTextFormatter
import com.example.util.ProviderErrorCategoryClassifier
import com.example.util.ProviderRollingLatencyTracker
import com.example.util.QuickReplyChipsHelper
import com.example.util.ReadingStreakTracker
import com.example.util.StoryMoodAnalytics
import com.example.util.StoryPacingAnalyzer
import com.example.util.StoryQuoteExtractor
import com.example.util.StorySessionGoalStore
import com.example.util.UserMood
import com.example.util.UserSentimentAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AnalyticsFormattersAndStoresOverhaulTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        ProviderRollingLatencyTracker.clearAll()
    }

    @Test
    fun userSentimentAnalyzer_isolatesNegationAcrossClausesAndSupportsAsciiTurkish() {
        // Cross-clause negation must NEVER flip a positive clause after a comma or conjunction
        val res1 = UserSentimentAnalyzer.analyzeLocalDetailed("Sorun değil, çok mutluyum!")
        assertEquals(UserMood.POSITIVE, res1.mood)

        val res2 = UserSentimentAnalyzer.analyzeLocalDetailed("Çok mutluyum, hiç sorun yok!")
        assertEquals(UserMood.POSITIVE, res2.mood)

        val res3 = UserSentimentAnalyzer.analyzeLocalDetailed("Üzgünüm ama şimdi iyiyim, harikasın!")
        assertTrue(res3.valence >= 0f)

        // ASCII Turkish without diacritics must score accurately
        val asciiPos = UserSentimentAnalyzer.analyzeLocalDetailed("Bugun cok mutluyum, tesekkur ederim!")
        assertEquals(UserMood.POSITIVE, asciiPos.mood)

        val asciiNeg = UserSentimentAnalyzer.analyzeLocalDetailed("Cok uzgunum ve kirildim.")
        assertEquals(UserMood.NEGATIVE, asciiNeg.mood)

        val asciiNegatedPos = UserSentimentAnalyzer.analyzeLocalDetailed("Hic mutlu degilim.")
        assertEquals(UserMood.NEGATIVE, asciiNegatedPos.mood)

        val asciiNegatedNeg = UserSentimentAnalyzer.analyzeLocalDetailed("Kizgin degilim, merak etme.")
        assertEquals(UserMood.NEUTRAL, asciiNegatedNeg.mood)
    }

    @Test
    fun userSentimentAnalyzer_parsesTurkishTonesAndGuardsNonFiniteNumbers() {
        val turkishDto = UserSentimentAnalyzer.parseMicroAiSentimentResponse(
            """{"tone":"kırgın","valence":-0.6,"intensity":0.8,"subtext":"Gizli kırgınlık","resistance_delta":-0.05}"""
        )
        assertNotNull(turkishDto)
        assertEquals("hurt", turkishDto!!.tone)

        val unknownToneDto = UserSentimentAnalyzer.parseMicroAiSentimentResponse(
            """{"tone":"weird_unknown","valence":"NaN","intensity":"Infinity","resistance_delta":"-Infinity"}"""
        )
        assertNotNull(unknownToneDto)
        assertEquals("neutral", unknownToneDto!!.tone)
        assertTrue(unknownToneDto.valence.isFinite())
        assertTrue(unknownToneDto.intensity.isFinite())
        assertTrue(unknownToneDto.resistanceDelta.isFinite())
    }

    @Test
    fun apiDiagnosticsFormatter_redactsSecretsFromProviderErrorMessages() {
        val rawErr = "HTTP 401 failed for https://generativelanguage.googleapis.com/v1?key=AIzaSyTestSecretKey1234567890 with Bearer sk-proj-1234567890abcdef1234567890"
        val logs = listOf(
            ProviderFallbackLogEntry(
                providerName = "Gemini",
                layer = 1,
                status = "FAILED",
                statusCode = 401,
                errorMessage = rawErr
            )
        )
        val report = ApiDiagnosticsFormatter.formatReport(
            appVersion = "1.0",
            healthMap = mapOf("gemini" to ProviderHealthState(providerKey = "gemini")),
            logs = logs
        )
        assertFalse("API key must be redacted", report.contains("AIzaSyTestSecretKey1234567890"))
        assertFalse("Bearer token must be redacted", report.contains("sk-proj-1234567890abcdef1234567890"))
    }

    @Test
    fun customApiHeaderSanitizer_blocksIllegalCharsHopByHopAndCaseDuplicates() {
        assertEquals("clean value", CustomApiHeaderSanitizer.sanitizeHeaderValue("clean\u0000\r\nvalue\u007Fğüşıöç"))
        val safe = CustomApiHeaderSanitizer.buildSafeHeaderMap(
            linkedMapOf(
                "Host" to "evil.example.com",
                "Content-Length" to "999",
                "Transfer-Encoding" to "chunked",
                "X-Custom-Key" to "first",
                "x-custom-key" to "second"
            )
        )
        assertFalse(safe.keys.any { it.equals("Host", ignoreCase = true) })
        assertFalse(safe.keys.any { it.equals("Content-Length", ignoreCase = true) })
        assertFalse(safe.keys.any { it.equals("Transfer-Encoding", ignoreCase = true) })
        assertEquals(1, safe.size)
        assertEquals("second", safe.values.first())
    }

    @Test
    fun providerErrorCategoryClassifier_handlesModernErrorSignalsAndTurkishTimeouts() {
        assertEquals(
            ProviderErrorCategoryClassifier.Category.QUOTA,
            ProviderErrorCategoryClassifier.classify(429, "RESOURCE_EXHAUSTED: third party quota reached")
        )
        assertEquals(
            ProviderErrorCategoryClassifier.Category.RATE_LIMIT,
            ProviderErrorCategoryClassifier.classify(null, "rate_limit_exceeded")
        )
        assertEquals(
            ProviderErrorCategoryClassifier.Category.TIMEOUT,
            ProviderErrorCategoryClassifier.classify(null, "İstek zaman aşımı nedeniyle iptal edildi")
        )
        assertEquals(
            ProviderErrorCategoryClassifier.Category.SERVER,
            ProviderErrorCategoryClassifier.classify(529, "Overloaded")
        )
        assertEquals(
            ProviderErrorCategoryClassifier.Category.NETWORK,
            ProviderErrorCategoryClassifier.classify(null, "SSL handshake terminated with EOF")
        )
    }

    @Test
    fun storyAnalyticsAndExporters_stripHiddenBlocksAndPreventSubstringFalsePositives() {
        val messages = listOf(
            MessageEntity(
                id = "m1",
                botId = "b1",
                role = "assistant",
                text = "<think>Should I ask a question? \"internal thought quote here\" saldır savaş</think>" +
                    "Köprüde birkaç başkan kanıt dosyasını inceledi. [[STATE_JSON:{\"emotion\":\"happy\",\"summary\":\"hidden state json text\"}]]"
            ),
            MessageEntity(
                id = "m2",
                botId = "b1",
                role = "user",
                text = "\"Benim söylediğim çok uzun ve etkileyici bir kullanıcı repliği!\""
            )
        )

        // Quote extractor must ignore <think> & [[STATE_JSON]] and must NOT attribute user quote to botDisplayName
        val bestQuote = StoryQuoteExtractor.extractBestQuote(messages, botDisplayName = "Aiden")
        assertNotNull(bestQuote)
        assertFalse(bestQuote!!.contains("internal thought"))
        assertFalse(bestQuote.contains("hidden state"))
        assertFalse("User quote must not be attributed to bot", bestQuote.contains("— Aiden"))

        // Mood analytics: "köprü", "birkaç", "kanıt" must NOT trigger romance/action; "dosya" triggers mystery
        val atmosphere = StoryMoodAnalytics.detectDominantAtmosphere(messages)
        assertEquals("Gizem & Entrika", atmosphere)

        // Pacing analyzer must ignore JSON quotes inside [[STATE_JSON]] and <think> words
        val pacing = StoryPacingAnalyzer.analyze(listOf(messages.first()))
        assertEquals(0, pacing.dialoguePercent)
        assertEquals(6, pacing.averageAssistantWords)

        // Word cloud must not index <think> or STATE_JSON tags
        val themes = CharacterWordCloudHelper.extractTopThemes(messages + messages)
        assertFalse(themes.any { it.contains("think") || it.contains("state") || it.contains("emotion") })

        // QuickReplyChipsHelper must strip <think> "?" and not match "köprü" as "öp"
        val chips = QuickReplyChipsHelper.suggestStarters(messages.first().text)
        assertFalse(chips.any { it.contains("neden şimdi soruyorsun") })
        assertFalse(chips.any { it.contains("elini tutup") })

        // ChapterSummaryExporter must not leak <think> or [[STATE_JSON]]
        val recap = ChapterSummaryExporter.formatRecapMarkdown("Aiden", "Sen", messages)
        assertFalse(recap.contains("<think>"))
        assertFalse(recap.contains("STATE_JSON"))
    }

    @Test
    fun characterMentionTracker_enforcesBoundariesForShortNamesAndStripsHiddenBlocks() {
        val messages = listOf(
            MessageEntity(
                id = "1",
                botId = "b1",
                role = "assistant",
                text = "<think>Ali hidden</think> Kalite ve halinden memnun olan Ali'nin sesi yankılandı. Ali gülümsedi."
            )
        )
        val mentions = CharacterMentionTracker.countMentions(messages, listOf("Ali"))
        assertEquals(1, mentions.size)
        assertEquals(2, mentions.first().count)
    }

    @Test
    fun narrativeTextFormatterAndMarkdownCleaner_preserveAbbreviationsAndCodeFences() {
        val longWall = "Dr. Ahmet Yılmaz uzun bir koridordan sessizce geçti ve odadaki haritaları inceledi. " +
            "Masadaki eski notlar gece boyunca toplanan bütün gizemli izleri bir araya getiriyordu. " +
            "Prof. Selin kapıdan içeri girdiğinde herkes bir anlığına nefesini tuttu ve belgeleri gizledi. " +
            "Pencereden sızan yağmur damlaları şehrin gürültüsünü bastırırken karar vakti hızla yaklaşıyordu."
        val formatted = NarrativeTextFormatter.formatWallOfText(longWall)
        assertTrue(formatted.contains("Dr. Ahmet"))
        assertTrue(formatted.contains("Prof. Selin"))
        assertFalse("Must not split after Dr.", formatted.contains("Dr.\n\nAhmet"))
        assertFalse("Must not split after Prof.", formatted.contains("Prof.\n\nSelin"))

        val withFence = "```kotlin\nval x = \"\"raw\"\"\nval y = a * b *\n```\n* güzel sahne *"
        val cleaned = MarkdownEmphasisCleaner.clean(withFence)
        assertTrue(cleaned.contains("val x = \"\"raw\"\""))
        assertTrue(cleaned.contains("*güzel sahne*"))
    }

    @Test
    fun storesAndBookHelpers_guardNaNClockRollbackAndBounds() {
        // NaN protection in float stores
        assertEquals(1.0f, ChatTypographyStore.saveFontScale(context, Float.NaN), 0.001f)
        assertEquals(1.0f, ChatTypographyStore.loadFontScale(context), 0.001f)
        assertEquals(0.85f, BookAudioPreferences.saveMusicVolume(context, Float.NaN), 0.001f)
        assertEquals(0.85f, BookAudioPreferences.loadMusicVolume(context), 0.001f)

        // ReadingStreakTracker clock-rollback safety & consecutive calendar days
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(2026, Calendar.MARCH, 28, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val day1 = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val day2 = cal.timeInMillis

        val s1 = ReadingStreakTracker.recordActivity(context, day1)
        assertEquals(1, s1.currentStreakDays)
        val s2 = ReadingStreakTracker.recordActivity(context, day2)
        assertEquals(2, s2.currentStreakDays)
        // Clock rolls back to day1 -> streak must stay intact at 2
        val sRollback = ReadingStreakTracker.recordActivity(context, day1)
        assertEquals(2, sRollback.currentStreakDays)

        // StorySessionGoalStore basic safety
        val g1 = StorySessionGoalStore.setDailyGoal(context, 20, day2)
        assertEquals(20, g1.dailyGoal)
        val g2 = StorySessionGoalStore.recordTurn(context, day2)
        assertEquals(1, g2.sentToday)

        // Book helpers bounds & chronicle
        assertEquals("🚨 Kritik Yorgunluk (100)", BookFatigueAlertHelper.fatigueBadge(150))
        assertEquals("⚡ Dinç (0)", BookFatigueAlertHelper.fatigueBadge(-10))
        val state = BookReadState(
            bookId = "cosmic_drift_trilogy_v1",
            activeChapter = 24,
            currentNodeId = "ch24_start",
            transcript = emptyList(),
            chapterDecisions = emptyMap(),
            bonds = emptyMap(),
            fatigue = 20,
            mystery = 45,
            flags = emptyMap(),
            discoveredSecrets = emptySet(),
            discoveredBranches = emptySet(),
            completedChapters = setOf(1, 2, 999),
            isChapterCompleted = false
        )
        val dossier = BookChronicleExporter.formatDossierMarkdown(state)
        assertTrue(dossier.contains("2 / 24"))
        val cert = StoryCompletionCertificateHelper.buildCertificate(state, nowMs = day2)
        assertTrue(cert.contains("KOZMİK SÜRÜKLENİŞ"))

        // ProviderRollingLatencyTracker bounded key cap
        for (i in 1..80) {
            ProviderRollingLatencyTracker.recordSample("provider_$i", 100L + i)
        }
        assertNull(ProviderRollingLatencyTracker.getMetrics("   "))
    }
}
