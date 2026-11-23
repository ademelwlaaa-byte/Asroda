package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Free, zero-API-key, zero-APK-bloat Neural TTS client using Microsoft Edge ReadAloud WebSocket
 * over the app's existing OkHttpClient.
 *
 * Features:
 *  - Curated Turkish & Multilingual studio-grade neural voices (`tr-TR-EmelNeural`, `tr-TR-AhmetNeural`, etc.)
 *  - `Sec-MS-GEC` SHA-256 token generation + automatic HTTP 403 clock-skew recovery (1 retry)
 *  - 15 MB LRU MP3 disk cache in `cacheDir/tts_audio_cache/` keyed by SHA-256(voice + ssml)
 *  - 4-second first-audio timeout: returns `null` cleanly on offline/error so [ChatTtsManager]
 *    falls back silently to enhanced device TTS with zero interruption.
 */
object EdgeNeuralTtsClient {

    private const val TAG = "EdgeNeuralTtsClient"
    private const val TRUSTED_CLIENT_TOKEN = "6A5AA1D4EAFF4E9FB37E23D68491D6F4"
    private const val CHROMIUM_FULL_VERSION = "130.0.2849.68"
    private const val SEC_MS_GEC_VERSION = "1-$CHROMIUM_FULL_VERSION"
    private const val WIN_EPOCH_OFFSET_SECONDS = 11644473600L
    private const val CACHE_DIR_NAME = "tts_audio_cache"
    private const val MAX_CACHE_BYTES = 15L * 1024L * 1024L // 15 MB LRU cap
    private const val SYNTHESIS_TIMEOUT_MS = 4_500L

    data class NeuralVoiceSpec(
        val id: String,
        val displayName: String,
        val subtitle: String,
        val localeTag: String,
        val isTurkish: Boolean
    )

    val NEURAL_VOICES: List<NeuralVoiceSpec> = listOf(
        NeuralVoiceSpec(
            id = "tr-TR-EmelNeural",
            displayName = "Emel (Nöral • Kadın)",
            subtitle = "Doğal, sıcak Türkçe roman sesi",
            localeTag = "tr-TR",
            isTurkish = true
        ),
        NeuralVoiceSpec(
            id = "tr-TR-AhmetNeural",
            displayName = "Ahmet (Nöral • Erkek)",
            subtitle = "Derin, akıcı Türkçe roman sesi",
            localeTag = "tr-TR",
            isTurkish = true
        ),
        NeuralVoiceSpec(
            id = "en-US-AvaMultilingualNeural",
            displayName = "Ava (Çok Dilli • Kadın)",
            subtitle = "Doğal çok dilli (TR/EN) nöral ses",
            localeTag = "en-US",
            isTurkish = true
        ),
        NeuralVoiceSpec(
            id = "en-US-AndrewMultilingualNeural",
            displayName = "Andrew (Çok Dilli • Erkek)",
            subtitle = "Derin çok dilli (TR/EN) nöral ses",
            localeTag = "en-US",
            isTurkish = true
        ),
        NeuralVoiceSpec(
            id = "en-US-AriaNeural",
            displayName = "Aria (Nöral • Kadın EN)",
            subtitle = "İfadeli İngilizce nöral ses",
            localeTag = "en-US",
            isTurkish = false
        ),
        NeuralVoiceSpec(
            id = "en-US-GuyNeural",
            displayName = "Guy (Nöral • Erkek EN)",
            subtitle = "Sıcak İngilizce nöral ses",
            localeTag = "en-US",
            isTurkish = false
        )
    )

    @Volatile
    private var clockSkewSeconds: Long = 0L

    /** Optional test hook for deterministic offline/error/fallback verification in Robolectric tests. */
    @Volatile
    internal var synthesizerOverride: (suspend (voiceId: String, ssml: String) -> ByteArray?)? = null

    /**
     * Synthesizes [ssml] into an MP3 [File] stored in the 15 MB LRU cache.
     * Returns `null` silently on any network/timeout/protocol error so callers can seamlessly
     * fall back to on-device TTS.
     */
    suspend fun synthesizeToCachedMp3(
        context: Context,
        voiceId: String,
        ssml: String,
        timeoutMs: Long = SYNTHESIS_TIMEOUT_MS
    ): File? = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
        val cacheKey = sha256Hex("$voiceId|$ssml")
        val cachedFile = File(cacheDir, "$cacheKey.mp3")
        if (cachedFile.exists() && cachedFile.length() > 128L) {
            cachedFile.setLastModified(System.currentTimeMillis())
            return@withContext cachedFile
        }

        val overrideFn = synthesizerOverride
        val audioBytes: ByteArray? = if (overrideFn != null) {
            try {
                withTimeoutOrNull(timeoutMs) { overrideFn(voiceId, ssml) }
            } catch (_: Exception) {
                null
            }
        } else {
            fetchWithClockSkewRetry(ssml, timeoutMs)
        }

        if (audioBytes == null || audioBytes.size < 128) {
            return@withContext null
        }

        try {
            val tmp = File(cacheDir, "$cacheKey.${UUID.randomUUID()}.tmp")
            tmp.writeBytes(audioBytes)
            if (!tmp.renameTo(cachedFile)) {
                tmp.copyTo(cachedFile, overwrite = true)
                tmp.delete()
            }
            enforceCacheLruLimit(cacheDir)
            cachedFile
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist TTS cache file: ${e.message}")
            null
        }
    }

    private sealed class WsFetchOutcome {
        data class Success(val mp3Bytes: ByteArray) : WsFetchOutcome()
        data class ClockSkew403(val serverDateHeader: String?) : WsFetchOutcome()
        data object Failed : WsFetchOutcome()
    }

    private suspend fun fetchWithClockSkewRetry(ssml: String, timeoutMs: Long): ByteArray? {
        val first = runSingleWebSocketFetch(ssml, timeoutMs)
        return when (first) {
            is WsFetchOutcome.Success -> first.mp3Bytes
            is WsFetchOutcome.ClockSkew403 -> {
                adjustClockSkewFromHeader(first.serverDateHeader)
                val second = runSingleWebSocketFetch(ssml, timeoutMs)
                (second as? WsFetchOutcome.Success)?.mp3Bytes
            }
            is WsFetchOutcome.Failed -> null
        }
    }

    private suspend fun runSingleWebSocketFetch(ssml: String, timeoutMs: Long): WsFetchOutcome {
        return withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { cont ->
                val connectionId = UUID.randomUUID().toString().replace("-", "")
                val muid = UUID.randomUUID().toString().replace("-", "").uppercase(Locale.ROOT)
                val secMsGec = generateSecMsGec()
                val wsUrl =
                    "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1" +
                        "?TrustedClientToken=$TRUSTED_CLIENT_TOKEN" +
                        "&Sec-MS-GEC=$secMsGec" +
                        "&Sec-MS-GEC-Version=$SEC_MS_GEC_VERSION" +
                        "&ConnectionId=$connectionId"

                val request = Request.Builder()
                    .url(wsUrl)
                    .header("Pragma", "no-cache")
                    .header("Cache-Control", "no-cache")
                    .header("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/$CHROMIUM_FULL_VERSION Safari/537.36 Edg/$CHROMIUM_FULL_VERSION"
                    )
                    .header("Cookie", "muid=$muid;")
                    .build()

                val wsClient = RetrofitClient.okHttpClient.newBuilder()
                    .connectTimeout(3_500L, TimeUnit.MILLISECONDS)
                    .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                    .writeTimeout(3_500L, TimeUnit.MILLISECONDS)
                    .build()

                val audioOut = ByteArrayOutputStream(16_384)
                val requestId = UUID.randomUUID().toString().replace("-", "")
                val timestamp = formatEdgeTimestamp()

                val listener = object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        val configMsg = buildString {
                            append("X-Timestamp:$timestamp\r\n")
                            append("Content-Type:application/json; charset=utf-8\r\n")
                            append("Path:speech.config\r\n\r\n")
                            append("""{"context":{"synthesis":{"audio":{"metadataoptions":{"sentenceBoundaryEnabled":"false","wordBoundaryEnabled":"false"},"outputFormat":"audio-24khz-48kbitrate-mono-mp3"}}}}""")
                            append("\r\n")
                        }
                        val ssmlMsg = buildString {
                            append("X-RequestId:$requestId\r\n")
                            append("Content-Type:application/ssml+xml\r\n")
                            append("X-Timestamp:${timestamp}Z\r\n")
                            append("Path:ssml\r\n\r\n")
                            append(ssml)
                        }
                        webSocket.send(configMsg)
                        webSocket.send(ssmlMsg)
                    }

                    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                        val raw = bytes.toByteArray()
                        if (raw.size < 2) return
                        // Edge binary frame: 2-byte big-endian header length + header + MP3 audio payload
                        val headerLen = ((raw[0].toInt() and 0xFF) shl 8) or (raw[1].toInt() and 0xFF)
                        val audioStart = 2 + headerLen
                        if (audioStart < raw.size) {
                            val headerStr = String(raw, 2, headerLen.coerceAtMost(raw.size - 2), Charsets.UTF_8)
                            if (headerStr.contains("Path:audio", ignoreCase = true)) {
                                audioOut.write(raw, audioStart, raw.size - audioStart)
                            }
                        }
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        if (text.contains("Path:turn.end", ignoreCase = true)) {
                            webSocket.close(1000, "done")
                            if (cont.isActive) {
                                val bytes = audioOut.toByteArray()
                                if (bytes.size >= 128) {
                                    cont.resume(WsFetchOutcome.Success(bytes))
                                } else {
                                    cont.resume(WsFetchOutcome.Failed)
                                }
                            }
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        if (cont.isActive) {
                            if (response?.code == 403) {
                                cont.resume(WsFetchOutcome.ClockSkew403(response.header("Date")))
                            } else {
                                cont.resume(WsFetchOutcome.Failed)
                            }
                        }
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        if (cont.isActive) {
                            val bytes = audioOut.toByteArray()
                            if (bytes.size >= 128) {
                                cont.resume(WsFetchOutcome.Success(bytes))
                            } else {
                                cont.resume(WsFetchOutcome.Failed)
                            }
                        }
                    }
                }

                val ws = wsClient.newWebSocket(request, listener)
                cont.invokeOnCancellation {
                    try {
                        ws.cancel()
                    } catch (_: Exception) {}
                }
            }
        } ?: WsFetchOutcome.Failed
    }

    /**
     * Generates the Microsoft Edge `Sec-MS-GEC` SHA-256 token rounded down to the current 5-minute
     * Windows file-time bucket.
     */
    internal fun generateSecMsGec(unixTimeSeconds: Long = System.currentTimeMillis() / 1000L): String {
        val adjustedUnix = unixTimeSeconds + clockSkewSeconds
        val winSeconds = adjustedUnix + WIN_EPOCH_OFFSET_SECONDS
        val roundedWinSeconds = winSeconds - (winSeconds % 300L)
        val ticks100Ns = roundedWinSeconds * 10_000_000L
        val payload = "${ticks100Ns}$TRUSTED_CLIENT_TOKEN"
        return sha256Hex(payload).uppercase(Locale.ROOT)
    }

    internal fun adjustClockSkewFromHeader(serverDateHeader: String?) {
        if (serverDateHeader.isNullOrBlank()) return
        try {
            val fmt = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("GMT")
            }
            val serverDate = fmt.parse(serverDateHeader) ?: return
            val serverSec = serverDate.time / 1000L
            val localSec = System.currentTimeMillis() / 1000L
            clockSkewSeconds = serverSec - localSec
        } catch (_: Exception) {}
    }

    private fun formatEdgeTimestamp(): String {
        val fmt = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT+0000 (Coordinated Universal Time)'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return fmt.format(Date(System.currentTimeMillis() + clockSkewSeconds * 1000L))
    }

    internal fun enforceCacheLruLimit(cacheDir: File, maxBytes: Long = MAX_CACHE_BYTES) {
        try {
            val files = cacheDir.listFiles()?.filter { it.isFile } ?: return
            var totalBytes = files.sumOf { it.length() }
            if (totalBytes <= maxBytes) return
            val sortedOldestFirst = files.sortedBy { it.lastModified() }
            for (f in sortedOldestFirst) {
                if (totalBytes <= maxBytes) break
                val len = f.length()
                if (f.delete()) {
                    totalBytes -= len
                }
            }
        } catch (_: Exception) {}
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        val sb = StringBuilder(digest.size * 2)
        for (b in digest) {
            sb.append(String.format(Locale.US, "%02x", b))
        }
        return sb.toString()
    }
}
