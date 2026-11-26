package com.example

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.ui.animation.CosmicMotionConfig
import com.example.ui.animation.MotionIntensityMode
import com.example.ui.animation.MotionPolicy
import com.example.util.FrostedBackdropCache
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CoreInfraAndMotionOverhaulTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        FrostedBackdropCache.evictAll()
    }

    @Test
    fun test1_mainActivity_extractOpenChatBotIdHandlesValidAndMalformedIntents() {
        val valid = Intent(MainActivity.ACTION_OPEN_CHAT).apply {
            putExtra(MainActivity.EXTRA_BOT_ID, "  bot_natasha_1  ")
        }
        assertEquals("bot_natasha_1", MainActivity.extractOpenChatBotId(valid))

        val wrongAction = Intent("android.intent.action.MAIN").apply {
            putExtra(MainActivity.EXTRA_BOT_ID, "bot_natasha_1")
        }
        assertNull(MainActivity.extractOpenChatBotId(wrongAction))

        val blankExtra = Intent(MainActivity.ACTION_OPEN_CHAT).apply {
            putExtra(MainActivity.EXTRA_BOT_ID, "   ")
        }
        assertNull(MainActivity.extractOpenChatBotId(blankExtra))
        assertNull(MainActivity.extractOpenChatBotId(null))
    }

    @Test
    fun test2_frostedBackdropCache_singleFlightDeduplicatesConcurrentBlursAndEvictsCleanly() = runBlocking {
        // Put a placeholder-backed entry and verify concurrent getOrGenerate shares the single cached object
        val jobs = (1..8).map {
            async {
                FrostedBackdropCache.getOrGenerate(
                    context = context,
                    key = "test_single_flight_bg",
                    drawableResId = R.drawable.img_global_settings_cosmic_bg,
                    targetWidth = 64,
                    maxHeight = 96,
                    blurRadius = 2,
                    blurPasses = 1
                )
            }
        }
        val results = jobs.awaitAll()
        val first = results.first()
        assertNotNull("Frosted backdrop must generate cleanly", first)
        results.forEach { bmp ->
            assertSame("All concurrent callers must receive the exact same cached ImageBitmap instance", first, bmp)
        }
        assertEquals(1, FrostedBackdropCache.size())

        FrostedBackdropCache.evictAll()
        assertEquals(0, FrostedBackdropCache.size())
    }

    @Test
    fun test3_cosmicMotionPolicy_disablesAmbientLoopsInBackgroundAndPowerSave() {
        val bgPolicy = MotionPolicy(
            reduceMotion = false,
            isAppInForeground = false,
            isPowerSaveMode = false,
            isSystemAnimatorDisabled = false
        )
        assertFalse(bgPolicy.shouldAnimateContinuously)
        val bgCfg = CosmicMotionConfig.effective(MotionIntensityMode.FULL, bgPolicy)
        assertFalse(bgCfg.enableAmbientLoops)
        assertFalse(bgCfg.enableParticles)

        val powerSavePolicy = MotionPolicy(
            reduceMotion = false,
            isAppInForeground = true,
            isPowerSaveMode = true,
            isSystemAnimatorDisabled = false
        )
        assertTrue(powerSavePolicy.shouldUseQuickTransitions)
        val psCfg = CosmicMotionConfig.effective(MotionIntensityMode.FULL, powerSavePolicy)
        assertEquals(MotionIntensityMode.REDUCED, psCfg.mode)
        assertFalse(psCfg.enableAmbientLoops)
    }
}
