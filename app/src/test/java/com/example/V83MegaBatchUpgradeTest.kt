package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.BackupRetentionAdvisor
import com.example.util.CharacterDialogueToneDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V83MegaBatchUpgradeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun characterDialogueToneDetector_classifiesTurkishDeliveryTones() {
        assertEquals("🤫 Fısıltı", CharacterDialogueToneDetector.detectTone("*Kulağına fısıldar.* Kimseye söyleme."))
        assertEquals("🔥 Yüksek Ton", CharacterDialogueToneDetector.detectTone("Hemen kuleyi terk edin?!"))
        assertEquals("❓ Sorgulayıcı", CharacterDialogueToneDetector.detectTone("Buraya nasıl geldin?"))
        assertEquals("🌙 Düşünceli", CharacterDialogueToneDetector.detectTone("Belki de zamanı gelmiştir..."))
        assertEquals("✦ Dengeli", CharacterDialogueToneDetector.detectTone("Kapıyı açıp içeri girdi."))
    }

    @Test
    fun backupRetentionAdvisor_tracksFreshAndStaleBackupStates() {
        val day0 = 1_700_000_000_000L
        val initial = BackupRetentionAdvisor.getStatus(context, day0)
        assertTrue(initial.isReminderDue)
        assertTrue(initial.label.contains("Henüz yedek alınmadı"))

        BackupRetentionAdvisor.recordBackup(context, day0)
        val fresh = BackupRetentionAdvisor.getStatus(context, day0 + 3_600_000L)
        assertFalse(fresh.isReminderDue)
        assertTrue(fresh.label.contains("1 sa önce"))

        val stale = BackupRetentionAdvisor.getStatus(context, day0 + 10L * 86_400_000L)
        assertTrue(stale.isReminderDue)
        assertTrue(stale.label.contains("Yeni yedek önerilir"))
    }
}
