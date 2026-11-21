package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.RetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CustomProviderEntity
import com.example.data.local.EmotionState
import com.example.data.local.PersonalityProfile
import com.example.data.local.PrimaryEmotions
import com.example.data.local.RelationshipAxes
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.BaselineEmotionEngine
import com.example.data.repository.EmochiRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WizardBaselineEmotionTest {

    private fun createInMemoryDb(): AppDatabase =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

    // =========================================================================
    // TEST 1: Turkish Apostrophe JSON Parsing Test (NO false fallback!)
    // =========================================================================
    @Test
    fun test1_turkishApostropheJson_parsesWithoutFallback() {
        val rawLlmJson = """
            ```json
            {
              "schemaVersion": 1,
              "relationshipType": "conflicted_romantic",
              "relationshipNote": "Aiden'ın İstanbul'daki geçmişi yüzünden güçlü bir çekim ile kırgınlık çatışıyor.",
              "bigFive": {
                "openness": 74,
                "conscientiousness": 81,
                "extraversion": 46,
                "agreeableness": 38,
                "neuroticism": 67
              },
              "attachmentStyle": "fearful_avoidant",
              "regulationCapacity": 72,
              "affectionDifficultyMultiplier": 1.35,
              "initialPlutchik": {
                "joy": 20, "trust": 42, "fear": 35, "anger": 40,
                "sadness": 55, "anticipation": 60, "surprise": 15, "disgust": 10
              },
              "initialAxes": {
                "affection": 76, "respect": 58, "comfort": 44, "resentment": 52
              },
              "dominantEmotion": "tutkulu ve mesafeli",
              "sensitiveTriggers": [
                {"topic": "Aiden'ın ailesi", "severity": 5, "reaction": "Soğuk duvar örer"},
                {"topic": "İstanbul'da yaşanan yangın", "severity": 4, "reaction": "Sesi kısılır ve gerginleşir"},
                {"topic": "Zeynep'in sessiz kalması", "severity": 3, "reaction": "Terk edilme korkusu tetiklenir"}
              ],
              "speechStyle": "Kısa, kontrollü ama derin bakışlı cümleler",
              "confidence": 91
            }
            ```
        """.trimIndent()

        val result = BaselineEmotionEngine.parseAnalysisJson(
            jsonStr = rawLlmJson,
            botId = "bot_aiden_tr",
            aiName = "Aiden",
            personality = "Soğukkanlı ve korumacı",
            scenario = "İstanbul'da yağmurlu bir gece"
        )

        assertEquals("Turkish apostrophes MUST parse cleanly as LLM, never keyword fallback", "llm", result.analysisSource)
        assertEquals("conflicted_romantic", result.relationshipType)
        assertTrue(result.relationshipNote.contains("Aiden'ın İstanbul'daki geçmişi"))
        assertEquals("fearful_avoidant", result.personalityProfile.attachmentStyle)
        assertEquals(72, result.regulationCapacity)
        assertEquals(91, result.confidence)
        assertEquals(3, result.sensitiveTriggers.size)
        assertEquals("Aiden'ın ailesi", result.sensitiveTriggers[0].triggerTopic)
        assertEquals("İstanbul'da yaşanan yangın", result.sensitiveTriggers[1].triggerTopic)
        assertEquals("Zeynep'in sessiz kalması", result.sensitiveTriggers[2].triggerTopic)
    }

    // =========================================================================
    // TEST 2: Round-Trip User Edit Persistence Test (toCanonicalJson -> parse)
    // =========================================================================
    @Test
    fun test2_roundTripCanonicalJson_preservesAllUserEditsLosslessly() {
        val botId = "bot_roundtrip_1"
        val editedTriggers = listOf(
            SensitiveTriggerEntity(botId = botId, triggerTopic = "eski ihanet", sourceDescription = "Öfkeyle geri çekilir", intensityMultiplier = 2.6),
            SensitiveTriggerEntity(botId = botId, triggerTopic = "aile mirası", sourceDescription = "Savunmaya geçer", intensityMultiplier = 2.2),
            SensitiveTriggerEntity(botId = botId, triggerTopic = "yalan söylenmesi", sourceDescription = "Güveni anında kırılır", intensityMultiplier = 1.8)
        )
        val original = BaselineEmotionEngine.BaselineAnalysisResult(
            relationshipType = "rival",
            relationshipNote = "Rekabetin altında gizli bir saygı barındırıyor.",
            personalityProfile = PersonalityProfile(
                openness = 82,
                conscientiousness = 41,
                extraversion = 63,
                agreeableness = 29,
                neuroticism = 77,
                attachmentStyle = "fearful_avoidant"
            ),
            regulationCapacity = 88,
            difficultyMultiplier = 0.75,
            baselineEmotionState = EmotionState(
                primaryEmotions = PrimaryEmotions(joy = 12, trust = 28, fear = 19, anger = 54, sadness = 22, anticipation = 65, surprise = 14, disgust = 18),
                relationshipAxes = RelationshipAxes(affectionScore = 24, respectScore = 64, comfortScore = 31, resentmentScore = 59),
                dominantEmotion = "tetikte ve alaycı",
                speechPattern = "Keskin, iğneleyici ama ölçülü"
            ),
            sensitiveTriggers = editedTriggers,
            speechPattern = "Keskin, iğneleyici ama ölçülü",
            confidence = 87,
            analysisSource = "user_edited",
            universeAtmosphere = BaselineEmotionEngine.UniverseAtmosphereSeed(
                currentLocation = "Karanlık Kütüphane",
                timeOfDay = "gece",
                globalTensionLevel = 8,
                sceneTension = 7,
                sceneMood = "fırtına öncesi sessizlik"
            )
        )

        val canonicalJson = original.toCanonicalJson()
        val restored = BaselineEmotionEngine.parseAnalysisJson(
            jsonStr = canonicalJson,
            botId = botId,
            aiName = "Soren",
            personality = "Rakip büyücü",
            scenario = "Akademi düellosu"
        )

        assertEquals("user_edited", restored.analysisSource)
        assertEquals("rival", restored.relationshipType)
        assertEquals(original.relationshipNote, restored.relationshipNote)
        assertEquals(82, restored.personalityProfile.openness)
        assertEquals(41, restored.personalityProfile.conscientiousness)
        assertEquals(63, restored.personalityProfile.extraversion)
        assertEquals(29, restored.personalityProfile.agreeableness)
        assertEquals(77, restored.personalityProfile.neuroticism)
        assertEquals("fearful_avoidant", restored.personalityProfile.attachmentStyle)
        assertEquals(88, restored.regulationCapacity)
        assertEquals(0.75, restored.difficultyMultiplier, 0.001)
        assertEquals(24, restored.baselineEmotionState.relationshipAxes.affectionScore)
        assertEquals(64, restored.baselineEmotionState.relationshipAxes.respectScore)
        assertEquals(31, restored.baselineEmotionState.relationshipAxes.comfortScore)
        assertEquals(59, restored.baselineEmotionState.relationshipAxes.resentmentScore)
        assertEquals("tetikte ve alaycı", restored.baselineEmotionState.dominantEmotion)
        assertEquals("Keskin, iğneleyici ama ölçülü", restored.speechPattern)
        assertEquals(listOf("eski ihanet", "aile mirası", "yalan söylenmesi"), restored.sensitiveTriggers.map { it.triggerTopic })
        assertNotNull(restored.universeAtmosphere)
        assertEquals("Karanlık Kütüphane", restored.universeAtmosphere?.currentLocation)
        assertEquals(8, restored.universeAtmosphere?.globalTensionLevel)
    }

    // =========================================================================
    // TEST 3: Fixed draftBotId & SensitiveTrigger + CastMember Matching Test
    // =========================================================================
    @Test
    fun test3_fixedDraftBotId_triggersAndCastMembersMatchSavedBot() = runBlocking {
        val db = createInMemoryDb()
        val repo = EmochiRepository(db)
        val fixedDraftId = UUID.randomUUID().toString()

        try {
            val bot = BotEntity(
                id = fixedDraftId,
                mode = "universe",
                aiName = "",
                universeName = "Eldoria Krallığı",
                aiPersonality = "Karanlık Gotik & Gizemli anlatıcı",
                scenario = "Taht mücadelesinin başladığı akşam",
                keyCharactersJson = """[{"id":"1","name":"Lord Kael","desc":"Soğukkanlı düşman komutan"},{"id":"2","name":"Lyra","desc":"Sadık dost ve sırdaş"},{"id":"3","name":"","desc":""}]""",
                userCharName = "Arden",
                userCharDesc = "Kraliyet muhafızı",
                openingMessage = "Meşaleler titrerken salon sessizliğe gömüldü.",
                writingStyle = "rp",
                intensity = "intense",
                isPublic = false,
                enableOoc = true,
                isNsfw = true
            )
            val triggers = listOf(
                SensitiveTriggerEntity(botId = fixedDraftId, triggerTopic = "kraliyet yemini", sourceDescription = "Öfke artar", intensityMultiplier = 2.2),
                SensitiveTriggerEntity(botId = fixedDraftId, triggerTopic = "kayıp mühür", sourceDescription = "Panik başlar", intensityMultiplier = 2.0),
                SensitiveTriggerEntity(botId = fixedDraftId, triggerTopic = "ihanet gecesi", sourceDescription = "Güven çöker", intensityMultiplier = 2.4)
            )

            repo.saveBot(bot)
            repo.saveSensitiveTriggers(bot.id, triggers)
            repo.syncWizardCastMembers(bot)

            val savedTriggers = db.sensitiveTriggerDao().getTriggersForBot(fixedDraftId)
            assertEquals("All 3 triggers must be stored under the exact fixed draftBotId", 3, savedTriggers.size)
            assertTrue("Every trigger's botId must equal saved bot.id", savedTriggers.all { it.botId == bot.id })

            val mult = repo.evaluateSensitiveTriggers(fixedDraftId, "Bu gece kayıp mühür hakkında konuşmamız gerekiyor.")
            assertTrue("Sensitive trigger must fire on matching botId (mult=$mult)", mult >= 1.9)

            val castMembers = db.castMemberDao().getCastMembersForBot(fixedDraftId)
            assertEquals("Blank keyCharacter row must be skipped; 2 valid cast members synced", 2, castMembers.size)
            val kael = castMembers.first { it.name == "Lord Kael" }
            val lyra = castMembers.first { it.name == "Lyra" }
            assertFalse("Manually added cast should have isAutoAdded=false", kael.isAutoAdded)
            assertTrue("Enemy cast member must have stricter difficulty multiplier", kael.baseAffectionDifficultyMultiplier < 0.8)
            assertTrue("Loyal friend cast member must have warmer initial affection", lyra.affectionScore > kael.affectionScore)
            assertTrue("Cast member must have serialized PersonalityProfile", kael.personalityJson.contains("openness"))
        } finally {
            db.close()
        }
    }

    // =========================================================================
    // TEST 4: generateOpeningMessage(forceRegenerate = true) Test
    // =========================================================================
    @Test
    fun test4_generateOpeningMessage_forceRegenerateCallsLlmWithToneHint() = runBlocking {
        val db = createInMemoryDb()
        val repo = EmochiRepository(db)

        val customProvider = CustomProviderEntity(
            id = 91L,
            label = "TestOpeningProvider",
            baseUrl = "https://opening.local.test/v1",
            apiKeyEncrypted = com.example.util.KeystoreEncryptionManager.encrypt("sk-opening-test"),
            modelName = "llama-3.3-70b-instruct",
            apiFormat = "openai",
            utilityModel = "llama-3.1-8b-instant",
            utilityModelSource = "auto_detected",
            detectedSmallModels = "[\"llama-3.1-8b-instant\"]"
        )
        db.customProviderDao().insertProvider(customProvider)
        db.userSettingsDao().insertOrUpdate(UserSettingsEntity(id = 1, selectedProvider = "custom_91"))

        val callCount = AtomicInteger(0)
        var capturedPromptBody = ""

        RetrofitClient.customInterceptor = Interceptor { chain ->
            callCount.incrementAndGet()
            val req = chain.request()
            val buffer = okio.Buffer()
            req.body?.writeTo(buffer)
            capturedPromptBody = buffer.readUtf8()

            val fakeResp = """
                {
                  "choices": [
                    {
                      "message": {
                        "role": "assistant",
                        "content": "Sis çöken rıhtımda fenerin solgun ışığı altında sana döndü. \"Bu kez geç kalmadın.\""
                      }
                    }
                  ]
                }
            """.trimIndent()
            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(fakeResp.toResponseBody("application/json".toMediaType()))
                .build()
        }

        try {
            val botWithExisting = BotEntity(
                id = "bot_opening_regen",
                mode = "personal",
                aiName = "Aiden",
                aiPersonality = "Gizemli ve ölçülü",
                scenario = "Sisli bir limanda gece yarısı buluşması",
                universeName = "",
                keyCharactersJson = "[]",
                userCharName = "Zeynep",
                userCharDesc = "Araştırmacı gazeteci",
                openingMessage = "Eski açılış taslağı burada duruyor.",
                writingStyle = "rp",
                intensity = "normal"
            )

            // 1. Default (forceRegenerate = false) preserves existing opening with 0 network calls
            val kept = repo.generateOpeningMessage(botWithExisting, forceRegenerate = false)
            assertEquals("Eski açılış taslağı burada duruyor.", kept)
            assertEquals(0, callCount.get())

            // 2. Step 4 regeneration (forceRegenerate = true) MUST call LLM and return fresh scene
            val regenerated = repo.generateOpeningMessage(
                bot = botWithExisting,
                forceRegenerate = true,
                toneHint = "mysterious"
            )
            assertEquals("forceRegenerate=true MUST trigger LLM call", 1, callCount.get())
            assertNotEquals("Regenerated opening must replace previous draft", botWithExisting.openingMessage, regenerated)
            assertTrue(regenerated.contains("Sis çöken rıhtımda"))
            assertTrue("Prompt should include mysterious tone hint", capturedPromptBody.contains("Gizemli"))
            assertTrue("Prompt should instruct alternative phrasing", capturedPromptBody.contains("alternatif"))
        } finally {
            RetrofitClient.customInterceptor = null
            db.close()
        }
    }

    // =========================================================================
    // TEST 5: Input Hash Cache + Utility Model Discipline Test
    // =========================================================================
    @Test
    fun test5_baselineAnalysis_usesUtilityModelAndInputHashCache() = runBlocking {
        val db = createInMemoryDb()
        val repo = EmochiRepository(db)

        val customProvider = CustomProviderEntity(
            id = 92L,
            label = "TestBaselineUtility",
            baseUrl = "https://baseline.local.test/v1",
            apiKeyEncrypted = com.example.util.KeystoreEncryptionManager.encrypt("sk-baseline-test"),
            modelName = "llama-3.3-70b-instruct",
            apiFormat = "openai",
            utilityModel = "llama-3.1-8b-instant",
            utilityModelSource = "auto_detected",
            detectedSmallModels = "[\"llama-3.1-8b-instant\"]"
        )
        db.customProviderDao().insertProvider(customProvider)
        db.userSettingsDao().insertOrUpdate(UserSettingsEntity(id = 1, selectedProvider = "custom_92"))

        val networkCalls = AtomicInteger(0)
        var capturedModel = ""
        var capturedTemp = -1.0

        RetrofitClient.customInterceptor = Interceptor { chain ->
            networkCalls.incrementAndGet()
            val req = chain.request()
            val buffer = okio.Buffer()
            req.body?.writeTo(buffer)
            val bodyStr = buffer.readUtf8()
            if (bodyStr.isNotBlank()) {
                val json = JSONObject(bodyStr)
                capturedModel = json.optString("model", "")
                capturedTemp = json.optDouble("temperature", -1.0)
            }
            val payload = JSONObject().apply {
                put("schemaVersion", 1)
                put("relationshipType", "friend")
                put("relationshipNote", "Yıllardır süren güven dolu bir dostluk.")
                put("bigFive", JSONObject().apply {
                    put("openness", 70); put("conscientiousness", 65); put("extraversion", 75)
                    put("agreeableness", 82); put("neuroticism", 30)
                })
                put("attachmentStyle", "secure")
                put("regulationCapacity", 76)
                put("affectionDifficultyMultiplier", 1.1)
                put("initialPlutchik", JSONObject().apply {
                    put("joy", 65); put("trust", 84); put("fear", 10); put("anger", 5)
                    put("sadness", 10); put("anticipation", 55); put("surprise", 20); put("disgust", 0)
                })
                put("initialAxes", JSONObject().apply {
                    put("affection", 82); put("respect", 78); put("comfort", 85); put("resentment", 0)
                })
                put("dominantEmotion", "sıcak ve samimi")
                put("sensitiveTriggers", org.json.JSONArray().apply {
                    put(JSONObject().apply { put("topic", "verilen sözün tutulmaması"); put("severity", 4); put("reaction", "Sessizce kırılır") })
                    put(JSONObject().apply { put("topic", "eski okul olayı"); put("severity", 3); put("reaction", "Konuyu değiştirir") })
                    put(JSONObject().apply { put("topic", "gizlenen gerçekler"); put("severity", 4); put("reaction", "Mesafe koyar") })
                })
                put("speechStyle", "Samimi ve esprili")
                put("confidence", 89)
            }
            val fakeResp = JSONObject().apply {
                put("choices", org.json.JSONArray().put(JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("role", "assistant")
                        put("content", payload.toString())
                    })
                }))
            }.toString()

            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(fakeResp.toResponseBody("application/json".toMediaType()))
                .build()
        }

        try {
            val draft = BotEntity(
                id = "bot_cache_test",
                mode = "personal",
                aiName = "Mert",
                aiPersonality = "Esprili ve sadık dost",
                scenario = "Kampüs kafesinde sınav çıkışı buluşma",
                universeName = "",
                keyCharactersJson = "[]",
                userCharName = "Deniz",
                userCharDesc = "Üniversite öğrencisi",
                openingMessage = "Kahve fincanını masaya bırakıp gülümsedi.",
                writingStyle = "chat",
                intensity = "normal"
            )

            // First call -> hits utility model at low temperature
            val (updatedBot, res1) = repo.runBaselineAnalysis(draft, forceReanalyze = false)
            assertEquals("llm", res1.analysisSource)
            assertEquals(1, networkCalls.get())
            assertEquals("Must route baseline analysis through cheap utility model", "llama-3.1-8b-instant", capturedModel)
            assertTrue("Utility call temperature must be <= 0.15 (actual=$capturedTemp)", capturedTemp in 0.0..0.15)

            // Second call with identical persisted bot -> 0 new network calls
            val (_, res2) = repo.runBaselineAnalysis(updatedBot, forceReanalyze = false)
            assertEquals("friend", res2.relationshipType)
            assertEquals("Unchanged inputs MUST return cached baseline with 0 extra network calls", 1, networkCalls.get())

            // Third call with fresh un-persisted draft having identical id & inputs -> 0 new network calls (memory cache)
            val (_, res3) = repo.runBaselineAnalysis(draft, forceReanalyze = false)
            assertEquals("friend", res3.relationshipType)
            assertEquals("Repeated wizard navigation with unchanged draft MUST hit cache", 1, networkCalls.get())

            // Fourth call with modified scenario -> MUST trigger new LLM analysis
            val changedDraft = updatedBot.copy(scenario = "Büyük bir kavganın ardından soğuk karşılaşma")
            repo.runBaselineAnalysis(changedDraft, forceReanalyze = false)
            assertEquals("Changed scenario MUST invalidate hash cache and trigger fresh analysis", 2, networkCalls.get())
        } finally {
            RetrofitClient.customInterceptor = null
            db.close()
        }
    }

    // =========================================================================
    // TEST 6: Canonical Attachment Style + Psychological Consistency + Turkish Keywords
    // =========================================================================
    @Test
    fun test6_canonicalAttachmentStyle_andPsychologicalConsistency() {
        // 1. Canonical mapping for all disorganized / fearful variants
        assertEquals("fearful_avoidant", BaselineEmotionEngine.normalizeAttachment("disorganized"))
        assertEquals("fearful_avoidant", BaselineEmotionEngine.normalizeAttachment("fearful"))
        assertEquals("fearful_avoidant", BaselineEmotionEngine.normalizeAttachment("fearful_avoidant"))
        assertEquals("fearful_avoidant", BaselineEmotionEngine.normalizeAttachment("korkulu"))
        assertEquals("fearful_avoidant", BaselineEmotionEngine.normalizeAttachment("Korkulu-Kaçınan"))
        assertEquals("avoidant", BaselineEmotionEngine.normalizeAttachment("kaçınan"))
        assertEquals("anxious", BaselineEmotionEngine.normalizeAttachment("kaygılı"))
        assertEquals("secure", BaselineEmotionEngine.normalizeAttachment("güvenli"))

        // 2. Legacy "disorganized" JSON parses into canonical "fearful_avoidant"
        val legacyJson = """
            {
              "relationshipType": "enemy",
              "attachmentStyle": "disorganized",
              "initialAxes": {"affection": 10, "respect": 15, "comfort": 80, "resentment": 85},
              "dominantEmotion": "soğuk"
            }
        """.trimIndent()
        val parsedLegacy = BaselineEmotionEngine.parseAnalysisJson(legacyJson, "b1", "X", "Soğuk", "Çatışma")
        assertEquals("fearful_avoidant", parsedLegacy.personalityProfile.attachmentStyle)
        assertTrue("Hostile character comfort must be clamped low", parsedLegacy.baselineEmotionState.relationshipAxes.comfortScore <= 35)

        // 3. Direct psychological consistency lock check: affection <= 20 + secure + comfort >= 75
        val (reconciledAtt, reconciledAxes) = BaselineEmotionEngine.reconcileAttachmentAndComfort(
            attachmentStyle = "secure",
            axes = RelationshipAxes(affectionScore = 15, respectScore = 20, comfortScore = 85, resentmentScore = 70)
        )
        assertNotEquals("Hostile low-affection bot cannot remain 'secure' with high comfort", "secure", reconciledAtt)
        assertTrue("Comfort must be clamped <= 35 when affection <= 20", reconciledAxes.comfortScore <= 35)

        // 4. Turkish adjective false-positive guards & enemy vs rival separation
        val rivalFallback = BaselineEmotionEngine.calculateDeterministicFallback(
            botId = "b_rival",
            aiName = "Kaan",
            personality = "Babacan görünümlü ama amansız bir rakip",
            scenario = "Şirkette terfi rekabeti"
        )
        assertEquals("Babacan must NOT falsely trigger parent; rival must win", "rival", rivalFallback.relationshipType)

        val enemyFallback = BaselineEmotionEngine.calculateDeterministicFallback(
            botId = "b_enemy",
            aiName = "Veysel",
            personality = "Eski bir hasım ve can düşmanı",
            scenario = "Karanlık sokakta yüzleşme"
        )
        assertEquals("Enemy keywords must classify as 'enemy', not 'rival'", "enemy", enemyFallback.relationshipType)
    }
}
