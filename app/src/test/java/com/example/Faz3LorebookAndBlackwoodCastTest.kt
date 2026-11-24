package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.LorebookEntity
import com.example.data.local.LorebookEntryEntity
import com.example.data.managers.LorebookManager
import com.example.data.repository.EmochiRepository
import com.example.data.repository.KeyCharacter
import com.example.ui.components.collectEmotionStudioCharacters
import com.example.ui.screens.getAidenPresetsList
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Faz3LorebookAndBlackwoodCastTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: EmochiRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EmochiRepository(db, context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun allTenAidenPresetsExistInTrAndEnWithPopulatedCharacterLists() {
        for (isEn in listOf(false, true)) {
            val presets = getAidenPresetsList(isEnglish = isEn)
            assertEquals("Expected 10 Aiden Blackwood presets (isEnglish=$isEn)", 10, presets.size)

            presets.forEach { preset ->
                val arr = JSONArray(preset.keyCharactersJson)
                assertTrue(
                    "Preset ${preset.id} (isEnglish=$isEn) must have non-empty keyCharactersJson",
                    arr.length() >= 4
                )
            }

            val nevermore = presets.firstOrNull { it.id == "preset_aiden_nevermore_wednesday" }
            assertNotNull("Nevermore preset missing (isEnglish=$isEn)", nevermore)
            nevermore!!
            val nevermoreCast = JSONArray(nevermore.keyCharactersJson)
            assertEquals("Nevermore must include all 18 cast characters", 18, nevermoreCast.length())
            assertTrue(nevermore.aiPersonality.contains("Galatasaray", ignoreCase = true))
            assertTrue(nevermore.scenario.contains("Kaya", ignoreCase = true))
            assertTrue(nevermore.scenario.contains("Ophelia Hall", ignoreCase = true))

            val lambo = presets.firstOrNull { it.id == "preset_aiden_lambo_football" }
            assertNotNull("Lambo football preset missing (isEnglish=$isEn)", lambo)
            lambo!!
            assertEquals("Lambo football must include 9 cast characters", 9, JSONArray(lambo.keyCharactersJson).length())
            assertTrue(lambo.aiPersonality.contains("Marie Blackwood", ignoreCase = true))
            assertTrue(lambo.aiPersonality.contains("105", ignoreCase = true))
            assertTrue(lambo.aiPersonality.contains("Mira", ignoreCase = true))
            assertTrue(lambo.scenario.contains("Lucía Vega", ignoreCase = true))
            assertTrue(lambo.scenario.contains("Lambo", ignoreCase = true))

            val marvelThanos = presets.firstOrNull { it.id == "preset_aiden_marvel_thanos" }
            assertNotNull("Marvel Thanos preset missing (isEnglish=$isEn)", marvelThanos)
            marvelThanos!!
            assertEquals("Marvel Thanos must include 9 cast characters", 9, JSONArray(marvelThanos.keyCharactersJson).length())
            assertTrue(marvelThanos.aiPersonality.contains("Thanos", ignoreCase = true))
            assertTrue(marvelThanos.aiPersonality.contains("Carol Danvers", ignoreCase = true))
            assertTrue(marvelThanos.openingMessage.contains("Carol", ignoreCase = true))
            assertTrue(marvelThanos.openingMessage.contains("Stark", ignoreCase = true))

            val spiderMan = presets.firstOrNull { it.id == "preset_aiden_impossible_spiderman" }
            assertNotNull("Impossible Spider-Man preset missing (isEnglish=$isEn)", spiderMan)
            spiderMan!!
            assertEquals("Impossible Spider-Man must include 6 main cast characters", 6, JSONArray(spiderMan.keyCharactersJson).length())
            assertTrue(spiderMan.keyCharactersJson.contains("Gwen Stacy"))
            assertTrue(spiderMan.keyCharactersJson.contains("Mary Jane Watson"))
            assertFalse("Generic school bully must not be in Spider-Man main cast", spiderMan.keyCharactersJson.contains("zorba", ignoreCase = true))
            assertTrue(spiderMan.openingMessage.contains("Gwen Stacy"))
            assertTrue(spiderMan.openingMessage.contains("Mary Jane Watson"))
            assertTrue(
                "Opening message must contain 1-week time-skip",
                spiderMan.openingMessage.contains("1 Hafta Sonra") || spiderMan.openingMessage.contains("1 Week Later")
            )

            val eilish = presets.firstOrNull { it.id == "preset_aiden_eilish_family" }
            assertNotNull("Aiden Eilish preset missing (isEnglish=$isEn)", eilish)
            eilish!!
            assertEquals("Aiden Eilish must include all 8 family & Myers cast characters", 8, JSONArray(eilish.keyCharactersJson).length())
            assertTrue(eilish.keyCharactersJson.contains("Billie Eilish"))
            assertTrue(eilish.keyCharactersJson.contains("Emma Myers"))
            assertTrue(eilish.keyCharactersJson.contains("Maggie Eilish"))
            assertTrue(eilish.aiPersonality.contains("Lo siento"))
        }
    }

    @Test
    fun normalTemplatesIncludeFourOriginalNonAidenBotsWithPopulatedCasts() {
        val originalIds = setOf(
            "preset_crimson_clocktower",
            "preset_neon_kowloon_noir",
            "preset_stormbreak_corsairs",
            "preset_aurora_lodge"
        )
        for (isEn in listOf(false, true)) {
            val appPresets = com.example.ui.screens.getAppPresets(isEnglish = isEn)
            assertEquals("Expected 7 total normal templates (isEnglish=$isEn)", 7, appPresets.size)
            val added = appPresets.filter { it.id in originalIds }
            assertEquals("All 4 original templates must exist", 4, added.size)
            added.forEach { tpl ->
                assertFalse("Original template ${tpl.id} must not borrow Aiden Blackwood", tpl.aiName.contains("Aiden", ignoreCase = true))
                assertFalse("Original template ${tpl.id} scenario must not borrow Blackwood", tpl.scenario.contains("Blackwood", ignoreCase = true))
                assertTrue("Original template ${tpl.id} must have >= 4 cast members", JSONArray(tpl.keyCharactersJson).length() >= 4)
            }
        }
    }

    @Test
    fun dynamicCastGrowthBlocksMinorGenericExtrasWhileAllowingRecurringNamedCharacters() = runBlocking {
        // 1. Generic extra detector rejects unnamed bullies/extras in TR & EN
        assertTrue(com.example.data.repository.CastDetectorEngine.isGenericBackgroundExtra("Zorba Çocuk"))
        assertTrue(com.example.data.repository.CastDetectorEngine.isGenericBackgroundExtra("Okul Zorbası"))
        assertTrue(com.example.data.repository.CastDetectorEngine.isGenericBackgroundExtra("Kaba Lise Öğrencisi"))
        assertTrue(com.example.data.repository.CastDetectorEngine.isGenericBackgroundExtra("Garson"))
        assertTrue(com.example.data.repository.CastDetectorEngine.isGenericBackgroundExtra("School Bully"))
        assertFalse(com.example.data.repository.CastDetectorEngine.isGenericBackgroundExtra("Gwen Stacy"))
        assertFalse(com.example.data.repository.CastDetectorEngine.isGenericBackgroundExtra("Ned Leeds"))

        // 2. CastMemberManager refuses to register "Zorba Çocuk" even with high confidence, while growing cast with named characters
        val spideyBot = getAidenPresetsList(isEnglish = false).first { it.id == "preset_aiden_impossible_spiderman" }
        db.botDao().insertOrUpdate(spideyBot)
        val castMgr = com.example.data.managers.CastMemberManager(
            db.castMemberDao(),
            db.characterEmotionDao(),
            db.botDao()
        )

        castMgr.ensureSideCharacterRegistered(
            botId = spideyBot.id,
            rawCharacterName = "Zorba Çocuk",
            role = "Okul Zorbası",
            cueSentence = "Koridorda zorba çocuk Aiden'a omuz attı.",
            bot = spideyBot,
            confidence = 90,
            pendingConfirmation = false
        )
        castMgr.ensureSideCharacterRegistered(
            botId = spideyBot.id,
            rawCharacterName = "Ned Leeds",
            role = "Yakın Arkadaş",
            cueSentence = "Ned Leeds laboratuvarda Aiden'ın yanına oturdu.",
            bot = spideyBot,
            confidence = 88,
            pendingConfirmation = false
        )

        val storedCast = castMgr.getCastMembersForBot(spideyBot.id)
        assertFalse("Zorba Çocuk must never enter stored cast list", storedCast.any { it.name.contains("Zorba", ignoreCase = true) })
        assertTrue("Named character Ned Leeds must be added dynamically", storedCast.any { it.name == "Ned Leeds" })

        // 3. Emotion studio hides generic extras and unconfirmed 1-hit auto-added extras, while showing confirmed/recurring ones
        val studioList = collectEmotionStudioCharacters(
            bot = spideyBot,
            characterEmotions = listOf(
                CharacterEmotionEntity(botId = spideyBot.id, characterName = "Zorba Çocuk", emotionState = "{}")
            ),
            keyCharacters = emptyList(),
            castMembers = listOf(
                CastMemberEntity(botId = spideyBot.id, name = "Ned Leeds", role = "Arkadaş", description = "", isAutoAdded = true, pendingUserConfirmation = false, appearanceCount = 2),
                CastMemberEntity(botId = spideyBot.id, name = "Geçici Kişi", role = "Yan Karakter", description = "", isAutoAdded = true, pendingUserConfirmation = true, appearanceCount = 1, importanceScore = 55)
            )
        )
        val studioNames = studioList.map { it.first }
        assertTrue("Ned Leeds must appear in emotion studio", "Ned Leeds" in studioNames)
        assertFalse("Zorba Çocuk must not appear in emotion studio", studioNames.any { it.contains("Zorba", ignoreCase = true) })
        assertFalse("Unconfirmed single-hit auto-added character must wait until recurring/confirmed", "Geçici Kişi" in studioNames)
    }

    @Test
    fun bookMediaTestShowcaseCatalogCoversAllGamesAnimationsLive3DAndPhotos() {
        assertEquals(3, com.example.ui.screens.BookMediaTestCatalog.bossDuels.size)
        assertEquals(3, com.example.ui.screens.BookMediaTestCatalog.miniGames.size)
        assertEquals(5, com.example.ui.screens.BookMediaTestCatalog.animations.size)
        assertEquals(10, com.example.ui.screens.BookMediaTestCatalog.live3DEncounters.size)
        assertEquals(32, com.example.ui.screens.BookMediaTestCatalog.photos.size)
        assertEquals(
            "All photo IDs must be unique",
            com.example.ui.screens.BookMediaTestCatalog.photos.size,
            com.example.ui.screens.BookMediaTestCatalog.photos.map { it.id }.distinct().size
        )
    }

    @Test
    fun emotionStudioMergesKeyCharactersJsonCastMembersAndEmotionsWithoutDuplicates() {
        val nevermorePreset = getAidenPresetsList(isEnglish = false)
            .first { it.id == "preset_aiden_nevermore_wednesday" }

        val merged = collectEmotionStudioCharacters(
            bot = nevermorePreset,
            characterEmotions = listOf(
                CharacterEmotionEntity(botId = nevermorePreset.id, characterName = "Wednesday Addams", emotionState = "{}")
            ),
            keyCharacters = listOf(
                KeyCharacter(name = "enid sinclair", desc = "duplicate case variant"),
                KeyCharacter(name = "Aiden Blackwood", desc = "user self should be excluded")
            ),
            castMembers = listOf(
                CastMemberEntity(botId = nevermorePreset.id, name = "Thing", role = "El", description = "Addams yardımcısı", isBlacklisted = false),
                CastMemberEntity(botId = nevermorePreset.id, name = "Silinen Karakter", role = "Yok", description = "", isBlacklisted = true)
            )
        )

        val names = merged.map { it.first }
        assertTrue("Wednesday Addams must appear", names.any { it.equals("Wednesday Addams", ignoreCase = true) })
        assertTrue("Enid Sinclair must appear", names.any { it.equals("Enid Sinclair", ignoreCase = true) })
        assertTrue("CastMember Thing must appear", names.any { it == "Thing" })
        assertFalse("Blacklisted cast member must be excluded", names.any { it == "Silinen Karakter" })
        assertFalse("User self name must be excluded", names.any { it.equals("Aiden Blackwood", ignoreCase = true) })
        assertEquals("No case-insensitive duplicates allowed", names.map { it.lowercase() }.distinct().size, names.size)
        // 18 from Nevermore JSON + 1 extra CastMember ("Thing") = 19
        assertEquals(19, merged.size)
    }

    @Test
    fun lorebookEngineTurkishMatchingChainedTriggerAndTokenBudgetWork() {
        val lbId = "lb_test"
        val entries = listOf(
            LorebookEntryEntity(
                id = "e_const",
                lorebookId = lbId,
                title = "Kurucu Yasası",
                keywords = "",
                content = "Nevermore'un yarısı Blackwood soyuna aittir.",
                category = "kural",
                priority = 10,
                isConstant = true
            ),
            LorebookEntryEntity(
                id = "e_necklace",
                lorebookId = lbId,
                title = "Galatasaray Kolyesi",
                keywords = "kolye, galatasaray",
                content = "Kolye güç çekildiğinde kızıl parlar ve Ophelia Mahzeni ile rezonansa girer.",
                category = "esya",
                priority = 8,
                isConstant = false
            ),
            LorebookEntryEntity(
                id = "e_vault",
                lorebookId = lbId,
                title = "Ophelia Mahzeni",
                keywords = "mahzen, ophelia",
                content = "Ophelia Hall altındaki mahzende mühürlü Blackwood Kan Defteri saklıdır.",
                category = "mekan",
                priority = 7,
                isConstant = false
            ),
            LorebookEntryEntity(
                id = "e_unused",
                lorebookId = lbId,
                title = "Alakasız Madde",
                keywords = "tokyo, shibuya",
                content = "Bu madde tetiklenmemeli.",
                category = "mekan",
                priority = 9,
                isConstant = false
            )
        )

        val result = LorebookManager.selectEntriesForContext(
            entries = entries,
            recentMessageTexts = listOf("Aiden parmaklarıyla kolyesine dokunup sessizce baktı."),
            tokenBudget = 750,
            allowChainedTriggering = true
        )

        val selectedIds = result.selected.map { it.entry.id }
        assertTrue("Constant entry must trigger", "e_const" in selectedIds)
        assertTrue("Turkish suffix 'kolyesine' must match keyword 'kolye'", "e_necklace" in selectedIds)
        assertTrue("1-step chained entry 'Ophelia Mahzeni' must trigger via necklace content", "e_vault" in selectedIds)
        assertFalse("Unrelated entry must NOT trigger", "e_unused" in selectedIds)
        assertTrue("Constant highest priority comes first", selectedIds.first() == "e_const")
        assertTrue(result.promptBlock.contains("EVREN ANSİKLOPEDİSİ"))
        assertTrue(result.estimatedTokens <= 750)
    }

    @Test
    fun lorebookJsonExportImportRoundTripAndSillyTavernFormatWork() {
        val pack = LorebookManager.createNevermoreStarterPack()
        val exported = LorebookManager.exportLorebookJson(pack.lorebook, pack.entries)
        val reimported = LorebookManager.importLorebookJson(exported)

        assertEquals(pack.lorebook.name, reimported.lorebook.name)
        assertEquals(pack.entries.size, reimported.entries.size)
        assertTrue(reimported.entries.any { it.title.contains("Galatasaray", ignoreCase = true) })

        val sillyTavernJson = """
            {
              "name": "Jericho Arşivi",
              "entries": {
                "0": {
                  "comment": "Kan Defteri",
                  "key": ["defter", "kan"],
                  "content": "Kurucu ailelerin mühürlü antlaşması.",
                  "order": 9,
                  "constant": false
                }
              }
            }
        """.trimIndent()
        val stImported = LorebookManager.importLorebookJson(sillyTavernJson)
        assertEquals("Jericho Arşivi", stImported.lorebook.name)
        assertEquals(1, stImported.entries.size)
        assertEquals("Kan Defteri", stImported.entries.first().title)
        assertTrue(stImported.entries.first().keywords.contains("defter"))
    }

    @Test
    fun roomV49SharedMultiBotLorebookAndAidenCastBackfillWork() = runBlocking {
        // 1. Multi-bot shared Lorebook binding
        val lb = LorebookEntity(name = "Ortak Nevermore Evreni", tokenBudget = 700)
        repo.upsertLorebook(lb)
        repo.upsertLorebookEntry(
            LorebookEntryEntity(
                lorebookId = lb.id,
                title = "Kaya",
                keywords = "kaya, malikane",
                content = "İkinci Malikane'de Aiden'ı büyüten sadık vasi.",
                priority = 9
            )
        )
        repo.setLorebookLinkedToBot("bot_a", lb.id, true)
        repo.setLorebookLinkedToBot("bot_b", lb.id, true)

        val blockA = repo.lorebookManager.buildLorebookPromptBlockForBot("bot_a", listOf("Kaya'yı aradım."))
        val blockB = repo.lorebookManager.buildLorebookPromptBlockForBot("bot_b", listOf("Malikaneye döndük."))
        assertTrue(blockA.promptBlock.contains("Kaya"))
        assertTrue(blockB.promptBlock.contains("Kaya"))

        // 2. Backfill empty keyCharactersJson on an older Aiden preset chat
        val oldPresetChat = BotEntity(
            id = "legacy_aiden_zoktay_chat",
            mode = "universe",
            aiName = "Aiden Blackwood & Zoktay",
            aiPersonality = "Aiden Blackwood",
            scenario = "Istanbul",
            universeName = "Aiden Blackwood & Zoktay (GS & Manifest)",
            keyCharactersJson = "[]",
            userCharName = "Aiden Blackwood",
            userCharDesc = "Galatasaray #9",
            openingMessage = "Istanbul...",
            writingStyle = "rp",
            intensity = "intense",
            isTemplate = false
        )
        db.botDao().insertOrUpdate(oldPresetChat)

        val backfilled = repo.backfillAidenPresetCharactersIfEmpty(oldPresetChat.id)
        assertTrue("Legacy empty Aiden chat should be backfilled", backfilled)

        val updated = db.botDao().getBotById(oldPresetChat.id)!!
        val parsed = repo.parseKeyCharacters(updated.keyCharactersJson)
        assertTrue("Backfilled Zoktay chat must have >= 8 characters", parsed.size >= 8)
        assertTrue(parsed.any { it.name.contains("Zeynep", ignoreCase = true) })
    }
}
