package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.ChatTypographyStore
import com.example.util.RoleplayMacroStore
import com.example.util.TokenBudgetEstimator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V52MegaBatchUpgradeTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun chatTypographyStore_clampsAndPersistsFontScale() {
        val clampedHigh = ChatTypographyStore.saveFontScale(context, 3.0f)
        assertEquals(ChatTypographyStore.MAX_SCALE, clampedHigh, 0.001f)
        assertEquals(ChatTypographyStore.MAX_SCALE, ChatTypographyStore.loadFontScale(context), 0.001f)

        val normal = ChatTypographyStore.saveFontScale(context, 1.12f)
        assertEquals(1.12f, normal, 0.001f)
    }

    @Test
    fun roleplayMacroStore_loadsDefaultsAddsAndRemovesCustomMacros() {
        val defaults = RoleplayMacroStore.loadMacros(context)
        assertTrue(defaults.size >= 3)

        val added = RoleplayMacroStore.addMacro(context, "Kılıç Çek", "*Kılıcını çekip öne adım atar.*")
        assertTrue(added.any { it.label == "Kılıç Çek" })
        val customId = added.first { it.label == "Kılıç Çek" }.id

        val afterRemove = RoleplayMacroStore.removeMacro(context, customId)
        assertTrue(afterRemove.none { it.id == customId })
    }

    @Test
    fun tokenBudgetEstimator_estimatesTurkishTokensAndContextFill() {
        val tokens = TokenBudgetEstimator.estimateTokens(1000)
        assertEquals(1350, tokens)
        assertEquals("~1.4K token", TokenBudgetEstimator.formatTokenCount(tokens))
        assertEquals(50, TokenBudgetEstimator.contextFillPercent(64_000, 128_000))
    }
}
