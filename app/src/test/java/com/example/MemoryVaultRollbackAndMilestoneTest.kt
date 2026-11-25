package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EmotionState
import com.example.data.local.MessageEntity
import com.example.data.local.PrimaryEmotions
import com.example.data.local.RelationshipAxes
import com.example.data.repository.EmochiRepository
import com.example.data.repository.MemoryEngine
import com.example.data.repository.MemoryTier
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
class MemoryVaultRollbackAndMilestoneTest {

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
        db.close()
    }

    private fun makeBot(botId: String): BotEntity {
        val baseEmo = EmotionState(
            primaryEmotions = PrimaryEmotions(trust = 50),
            relationshipAxes = RelationshipAxes(affectionScore = 50),
            dominantEmotion = "sakin"
        ).toJson()
        return BotEntity(
            id = botId,
            mode = "personal",
            aiName = "Natasha",
            aiPersonality = "Soğukkanlı, sadık, keskin zekalı",
            scenario = "Manhattan çatı katı",
            universeName = "",
            keyCharactersJson = "[]",
            userCharName = "Aiden",
            userCharDesc = "Kahraman",
            openingMessage = "Merhaba.",
            writingStyle = "sinematik",
            intensity = "dengeli",
            emotionState = baseEmo,
            previousEmotionState = baseEmo,
            storyDayCounter = 1L,
            storyCalendarDate = "2026-08-29",
            sceneTime = "gece",
            currentAge = 24
        )
    }

    @Test
    fun test1_neuralVaultAndHiddenRagAreStrictlySeparatedAndBlockRawUserMessages() = runBlocking {
        val botId = "bot_vault_rag_sep"
        repo.saveBot(makeBot(botId))

        // 1. Ephemeral / low-mid importance detail routes to Hidden RAG (`memory_fragments`), NOT Neural Vault!
        val ragSaved = repo.processSaveMemory(
            botId = botId,
            content = "Akşam sarımsaklı kremalı mantarlı makarna yaptılar.",
            category = "fact",
            importance = 4,
            subject = "Aiden",
            key = "Akşam Yemeği",
            tier = "ephemeral",
            ttlDays = 3
        )
        assertTrue("Contextual detail should be saved to hidden RAG", ragSaved)
        assertEquals("Visible Neural Vault must remain clean (0 facts)", 0, db.memoryFactDao().getActiveFacts(botId).size)
        assertEquals("Hidden RAG fragments must store the ephemeral item", 1, db.memoryFragmentDao().getRecentFragments(botId, 50).size)
        assertEquals(1, repo.getHiddenRagFragmentCount(botId))

        // 2. Raw user chat chatter & verbatim user messages are REJECTED from Neural Vault
        val rawChatRejected = repo.upsertNeuralVaultFact(
            botId = botId,
            subject = "Aiden",
            key = "Son Mesaj",
            value = "Kullanıcı merhaba dedi ve nasılsın diye sordu",
            importance = 8,
            tier = MemoryTier.CORE,
            rawUserText = "Merhaba nasılsın?"
        )
        val rawQuestionRejected = repo.upsertNeuralVaultFact(
            botId = botId,
            subject = "Aiden",
            key = "Soru",
            value = "Bugün üsse ne zaman gideceğiz?",
            importance = 8,
            tier = MemoryTier.CORE
        )
        assertFalse("Raw user chat filler must NOT enter Neural Vault", rawChatRejected)
        assertFalse("Raw conversational question must NOT enter Neural Vault", rawQuestionRejected)
        assertEquals(0, db.memoryFactDao().getActiveFacts(botId).size)

        // 3. Truly important CORE fact DOES enter visible Neural Vault
        val coreSaved = repo.processSaveMemory(
            botId = botId,
            content = "Aiden zaman kırılmasından gelen gizli bir anahtarı saklıyor.",
            category = "fact",
            importance = 9,
            subject = "Aiden",
            key = "Gizli Sır",
            tier = "core"
        )
        assertTrue("Important CORE fact must save to Neural Vault", coreSaved)
        val activeFacts = db.memoryFactDao().getActiveFacts(botId)
        assertEquals(1, activeFacts.size)
        assertTrue(MemoryEngine.isVisibleInNeuralVault(activeFacts.first()))
    }

    @Test
    fun test2_semanticTopicBucketUpdatesOldFactInsteadOfDuplicating() = runBlocking {
        val botId = "bot_semantic_update"
        repo.saveBot(makeBot(botId))

        // Initial location saved under key "Konum"
        assertTrue(
            repo.upsertNeuralVaultFact(
                botId = botId,
                subject = "Natasha",
                key = "Konum",
                value = "Brooklyn güvenli evinde saklanıyor.",
                importance = 8,
                tier = MemoryTier.CORE
            )
        )
        assertEquals(1, db.memoryFactDao().getActiveFacts(botId).size)

        // Location changes under synonymous key "Bulunduğu Yer" -> MUST supersede old fact!
        assertTrue(
            repo.upsertNeuralVaultFact(
                botId = botId,
                subject = "Natasha",
                key = "Bulunduğu Yer",
                value = "Manhattan kulesinin balkonuna geçti.",
                importance = 8,
                tier = MemoryTier.CORE
            )
        )
        val activeFacts = db.memoryFactDao().getActiveFacts(botId)
        assertEquals("Changed location under synonymous key must supersede old fact, not duplicate", 1, activeFacts.size)
        assertTrue(activeFacts.first().value.contains("Manhattan"))
        assertEquals("Old superseded fact must be archived", 1, db.archivedMemoryDao().getArchivesForBot(botId).size)
    }

    @Test
    fun test3_livingMemoryAcceptsSalience5AndOptionalInnerThought() = runBlocking {
        val botId = "bot_living_mem_easy"
        repo.saveBot(makeBot(botId))

        val ok = repo.commitLivingMemory(
            botId = botId,
            povCharacter = "Natasha",
            title = "Balkonda Sessiz Güven",
            emotionTag = "Huzur",
            salience = 5,
            narrative = "Balkonun serin rüzgarında Aiden yanıma yaklaşıp sessizce omzuma yaslandığında ilk kez gardımı indirdim.",
            innerThought = "",
            storyDayIndex = 1L
        )
        assertTrue("Salience 5 meaningful moment without innerThought must now commit to Anılar", ok)
        assertEquals(1, db.memoryEventDao().getActiveEvents(botId).size)
    }

    @Test
    fun test4_milestoneAntiSpamSuppressesBaselineAndEnforcesCooldown() = runBlocking {
        val botId = "bot_milestone_antispam"
        repo.saveBot(makeBot(botId))

        // Default baseline 50/50 on message 2 must NOT pop a celebration banner!
        val earlyBanner = repo.checkAndSaveMilestone(
            botId = botId,
            affection = 50,
            trust = 50,
            previousAffection = 50,
            previousTrust = 50,
            messageCount = 2,
            suppressBaselineSpam = true
        )
        assertNull("Baseline 50/50 must NEVER spam a milestone banner on early turns", earlyBanner)

        // Genuine upward crossing (58 -> 61) at message 8 DOES unlock affection_60 banner!
        val realUnlock = repo.checkAndSaveMilestone(
            botId = botId,
            affection = 61,
            trust = 52,
            previousAffection = 58,
            previousTrust = 50,
            messageCount = 8,
            suppressBaselineSpam = true
        )
        assertNotNull("Genuine upward threshold crossing after message 6 must unlock milestone", realUnlock)
        assertEquals("affection_60", realUnlock?.type)

        // Narrative milestone anti-spam: blocked before message 8
        val tooEarlyNarr = repo.recordNarrativeMilestone(
            botId = botId,
            rawKey = "first_chat",
            rawTitle = "İlk Konuşma",
            currentMessageCount = 4,
            enforceAntiSpam = true
        )
        assertNull("Narrative milestone must be blocked before message 8", tooEarlyNarr)

        // Allowed at message 10
        val narr1 = repo.recordNarrativeMilestone(
            botId = botId,
            rawKey = "rooftop_oath",
            rawTitle = "Çatıdaki Büyük Yemin",
            currentMessageCount = 10,
            enforceAntiSpam = true
        )
        assertNotNull(narr1)

        // Immediate second narrative milestone at message 12 must be blocked by cooldown!
        val narrSpam = repo.recordNarrativeMilestone(
            botId = botId,
            rawKey = "another_moment",
            rawTitle = "Hemen Ertesi Dönüm Noktası",
            currentMessageCount = 12,
            enforceAntiSpam = true
        )
        assertNull("Consecutive narrative milestone must be blocked by cooldown", narrSpam)
    }

    @Test
    fun test5_atomicTurnRollbackRestoresAllMemoriesEmotionsCastMilestonesAndSupersededFacts() = runBlocking {
        val botId = "bot_atomic_rollback"
        val initialBot = makeBot(botId)
        repo.saveBot(initialBot)

        // Seed pre-turn state: 1 existing CORE fact + 1 existing side cast member + character emotion
        repo.upsertNeuralVaultFact(
            botId = botId,
            subject = "Natasha",
            key = "Konum",
            value = "Brooklyn güvenli evinde bekliyor.",
            importance = 9,
            tier = MemoryTier.CORE
        )
        val preFact = db.memoryFactDao().getActiveFacts(botId).single()

        db.castMemberDao().insertCastMember(
            CastMemberEntity(
                botId = botId,
                name = "Wanda",
                description = "Müttefik",
                role = "Müttefik",
                affectionScore = 50,
                relationshipState = "Tanıdık",
                firstAppearedAt = System.currentTimeMillis() - 60_000L,
                isAutoAdded = false
            )
        )
        val wandaBefore = db.castMemberDao().findByName(botId, "Wanda")!!
        val wandaPreEmoJson = EmotionState(
            primaryEmotions = PrimaryEmotions(trust = 45),
            relationshipAxes = RelationshipAxes(affectionScore = 50),
            dominantEmotion = "temkinli"
        ).toJson()
        db.characterEmotionDao().insertOrUpdate(
            CharacterEmotionEntity(botId = botId, characterName = "Wanda", emotionState = wandaPreEmoJson)
        )

        val userMsgId = "u_msg_1"
        val aiMsgId = "a_msg_1"
        repo.saveMessage(MessageEntity(id = userMsgId, botId = botId, role = "user", text = "Manhattan'a gidiyoruz!", timestamp = 1000L))

        // 1. Capture pre-turn snapshot BEFORE turn mutates anything
        repo.captureTurnPreSnapshot(botId = botId, messageId = aiMsgId, userMessageId = userMsgId)

        // 2. Simulate EVERY side effect during the turn:
        //    a) Save assistant message
        repo.saveMessage(MessageEntity(id = aiMsgId, botId = botId, role = "assistant", text = "Tamam, çatıya geçiyoruz.", timestamp = 1010L))
        //    b) Mutate bot emotion & story day
        val mutatedBotEmo = EmotionState(
            primaryEmotions = PrimaryEmotions(trust = 72),
            relationshipAxes = RelationshipAxes(affectionScore = 76),
            dominantEmotion = "heyecanlı"
        ).toJson()
        repo.saveBot(initialBot.copy(emotionState = mutatedBotEmo, storyDayCounter = 2L, sceneTime = "sabah"))
        //    c) Mutate existing side cast affection & emotion
        db.castMemberDao().updateCastMember(wandaBefore.copy(affectionScore = 82, relationshipState = "Yakın Dost"))
        db.characterEmotionDao().insertOrUpdate(
            CharacterEmotionEntity(
                id = db.characterEmotionDao().getEmotionForCharacter(botId, "Wanda")!!.id,
                botId = botId,
                characterName = "Wanda",
                emotionState = mutatedBotEmo
            )
        )
        //    d) Supersede old "Konum" fact AND add a brand-new CORE fact
        repo.upsertNeuralVaultFact(
            botId = botId,
            subject = "Natasha",
            key = "Bulunduğu Yer",
            value = "Manhattan kulesinin çatısına ulaştı.",
            importance = 9,
            tier = MemoryTier.CORE
        )
        repo.upsertNeuralVaultFact(
            botId = botId,
            subject = "Aiden",
            key = "Verilen Söz",
            value = "Natasha'yı asla yalnız bırakmayacağına yemin etti.",
            importance = 9,
            tier = MemoryTier.CORE
        )
        //    e) Add hidden RAG fragment, Living Memory, Milestone, Relation, and Auto-added CastMember
        repo.saveHiddenRagFragment(botId, "Aiden siyah deri ceketini giyip çatıya çıktı.", "RAG_EPHEMERAL")
        repo.commitLivingMemory(
            botId = botId,
            povCharacter = "Natasha",
            title = "Çatıdaki Yemin",
            emotionTag = "Güven",
            salience = 8,
            narrative = "Aiden çatıya adım atıp gözlerime bakarak beni asla bırakmayacağını söylediğinde kalbim hızlandı.",
            innerThought = "İlk kez birinin sözüne gerçekten inandım."
        )
        repo.recordNarrativeMilestone(botId, "roof_promise", "Çatı Yemini", "Büyük güven anı")
        db.castMemberDao().insertCastMember(
            CastMemberEntity(
                botId = botId,
                name = "Voss",
                description = "Ajan",
                role = "Yan Karakter",
                firstAppearedAt = System.currentTimeMillis(),
                isAutoAdded = true
            )
        )

        // 3. Finalize rollback snapshot (as done after reply + post-turn extraction)
        repo.finalizeTurnRollbackSnapshot(botId = botId, messageId = aiMsgId)

        // Sanity check that all turn side-effects exist before rollback
        assertEquals(2, db.memoryFactDao().getActiveFacts(botId).size)
        assertEquals(1, db.memoryFragmentDao().getRecentFragments(botId, 50).size)
        assertEquals(1, db.memoryEventDao().getActiveEvents(botId).size)
        assertEquals(1, db.milestoneDao().getMilestonesForBot(botId).size)
        assertNotNull(db.castMemberDao().findByName(botId, "Voss"))

        // 4. Delete the message -> MUST trigger FULL atomic rollback!
        repo.deleteMessage(aiMsgId)

        // 5. Verify 100% lossless restoration across EVERY table:
        assertNull("Deleted message must be gone", db.messageDao().getMessageById(aiMsgId))

        val factsAfter = db.memoryFactDao().getActiveFacts(botId)
        assertEquals("Only the original pre-turn fact must remain active", 1, factsAfter.size)
        assertEquals("Original superseded fact must be restored!", preFact.id, factsAfter.single().id)
        assertTrue(factsAfter.single().value.contains("Brooklyn"))

        assertEquals("Hidden RAG fragments from turn must be deleted", 0, db.memoryFragmentDao().getRecentFragments(botId, 50).size)
        assertEquals("Living Memories from turn must be deleted", 0, db.memoryEventDao().getActiveEvents(botId).size)
        assertEquals("Milestones from turn must be deleted", 0, db.milestoneDao().getMilestonesForBot(botId).size)
        assertEquals("Archives created by turn must be deleted", 0, db.archivedMemoryDao().getArchivesForBot(botId).size)
        assertNull("Auto-added cast member from turn must be deleted", db.castMemberDao().findByName(botId, "Voss"))

        val wandaAfter = db.castMemberDao().findByName(botId, "Wanda")!!
        assertEquals("Existing side cast affection must revert to pre-turn value", 50, wandaAfter.affectionScore)
        assertEquals("Tanıdık", wandaAfter.relationshipState)
        val wandaEmoAfter = EmotionState.fromJson(db.characterEmotionDao().getEmotionForCharacter(botId, "Wanda")!!.emotionState)
        assertEquals(50, wandaEmoAfter.affection)

        val botAfter = db.botDao().getBotById(botId)!!
        val botEmoAfter = EmotionState.fromJson(botAfter.emotionState)
        assertEquals("Bot affection must revert to pre-turn 50", 50, botEmoAfter.affection)
        assertEquals("Bot trust must revert to pre-turn 50", 50, botEmoAfter.trust)
        assertEquals("Bot storyDayCounter must revert to 1", 1L, botAfter.storyDayCounter)
        assertEquals("gece", botAfter.sceneTime)
    }
}
