package com.example

import com.example.book.CosmicDriftBookRegistry
import com.example.ui.components.evictGlobalSettingsRefPlatesCache
import com.example.util.KeystoreEncryptionManager
import com.example.util.SecureApiKeyVault
import com.example.work.RoutineNotificationWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorkersAndSettingsIntegrityOverhaulTest {

    @Test
    fun test1_routineNotificationWorker_skipsBookBotsQuietHoursAndEnglishArtifacts() {
        // Book mode bots and Cosmic Drift preset MUST NEVER trigger RP routine notifications
        assertFalse(
            "Book mode bot must be skipped",
            RoutineNotificationWorker.shouldNotifyRoutine(
                botId = "bot_custom_book",
                activeBotId = null,
                isRoutineNotifEnabled = true,
                proactiveEnabled = true,
                currentHourOfDay = 14,
                botMode = "book"
            )
        )
        assertFalse(
            "Cosmic Drift deterministic book preset must be skipped",
            RoutineNotificationWorker.shouldNotifyRoutine(
                botId = CosmicDriftBookRegistry.BOOK_ID,
                activeBotId = null,
                isRoutineNotifEnabled = true,
                proactiveEnabled = true,
                currentHourOfDay = 14,
                botMode = "personal"
            )
        )
        // Normal RP bot during daytime (14:00) when not active -> allowed
        assertTrue(
            RoutineNotificationWorker.shouldNotifyRoutine(
                botId = "bot_rp_1",
                activeBotId = "other_bot",
                isRoutineNotifEnabled = true,
                proactiveEnabled = true,
                currentHourOfDay = 14,
                botMode = "personal"
            )
        )
        // Quiet hours (23:00..07:59) -> blocked
        assertFalse(
            RoutineNotificationWorker.shouldNotifyRoutine(
                botId = "bot_rp_1",
                activeBotId = null,
                isRoutineNotifEnabled = true,
                proactiveEnabled = true,
                currentHourOfDay = 23,
                botMode = "personal"
            )
        )

        // Turkish sanitization strips internal tags and blocks English-dominant routine artifacts
        val cleanTr = RoutineNotificationWorker.sanitizeRoutineNotificationText(
            "<think>plan</think>Sabah 08:00'de kahve içip günlük antrenmana başlıyor. [[STATE_JSON {}]]"
        )
        assertNotNull(cleanTr)
        assertFalse(cleanTr!!.contains("STATE_JSON"))
        assertFalse(cleanTr.contains("plan"))
        assertTrue(cleanTr.contains("kahve"))

        val rejectedEn = RoutineNotificationWorker.sanitizeRoutineNotificationText(
            "Every morning she walks through the garden and reads her private notebook before training."
        )
        assertNull("English-dominant routine text must be suppressed", rejectedEn)
    }

    @Test
    fun test2_routineNotificationWorker_stableNotificationIdsAndDayMatching() {
        val idA1 = RoutineNotificationWorker.computeStableNotificationId(101L)
        val idA2 = RoutineNotificationWorker.computeStableNotificationId(101L)
        val idB = RoutineNotificationWorker.computeStableNotificationId(202L)
        assertEquals(idA1, idA2)
        assertNotEquals(idA1, idB)
        assertTrue(idA1 >= 9100)

        assertTrue(RoutineNotificationWorker.matchesDayOfWeek("Çarşamba", Calendar.WEDNESDAY))
        assertTrue(RoutineNotificationWorker.matchesDayOfWeek("carsamba", Calendar.WEDNESDAY))
        assertTrue(RoutineNotificationWorker.matchesDayOfWeek("Wednesday", Calendar.WEDNESDAY))
        assertFalse(RoutineNotificationWorker.matchesDayOfWeek("Pazartesi", Calendar.WEDNESDAY))
    }

    @Test
    fun test3_globalSettingsFlexibleDecryptAndRefPlatesCacheEviction() {
        // Simulates GlobalSettingsModal decrypting both newly saved ENC_V2 custom providers AND legacy keys
        val secret = "sk-ant-api03-globalSettingsTestKey1234567890"
        val encV2 = SecureApiKeyVault.encrypt(secret)
        val encLegacy = KeystoreEncryptionManager.encrypt(secret)

        assertEquals(secret, SecureApiKeyVault.decryptFlexible(encV2))
        assertEquals(secret, SecureApiKeyVault.decryptFlexible(encLegacy))

        // Cache eviction must be safe to call repeatedly (e.g., from Application.onTrimMemory)
        evictGlobalSettingsRefPlatesCache()
        evictGlobalSettingsRefPlatesCache()
    }
}
