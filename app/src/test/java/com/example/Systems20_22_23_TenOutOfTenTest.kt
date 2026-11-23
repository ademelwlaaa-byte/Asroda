package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.ModelResponseResult
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.EmotionState
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MessageEntity
import com.example.data.local.PrimaryEmotions
import com.example.data.local.RelationshipAxes
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.ProviderFallbackManager
import com.example.data.managers.ProviderHealthTracker
import com.example.data.repository.EmochiRepository
import com.example.data.repository.SystemPromptBuilder
import com.example.util.AppFeatureFlags
import com.example.util.SseStreamParser
import com.example.util.StreamingTextSanitizer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Systems20_22_23_TenOutOfTenTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: EmochiRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppFeatureFlags.init(context)
        AppFeatureFlags.setSseStreaming(true)
        AppFeatureFlags.setStrictApiTimeout(false)
        ProviderHealthTracker.resetAll()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        ProviderHealthTracker.resetAll()
        AppFeatureFlags.setSseStreaming(true)
        AppFeatureFlags.setStrictApiTimeout(false)
        db.close()
    }

    private fun makeBot(
        id: String = "bot_s202223",
        storyDayCounter: Long = 5L,
        storyCalendarDate: String = "18 Kasım",
        emotionState: String = EmotionState(
            primaryEmotions = PrimaryEmotions(trust = 40, anger = 50),
            relationshipAxes = RelationshipAxes(affectionScore = 42, resentmentScore = 48),
            dominantEmotion = "temkinli"
        ).toJson()
    ): BotEntity = BotEntity(
        id = id,
        mode = "personal",
        aiName = "Aiden",
        aiPersonality = "Soğukkanlı, ketum ve sadık bir dedektif.",
        scenario = "Yağmurlu gecede gizemli bir soruşturma yürütüyoruz.",
        universeName = "",
        keyCharactersJson = "[]",
        userCharName = "Selin",
        userCharDesc = "Araştırmacı gazeteci.",
        openingMessage = "Yağmur dinmeden bu dosyayı çözmeliyiz.",
        writingStyle = "rp",
        intensity = "normal",
        emotionState = emotionState,
        emotionalRegulationCapacity = 50,
        storyDayCounter = storyDayCounter,
        storyCalendarDate = storyCalendarDate
    )

    // =========================================================================
    // TEST 1 (#20): Çok Adımlı Bilgi Grafiği & Döngü Koruması
    // =========================================================================
    @Test
    fun test1_system20_multiHopKnowledgeGraphAndCycleSafety() = runBlocking {
        val bot = makeBot(id = "bot_graph_multihop")
        repository.saveBot(bot)

        // 2-hop / 3-hop zincir: Aiden -> kardeşi -> Mira -> patronu -> Vaelor -> müttefiki -> Kael
        repository.upsertEntityRelation(bot.id, "Aiden", "kardeşi", "Mira", currentMessageIndex = 1)
        repository.upsertEntityRelation(bot.id, "Mira", "patronu", "Vaelor", currentMessageIndex = 2)
        repository.upsertEntityRelation(bot.id, "Vaelor", "müttefiki", "Kael", currentMessageIndex = 3)

        val resolved = repository.detectAndResolveGraphQuery(
            botId = bot.id,
            query = "Aiden'ın kardeşinin patronu kim?",
            castMembers = listOf(CastMemberEntity(botId = bot.id, name = "Aiden", description = "Dedektif (Aiden Vance)"))
        )
        assertNotNull("Çok adımlı sorgu çözümlenmeli", resolved)
        assertTrue(
            "Zincir sonucu Vaelor'u ve ara düğüm Mira'yı içermeli: ${resolved?.promptExplanation}",
            resolved!!.promptExplanation.contains("Vaelor") && resolved.promptExplanation.contains("Mira")
        )

        // Döngü koruması: Alpha <-> Beta (depth <= 4, sonsuz döngüye girmeden güvenle sonlanmalı)
        repository.upsertEntityRelation(bot.id, "Alpha", "arkadaşı", "Beta", currentMessageIndex = 4)
        repository.upsertEntityRelation(bot.id, "Beta", "arkadaşı", "Alpha", currentMessageIndex = 5)

        val cyclicPath = repository.managers.worldStateManager.traverseRelationGraph(
            botId = bot.id,
            startEntity = "Alpha",
            relationSteps = listOf("arkadaşı", "arkadaşı", "arkadaşı", "arkadaşı", "arkadaşı")
        )
        assertNotNull("Döngü korumalı yürüyüş sonuç üretmeli", cyclicPath)
        assertTrue(
            "Döngüde aynı düğüm tekrar ziyaret edilmeden güvenle durmalı: ${cyclicPath?.visitedNodes}",
            cyclicPath!!.visitedNodes.size <= 3
        )
    }

    // =========================================================================
    // TEST 2 (#20): Canlı Enjeksiyon, Çelişki Güncellemesi & Rewind Temizliği
    // =========================================================================
    @Test
    fun test2_system20_livePromptInjectionContradictionAndRewind() = runBlocking {
        val bot = makeBot(id = "bot_graph_contradict")
        repository.saveBot(bot)

        // Mesaj geçmişi (rewind için 4 mesaj)
        val m1 = MessageEntity(id = "m1", botId = bot.id, role = "user", text = "Selam", timestamp = 1000L)
        val m2 = MessageEntity(id = "m2", botId = bot.id, role = "assistant", text = "Merhaba", timestamp = 2000L)
        val m3 = MessageEntity(id = "m3", botId = bot.id, role = "user", text = "Ayrıldık", timestamp = 3000L)
        val m4 = MessageEntity(id = "m4", botId = bot.id, role = "assistant", text = "Anladım", timestamp = 4000L)
        listOf(m1, m2, m3, m4).forEach { repository.saveMessage(it) }

        // Başlangıç ilişkisi (msg 1) -> sonra çelişen güncelleme (msg 2): sevgilisi -> eski sevgilisi
        repository.upsertEntityRelation(bot.id, "Aiden", "sevgilisi", "Elif", currentMessageIndex = 1)
        repository.upsertEntityRelation(bot.id, "Aiden", "eski sevgilisi", "Elif", currentMessageIndex = 2)
        // Rewind sonrası silinmesi gereken ileri tarihli kenar (msg 4)
        repository.upsertEntityRelation(bot.id, "Aiden", "düşmanı", "Dorian", currentMessageIndex = 4)

        val beforeRewind = repository.getEntityRelationsForBot(bot.id)
        assertFalse(
            "Çelişen eski 'sevgilisi' kenarı temizlenmiş olmalı",
            beforeRewind.any { it.relationType == "sevgilisi" && it.targetEntityName.equals("Elif", ignoreCase = true) }
        )
        assertTrue(
            "Yeni 'eski sevgilisi' kenarı aktif olmalı",
            beforeRewind.any { it.relationType == "eski sevgilisi" && it.targetEntityName.equals("Elif", ignoreCase = true) }
        )

        // SystemPromptBuilder'a [İLİŞKİ AĞI GERÇEĞİ] enjeksiyonu + relevantFacts tekilleştirme
        val resolution = repository.detectAndResolveGraphQuery(bot.id, "Aiden'ın eski sevgilisi kim?")
        assertNotNull("Eski sevgili sorgusu çözümlenmeli", resolution)
        val prompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = UserSettingsEntity(),
                graphResolutionNote = resolution!!.promptExplanation,
                relevantFacts = listOf(
                    MemoryFactEntity(
                        id = 1L,
                        botId = bot.id,
                        subject = "Aiden",
                        key = "eski sevgilisi",
                        value = "Aiden --[eski sevgilisi]--> Elif"
                    )
                )
            )
        )
        assertTrue(
            "SystemPromptBuilder çıktısında [İLİŞKİ AĞI GERÇEĞİ] bloğu olmalı",
            prompt.contains("[İLİŞKİ AĞI GERÇEĞİ]") && prompt.contains("Elif")
        )

        // Rewind m2'ye (2 mesaj) -> msg 4'te eklenen Dorian kenarı silinmeli, Elif kalmalı
        repository.rewindToMessage(bot.id, m2.id)
        val afterRewind = repository.getEntityRelationsForBot(bot.id)
        assertFalse("Rewind sonrası gelecek kenar (Dorian) silinmeli", afterRewind.any { it.targetEntityName == "Dorian" })
        assertTrue("Rewind öncesi kenar (Elif) korunmalı", afterRewind.any { it.targetEntityName == "Elif" })
    }

    // =========================================================================
    // TEST 3 (#22): Canlı Hassas Tetikleyici, OOC Koruması & Spam Cooldown
    // =========================================================================
    @Test
    fun test3_system22_liveSensitiveTriggerOocGuardAndSpamCooldown() = runBlocking {
        val bot = makeBot(id = "bot_triggers_live")
        repository.saveBot(bot)
        db.sensitiveTriggerDao().insertOrUpdate(
            SensitiveTriggerEntity(
                botId = bot.id,
                triggerTopic = "terk edilme",
                sourceDescription = "İçine kapanır ve savunmaya geçer.",
                intensityMultiplier = 2.0
            )
        )

        // 1. OOC mesajı ASLA tetiklememeli
        val oocEval = repository.evaluateSensitiveTriggersLive(
            bot = bot,
            userMessageText = "((Bu sahnede Aiden'ın terk edilme korkusunu hatırla))",
            currentMessageIndex = 3,
            isOocMeta = true
        )
        assertFalse("OOC mesajı tetikleyiciyi patlatmamalı", oocEval.matchedTriggers.isNotEmpty())

        // 2. Meta kelime sorusu tetiklememeli
        val metaEval = repository.evaluateSensitiveTriggersLive(
            bot = bot,
            userMessageText = "Terk edilme kelimesinin anlamı nedir?",
            currentMessageIndex = 4,
            isOocMeta = false
        )
        assertFalse("Zararsız sözlük/meta sorusu tetiklememeli", metaEval.matchedTriggers.isNotEmpty())

        // 3. Gerçek sahne içi dolaylı ima / yara teması -> canlı tetikleme + PendingReappraisal + prompt enjeksiyonu
        val liveEval = repository.evaluateSensitiveTriggersLive(
            bot = bot,
            userMessageText = "Zaten herkes gibi sen de bir gün çekip gideceksin, beni terk edeceksin.",
            currentMessageIndex = 5,
            isOocMeta = false
        )
        assertTrue("Hassas yara canlıda tetiklenmeli", liveEval.matchedTriggers.isNotEmpty())
        assertTrue(
            "Tetikleme yönergesi [HASSAS YARA TETİKLENDİ] içermeli",
            liveEval.promptDirective.contains("[HASSAS YARA TETİKLENDİ]")
        )

        val prompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = UserSettingsEntity(),
                sensitiveTriggerDirective = liveEval.promptDirective
            )
        )
        assertTrue("Canlı prompt [HASSAS YARA TETİKLENDİ] taşımalı", prompt.contains("[HASSAS YARA TETİKLENDİ]"))

        val pending = repository.getActivePendingReappraisalsForBot(bot.id)
        assertTrue("Tetikleme sonrası PendingReappraisalEntity oluşturulmalı", pending.isNotEmpty())

        // 4. Spam Cooldown: hemen 1 mesaj sonra aynı yara tekrar patlamamalı
        val cooldownEval = repository.evaluateSensitiveTriggersLive(
            bot = bot,
            userMessageText = "Yine beni terk edeceksin değil mi?",
            currentMessageIndex = 6,
            isOocMeta = false
        )
        assertFalse("2 mesajlık spam cooldown içinde tekrar tetiklenmemeli", cooldownEval.matchedTriggers.isNotEmpty())
    }

    // =========================================================================
    // TEST 4 (#22): Katı Kilitsiz Esnek Olgunlaşma (`reapp`) & 14+ Gün Bayatlama
    // =========================================================================
    @Test
    fun test4_system22_flexibleReappraisalWithout15MsgLockAndExpiration() = runBlocking {
        val bot = makeBot(id = "bot_reapp_flex", storyDayCounter = 5L)
        repository.saveBot(bot)

        // Mesaj 5'te yara kaydı aç (eski +15 mesaj kilidi olmadan!)
        repository.recordPendingReappraisalIfEligible(
            botId = bot.id,
            eventDescription = "Güvenini kıran ağır sözler",
            emotionStateJson = bot.emotionState,
            currentMessageIndex = 5,
            rawDelta = -20,
            storyDayCounter = 5L
        )

        // Yalnızca 2 mesaj sonra (totalMessageCount = 7) promptta aktif görünmeli!
        val activeAfter2Msgs = repository.getActivePendingReappraisalsForBot(bot.id)
        assertEquals("2 mesaj sonra açık olgunlaşma kaydı erişilebilir olmalı", 1, activeAfter2Msgs.size)
        val reappId = activeAfter2Msgs.first().id

        val promptAfter2Msgs = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = UserSettingsEntity(),
                pendingReappraisals = activeAfter2Msgs,
                totalMessageCount = 7
            )
        )
        assertTrue(
            "Prompt [BEKLEYEN DUYGUSAL OLGUNLAŞMA] ve [İÇSEL HESAPLAŞMA (AÇIK YARA #$reappId)] içermeli",
            promptAfter2Msgs.contains("[BEKLEYEN DUYGUSAL OLGUNLAŞMA]") &&
                promptAfter2Msgs.contains("[İÇSEL HESAPLAŞMA (AÇIK YARA #$reappId)]")
        )

        // Ana model [[STATE_JSON {"reapp":{"id":reappId,"out":"resolved"}}]] döndüğünde yara kapanıp duygular toparlanmalı
        val beforeEmo = EmotionState.fromJson(repository.getBot(bot.id)!!.emotionState)
        repository.parseAndApplyEmotionUpdates(
            botId = bot.id,
            rawResponse = "Derin bir nefes aldı ve gözlerine baktı. \"Özürünü kabul ediyorum.\" [[STATE_JSON {\"dom\":\"rahatlamış\",\"reapp\":{\"id\":$reappId,\"out\":\"resolved\"}}]]"
        )
        val afterEmo = EmotionState.fromJson(repository.getBot(bot.id)!!.emotionState)
        val remainingActive = repository.getActivePendingReappraisalsForBot(bot.id)
        assertTrue("resolved sonrası kayıt aktif listeden çıkmalı", remainingActive.isEmpty())
        assertTrue("resolved sonrası güven artmalı (${beforeEmo.trust} -> ${afterEmo.trust})", afterEmo.trust > beforeEmo.trust)
        assertTrue(
            "resolved sonrası kırgınlık/öfke azalmalı (${beforeEmo.relationshipAxes.resentmentScore} -> ${afterEmo.relationshipAxes.resentmentScore})",
            afterEmo.relationshipAxes.resentmentScore < beforeEmo.relationshipAxes.resentmentScore
        )

        // 14+ hikâye günü geçince açılmayan yeni bir yara doğal olarak bayatlayıp expired = true olmalı
        repository.recordPendingReappraisalIfEligible(
            botId = bot.id,
            eventDescription = "Eski soğuk tartışma",
            emotionStateJson = bot.emotionState,
            currentMessageIndex = 8,
            rawDelta = -20,
            storyDayCounter = 5L
        )
        val expiredCount = repository.expireStaleReappraisals(
            botId = bot.id,
            currentStoryDay = 20L, // +15 hikâye günü
            regulationCapacity = 50
        )
        assertEquals("14+ hikâye günü geçen açık yara bayatlamalı", 1, expiredCount)
        assertTrue("Bayatlayan kayıt artık aktif olmamalı", repository.getActivePendingReappraisalsForBot(bot.id).isEmpty())
    }

    // =========================================================================
    // TEST 5 (#23): OpenAI & Gemini SSE Parser + `: keep-alive` Toleransı
    // =========================================================================
    @Test
    fun test5_system23_sseStreamParserOpenAiAndGemini() {
        val openAiLines = listOf(
            ": OPENROUTER PROCESSING",
            "data: {\"choices\":[{\"delta\":{\"reasoning_content\":\"ic dusunce\"}}]}",
            "data: {\"choices\":[{\"delta\":{\"content\":\"Merhaba \"}}]}",
            "data: {bozuk_json_satiri",
            "data: {\"choices\":[{\"delta\":{\"content\":\"dünya!\"}}]}",
            "data: [DONE]",
            "data: {\"choices\":[{\"delta\":{\"content\":\"sonra gelen goz ardi\"}}]}"
        )
        val openAiChunks = SseStreamParser.parseSseLines(openAiLines, SseStreamParser.SseFormat.OPENAI)
        val openAiText = openAiChunks.joinToString("") { it.token }
        val openAiReasoning = openAiChunks.joinToString("") { it.reasoningToken }
        assertEquals("OpenAI SSE tokenları birleşmeli ve [DONE]'da durmalı", "Merhaba dünya!", openAiText)
        assertEquals("reasoning_content ayrı tutulmalı, kullanıcı metnine sızmamalı", "ic dusunce", openAiReasoning)
        assertTrue("Son chunk isDone = true olmalı", openAiChunks.last().isDone)

        val geminiLines = listOf(
            ": keep-alive",
            "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Gece \"}]}}]}",
            "",
            "data: {\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"sessizdi.\"}]}}]}"
        )
        val geminiChunks = SseStreamParser.parseSseLines(geminiLines, SseStreamParser.SseFormat.GEMINI)
        val geminiText = geminiChunks.joinToString("") { it.token }
        assertEquals("Gemini SSE parçaları birleşmeli", "Gece sessizdi.", geminiText)
    }

    // =========================================================================
    // TEST 6 (#23): StreamingTextSanitizer Sıfır Sızıntı Testi (1-3 Karakter Parçalar)
    // =========================================================================
    @Test
    fun test6_system23_streamingTextSanitizerZeroLeak() {
        val sanitizer = StreamingTextSanitizer()
        val fragmentedChunks = listOf(
            "Merhaba ",
            "<th",
            "ink>gizli",
            " düşünce</th",
            "ink>dostum ",
            "[[STA",
            "TE_JSON {\"dom\":\"mutlu\"}]]"
        )

        var sawThinkingTrue = false
        for (chunk in fragmentedChunks) {
            val delta = sanitizer.append(chunk)
            if (delta.isThinking) sawThinkingTrue = true
            val visibleSoFar = sanitizer.visibleText
            assertFalse("Hiçbir adımda '<think' sızmamalı: $visibleSoFar", visibleSoFar.contains("<th", ignoreCase = true))
            assertFalse("Hiçbir adımda gizli düşünce sızmamalı: $visibleSoFar", visibleSoFar.contains("gizli"))
            assertFalse("Hiçbir adımda '[[STA' veya STATE_JSON sızmamalı: $visibleSoFar", visibleSoFar.contains("[[") || visibleSoFar.contains("STATE_JSON"))
        }
        sanitizer.finish()

        assertTrue("Düşünme bloğu sırasında isThinking=true olmalı", sawThinkingTrue)
        assertFalse("Akış sonunda isThinking=false olmalı", sanitizer.isThinking)
        assertEquals("Kullanıcıya yalnızca temiz anlatım akmalı", "Merhaba dostum ", sanitizer.visibleText)
        assertTrue("Ham metin STATE_JSON etiketini işlem için korumalı", sanitizer.rawFullText.contains("[[STATE_JSON {\"dom\":\"mutlu\"}]]"))
    }

    // =========================================================================
    // TEST 7 (#23): Normal Köşeli Parantez Koruması
    // =========================================================================
    @Test
    fun test7_system23_streamingTextSanitizerPreservesNormalBrackets() {
        val sanitizer = StreamingTextSanitizer()
        val input = "Merhaba [gülümseyerek] nasılsın?"
        // En zorlu senaryo: tek karakterlik akış
        for (ch in input) {
            sanitizer.append(ch.toString())
        }
        sanitizer.finish()
        assertEquals("Normal köşeli parantezler eksiksiz korunmalı", input, sanitizer.visibleText)
    }

    // =========================================================================
    // TEST 8 (#23): Pre-First-Token 429 Fallback & CancellationException 0 Ceza
    // =========================================================================
    @Test
    fun test8_system23_preFirstToken429FallbackAndZeroPenaltyCancel() = runBlocking {
        val settings = UserSettingsEntity(
            selectedProvider = "groq",
            groqApiKey = "gsk_test",
            openRouterApiKey = "sk_or_test",
            enableAutoFallback = true,
            fallbackChainOrder = "groq,openrouter"
        )
        val userMsg = MessageEntity(id = "u1", botId = "b1", role = "user", text = "Selam", timestamp = 1L)

        // Senaryo A: 1. sağlayıcı ilk tokendan önce 429 verir -> 2. sağlayıcıya kesintisiz geçer
        val fallbackCalls = mutableListOf<String>()
        val fallbackManager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = {},
            customStreamExecutor = { providerKey, _, _, _, onToken, _ ->
                fallbackCalls.add(providerKey)
                if (providerKey == "groq") {
                    throw SseStreamParser.SseHttpException(429, "Rate limit exceeded")
                }
                onToken("Yedek ")
                onToken("sağlayıcı çalıştı.")
                ModelResponseResult(text = "Yedek sağlayıcı çalıştı.", usedProvider = providerKey)
            }
        )

        val streamedSnapshots = mutableListOf<String>()
        val result = fallbackManager.executeChatStream(
            model = "test-model",
            settings = settings,
            systemPrompt = "Sys",
            messages = listOf(userMsg),
            buildConfigGeminiKey = "",
            onVisibleDelta = { snap, _ -> streamedSnapshots.add(snap) }
        )
        assertEquals("İkinci sağlayıcı kullanılmalı", "openrouter", result.response.usedProvider)
        assertEquals("Akış metni eksiksiz dönmeli", "Yedek sağlayıcı çalıştı.", result.visibleText)
        assertEquals(listOf("groq", "openrouter"), fallbackCalls)

        // Senaryo B: Kullanıcı iptali (CancellationException) -> ProviderHealthTracker'a 0 ceza!
        ProviderHealthTracker.resetAll()
        val cancelManager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = {},
            customStreamExecutor = { _, _, _, _, onToken, _ ->
                onToken("Başladı...")
                throw CancellationException("User tapped Stop")
            }
        )
        var caughtCancel = false
        try {
            cancelManager.executeChatStream(
                model = "test-model",
                settings = settings,
                systemPrompt = "Sys",
                messages = listOf(userMsg),
                buildConfigGeminiKey = "",
                onVisibleDelta = { _, _ -> }
            )
        } catch (_: CancellationException) {
            caughtCancel = true
        }
        assertTrue("CancellationException yukarı iletilmeli", caughtCancel)
        val groqHealth = ProviderHealthTracker.getState("groq")
        assertEquals("CancellationException asla hata cezası yazmamalı", 0, groqHealth?.totalFailureCount ?: 0)
        assertTrue("CancellationException devre kesiciyi açmamalı", ProviderHealthTracker.isAvailable("groq"))
    }

    // =========================================================================
    // TEST 9 (#23): SSE Kapalı Non-Stream Fallback & startTypewriter Temizliği
    // =========================================================================
    @Test
    fun test9_system23_sseDisabledFallbackAndNoStartTypewriter() = runBlocking {
        AppFeatureFlags.setSseStreaming(false)
        val settings = UserSettingsEntity(
            selectedProvider = "groq",
            groqApiKey = "gsk_test",
            enableAutoFallback = false
        )
        val userMsg = MessageEntity(id = "u2", botId = "b2", role = "user", text = "Test", timestamp = 1L)

        var streamExecutorCalled = false
        var nonStreamExecutorCalled = false
        val manager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = {},
            customExecutor = { providerKey, _, _, _, _ ->
                nonStreamExecutorCalled = true
                ModelResponseResult(text = "Klasik tek parça yanıt [[STATE_JSON {\"dom\":\"sakin\"}]]", usedProvider = providerKey)
            },
            customStreamExecutor = { providerKey, _, _, _, _, _ ->
                streamExecutorCalled = true
                ModelResponseResult(text = "Stream olmamalı", usedProvider = providerKey)
            }
        )

        val res = manager.executeChatStream(
            model = "test-model",
            settings = settings,
            systemPrompt = "Sys",
            messages = listOf(userMsg),
            buildConfigGeminiKey = "",
            onVisibleDelta = { _, _ -> }
        )
        assertTrue("SSE kapalıyken klasik non-stream executor çağrılmalı", nonStreamExecutorCalled)
        assertFalse("SSE kapalıyken stream executor çağrılmamalı", streamExecutorCalled)
        assertFalse("usedStreaming = false dönmeli", res.usedStreaming)
        assertEquals("Klasik tek parça yanıt", res.visibleText)

        // Sahte startTypewriter fonksiyonunun EmochiViewModel.kt'den tamamen kaldırıldığını doğrula
        val vmFile = listOf(
            File("src/main/java/com/example/ui/viewmodel/EmochiViewModel.kt"),
            File("app/src/main/java/com/example/ui/viewmodel/EmochiViewModel.kt")
        ).firstOrNull { it.exists() }
        assertNotNull("EmochiViewModel.kt bulunabilmeli", vmFile)
        val vmSource = vmFile!!.readText()
        assertFalse("Yapay startTypewriter EmochiViewModel.kt içinden tamamen kaldırılmış olmalı", vmSource.contains("startTypewriter"))
    }

    // =========================================================================
    // TEST 10 (#20, #22, #23): Strict Timeout Bayrağı & Sistem Bütünlüğü
    // =========================================================================
    @Test
    fun test10_systems20_22_23_timeoutFlagAlignmentAndIntegrity() {
        assertEquals("sse_streaming", AppFeatureFlags.KEY_SSE_STREAMING_ENABLED)

        AppFeatureFlags.setStrictApiTimeout(false)
        val relaxedCfg = ProviderFallbackManager.resolveTimeoutConfig(ProviderFallbackManager.ExecutionMode.NARRATIVE)
        assertEquals("Strict timeout kapalıyken SSE readTimeout sınırsız (0) olmalı", 0L, relaxedCfg.readTimeoutMs)
        assertFalse(relaxedCfg.strictEnabled)

        AppFeatureFlags.setStrictApiTimeout(true)
        val strictCfg = ProviderFallbackManager.resolveTimeoutConfig(ProviderFallbackManager.ExecutionMode.NARRATIVE)
        assertEquals("Strict timeout açıkken NARRATIVE timeout 60s olmalı", 60_000L, strictCfg.readTimeoutMs)
        assertTrue(strictCfg.strictEnabled)
    }
}
