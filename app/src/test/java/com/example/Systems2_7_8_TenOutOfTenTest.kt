package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BotEntity
import com.example.data.local.EntityRegistryEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.managers.ProviderFallbackManager
import com.example.data.managers.ProviderHealthTracker
import com.example.data.repository.EmochiRepository
import com.example.data.repository.SystemPromptBuilder
import com.example.ui.viewmodel.EmochiViewModel
import com.example.ui.viewmodel.UiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Systems2_7_8_TenOutOfTenTest {

    private lateinit var app: Application
    private lateinit var db: AppDatabase
    private lateinit var repo: EmochiRepository

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext<Context>() as Application
        ProviderHealthTracker.resetAll()
        db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = EmochiRepository(db, app)
    }

    @After
    fun tearDown() {
        EmochiRepository.activeBotId = null
        db.close()
    }

    // ── System #8: SystemPromptBuilder ────────────────────────────────────────

    @Test
    fun system8_stateJsonSchemaAlignedAndActiveInCompactMode() {
        val bot = BotEntity(
            id = "b_prompt_v3",
            mode = "personal",
            aiName = "Aiden",
            aiPersonality = "Soğukkanlı.",
            scenario = "Kütüphane.",
            universeName = "",
            keyCharactersJson = "[]",
            userCharName = "Elif",
            userCharDesc = "Araştırmacı.",
            openingMessage = "",
            writingStyle = "rp",
            intensity = "normal",
            worldAtmosphere = ""
        )
        val settings = UserSettingsEntity()

        val fullPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = settings,
                registeredEntities = listOf(
                    EntityRegistryEntity(
                        id = 1L,
                        botId = bot.id,
                        entityName = "Kuzey Kulesi",
                        entityType = "mekan",
                        description = "Eski gözlemevi"
                    )
                )
            )
        )
        assertTrue("Full prompt must emit parser-aligned dominant_emotion key", fullPrompt.contains("\"dominant_emotion\""))
        assertTrue("Full prompt must emit parser-aligned delta.value key", fullPrompt.contains("\"delta\":{\"value\":0"))
        assertTrue("Registered entities must be injected", fullPrompt.contains("Kuzey Kulesi"))
        assertFalse(
            "Fresh personal bot with empty worldAtmosphere must not emit stub WorldState block",
            fullPrompt.contains("[SAHNE MEKANI VE EVREN ATMOSFERİ]")
        )

        val compactPrompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = settings,
                compactMode = true
            )
        )
        assertTrue("Compact mode must still emit [[STATE_JSON]] so emotions never freeze", compactPrompt.contains("[[STATE_JSON"))
        assertTrue("Compact mode must use dominant_emotion key", compactPrompt.contains("\"dominant_emotion\""))
    }

    @Test
    fun system8_tokenBudgetCapsOversizedPersonalityAndScenario() {
        val hugeText = "Uzun geçmiş detayı ve kurgu açıklaması. ".repeat(500)
        val bot = BotEntity(
            id = "b_prompt_budget",
            mode = "personal",
            aiName = "Aiden",
            aiPersonality = hugeText,
            scenario = hugeText,
            pinnedMemory = hugeText,
            storyNotes = hugeText,
            universeName = "",
            keyCharactersJson = "[]",
            userCharName = "Elif",
            userCharDesc = hugeText,
            openingMessage = "",
            writingStyle = "rp",
            intensity = "normal"
        )
        val prompt = SystemPromptBuilder.build(
            SystemPromptBuilder.PromptBuildInput(
                bot = bot,
                settings = UserSettingsEntity(),
                compactMode = true
            )
        )
        val tokens = SystemPromptBuilder.estimateTokens(prompt)
        assertTrue("Compact prompt tokens ($tokens) must stay strictly bounded", tokens <= 3_400)
    }

    // ── System #2: ProviderFallbackManager ───────────────────────────────────

    @Test
    fun system2_ovhCooldownUnifiedInProviderHealthTracker() {
        EmochiRepository.ovhCooldownUntilTimestamp = System.currentTimeMillis() + 600_000L
        val manager = ProviderFallbackManager(
            customProviderDao = db.customProviderDao(),
            onLogFallback = {}
        )
        val settings = UserSettingsEntity(
            enableOvh = true,
            ovhEnabled = true,
            enableLlm7 = false,
            llm7Enabled = false,
            enablePollinations = false,
            pollinationsEnabled = false
        )
        val chain = manager.buildProviderChain(settings, emptyMap(), "")
        assertTrue(
            "buildProviderChain must rely on ProviderHealthTracker, not stale EmochiRepository.ovhCooldownUntilTimestamp",
            chain.contains("ovh")
        )
        EmochiRepository.ovhCooldownUntilTimestamp = 0L
    }

    // ── System #7: EmochiViewModel ───────────────────────────────────────────

    @Test
    fun system7_viewModelConstructorInjectionAndCentralizedActiveBotSync() {
        val vm = EmochiViewModel(app, repository = repo)
        assertEquals(UiState.Menu, vm.uiState.value)
        assertNull(vm.activeBotId.value)
        assertNull(EmochiRepository.activeBotId)

        vm.openBot("bot_test_sync")
        assertEquals("bot_test_sync", vm.activeBotId.value)
        assertEquals("bot_test_sync", EmochiRepository.activeBotId)
        assertEquals(UiState.Chat("bot_test_sync"), vm.uiState.value)

        vm.stopGeneration()
        assertNull(vm.streamingMsgId.value)
        assertNull(vm.streamingText.value)

        vm.openMenu()
        assertNull(vm.activeBotId.value)
        assertNull(EmochiRepository.activeBotId)
        assertEquals(UiState.Menu, vm.uiState.value)
    }
}
