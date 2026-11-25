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
    private const val PRIMARY_CHROMIUM_VERSION = "143.0.3650.75"
    private const val SECONDARY_CHROMIUM_VERSION = "136.0.3240.76"
    private const val FALLBACK_CHROMIUM_VERSION = "131.0.2903.86"
    private const val WIN_EPOCH_OFFSET_SECONDS = 11644473600L
    private const val CACHE_DIR_NAME = "tts_audio_cache_v3"
    private const val MAX_CACHE_BYTES = 24L * 1024L * 1024L // 24 MB LRU cap
    private const val FIRST_AUDIO_READ_TIMEOUT_MS = 4_500L
    private const val TOTAL_STREAM_TIMEOUT_MS = 12_000L

    data class NeuralVoiceSpec(
        val id: String,
        val displayName: String,
        val subtitle: String,
        val localeTag: String,
        val isTurkish: Boolean,
        val isFemale: Boolean = false
    )

    val NEURAL_VOICES: List<NeuralVoiceSpec> = listOf(
        NeuralVoiceSpec(
            id = "tr-TR-EmelNeural",
            displayName = "Emel (Nöral • Kadın)",
            subtitle = "Doğal, sıcak, kadifemsi Türkçe kadın sesi",
            localeTag = "tr-TR",
            isTurkish = true,
            isFemale = true
        ),
        NeuralVoiceSpec(
            id = "tr-TR-AhmetNeural",
            displayName = "Ahmet (Nöral • Erkek)",
            subtitle = "Derin, karizmatik Türkçe erkek bariton sesi",
            localeTag = "tr-TR",
            isTurkish = true,
            isFemale = false
        ),
        NeuralVoiceSpec(
            id = "en-US-AvaMultilingualNeural",
            displayName = "Ava (Çok Dilli • Kadın)",
            subtitle = "Duygusal, içten çok dilli (TR/EN) kadın nöral ses",
            localeTag = "en-US",
            isTurkish = true,
            isFemale = true
        ),
        NeuralVoiceSpec(
            id = "en-US-EmmaMultilingualNeural",
            displayName = "Emma (Çok Dilli • Genç Kadın)",
            subtitle = "Canlı, sıcak ve umutlu genç kadın nöral ses",
            localeTag = "en-US",
            isTurkish = true,
            isFemale = true
        ),
        NeuralVoiceSpec(
            id = "de-DE-SeraphinaMultilingualNeural",
            displayName = "Seraphina (Çok Dilli • Kadın)",
            subtitle = "Zarif, sakin ve sinematik kadın nöral ses",
            localeTag = "de-DE",
            isTurkish = true,
            isFemale = true
        ),
        NeuralVoiceSpec(
            id = "en-US-AndrewMultilingualNeural",
            displayName = "Andrew (Çok Dilli • Erkek)",
            subtitle = "Akıcı, zeki ve dinamik çok dilli (TR/EN) erkek ses",
            localeTag = "en-US",
            isTurkish = true,
            isFemale = false
        ),
        NeuralVoiceSpec(
            id = "en-US-BrianMultilingualNeural",
            displayName = "Brian (Çok Dilli • Derin Erkek)",
            subtitle = "Gölgeli, yorgun ve ağırbaşlı erkek sinema sesi",
            localeTag = "en-US",
            isTurkish = true,
            isFemale = false
        ),
        NeuralVoiceSpec(
            id = "de-DE-FlorianMultilingualNeural",
            displayName = "Florian (Çok Dilli • Bas Bariton)",
            subtitle = "Otoriter, soğuk ve güçlü bas-bariton erkek ses",
            localeTag = "de-DE",
            isTurkish = true,
            isFemale = false
        ),
        NeuralVoiceSpec(
            id = "fr-FR-RemyMultilingualNeural",
            displayName = "Remy (Çok Dilli • Dramatik Erkek)",
            subtitle = "Güçlü, enerjik ve ifadeli erkek sinema sesi",
            localeTag = "fr-FR",
            isTurkish = true,
            isFemale = false
        ),
        NeuralVoiceSpec(
            id = "en-US-AriaNeural",
            displayName = "Aria (Nöral • Kadın EN)",
            subtitle = "İfadeli İngilizce nöral ses",
            localeTag = "en-US",
            isTurkish = false,
            isFemale = true
        ),
        NeuralVoiceSpec(
            id = "en-US-GuyNeural",
            displayName = "Guy (Nöral • Erkek EN)",
            subtitle = "Sıcak İngilizce nöral ses",
            localeTag = "en-US",
            isTurkish = false,
            isFemale = false
        )
    )

    @Volatile
    private var clockSkewSeconds: Long = 0L

    private val cacheWriteLock = Any()

    /** Optional test hook for deterministic offline/error/fallback verification in Robolectric tests. */
    @Volatile
    internal var synthesizerOverride: (suspend (voiceId: String, ssml: String) -> ByteArray?)? = null

    /**
     * Synthesizes [ssml] into an MP3 [File] stored in the 20 MB LRU cache.
     * Automatically normalizes SSML to the single-prosody format accepted by Microsoft Edge ReadAloud.
     * Returns `null` silently on any network/timeout/protocol error so callers can seamlessly
     * fall back to on-device TTS.
     */
    suspend fun synthesizeToCachedMp3(
        context: Context,
        voiceId: String,
        ssml: String,
        timeoutMs: Long = TOTAL_STREAM_TIMEOUT_MS
    ): File? = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
        val normalizedSsml = normalizeSsmlForEdgeReadAloud(voiceId, ssml)
        val cacheKey = sha256Hex("$voiceId|$normalizedSsml")
        val cachedFile = File(cacheDir, "$cacheKey.mp3")
        if (cachedFile.exists() && cachedFile.length() > 128L) {
            cachedFile.setLastModified(System.currentTimeMillis())
            return@withContext cachedFile
        }

        val overrideFn = synthesizerOverride
        val audioBytes: ByteArray? = if (overrideFn != null) {
            try {
                withTimeoutOrNull(timeoutMs) { overrideFn(voiceId, normalizedSsml) }
            } catch (_: Exception) {
                null
            }
        } else {
            fetchWithClockSkewRetry(normalizedSsml, timeoutMs)
        }

        if (audioBytes == null || audioBytes.size < 128) {
            return@withContext null
        }

        try {
            val tmp = File(cacheDir, "$cacheKey.${UUID.randomUUID()}.tmp")
            tmp.writeBytes(audioBytes)
            synchronized(cacheWriteLock) {
                if (cachedFile.exists() && cachedFile.length() == audioBytes.size.toLong()) {
                    tmp.delete()
                } else if (!tmp.renameTo(cachedFile)) {
                    tmp.copyTo(cachedFile, overwrite = true)
                    tmp.delete()
                }
                enforceCacheLruLimit(cacheDir)
            }
            cachedFile
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist TTS cache file: ${e.message}")
            null
        }
    }

    /**
     * Converts rich multi-segment SSML (which may include `<break/>` or multiple `<prosody>` tags)
     * into the strict single-`<prosody>` SSML accepted by the public Microsoft Edge ReadAloud
     * WebSocket endpoint, preventing silent 0-byte connection drops while preserving pitch/rate/pauses.
     */
    internal fun normalizeSsmlForEdgeReadAloud(voiceId: String, rawSsml: String): String {
        val trimmed = rawSsml.trim()
        val fullVoiceName = toFullEdgeVoiceName(voiceId)
        if (!trimmed.contains("<break", ignoreCase = true) &&
            Regex("<prosody\\b", RegexOption.IGNORE_CASE).findAll(trimmed).count() <= 1 &&
            trimmed.contains("volume=", ignoreCase = true) &&
            trimmed.contains(fullVoiceName)
        ) {
            return trimmed
        }

        val rateMatch = Regex("""rate=['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE).find(trimmed)
        val pitchMatch = Regex("""pitch=['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE).find(trimmed)
        val volMatch = Regex("""volume=['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE).find(trimmed)
        val langMatch = Regex("""xml:lang=['"]([^'"]+)['"]""", RegexOption.IGNORE_CASE).find(trimmed)

        val rateVal = rateMatch?.groupValues?.getOrNull(1)?.ifBlank { "+0%" } ?: "+0%"
        val pitchVal = pitchMatch?.groupValues?.getOrNull(1)?.ifBlank { "+0Hz" } ?: "+0Hz"
        val volVal = volMatch?.groupValues?.getOrNull(1)?.ifBlank { "+0%" } ?: "+0%"
        val langVal = langMatch?.groupValues?.getOrNull(1)?.ifBlank { "tr-TR" }
            ?: when {
                voiceId.startsWith("tr-", ignoreCase = true) -> "tr-TR"
                voiceId.startsWith("de-", ignoreCase = true) -> "de-DE"
                voiceId.startsWith("fr-", ignoreCase = true) -> "fr-FR"
                else -> "en-US"
            }

        // Extract inner text while turning <break .../> into natural breath punctuation
        val voiceInner = Regex("""<voice[^>]*>(.*)</voice>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .find(trimmed)?.groupValues?.getOrNull(1) ?: trimmed
        val withPauses = voiceInner
            .replace(Regex("""<break[^>]*/>""", RegexOption.IGNORE_CASE), "... ")
            .replace(Regex("""</?prosody[^>]*>""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""</?lang[^>]*>""", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        if (withPauses.isBlank()) return trimmed

        return "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='$langVal'>" +
            "<voice name='$fullVoiceName'>" +
            "<prosody pitch='$pitchVal' rate='$rateVal' volume='$volVal'>$withPauses</prosody>" +
            "</voice></speak>"
    }

    /**
     * Converts a short Edge voice ID (e.g. `tr-TR-AhmetNeural`) into the official
     * Microsoft Edge ReadAloud full voice name (`Microsoft Server Speech Text to Speech Voice (tr-TR, AhmetNeural)`),
     * matching Microsoft Edge browser / edge-tts v7 exact protocol requirements.
     */
    internal fun toFullEdgeVoiceName(voiceId: String): String {
        val trimmed = voiceId.trim()
        if (trimmed.startsWith("Microsoft Server Speech", ignoreCase = true)) return trimmed
        val parts = trimmed.split("-")
        if (parts.size >= 3) {
            val locale = "${parts[0]}-${parts[1]}"
            val shortName = parts.drop(2).joinToString("-")
            return "Microsoft Server Speech Text to Speech Voice ($locale, $shortName)"
        }
        return trimmed
    }

    /**
     * Builds a clean, 100% Microsoft Edge ReadAloud compatible single-prosody SSML envelope.
     */
    fun buildEdgeSingleProsodySsml(
        voiceId: String,
        plainText: String,
        ratePercent: Int = 0,
        pitchHz: Int = 0,
        volumePercent: Int = 0
    ): String {
        val safeRate = ratePercent.coerceIn(-40, 50).let { if (it >= 0) "+$it%" else "$it%" }
        val safePitch = pitchHz.coerceIn(-20, 20).let { if (it >= 0) "+${it}Hz" else "${it}Hz" }
        val safeVol = volumePercent.coerceIn(-25, 25).let { if (it >= 0) "+$it%" else "$it%" }
        val lang = when {
            voiceId.startsWith("tr-", ignoreCase = true) -> "tr-TR"
            voiceId.startsWith("de-", ignoreCase = true) -> "de-DE"
            voiceId.startsWith("fr-", ignoreCase = true) -> "fr-FR"
            voiceId.startsWith("it-", ignoreCase = true) -> "it-IT"
            else -> "en-US"
        }
        val fullVoiceName = toFullEdgeVoiceName(voiceId)
        val escaped = EmotionalTtsProsodyEngine.escapeXml(plainText.trim())
        return "<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xml:lang='$lang'>" +
            "<voice name='$fullVoiceName'>" +
            "<prosody pitch='$safePitch' rate='$safeRate' volume='$safeVol'>$escaped</prosody>" +
            "</voice></speak>"
    }

    /**
     * Tier 2.5 (100% FREE, ZERO API KEY, plain HTTPS):
     * Fallback neural Turkish synthesis via Google Translate public TTS endpoint when
     * WebSocket port / Edge ReadAloud is blocked by a corporate/emulator firewall.
     */
    suspend fun synthesizeFreeHttpNeuralBackup(
        context: Context,
        plainText: String,
        langCode: String = "tr",
        timeoutMs: Long = 6_000L
    ): File? = withContext(Dispatchers.IO) {
        val cleaned = plainText.trim()
        if (cleaned.isBlank()) return@withContext null
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME).apply { mkdirs() }
        val hash = sha256Hex("GHTTP_V1|$langCode|$cleaned")
        val targetFile = File(cacheDir, "ghttp_$hash.mp3")
        if (targetFile.exists() && targetFile.length() > 256L) {
            targetFile.setLastModified(System.currentTimeMillis())
            return@withContext targetFile
        }
        try {
            val chunks = chunkForHttpTts(cleaned, maxLen = 165)
            val httpClient = RetrofitClient.okHttpClient.newBuilder()
                .connectTimeout(3_500L, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs.coerceIn(3_500L, 9_000L), TimeUnit.MILLISECONDS)
                .build()
            val out = ByteArrayOutputStream(16_384)
            for (chunk in chunks) {
                val encoded = java.net.URLEncoder.encode(chunk, "UTF-8")
                val url = "https://translate.googleapis.com/translate_tts?ie=UTF-8&client=tw-ob&tl=$langCode&q=$encoded"
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Referer", "https://translate.google.com/")
                    .build()
                httpClient.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val bodyBytes = resp.body?.bytes() ?: return@withContext null
                    if (bodyBytes.size < 128) return@withContext null
                    out.write(bodyBytes)
                }
            }
            val finalBytes = out.toByteArray()
            if (finalBytes.size < 256) return@withContext null
            val tmp = File(cacheDir, "ghttp_$hash.tmp")
            tmp.writeBytes(finalBytes)
            if (!tmp.renameTo(targetFile)) {
                tmp.copyTo(targetFile, overwrite = true)
                tmp.delete()
            }
            enforceCacheLruLimit(cacheDir)
            if (targetFile.exists() && targetFile.length() > 256L) targetFile else null
        } catch (t: Throwable) {
            Log.w(TAG, "Free HTTP neural TTS backup skipped: ${t.message}")
            null
        }
    }

    private fun chunkForHttpTts(text: String, maxLen: Int): List<String> {
        if (text.length <= maxLen) return listOf(text)
        val result = mutableListOf<String>()
        val sentences = Regex("[^.!?,;]+[.!?,;]*").findAll(text).map { it.value.trim() }.filter { it.isNotEmpty() }.toList()
        var current = StringBuilder()
        for (s in sentences) {
            if (current.isNotEmpty() && current.length + 1 + s.length > maxLen) {
                result.add(current.toString().trim())
                current = StringBuilder()
            }
            if (s.length > maxLen) {
                val words = s.split(Regex("\\s+"))
                for (w in words) {
                    if (current.isNotEmpty() && current.length + 1 + w.length > maxLen) {
                        result.add(current.toString().trim())
                        current = StringBuilder()
                    }
                    if (current.isNotEmpty()) current.append(' ')
                    current.append(w)
                }
            } else {
                if (current.isNotEmpty()) current.append(' ')
                current.append(s)
            }
        }
        if (current.isNotEmpty()) result.add(current.toString().trim())
        return result.ifEmpty { listOf(text.take(maxLen)) }
    }

    private sealed class WsFetchOutcome {
        data class Success(val mp3Bytes: ByteArray) : WsFetchOutcome()
        data class ClockSkew403(val serverDateHeader: String?) : WsFetchOutcome()
        data object Failed : WsFetchOutcome()
    }

    private suspend fun fetchWithClockSkewRetry(ssml: String, timeoutMs: Long): ByteArray? {
        val first = runSingleWebSocketFetch(ssml, timeoutMs, PRIMARY_CHROMIUM_VERSION)
        return when (first) {
            is WsFetchOutcome.Success -> first.mp3Bytes
            is WsFetchOutcome.ClockSkew403 -> {
                adjustClockSkewFromHeader(first.serverDateHeader)
                when (val second = runSingleWebSocketFetch(ssml, timeoutMs, SECONDARY_CHROMIUM_VERSION)) {
                    is WsFetchOutcome.Success -> second.mp3Bytes
                    else -> {
                        val third = runSingleWebSocketFetch(ssml, timeoutMs, FALLBACK_CHROMIUM_VERSION)
                        (third as? WsFetchOutcome.Success)?.mp3Bytes
                    }
                }
            }
            is WsFetchOutcome.Failed -> {
                val second = runSingleWebSocketFetch(ssml, timeoutMs, SECONDARY_CHROMIUM_VERSION)
                (second as? WsFetchOutcome.Success)?.mp3Bytes
            }
        }
    }

    private suspend fun runSingleWebSocketFetch(
        ssml: String,
        timeoutMs: Long,
        chromiumVersion: String = PRIMARY_CHROMIUM_VERSION
    ): WsFetchOutcome {
        val totalTimeoutMs = timeoutMs.coerceAtLeast(FIRST_AUDIO_READ_TIMEOUT_MS)
        val perFrameReadTimeoutMs = FIRST_AUDIO_READ_TIMEOUT_MS.coerceAtMost(totalTimeoutMs)
        return withTimeoutOrNull(totalTimeoutMs) {
            suspendCancellableCoroutine { cont ->
                val connectionId = UUID.randomUUID().toString().replace("-", "")
                val muid = UUID.randomUUID().toString().replace("-", "").uppercase(Locale.ROOT)
                val secMsGec = generateSecMsGec()
                val secMsGecVersion = "1-$chromiumVersion"
                val wsUrl =
                    "wss://speech.platform.bing.com/consumer/speech/synthesize/readaloud/edge/v1" +
                        "?TrustedClientToken=$TRUSTED_CLIENT_TOKEN" +
                        "&Sec-MS-GEC=$secMsGec" +
                        "&Sec-MS-GEC-Version=$secMsGecVersion" +
                        "&ConnectionId=$connectionId"

                val request = Request.Builder()
                    .url(wsUrl)
                    .header("Pragma", "no-cache")
                    .header("Cache-Control", "no-cache")
                    .header("Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Origin", "chrome-extension://jdiccldimpdaibmpdkjnbmckianbfold")
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/$chromiumVersion Safari/537.36 Edg/$chromiumVersion"
                    )
                    .header("Cookie", "muid=$muid;")
                    .build()

                val wsClient = RetrofitClient.okHttpClient.newBuilder()
                    .connectTimeout(4_000L, TimeUnit.MILLISECONDS)
                    .readTimeout(perFrameReadTimeoutMs, TimeUnit.MILLISECONDS)
                    .writeTimeout(4_000L, TimeUnit.MILLISECONDS)
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
            val allFiles = cacheDir.listFiles()?.filter { it.isFile } ?: return
            val staleTmpCutoff = System.currentTimeMillis() - 60_000L
            allFiles.filter { it.name.endsWith(".tmp") && it.lastModified() < staleTmpCutoff }
                .forEach { it.delete() }
            val files = allFiles.filter { it.exists() && !it.name.endsWith(".tmp") }
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
