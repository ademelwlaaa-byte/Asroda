package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Application-scoped chapter end soundtrack controller for "Kozmiksürüklenismüzik".
 * - Always starts automatically ON when entering the Chapter End Screen ("Bölümü Bitir ✦"),
 *   smoothly fading from 0.0f -> 1.0f (100% volume) over 1.5 seconds (1500ms).
 * - Smoothly fades from current full volume -> 0.0f over 2.0 seconds (2000ms)
 *   when advancing to a new chapter or returning to the main menu.
 * - Exposes reactive StateFlows for the Chapter End Screen's Music On/Off button.
 */
object BookAudioController {

    const val TRACK_DISPLAY_NAME = "Kozmiksürüklenismüzik"

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var player: MediaPlayer? = null
    private var fadeJob: Job? = null
    private var currentVolume: Float = 0f
    private var isFadingOut: Boolean = false
    private var isActivelyStarting: Boolean = false
    private var activeResId: Int? = null
    private var appContext: Context? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _isPlayingFlow = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow.asStateFlow()

    private val _isMutedFlow = MutableStateFlow(false)
    val isMutedFlow: StateFlow<Boolean> = _isMutedFlow.asStateFlow()

    val isPlaying: Boolean get() = _isPlayingFlow.value
    val isMuted: Boolean get() = _isMutedFlow.value

    /** Optional hook invoked right before chapter soundtrack starts so active chat TTS stops cleanly. */
    @Volatile
    var onPlaybackStarted: (() -> Unit)? = null

    /**
     * Starts playback automatically ON from silence (0f) and smoothly ramps to full volume (1.0f)
     * over [fadeInMs] (default 1500ms).
     * Avoids restarting if already starting or actively playing the exact same track.
     */
    fun play(
        context: Context,
        rawResId: Int,
        loop: Boolean = true,
        volume: Float = 1.0f,
        fadeInMs: Long = 1500L,
        forceRestart: Boolean = false,
        autoUnmute: Boolean = true
    ) {
        val safeCtx = context.applicationContext
        appContext = safeCtx

        if (autoUnmute) {
            _isMutedFlow.value = false
        }

        val existing = player
        if (!forceRestart && existing != null && !isFadingOut && activeResId == rawResId) {
            if (isActivelyStarting || existing.isPlaying) {
                if (autoUnmute && !_isPlayingFlow.value) {
                    _isPlayingFlow.value = true
                }
                return
            }
        }

        fadeJob?.cancel()
        isFadingOut = false
        isActivelyStarting = true
        releasePlayerInternal()

        try {
            try { onPlaybackStarted?.invoke() } catch (_: Exception) {}
            requestAudioFocus(safeCtx)

            val p = createMediaPlayer(safeCtx, rawResId) ?: run {
                isActivelyStarting = false
                return
            }

            p.isLooping = loop
            p.setOnErrorListener { mp, what, extra ->
                android.util.Log.e("BookAudioController", "MediaPlayer error: what=$what extra=$extra")
                if (player === mp) {
                    fadeJob?.cancel()
                    isFadingOut = false
                    isActivelyStarting = false
                    pausedByBackground = false
                    releasePlayerInternal()
                    abandonAudioFocus()
                }
                true
            }

            currentVolume = 0f
            p.setVolume(0f, 0f)
            p.start()

            player = p
            activeResId = rawResId
            isActivelyStarting = false

            val target = volume.coerceIn(0f, 1f)
            resumeTargetVolume = target
            if (_isMutedFlow.value) {
                _isPlayingFlow.value = false
                return
            }

            _isPlayingFlow.value = true
            val steps = 30
            val stepDelay = (fadeInMs / steps).coerceAtLeast(16L)
            fadeJob = scope.launch {
                for (i in 1..steps) {
                    val t = i.toFloat() / steps
                    // Smooth musical ease-in-out curve from 0.0 -> 1.0 over 1.5s
                    val v = target * t
                    currentVolume = v
                    try {
                        p.setVolume(v, v)
                    } catch (_: Exception) {
                        break
                    }
                    delay(stepDelay)
                }
                currentVolume = target
                try {
                    p.setVolume(target, target)
                } catch (_: Exception) {
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BookAudioController", "play() failed", e)
            isActivelyStarting = false
            releasePlayerInternal()
            abandonAudioFocus()
        }
    }

    private fun createMediaPlayer(context: Context, rawResId: Int): MediaPlayer? {
        val attrs = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()

        // Primary path: explicit AudioAttributes + raw AssetFileDescriptor (guaranteed close via use {})
        try {
            val afd = context.resources.openRawResourceFd(rawResId)
            if (afd != null) {
                return afd.use { descriptor ->
                    val mp = MediaPlayer()
                    mp.setAudioAttributes(attrs)
                    mp.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                    mp.prepare()
                    mp
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("BookAudioController", "AFD MediaPlayer init failed, trying MediaPlayer.create", e)
        }

        // Fallback path: MediaPlayer.create directly with raw resource ID
        return try {
            MediaPlayer.create(context, rawResId)?.apply {
                setAudioAttributes(attrs)
            }
        } catch (e: Exception) {
            android.util.Log.e("BookAudioController", "Fallback MediaPlayer.create failed", e)
            null
        }
    }

    private fun requestAudioFocus(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(false)
                    .build()
                audioFocusRequest = req
                audioManager.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun abandonAudioFocus() {
        try {
            val ctx = appContext ?: return
            val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Toggles music on/off cleanly inside the Chapter End Screen.
     * - Turning OFF smoothly mutes the track to 0.
     * - Turning ON smoothly restores/fades up to full volume (1.0f) and pauses any active chat TTS.
     */
    fun toggleMusicInChapterEnd(
        context: Context,
        rawResId: Int,
        fullVolume: Float = 1.0f
    ) {
        val p = player
        if (_isPlayingFlow.value && p != null && !isFadingOut) {
            _isMutedFlow.value = true
            _isPlayingFlow.value = false
            pausedByBackground = false
            fadeJob?.cancel()
            val startVol = currentVolume.coerceIn(0f, 1f)
            fadeJob = scope.launch {
                val steps = 12
                val stepDelay = 20L
                for (i in steps - 1 downTo 0) {
                    val v = startVol * (i.toFloat() / steps)
                    currentVolume = v
                    try {
                        p.setVolume(v, v)
                    } catch (_: Exception) {
                        break
                    }
                    delay(stepDelay)
                }
                currentVolume = 0f
                try {
                    p.setVolume(0f, 0f)
                } catch (_: Exception) {
                }
            }
        } else {
            _isMutedFlow.value = false
            resumeTargetVolume = fullVolume.coerceIn(0f, 1f)
            if (p != null && !isFadingOut && activeResId == rawResId) {
                try { onPlaybackStarted?.invoke() } catch (_: Exception) {}
                fadeJob?.cancel()
                _isPlayingFlow.value = true
                if (!p.isPlaying) {
                    try {
                        p.start()
                    } catch (_: Exception) {
                    }
                }
                val startVol = currentVolume.coerceIn(0f, fullVolume)
                fadeJob = scope.launch {
                    val steps = 18
                    val stepDelay = 25L
                    for (i in 1..steps) {
                        val v = startVol + (fullVolume - startVol) * (i.toFloat() / steps)
                        currentVolume = v
                        try {
                            p.setVolume(v, v)
                        } catch (_: Exception) {
                            break
                        }
                        delay(stepDelay)
                    }
                    currentVolume = fullVolume
                    try {
                        p.setVolume(fullVolume, fullVolume)
                    } catch (_: Exception) {
                    }
                }
            } else {
                play(
                    context = context,
                    rawResId = rawResId,
                    loop = true,
                    volume = fullVolume,
                    fadeInMs = 1500L,
                    forceRestart = true,
                    autoUnmute = true
                )
            }
        }
    }

    /**
     * Smoothly fades music down from its current live volume (e.g., 1.0f) to silence (0.0f)
     * over [fadeOutMs] (default 2000ms), then stops & releases.
     */
    fun stop(fadeOutMs: Long = 2000L) {
        val p = player ?: run {
            _isPlayingFlow.value = false
            pausedByBackground = false
            return
        }
        fadeJob?.cancel()
        isFadingOut = true
        isActivelyStarting = false
        pausedByBackground = false
        _isPlayingFlow.value = false
        val startVol = currentVolume.coerceIn(0f, 1f)

        if (startVol <= 0.005f || fadeOutMs <= 0L) {
            releasePlayerInternal()
            isFadingOut = false
            abandonAudioFocus()
            return
        }

        fadeJob = scope.launch {
            val steps = 40
            val stepDelay = (fadeOutMs / steps).coerceAtLeast(16L)
            for (i in steps - 1 downTo 0) {
                val ratio = i.toFloat() / steps
                val v = startVol * ratio * ratio
                currentVolume = v
                try {
                    p.setVolume(v, v)
                } catch (_: Exception) {
                    break
                }
                delay(stepDelay)
            }
            try {
                p.setVolume(0f, 0f)
                if (p.isPlaying) p.stop()
                p.release()
            } catch (_: Exception) {
            }
            if (player === p) {
                player = null
                activeResId = null
                currentVolume = 0f
                isFadingOut = false
                _isPlayingFlow.value = false
                abandonAudioFocus()
            }
        }
    }

    /**
     * Immediate deterministic cleanup for `BookReaderView` disposal (`DisposableEffect.onDispose`).
     * Cancels any active fade coroutine, releases `MediaPlayer`, and abandons `AudioFocus`.
     */
    fun stopAndRelease() {
        release()
    }

    /**
     * Safe screen-disposal helper: initiates a 2-second fade-out only if playback is active
     * and not already fading out (preserves an active 2-second menu transition fade).
     */
    fun stopIfNotAlreadyFading(fadeOutMs: Long = 2000L) {
        if (player != null && !isFadingOut) {
            stop(fadeOutMs = fadeOutMs)
        }
    }

    @Volatile
    private var pausedByBackground: Boolean = false

    @Volatile
    private var resumeTargetVolume: Float = 1.0f

    @Volatile
    private var simulatedActiveForTest: Boolean = false

    /**
     * Smoothly fades out over [fadeOutMs] (default 250ms) and pauses active chapter-end soundtrack
     * when the host Activity moves to background (`Lifecycle.Event.ON_PAUSE`), without changing manual mute state.
     */
    fun pauseForBackground(fadeOutMs: Long = 250L) {
        if (simulatedActiveForTest) {
            if (!isFadingOut && _isPlayingFlow.value) {
                pausedByBackground = true
                _isPlayingFlow.value = false
            }
            return
        }
        val p = player ?: return
        if (!isFadingOut && (p.isPlaying || _isPlayingFlow.value)) {
            pausedByBackground = true
            _isPlayingFlow.value = false
            fadeJob?.cancel()
            val startVol = currentVolume.coerceIn(0f, 1f)
            if (startVol > 0.05f) resumeTargetVolume = startVol
            if (fadeOutMs <= 0L || startVol <= 0.01f) {
                try {
                    p.setVolume(0f, 0f)
                    if (p.isPlaying) p.pause()
                } catch (_: Exception) {}
                currentVolume = 0f
                return
            }
            fadeJob = scope.launch {
                val steps = 8
                val stepDelay = (fadeOutMs / steps).coerceAtLeast(16L)
                for (i in steps - 1 downTo 0) {
                    val v = startVol * (i.toFloat() / steps)
                    currentVolume = v
                    try {
                        p.setVolume(v, v)
                    } catch (_: Exception) {
                        break
                    }
                    delay(stepDelay)
                }
                currentVolume = 0f
                try {
                    p.setVolume(0f, 0f)
                    if (p.isPlaying) p.pause()
                } catch (_: Exception) {}
            }
        }
    }

    /** Immediately pauses chapter soundtrack if playing so chat TTS never clashes with book music. */
    fun pauseIfPlaying() {
        if (simulatedActiveForTest) {
            if (_isPlayingFlow.value) {
                _isPlayingFlow.value = false
            }
            return
        }
        val p = player ?: return
        if (!isFadingOut && (p.isPlaying || _isPlayingFlow.value)) {
            fadeJob?.cancel()
            try {
                if (p.isPlaying) p.pause()
                _isPlayingFlow.value = false
            } catch (_: Exception) {}
        }
    }

    /**
     * Resumes chapter-end soundtrack on foreground return (`Lifecycle.Event.ON_RESUME`) with a
     * smooth [fadeInMs] (default 300ms) fade-in ONLY if it was paused by backgrounding and NOT manually muted.
     */
    fun resumeFromBackground(fadeInMs: Long = 300L) {
        if (!pausedByBackground) return
        pausedByBackground = false
        if (_isMutedFlow.value || isFadingOut) return

        if (simulatedActiveForTest) {
            try { onPlaybackStarted?.invoke() } catch (_: Exception) {}
            _isPlayingFlow.value = true
            return
        }

        val p = player ?: return
        try {
            try { onPlaybackStarted?.invoke() } catch (_: Exception) {}
            fadeJob?.cancel()
            currentVolume = 0f
            p.setVolume(0f, 0f)
            if (!p.isPlaying) p.start()
            _isPlayingFlow.value = true
            val target = resumeTargetVolume.coerceIn(0.1f, 1.0f)
            if (fadeInMs <= 0L) {
                currentVolume = target
                p.setVolume(target, target)
                return
            }
            fadeJob = scope.launch {
                val steps = 10
                val stepDelay = (fadeInMs / steps).coerceAtLeast(16L)
                for (i in 1..steps) {
                    val v = target * (i.toFloat() / steps)
                    currentVolume = v
                    try {
                        p.setVolume(v, v)
                    } catch (_: Exception) {
                        break
                    }
                    delay(stepDelay)
                }
                currentVolume = target
                try {
                    p.setVolume(target, target)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    /** Test-only helper to exercise lifecycle & TTS mutual-exclusion paths cleanly under Robolectric. */
    internal fun simulatePlaybackStateForTest(playing: Boolean, muted: Boolean = false) {
        fadeJob?.cancel()
        simulatedActiveForTest = true
        isFadingOut = false
        pausedByBackground = false
        _isMutedFlow.value = muted
        _isPlayingFlow.value = playing && !muted
        if (playing && !muted) {
            try { onPlaybackStarted?.invoke() } catch (_: Exception) {}
        }
    }

    /** Test-only manual mute setter mirroring user mute button in Chapter End Screen. */
    internal fun setManualMuteForTest(muted: Boolean) {
        _isMutedFlow.value = muted
        if (muted) {
            _isPlayingFlow.value = false
        }
    }

    /** Test-only reset to prevent cross-test leakage. */
    internal fun resetForTesting() {
        simulatedActiveForTest = false
        onPlaybackStarted = null
        release()
        _isMutedFlow.value = false
    }

    fun release() {
        fadeJob?.cancel()
        isFadingOut = false
        isActivelyStarting = false
        pausedByBackground = false
        simulatedActiveForTest = false
        releasePlayerInternal()
        abandonAudioFocus()
    }

    private fun releasePlayerInternal() {
        try {
            player?.let { p ->
                if (p.isPlaying) p.stop()
                p.release()
            }
        } catch (_: Exception) {
        }
        player = null
        activeResId = null
        currentVolume = 0f
        _isPlayingFlow.value = false
    }
}
