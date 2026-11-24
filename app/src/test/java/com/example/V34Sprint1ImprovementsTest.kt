package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.AetheriaRefGlassCache
import com.example.util.ApiErrorFormatter
import com.example.util.BookAudioController
import com.example.util.ChatScrollPolicy
import com.example.util.SseStreamParser
import org.junit.After
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
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class V34Sprint1ImprovementsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        BookAudioController.resetForTesting()
        AetheriaRefGlassCache.evictAll()
    }

    @After
    fun tearDown() {
        BookAudioController.resetForTesting()
        AetheriaRefGlassCache.evictAll()
    }

    // ── 1. ChatScreen Streaming & Scroll Fluidity (ChatScrollPolicy) ────────

    @Test
    fun chatScrollPolicy_tallStreamingBubble_computesExactBottomOverflowAndKeepsSticky() {
        // Tall multi-paragraph streaming bubble extends 240px past viewport bottom
        val overflow = ChatScrollPolicy.computeBottomOverflowPx(
            totalItems = 10,
            lastVisibleIndex = 9,
            lastVisibleEndPx = 1240,
            viewportEndPx = 1000
        )
        assertEquals(240f, overflow)

        // Because user is NOT actively scrolling upward, sticky mode must stay engaged
        // even though the growing bubble temporarily pushed isAtBottom to false
        val isAtBottom = ChatScrollPolicy.isAtBottom(
            totalItems = 10,
            canScrollForward = true,
            lastVisibleIndex = 9,
            lastVisibleEndPx = 1240,
            viewportEndPx = 1000
        )
        assertFalse(isAtBottom)

        val stickyAfterGrowth = ChatScrollPolicy.nextStickyState(
            currentSticky = true,
            isUserScrollInProgress = false,
            isAtBottom = isAtBottom
        )
        assertTrue("Sticky mode must stay true while bubble grows without user scroll", stickyAfterGrowth)

        assertTrue(
            ChatScrollPolicy.shouldAutoScrollOnStreamChunk(
                chunk = "Yeni paragraf akışı...",
                isStickyToBottom = stickyAfterGrowth,
                isUserScrollInProgress = false,
                nowMs = 1000L,
                lastScrollMs = 800L
            )
        )
    }

    @Test
    fun chatScrollPolicy_userScrollUpDisengagesSticky_andSnapOrBottomReengages() {
        // User actively scrolls upward away from bottom -> sticky must disengage immediately
        val stickyAfterUserDrag = ChatScrollPolicy.nextStickyState(
            currentSticky = true,
            isUserScrollInProgress = true,
            isAtBottom = false
        )
        assertFalse(stickyAfterUserDrag)

        // Streaming chunks must NOT yank user back down while sticky is false
        assertFalse(
            ChatScrollPolicy.shouldAutoScrollOnStreamChunk(
                chunk = "Akış devam ediyor...",
                isStickyToBottom = stickyAfterUserDrag,
                isUserScrollInProgress = false,
                nowMs = 2000L,
                lastScrollMs = 0L
            )
        )

        // Sending a new message or tapping Scroll-to-Bottom FAB re-engages sticky
        assertTrue(
            ChatScrollPolicy.nextStickyState(
                currentSticky = false,
                isUserScrollInProgress = false,
                isAtBottom = false,
                userInitiatedSnapToBottom = true
            )
        )

        // Scrolling back to bottom naturally also re-engages sticky
        assertTrue(
            ChatScrollPolicy.nextStickyState(
                currentSticky = false,
                isUserScrollInProgress = true,
                isAtBottom = true
            )
        )
    }

    @Test
    fun chatScrollPolicy_lastItemOffscreenReturnsNullOverflowForFallbackScroll() {
        val overflow = ChatScrollPolicy.computeBottomOverflowPx(
            totalItems = 20,
            lastVisibleIndex = 12,
            lastVisibleEndPx = 980,
            viewportEndPx = 1000
        )
        assertNull("Off-screen last item should signal fallback to scrollToItem", overflow)
    }

    // ── 2. Memory/RAM Cache for AetheriaRefGlassBitmaps ─────────────────────

    @Test
    fun aetheriaRefGlassCache_cachesSingletonAndEvictsCleanly() {
        assertFalse(AetheriaRefGlassCache.isCached())
        assertNotNull(AetheriaRefGlassCache.placeholder)
        assertEquals(1, AetheriaRefGlassCache.placeholder.headerNoLetter.width)

        val stub = AetheriaRefGlassCache.placeholder
        AetheriaRefGlassCache.put(stub)
        assertTrue(AetheriaRefGlassCache.isCached())
        assertSame(stub, AetheriaRefGlassCache.get())
        assertSame(stub, AetheriaRefGlassCache.getOrLoadSync(context))

        AetheriaRefGlassCache.evictAll()
        assertFalse(AetheriaRefGlassCache.isCached())
        assertNull(AetheriaRefGlassCache.get())
    }

    // ── 3. Book Mode Audio & Scene Transitions ──────────────────────────────

    @Test
    fun bookAudioController_sameTrackVolumeUpgradeRampsFromAmbientToFull() {
        val trackResId = R.raw.kozmiksuruklenismuzik
        BookAudioController.simulatePlaybackStateForTest(
            playing = true,
            muted = false,
            rawResId = trackResId,
            volume = 0.28f
        )
        assertEquals(0.28f, BookAudioController.currentTargetVolumeForTest, 0.001f)
        assertEquals(0.28f, BookAudioController.currentVolumeForTest, 0.001f)

        // Entering Chapter End Screen requests same track at 1.0f volume
        BookAudioController.play(
            context = context,
            rawResId = trackResId,
            loop = true,
            volume = 1.0f,
            fadeInMs = 1500L,
            autoUnmute = true
        )

        assertTrue(BookAudioController.isPlayingFlow.value)
        assertEquals(1.0f, BookAudioController.currentTargetVolumeForTest, 0.001f)
        assertEquals(1.0f, BookAudioController.currentVolumeForTest, 0.001f)
    }

    @Test
    fun bookAudioController_pauseForBackgroundIsIdempotentAndResumesCleanly() {
        BookAudioController.simulatePlaybackStateForTest(playing = true, muted = false, volume = 0.8f)

        // Simulate both BookReaderView ON_STOP observer and MainActivity.onStop() firing
        BookAudioController.pauseForBackground(fadeOutMs = 250L)
        BookAudioController.pauseForBackground(fadeOutMs = 250L)

        assertFalse(BookAudioController.isPlayingFlow.value)
        assertFalse(BookAudioController.isMutedFlow.value)

        // Returning to foreground restores playback smoothly
        BookAudioController.resumeFromBackground(fadeInMs = 300L)
        assertTrue(BookAudioController.isPlayingFlow.value)
    }

    // ── 4. Custom API & Provider Error Formatter ────────────────────────────

    @Test
    fun apiErrorFormatter_formatsHttpCodesExtractsJsonAndRedactsSecrets() {
        val err401 = ApiErrorFormatter.formatHttpError(
            httpCode = 401,
            rawBody = """{"error":{"message":"Incorrect API key provided: sk-proj-1234567890abcdef1234567890"}}""",
            providerLabel = "Özel API"
        )
        assertTrue(err401.contains("[Özel API]"))
        assertTrue(err401.contains("HTTP 401"))
        assertTrue(err401.contains("API anahtarı"))
        assertFalse("Secret API key must never appear in UI error", err401.contains("sk-proj-1234567890"))

        val err404Html = ApiErrorFormatter.formatHttpError(
            httpCode = 404,
            rawBody = "<!DOCTYPE html><html><body><h1>404 Not Found</h1></body></html>"
        )
        assertTrue(err404Html.contains("HTTP 404"))
        assertTrue(err404Html.contains("Base URL"))
        assertFalse("Raw HTML must be stripped", err404Html.contains("<html"))

        val err402 = ApiErrorFormatter.formatHttpError(
            httpCode = 402,
            rawBody = """{"error":{"message":"Insufficient credit balance"}}"""
        )
        assertTrue(err402.contains("HTTP 402"))
        assertTrue(err402.contains("bakiye", ignoreCase = true) || err402.contains("kredi", ignoreCase = true))

        val err502 = ApiErrorFormatter.formatException(
            SseStreamParser.SseHttpException(502, "Bad Gateway")
        )
        assertTrue(err502.contains("HTTP 502"))
        assertTrue(err502.contains("sunucu", ignoreCase = true))
    }

    @Test
    fun apiErrorFormatter_formatsNetworkAndSslExceptionsActionably() {
        val dns = ApiErrorFormatter.formatException(UnknownHostException("Unable to resolve host \"bad.local\""))
        assertTrue(dns.contains("Base URL", ignoreCase = true))

        val refused = ApiErrorFormatter.formatException(ConnectException("Connection refused"))
        assertTrue(refused.contains("Ollama", ignoreCase = true) || refused.contains("bağlanılamadı", ignoreCase = true))

        val timeout = ApiErrorFormatter.formatException(SocketTimeoutException("Read timed out"))
        assertTrue(timeout.contains("zaman aşımı", ignoreCase = true))

        val ssl = ApiErrorFormatter.formatException(SSLHandshakeException("CertPathValidatorException"))
        assertTrue(ssl.contains("SSL/TLS", ignoreCase = true))
    }
}
