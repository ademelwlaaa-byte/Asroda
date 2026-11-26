package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.LorebookEntryEntity
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactEntity
import com.example.data.managers.LorebookManager
import com.example.data.repository.CastDetectorEngine
import com.example.data.repository.EmochiRepository
import com.example.data.repository.EmotionEngine
import com.example.data.repository.LivingMemoryCodec
import com.example.data.repository.LivingMemoryPolicy
import com.example.data.repository.MemoryTier
import com.example.data.repository.MicroAiCastMemberDto
import com.example.data.repository.MicroAiCastResponse
import com.example.data.repository.TurkishLanguageGuard
import com.example.util.RepetitionDetector
import com.example.util.ResponseSanitizer
import com.example.util.StreamingTextSanitizer
import kotlinx.coroutines.runBlocking
import org.junit.After
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CoreChatMemoryCastOverhaulTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repo: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        repo.close()
        db.close()
    }

    private fun makeBot(botId: String = "bot_overhaul_test"): BotEntity = BotEntity(
        id = botId,
        mode = "personal",
        aiName = "Aiden",
        aiPersonality = "Kararlı, korumacı, keskin zekalı",
        scenario = "Manhattan çatısında yağmurlu bir gece",
        universeName = "",
        keyCharactersJson = "[]",
        userCharName = "Eren",
        userCharDesc = "Stratejist",
        openingMessage = "Hazır mısın?",
        writingStyle = "novel",
        intensity = "normal",
        storyDayCounter = 3L,
        storyCalendarDate = "2026-09-12",
        sceneTime = "gece"
    )

    // ── 1. TurkishLanguageGuard (0% English Leakage, 100% Proper-Noun Safe) ──

    @Test
    fun test1_turkishLanguageGuardBlocksEnglishAndAllowsForeignProperNounsInTurkish() {
        // Pure English & mixed English prose must be caught
        assertTrue(TurkishLanguageGuard.isEnglishDominant("She looked into his eyes and realized she could trust him."))
        assertTrue(TurkishLanguageGuard.isEnglishDominant("Silent Trust"))
        assertTrue(TurkishLanguageGuard.isEnglishDominant("Emotional Moment"))
        assertTrue(
            TurkishLanguageGuard.isEnglishDominant(
                "Çatıda yağmur altında konuştuk. He finally revealed his hidden secret to her."
            )
        )

        // Authentic Turkish sentences containing English character/place proper nouns MUST pass!
        assertFalse(
            TurkishLanguageGuard.isEnglishDominant(
                "Tony Stark ile Manhattan çatısında buluştuk ve S.H.I.E.L.D. dosyasındaki sırrı açığa çıkardık."
            )
        )
        assertFalse(
            TurkishLanguageGuard.isEnglishDominant(
                "Aiden Blackwood, Nevermore Akademisi'ndeki yemini bozmayacağına söz verdi."
            )
        )
        assertFalse(TurkishLanguageGuard.isEnglishDominant("Çatıdaki Büyük Yemin"))

        // Auto-translation of leaked English tags, keys, and relations
        assertEquals("Derin Güven", TurkishLanguageGuard.normalizeTurkishEmotionTag("Deep Trust"))
        assertEquals("Güven ve Rahatlama", TurkishLanguageGuard.normalizeTurkishEmotionTag("Trust & Relief"))
        assertEquals("Konum", TurkishLanguageGuard.normalizeTurkishFactKey("Location", "Manhattan"))
        assertEquals("Meslek", TurkishLanguageGuard.normalizeTurkishFactKey("Occupation", "Dedektif"))
        assertEquals("kardeşi", TurkishLanguageGuard.normalizeTurkishRelation("brother"))
        assertEquals("akıl hocası", TurkishLanguageGuard.normalizeTurkishRelation("mentor"))
    }

    // ── 2. LivingMemoryPolicy Anti-Spam & Consolidation ──────────────────────

    @Test
    fun test2_livingMemoryAntiSpamCooldownSalienceAndConsolidation() = runBlocking {
        val botId = "bot_living_mem_pacing"
        repo.saveBot(makeBot(botId))

        // 1. Routine salience 6 must be rejected
        val routineRejected = repo.commitLivingMemory(
            botId = botId,
            povCharacter = "Aiden",
            title = "Kahve Molası",
            emotionTag = "Sakinlik",
            salience = 6,
            narrative = "Mutfak masasında oturup kahve içerken hava durumundan ve yarının planından kısaca konuştuk.",
            innerThought = "",
            storyDayIndex = 3L,
            currentMessageCount = 10
        )
        assertFalse("Salience 6 must be rejected (minimum is 7)", routineRejected)

        // 2. First real turning point (salience 8) at message 10 commits cleanly
        val firstCommitted = repo.commitLivingMemory(
            botId = botId,
            povCharacter = "Aiden",
            title = "Çatıdaki Büyük Yemin",
            emotionTag = "Deep Trust", // auto-normalized to Turkish!
            salience = 8,
            narrative = "Yağmur altında sırtımızı birbirimize yaslayıp ne olursa olsun ihanet etmeyeceğimize yemin ettik.",
            innerThought = "İlk kez birine hayatımı gözümü kırpmadan emanet ediyorum.",
            storyDayIndex = 3L,
            currentMessageCount = 10
        )
        assertTrue(firstCommitted)
        val afterFirst = db.memoryEventDao().getActiveEvents(botId)
        assertEquals(1, afterFirst.size)
        val parsedFirst = LivingMemoryCodec.parse(afterFirst.first().description)
        assertEquals("Derin Güven", parsedFirst.emotionTag)

        // 3. Unrelated second salience-8 memory only 2 messages later (msg 12) MUST be blocked by 6-message cooldown!
        val spamBlocked = repo.commitLivingMemory(
            botId = botId,
            povCharacter = "Eren",
            title = "Laboratuvar İpucu",
            emotionTag = "Merak",
            salience = 8,
            narrative = "Eski kasadan çıkardığımız şifreli belgede bambaşka bir deneyin koordinatlarını fark ettim.",
            innerThought = "Bu iz bizi doğrudan başlangıca götürecek.",
            storyDayIndex = 3L,
            currentMessageCount = 12
        )
        assertFalse("Second memory within 6-message cooldown must be blocked", spamBlocked)
        assertEquals(1, db.memoryEventDao().getActiveEvents(botId).size)

        // 4. Same-day, same-POV similar memory consolidates/enriches existing entry instead of creating a duplicate!
        val consolidated = repo.commitLivingMemory(
            botId = botId,
            povCharacter = "Aiden",
            title = "Çatıdaki Büyük Yemin ve Sır",
            emotionTag = "Derin Güven",
            salience = 9,
            narrative = "Yağmur altında sırtımızı birbirimize yaslayıp ne olursa olsun ihanet etmeyeceğimize ve gizli anahtarı birlikte koruyacağımıza yemin ettik.",
            innerThought = "Artık bu yükü tek başıma taşımıyorum.",
            storyDayIndex = 3L,
            currentMessageCount = 13
        )
        assertTrue("Similar same-day same-POV memory should enrich existing memory in place", consolidated)
        assertEquals("Must remain 1 consolidated Living Memory, never 2 duplicates", 1, db.memoryEventDao().getActiveEvents(botId).size)

        // 5. Landmark climax (salience 9) allowed after 4-message gap (13 -> 18) even within realtime window
        val climaxAllowed = repo.commitLivingMemory(
            botId = botId,
            povCharacter = "Eren",
            title = "Büyük İhanetin Ortaya Çıkışı",
            emotionTag = "Şaşkınlık",
            salience = 9,
            narrative = "Konsey kayıtlarını açtığımızda yıllardır bize yardım ettiğini sandığımız kişinin asıl düşman olduğunu gördüm.",
            innerThought = "Bütün planı en baştan kurmak zorundayız.",
            storyDayIndex = 3L,
            currentMessageCount = 18
        )
        assertTrue("Salience 9 landmark event after 4+ message gap must commit", climaxAllowed)
        assertEquals(2, db.memoryEventDao().getActiveEvents(botId).size)

        // 6. Pure gate check for per-story-day quota (4 events on same day blocks non-landmark salience 8)
        val dayQuotaGate = LivingMemoryPolicy.evaluateGate(
            salience = 8,
            currentMessageCount = 40,
            lastMemoryMessageCount = 20,
            nowMs = 500_000L,
            latestEventTimestampMs = 100_000L,
            sameStoryDayEventCount = 4
        )
        assertFalse("Per-story-day soft quota must throttle non-landmark spam", dayQuotaGate.allowed)
    }

    // ── 3. Memory Self-Healing Consolidation ─────────────────────────────────

    @Test
    fun test3_consolidateAndCleanBotMemoriesRemovesLegacyEnglishAndMergesDuplicates() = runBlocking {
        val botId = "bot_mem_heal"
        repo.saveBot(makeBot(botId))

        // Insert legacy polluted English event + duplicate Turkish events + English fact
        db.memoryEventDao().insertEvent(
            MemoryEventEntity(
                botId = botId,
                timestamp = 1000L,
                description = LivingMemoryCodec.encode(
                    povCharacter = "Aiden",
                    title = "Quiet Promise",
                    emotionTag = "Trust",
                    salience = 8,
                    narrative = "We stood on the cold rooftop and promised never to leave each other behind.",
                    innerThought = "I finally feel safe."
                ),
                importanceScore = 80,
                isBotSaved = true
            )
        )
        db.memoryEventDao().insertEvent(
            MemoryEventEntity(
                botId = botId,
                timestamp = 2000L,
                description = LivingMemoryCodec.encode(
                    povCharacter = "Aiden",
                    title = "Gizli Anahtar Sözü",
                    emotionTag = "Güven",
                    salience = 9,
                    narrative = "Yeraltı sarnıcında gizli anahtarı koruyacağımıza söz verdik ve eski defteri mühürledik.",
                    innerThought = "Bu sır ikimizin arasında kalacak."
                ),
                importanceScore = 90,
                isBotSaved = true
            )
        )
        db.memoryEventDao().insertEvent(
            MemoryEventEntity(
                botId = botId,
                timestamp = 1500L,
                description = LivingMemoryCodec.encode(
                    povCharacter = "Aiden",
                    title = "Gizli Anahtar Sözü",
                    emotionTag = "Güven",
                    salience = 7,
                    narrative = "Yeraltı sarnıcında gizli anahtarı koruyacağımıza söz verdik ve eski defteri mühürledik.",
                    innerThought = ""
                ),
                importanceScore = 70,
                isBotSaved = true
            )
        )
        db.memoryFactDao().insertFact(
            MemoryFactEntity(
                botId = botId,
                subject = "Eren",
                key = "Location",
                value = "İstanbul Boğazı kıyısındaki güvenli evde kalıyor.",
                confidence = "certain",
                lastConfirmedAt = 2000L,
                isBotSaved = true
            )
        )

        val report = repo.consolidateAndCleanBotMemories(botId)
        assertEquals(1, report.removedEnglishSpamCount)
        assertEquals(1, report.mergedDuplicateEventsCount)
        assertEquals(1, report.normalizedLabelsCount)

        val remainingEvents = db.memoryEventDao().getActiveEvents(botId)
        assertEquals(1, remainingEvents.size)
        assertTrue(remainingEvents.first().description.contains("Gizli Anahtar"))

        val remainingFacts = db.memoryFactDao().getActiveFacts(botId)
        assertEquals(1, remainingFacts.size)
        assertEquals("Konum", remainingFacts.first().key)
    }

    // ── 4. Auto-Cast Overhaul: Non-Person Filtering, Turkish Roles, Alias Preservation & Full-Name Promotion ──

    @Test
    fun test4_autoCastRejectsPlacesNormalizesRolesPromotesFullNameAndPreservesAliases() = runBlocking {
        val botId = "bot_cast_overhaul"
        val bot = makeBot(botId)
        repo.saveBot(bot)

        // Places, buildings, organizations, acronyms & English non-names MUST be rejected
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("Galata Kulesi"))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("Nevermore Akademisi"))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("Stark Tower"))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("Kurucular Konseyi"))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("S.H.I.E.L.D."))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("However"))
        assertTrue(CastDetectorEngine.isNonPersonPlaceOrOrganization("Denek 7-A"))
        assertFalse(CastDetectorEngine.isNonPersonPlaceOrOrganization("Natasha Romanoff"))
        assertFalse(CastDetectorEngine.isNonPersonPlaceOrOrganization("Kadir"))

        // English roles & relations normalize cleanly to Turkish
        val sanitized = CastDetectorEngine.sanitizeMicroAiCastResponse(
            response = MicroAiCastResponse(
                cast = listOf(
                    MicroAiCastMemberDto(
                        name = "Natasha",
                        isPerson = true,
                        aliases = listOf("Kızıl Ajan"),
                        role = "Agent",
                        relation = "ally",
                        inScene = true
                    ),
                    MicroAiCastMemberDto(
                        name = "Natasha Romanoff",
                        isPerson = true,
                        aliases = emptyList(),
                        role = "Side Character",
                        relation = "close friend",
                        inScene = true
                    ),
                    MicroAiCastMemberDto(
                        name = "Stark Tower",
                        isPerson = true,
                        role = "Location",
                        inScene = true
                    )
                )
            ),
            botName = bot.aiName,
            userCharName = bot.userCharName
        )
        assertEquals("Duplicate Natasha + Natasha Romanoff must merge into 1, Stark Tower rejected", 1, sanitized.members.size)
        assertEquals("Natasha Romanoff", sanitized.members.first().name)
        assertEquals("Ajan", sanitized.members.first().role)
        assertEquals("müttefiki", sanitized.members.first().relation)
        assertTrue(sanitized.members.first().aliases.contains("Natasha"))

        // DB test: register "Natasha" first, then promote to "Natasha Romanoff" with emotion migration & alias preservation
        repo.managers.castMemberManager.ensureSideCharacterRegistered(
            botId = botId,
            rawCharacterName = "Natasha",
            role = "Agent",
            aliases = listOf("Kızıl Ajan"),
            bot = bot,
            confidence = 90,
            pendingConfirmation = false
        )
        assertNotNull(db.characterEmotionDao().getEmotionForCharacter(botId, "Natasha"))

        repo.managers.castMemberManager.ensureSideCharacterRegistered(
            botId = botId,
            rawCharacterName = "Natasha Romanoff",
            role = "Ajan",
            bot = bot,
            confidence = 92,
            pendingConfirmation = false
        )
        val castList = repo.managers.castMemberManager.getCastMembersForBot(botId)
        assertEquals(1, castList.size)
        assertEquals("Natasha Romanoff", castList.first().name)
        val storedAliases = CastDetectorEngine.extractStoredAliases(castList.first().multiRoleJson)
        assertTrue("Original short name must be kept as alias", storedAliases.contains("Natasha"))
        assertTrue("Previous alias must be kept", storedAliases.contains("Kızıl Ajan"))
        assertNotNull("Emotion must migrate to promoted canonical name", db.characterEmotionDao().getEmotionForCharacter(botId, "Natasha Romanoff"))
        assertNull("Old orphan emotion must be cleaned", db.characterEmotionDao().getEmotionForCharacter(botId, "Natasha"))

        // Regression test: reEvaluateCastMemberRolesIfNeeded MUST preserve stored aliases in multiRoleJson!
        repo.managers.castMemberManager.reEvaluateCastMemberRolesIfNeeded(
            botId = botId,
            currentMessageCount = 30,
            sceneContext = "Natasha Romanoff sakin bir sesle strateji dosyasını açtı ve ekibe talimat verdi."
        )
        val afterReEval = repo.managers.castMemberManager.getCastMembersForBot(botId).first()
        val aliasesAfterReEval = CastDetectorEngine.extractStoredAliases(afterReEval.multiRoleJson)
        assertTrue("Aliases MUST NOT be wiped on periodic role re-evaluation", aliasesAfterReEval.contains("Natasha"))
        assertTrue("Aliases MUST NOT be wiped on periodic role re-evaluation", aliasesAfterReEval.contains("Kızıl Ajan"))
    }

    // ── 5. Chat Pipeline: Lorebook, Emotion Labels, Repetition & Sanitizers ──

    @Test
    fun test5_lorebookSofteningExclusionsEmotionNormalizationAndSanitizers() {
        // Lorebook: Turkish consonant softening ("kitap" -> "kitabı", "kılıç" -> "kılıcını") & negative exclusions ("göz, -gözlük")
        val entries = listOf(
            LorebookEntryEntity(
                id = "e1",
                lorebookId = "lb1",
                title = "Kadim Kitap",
                keywords = "kitap, kılıç",
                content = "Kadim kitap mahzende saklanır.",
                priority = 8
            ),
            LorebookEntryEntity(
                id = "e2",
                lorebookId = "lb1",
                title = "Kızıl Göz Gücü",
                keywords = "göz, -gözlük",
                content = "Kızıl gözler zihin kontrolünü açar.",
                priority = 9
            )
        )
        val resSoftening = LorebookManager.selectEntriesForContext(
            entries = entries,
            recentMessageTexts = listOf("Eski kitabı açıp gözlüğünü taktı.")
        )
        assertTrue("Softened 'kitabı' must match keyword 'kitap'", resSoftening.selected.any { it.entry.id == "e1" })
        assertFalse("Negative exclusion '-gözlük' must block false trigger on 'gözlüğünü'", resSoftening.selected.any { it.entry.id == "e2" })

        // EmotionEngine Turkish label normalization
        assertEquals("şüpheci", EmotionEngine.normalizeTurkishEmotionLabel("suspicious"))
        assertEquals("koruyucu", EmotionEngine.normalizeTurkishEmotionLabel("Protective"))
        assertEquals("rahatlamış", EmotionEngine.normalizeTurkishEmotionLabel("relieved"))
        assertEquals("huzurlu", EmotionEngine.normalizeTurkishEmotionLabel("huzurlu"))

        // StreamingTextSanitizer: 0-char leakage on <thought>, <scratchpad>, [[MEMORY_SAVE]], [[CAST_MEMBER]]
        val sanitizer = StreamingTextSanitizer()
        val chunks = listOf(
            "<tho", "ught>gizli plan</thought>",
            "Merhaba ", "[fısıldayarak] gel.",
            " [[MEMORY_", "SAVE {\"x\":1}]]",
            " [[CAST_MEMBER: Ali | Dost]]"
        )
        chunks.forEach { sanitizer.append(it) }
        sanitizer.finish()
        assertEquals("Merhaba [fısıldayarak] gel.  ", sanitizer.visibleText)
        assertFalse(sanitizer.isThinking)

        // ResponseSanitizer: strips <thought>, stray [[STATE_JSON]], and AI: prefix
        val cleanResp = ResponseSanitizer.sanitizeForDisplay(
            "<thought>internal</thought>\nAI: *Yağmur altında durdu.* \"Hazırım.\" [[STATE_JSON {\"dom\":\"sakin\"}]]"
        )
        assertFalse(cleanResp.contains("internal"))
        assertFalse(cleanResp.contains("STATE_JSON"))
        assertFalse(cleanResp.startsWith("AI:"))
        assertTrue(cleanResp.contains("Hazırım."))

        // RepetitionDetector: catches repetitive 2-word/3-word opening clichés & Turkish-stemmed loops
        val loops = RepetitionDetector.detectLoopingPhrases(
            listOf(
                "Gözlerini kıstı ve sessizce pencereden dışarı baktı.",
                "Gözlerini kısarak yeniden pencereden dışarı baktı.",
                "Gözlerini kısıyor, yine pencereden dışarı bakıyordu."
            )
        )
        assertTrue("RepetitionDetector must detect Turkish-stemmed opening/phrase loop", loops.isNotEmpty())
    }

    // ── 6. Wave 2: Knowledge Graph Alias+Bidirectional, Cast Audit, Context Window & Subtext Guard ──

    @Test
    fun test6_knowledgeGraphBidirectionalAndAliasResolutionContextWindowAndSubtextGuard() = runBlocking {
        val botId = "bot_wave2_test"
        val bot = makeBot(botId)
        repo.saveBot(bot)

        // 1. Knowledge Graph: stored alias resolution ("Kızıl Ajan" -> "Natasha Romanoff") & English predicate normalization
        repo.managers.castMemberManager.ensureSideCharacterRegistered(
            botId = botId,
            rawCharacterName = "Natasha Romanoff",
            role = "Ajan",
            aliases = listOf("Kızıl Ajan"),
            bot = bot,
            confidence = 95,
            pendingConfirmation = false
        )
        repo.upsertEntityRelation(botId, "Natasha Romanoff", "patronu", "Nick Fury", currentMessageIndex = 5)
        repo.upsertEntityRelation(botId, "Ahmet", "brother", "Mehmet", currentMessageIndex = 6)

        val cast = repo.managers.castMemberManager.getCastMembersForBot(botId)
        val aliasGraphHit = repo.detectAndResolveGraphQuery(botId, "Kızıl Ajan'ın patronu kim?", cast)
        assertNotNull("Stored multiRoleJson alias must resolve in Knowledge Graph queries", aliasGraphHit)
        assertEquals("Nick Fury", aliasGraphHit?.finalTargetName)

        // 2. Bidirectional symmetric traversal ("Mehmet'in kardeşi kim?" even though stored Ahmet -> Mehmet)
        val reverseHit = repo.detectAndResolveGraphQuery(botId, "Mehmet'in kardeşi kim?", cast)
        assertNotNull("Symmetric relation 'kardeşi' must traverse in reverse too", reverseHit)
        assertEquals("Ahmet", reverseHit?.finalTargetName)

        // 3. Cast self-healing audit: removes places/extras, merges alias duplicates, fixes English roles
        db.castMemberDao().insertCastMember(
            CastMemberEntity(botId = botId, name = "Stark Tower", description = "", role = "Location", isAutoAdded = true)
        )
        db.castMemberDao().insertCastMember(
            CastMemberEntity(botId = botId, name = "Natasha", description = "", role = "Friend", isAutoAdded = true)
        )
        db.castMemberDao().insertCastMember(
            CastMemberEntity(botId = botId, name = "Kadir", description = "", role = "Mentor", isAutoAdded = true)
        )
        val cleanup = repo.managers.castMemberManager.auditAndCleanCastForBot(botId)
        assertEquals(1, cleanup.removedFalsePositives)
        assertEquals(1, cleanup.mergedDuplicates)
        assertEquals(1, cleanup.normalizedTurkishRoles)
        val cleanCast = repo.managers.castMemberManager.getCastMembersForBot(botId)
        assertEquals(2, cleanCast.size)
        assertTrue(cleanCast.any { it.name == "Kadir" && it.role == "Akıl Hocası" })

        // 4. Modern model context window detection & sliding window token trimming
        assertEquals(200_000, repo.getModelContextLimit("claude-sonnet-4-20250514"))
        assertEquals(1_000_000, repo.getModelContextLimit("gpt-4.1"))
        assertEquals(128_000, repo.getModelContextLimit("qwen3-235b"))
        val bigMessages = (1..12).map { i ->
            com.example.data.local.MessageEntity(
                id = "m$i",
                botId = botId,
                role = if (i % 2 == 1) "user" else "assistant",
                text = "Uzun roman paragrafı ".repeat(80),
                timestamp = i * 1000L
            )
        }
        val trimmed = repo.trimMessagesToTokenBudget(bigMessages, "Sistem promptu", maxBudgetTokens = 3000, minKeepMessages = 6)
        assertTrue("Sliding window must trim oldest messages under tight budget", trimmed.size in 6..10)
        assertEquals("m12", trimmed.last().id)

        // 5. UserSentimentAnalyzer subtext Turkish purity guard
        val local = com.example.util.UserSentimentAnalyzer.analyzeLocalDetailed("Neyse boşver, sen her zaman haklısın...")
        val englishSubtextBlended = com.example.util.UserSentimentAnalyzer.blendLocalAndMicroAi(
            local = local,
            ai = com.example.util.MicroAiSentimentDto(
                tone = "hurt",
                valence = -0.6f,
                intensity = 0.7f,
                subtext = "The user feels hurt and is hiding their disappointment behind sarcasm."
            )
        )
        assertEquals("English subtext leak must be dropped cleanly", "", englishSubtextBlended.subtext)

        val turkishSubtextBlended = com.example.util.UserSentimentAnalyzer.blendLocalAndMicroAi(
            local = local,
            ai = com.example.util.MicroAiSentimentDto(
                tone = "hurt",
                valence = -0.6f,
                intensity = 0.7f,
                subtext = "Kullanıcı kırgınlığını alaycı bir tonun arkasına saklıyor."
            )
        )
        assertTrue(turkishSubtextBlended.subtext.contains("kırgınlığını"))
    }
}
