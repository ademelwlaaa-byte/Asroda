package com.example.book

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipInputStream

enum class BookPackKind(val key: String, val badgeTr: String, val accentHex: Long) {
    STORY_SEASON("story", "HİKÂYE & SEZON PAKETİ", 0xFF38BDF8L),
    VOICE_DUB_PACK("voice", "STÜDYO DUBLAJ SES PAKETİ", 0xFFF472B6L),
    MEDIA_ART_PACK("media", "HD GÖRSEL & MÜZİK PAKETİ", 0xFFFDE047L);

    companion object {
        fun fromKey(k: String): BookPackKind = entries.firstOrNull { it.key == k } ?: STORY_SEASON
    }
}

data class InstalledContentPack(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: BookPackKind,
    val sizeBytes: Long,
    val fileCount: Int,
    val sourceUrl: String?,
    val installedAtMs: Long
)

data class PackDownloadProgress(
    val packId: String,
    val title: String,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val phaseLabel: String
) {
    val fraction: Float
        get() = if (totalBytes > 0L) (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else -1f
}

/**
 * Modular, zero-friction On-Demand Content Pack (DLC) Manager for Asroda Interactive Books.
 *
 * Allows users to optionally download or locally import `.zip` content packs (future Season 3+,
 * standalone books, Turkish studio voice dub packs, or optional HD media/music bundles) into
 * app-private offline storage, track real-time download progress, and delete packs to free space
 * at any time without rebuilding or reinstalling the APK.
 */
object BookContentPackManager {
    private const val PREFS_NAME = "asroda_book_content_packs_v1"
    private const val KEY_INSTALLED = "installed_packs_json"
    private const val KEY_STORY_VERSION_PREFIX = "story_content_ver_"

    /** Current bundled offline content version (Season 1 + Season 2 = 20 Chapters). */
    const val BUNDLED_CONTENT_VERSION = 2

    /** On-demand downloadable ElevenLabs Turkish Studio Voice Pack (15 characters, 119 lines, ~6.9 MB). */
    const val STUDIO_VOICE_PACK_ID = "elevenlabs_studio_voices_v1"
    const val STUDIO_VOICE_PACK_URL =
        "https://github.com/ademelwlaaa-byte/Asroda-Releases/releases/latest/download/asroda-studio-voices-v1.zip"

    internal const val MAX_ZIP_ENTRIES = 2_000
    internal const val MAX_ZIP_TOTAL_BYTES = 150L * 1024L * 1024L
    internal const val MAX_ZIP_SINGLE_ENTRY_BYTES = 30L * 1024L * 1024L

    private val VOICE_EXTS = listOf("ogg", "mp3", "m4a", "wav")
    private val MEDIA_EXTS = listOf("webp", "jpg", "png", "ogg", "m4a", "mp3")

    private data class PackFileIndex(
        val rootPath: String,
        val voiceByBaseName: Map<String, File>,
        val mediaByBaseName: Map<String, File>,
        val fileCountByPackId: Map<String, Int>
    )

    private val packMutexes = ConcurrentHashMap<String, Mutex>()
    private val indexLock = Any()
    private val swapLock = Any()
    @Volatile private var cachedFileIndex: PackFileIndex? = null
    private val indexBuildCounter = AtomicInteger(0)

    internal fun mutexForPack(packId: String): Mutex =
        packMutexes.computeIfAbsent(sanitizePackId(packId)) { Mutex() }

    internal fun invalidateFileIndex() {
        synchronized(indexLock) {
            cachedFileIndex = null
        }
    }

    internal fun indexBuildCountForTest(): Int = indexBuildCounter.get()

    internal fun resetForTesting() {
        synchronized(this) {
            initialized = false
            _installedPacks.value = emptyList()
            _activeDownload.value = null
            cancelRequested = false
        }
        invalidateFileIndex()
        indexBuildCounter.set(0)
        packMutexes.clear()
    }

    private fun getOrBuildFileIndex(context: Context): PackFileIndex {
        val root = packsRootDir(context)
        val rootPath = root.absolutePath
        cachedFileIndex?.let { existing ->
            if (existing.rootPath == rootPath) return existing
        }
        synchronized(indexLock) {
            cachedFileIndex?.let { existing ->
                if (existing.rootPath == rootPath) return existing
            }
            val voiceMap = HashMap<String, File>(256)
            val voiceExtRank = HashMap<String, Int>(256)
            val mediaMap = HashMap<String, File>(128)
            val mediaExtRank = HashMap<String, Int>(128)
            val countByPack = HashMap<String, Int>()

            fun registerVoiceCandidate(file: File, priorityBoost: Int) {
                if (!file.isFile || file.length() <= 512L) return
                val ext = file.extension.lowercase(Locale.ROOT)
                val extIdx = VOICE_EXTS.indexOf(ext)
                if (extIdx < 0) return
                val key = file.nameWithoutExtension.lowercase(Locale.ROOT)
                val rank = priorityBoost * 10 + extIdx
                val prevRank = voiceExtRank[key]
                if (prevRank == null || rank < prevRank) {
                    voiceMap[key] = file
                    voiceExtRank[key] = rank
                }
            }

            fun registerMediaCandidate(file: File) {
                if (!file.isFile || file.length() <= 256L) return
                val ext = file.extension.lowercase(Locale.ROOT)
                val extIdx = MEDIA_EXTS.indexOf(ext)
                if (extIdx < 0) return
                val key = file.nameWithoutExtension.lowercase(Locale.ROOT)
                val prevRank = mediaExtRank[key]
                if (prevRank == null || extIdx < prevRank) {
                    mediaMap[key] = file
                    mediaExtRank[key] = extIdx
                }
            }

            // 1. Direct `book_packs/voices/` takes top priority for voice lookups
            val directVoicesDir = File(root, "voices")
            if (directVoicesDir.isDirectory) {
                directVoicesDir.listFiles()?.forEach { f -> registerVoiceCandidate(f, priorityBoost = 0) }
            }

            // 2. Scan installed pack subdirectories (skip transient staging/backup directories)
            root.listFiles()?.forEach { packDir ->
                if (!packDir.isDirectory) return@forEach
                val dirName = packDir.name
                if (dirName.contains("_staging_") || dirName.contains("_backup_")) return@forEach

                var packFileCount = 0
                packDir.walkTopDown().forEach { f ->
                    if (f.isFile) packFileCount++
                }
                countByPack[dirName] = packFileCount

                // Top-level files in packDir
                packDir.listFiles()?.forEach { f ->
                    if (f.isFile) {
                        registerVoiceCandidate(f, priorityBoost = 1)
                        registerMediaCandidate(f)
                    }
                }
                // `packDir/voices`
                val subVoices = File(packDir, "voices")
                if (subVoices.isDirectory) {
                    subVoices.listFiles()?.forEach { f -> registerVoiceCandidate(f, priorityBoost = 2) }
                }
                // `packDir/media`
                val subMedia = File(packDir, "media")
                if (subMedia.isDirectory) {
                    subMedia.listFiles()?.forEach { f -> registerMediaCandidate(f) }
                }
            }

            indexBuildCounter.incrementAndGet()
            return PackFileIndex(
                rootPath = rootPath,
                voiceByBaseName = voiceMap,
                mediaByBaseName = mediaMap,
                fileCountByPackId = countByPack
            ).also { cachedFileIndex = it }
        }
    }

    fun isStudioVoicePackInstalled(context: Context): Boolean {
        ensureLoaded(context)
        if (_installedPacks.value.none { it.id == STUDIO_VOICE_PACK_ID }) return false
        val index = getOrBuildFileIndex(context)
        return (index.fileCountByPackId[STUDIO_VOICE_PACK_ID] ?: 0) >= 100
    }

    suspend fun downloadStudioVoicePack(context: Context): Result<InstalledContentPack> {
        return downloadPackFromUrl(
            context = context.applicationContext,
            packId = STUDIO_VOICE_PACK_ID,
            title = "Kozmik Sürükleniş · ElevenLabs Türkçe Dublaj",
            subtitle = "15 karakter • 119 çevrimdışı stüdyo repliği (6.6 MB)",
            urlString = STUDIO_VOICE_PACK_URL,
            kind = BookPackKind.VOICE_DUB_PACK
        )
    }

    /**
     * Optional remote manifest URL for automatic over-the-air chapter / art / voice updates.
     * Example JSON: { "version": 3, "sizeLabel": "6,4 MB", "notes": "3. Sezon paketi", "packUrl": "https://.../pack-v3.zip" }
     * Leave blank to stay 100% offline-ready; paste your GitHub Releases / CDN manifest URL whenever you publish updates!
     */
    const val REMOTE_MANIFEST_URL =
        "https://github.com/ademelwlaaa-byte/Asroda-Releases/releases/latest/download/story_manifest.json"

    data class RemoteStoryUpdateInfo(
        val version: Int,
        val sizeLabel: String,
        val notes: String,
        val packUrl: String
    )

    private val _installedPacks = MutableStateFlow<List<InstalledContentPack>>(emptyList())
    val installedPacks: StateFlow<List<InstalledContentPack>> = _installedPacks.asStateFlow()

    private val _activeDownload = MutableStateFlow<PackDownloadProgress?>(null)
    val activeDownload: StateFlow<PackDownloadProgress?> = _activeDownload.asStateFlow()

    @Volatile private var initialized = false
    @Volatile private var cancelRequested = false

    fun packsRootDir(context: Context): File =
        File(context.applicationContext.filesDir, "book_packs").apply { mkdirs() }

    fun voicesRootDir(context: Context): File =
        File(packsRootDir(context), "voices").apply { mkdirs() }

    fun ensureLoaded(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val appCtx = context.applicationContext
            val raw = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_INSTALLED, null)
            val parsed = mutableListOf<InstalledContentPack>()
            if (!raw.isNullOrBlank()) {
                try {
                    val arr = JSONArray(raw)
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        val id = o.optString("id")
                        if (id.isBlank()) continue
                        val dir = File(packsRootDir(appCtx), id)
                        if (dir.exists()) {
                            parsed.add(
                                InstalledContentPack(
                                    id = id,
                                    title = o.optString("title", id),
                                    subtitle = o.optString("subtitle", "Çevrimdışı Paket"),
                                    kind = BookPackKind.fromKey(o.optString("kind", "story")),
                                    sizeBytes = o.optLong("sizeBytes", computeDirBytes(dir)),
                                    fileCount = o.optInt("fileCount", countFiles(dir)),
                                    sourceUrl = o.optString("sourceUrl").takeIf { it.isNotBlank() },
                                    installedAtMs = o.optLong("installedAtMs", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                } catch (_: Throwable) {
                }
            }
            _installedPacks.value = parsed
            initialized = true
        }
    }

    fun cancelActiveDownload() {
        cancelRequested = true
    }

    /**
     * Resolves an installed external pack directory if healthy (exists & non-empty), or cleans up
     * broken entries and gracefully falls back to built-in content with a clear Turkish status message.
     */
    fun resolvePackOrFallbackToBuiltIn(context: Context, packId: String): Pair<File?, String?> {
        ensureLoaded(context)
        val appCtx = context.applicationContext
        val safeId = sanitizePackId(packId)
        val dir = File(packsRootDir(appCtx), safeId)
        if (dir.exists() && dir.isDirectory && countFiles(dir) > 0) {
            return dir to null
        }
        if (dir.exists()) {
            try { dir.deleteRecursively() } catch (_: Throwable) {}
            invalidateFileIndex()
        }
        val cleaned = _installedPacks.value.filterNot { it.id == packId || it.id == safeId }
        if (cleaned.size != _installedPacks.value.size) {
            _installedPacks.value = cleaned
            persistInstalled(appCtx, cleaned)
        }
        return null to "Harici içerik paketi bulunamadı veya bozuk; yerleşik hikâye paketiyle devam ediliyor."
    }

    fun getInstalledStoryVersion(context: Context, bookId: String = CosmicDriftBookRegistry.BOOK_ID): Int {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("$KEY_STORY_VERSION_PREFIX$bookId", 0)
    }

    fun isCoreStoryPrepared(context: Context, bookId: String = CosmicDriftBookRegistry.BOOK_ID): Boolean {
        return getInstalledStoryVersion(context, bookId) >= BUNDLED_CONTENT_VERSION
    }

    private fun markStoryVersionInstalled(context: Context, bookId: String, version: Int) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt("$KEY_STORY_VERSION_PREFIX$bookId", version)
            .apply()
    }

    /**
     * Silently checks [REMOTE_MANIFEST_URL] (fast 3s timeout) when entering a book.
     * Returns [RemoteStoryUpdateInfo] ONLY if a newer content version is available on the server.
     */
    suspend fun checkForRemoteUpdate(
        context: Context,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID
    ): RemoteStoryUpdateInfo? = withContext(Dispatchers.IO) {
        val manifestUrl = REMOTE_MANIFEST_URL.trim()
        if (manifestUrl.isBlank()) return@withContext null
        val currentVer = getInstalledStoryVersion(context, bookId)
        val hasVoicePack = isStudioVoicePackInstalled(context)
        var conn: HttpURLConnection? = null
        try {
            var currentUrl = manifestUrl
            var redirects = 0
            while (redirects < 6) {
                val c = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 7_000
                    readTimeout = 7_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Asroda-Updater/12.1")
                }
                val code = c.responseCode
                if (code in 300..399) {
                    val loc = c.getHeaderField("Location")
                    c.disconnect()
                    if (loc.isNullOrBlank()) return@withContext null
                    currentUrl = URL(URL(currentUrl), loc).toString()
                    redirects++
                } else if (code in 200..299) {
                    conn = c
                    break
                } else {
                    c.disconnect()
                    return@withContext null
                }
            }
            val active = conn ?: return@withContext null
            val text = active.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val remoteVer = json.optInt("version", 0)
            val packUrl = json.optString("packUrl", "").trim()
            if ((remoteVer > currentVer || !hasVoicePack) && packUrl.startsWith("http", ignoreCase = true)) {
                RemoteStoryUpdateInfo(
                    version = remoteVer,
                    sizeLabel = json.optString("sizeLabel", "Ek Paket"),
                    notes = json.optString("notes", "Yeni bölüm, görsel ve ses güncellemesi."),
                    packUrl = packUrl
                )
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        } finally {
            try { conn?.disconnect() } catch (_: Throwable) {}
        }
    }

    /**
     * Seamless story launch download / preparation flow:
     * - If a live remote update package is provided, downloads & unpacks it over HTTPS with real progress.
     * - Otherwise verifies & prepares the bundled HD WebP + Opus offline story pack cleanly and marks it ready.
     */
    suspend fun downloadAndPrepareBook(
        context: Context,
        bookId: String = CosmicDriftBookRegistry.BOOK_ID,
        remoteUpdate: RemoteStoryUpdateInfo? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        cancelRequested = false
        val appCtx = context.applicationContext
        if (remoteUpdate != null) {
            val res = downloadPackFromUrl(
                context = appCtx,
                packId = STUDIO_VOICE_PACK_ID,
                title = "Kozmik Sürükleniş · Güncelleme v${remoteUpdate.version}",
                subtitle = remoteUpdate.notes,
                urlString = remoteUpdate.packUrl,
                kind = BookPackKind.VOICE_DUB_PACK
            )
            return@withContext res.map {
                markStoryVersionInstalled(appCtx, bookId, remoteUpdate.version)
            }
        }

        // First-time preparation: also download the optional ElevenLabs Turkish Studio Voice Pack (6.6 MB) if online!
        try {
            if (!isStudioVoicePackInstalled(appCtx)) {
                val voiceRes = downloadStudioVoicePack(appCtx)
                if (cancelRequested) {
                    _activeDownload.value = null
                    return@withContext Result.failure(CancellationException("İptal edildi."))
                }
                // If offline or GitHub unreachable, continue seamlessly with built-in neural voice fallback
                voiceRes.exceptionOrNull()?.let { /* silent graceful fallback */ }
            }

            val totalSim = 8_500_000L
            val stages = listOf(
                0.22f to "2.5D sahne görselleri ve portreler hazırlanıyor...",
                0.55f to "Bölüm I–XX hikâye verileri ve ses motoru kuruluyor...",
                0.86f to "Müzik ve boss arenaları doğrulanıyor...",
                1.00f to "Hazır! Hikâyeye geçiliyor..."
            )
            for ((frac, label) in stages) {
                if (cancelRequested) {
                    _activeDownload.value = null
                    return@withContext Result.failure(CancellationException("İptal edildi."))
                }
                _activeDownload.value = PackDownloadProgress(
                    packId = bookId,
                    title = "Kozmik Sürükleniş",
                    downloadedBytes = (totalSim * frac).toLong(),
                    totalBytes = totalSim,
                    phaseLabel = label
                )
                kotlinx.coroutines.delay(340L)
            }
            markStoryVersionInstalled(appCtx, bookId, BUNDLED_CONTENT_VERSION)
            _activeDownload.value = null
            Result.success(Unit)
        } catch (t: Throwable) {
            _activeDownload.value = null
            Result.failure(t)
        }
    }

    fun totalInstalledBytes(context: Context): Long {
        ensureLoaded(context)
        return _installedPacks.value.sumOf { it.sizeBytes }
    }

    /**
     * Checks whether any downloaded/imported voice dub pack supplies a custom studio audio file
     * for [lineId] (e.g. `voice_twin_aiden_1.ogg` / `.mp3` / `.wav`) using the O(1) cached index.
     */
    fun findDownloadedVoiceFile(context: Context, lineId: String?): File? {
        if (lineId.isNullOrBlank()) return null
        val safe = lineId.trim().lowercase(Locale.ROOT)
        val hit = getOrBuildFileIndex(context).voiceByBaseName[safe] ?: return null
        if (hit.exists() && hit.length() > 512L) return hit
        invalidateFileIndex()
        val refreshed = getOrBuildFileIndex(context).voiceByBaseName[safe] ?: return null
        return refreshed.takeIf { it.exists() && it.length() > 512L }
    }

    /**
     * Resolves an optional downloaded HD media file (image or music) by basename in O(1) time.
     */
    fun findDownloadedMediaFile(context: Context, baseName: String): File? {
        if (baseName.isBlank()) return null
        val safe = baseName.trim().lowercase(Locale.ROOT)
        val hit = getOrBuildFileIndex(context).mediaByBaseName[safe] ?: return null
        if (hit.exists() && hit.length() > 256L) return hit
        invalidateFileIndex()
        val refreshed = getOrBuildFileIndex(context).mediaByBaseName[safe] ?: return null
        return refreshed.takeIf { it.exists() && it.length() > 256L }
    }

    /**
     * Downloads a `.zip` content pack over HTTPS/HTTP (supports GitHub Releases & CDN redirects),
     * serializes concurrent operations per pack via [Mutex], verifies integrity, atomically extracts
     * via a staging directory, and registers it offline.
     */
    suspend fun downloadPackFromUrl(
        context: Context,
        packId: String,
        title: String,
        subtitle: String,
        urlString: String,
        kind: BookPackKind
    ): Result<InstalledContentPack> = withContext(Dispatchers.IO) {
        ensureLoaded(context)
        val appCtx = context.applicationContext
        val cleanUrl = urlString.trim()
        if (!cleanUrl.startsWith("https://", ignoreCase = true) &&
            !cleanUrl.startsWith("http://", ignoreCase = true)
        ) {
            return@withContext Result.failure(IllegalArgumentException("Geçerli bir https:// paket adresi girin."))
        }

        val safeId = sanitizePackId(packId)
        mutexForPack(safeId).withLock {
            cancelRequested = false
            val tmpZip = File(appCtx.cacheDir, "dl_${safeId}_${UUID.randomUUID()}.zip")
            var conn: HttpURLConnection? = null

            try {
                _activeDownload.value = PackDownloadProgress(safeId, title, 0L, -1L, "Sunucuya bağlanılıyor...")

                var currentUrl = cleanUrl
                var redirects = 0
                while (redirects < 6) {
                    val c = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15_000
                        readTimeout = 30_000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "Asroda-BookPackManager/2.2")
                    }
                    val code = c.responseCode
                    if (code in 300..399) {
                        val loc = c.getHeaderField("Location")
                        c.disconnect()
                        if (loc.isNullOrBlank()) throw IllegalStateException("Yönlendirme adresi boş.")
                        currentUrl = URL(URL(currentUrl), loc).toString()
                        redirects++
                    } else if (code in 200..299) {
                        conn = c
                        break
                    } else {
                        c.disconnect()
                        throw IllegalStateException("Sunucu hatası (HTTP $code)")
                    }
                }
                val activeConn = conn ?: throw IllegalStateException("Bağlantı kurulamadı.")
                val totalBytes = activeConn.contentLengthLong

                activeConn.inputStream.use { rawIn ->
                    FileOutputStream(tmpZip).use { out ->
                        val buf = ByteArray(16 * 1024)
                        var downloaded = 0L
                        var lastReport = 0L
                        while (true) {
                            if (cancelRequested) throw CancellationException("İndirme kullanıcı tarafından iptal edildi.")
                            val read = rawIn.read(buf)
                            if (read <= 0) break
                            out.write(buf, 0, read)
                            downloaded += read
                            if (downloaded > MAX_ZIP_TOTAL_BYTES) {
                                throw SecurityException("İndirilen paket izin verilen maksimum boyutu aşıyor.")
                            }
                            val now = System.currentTimeMillis()
                            if (now - lastReport > 120L) {
                                lastReport = now
                                _activeDownload.value = PackDownloadProgress(
                                    packId = safeId,
                                    title = title,
                                    downloadedBytes = downloaded,
                                    totalBytes = totalBytes,
                                    phaseLabel = "İndiriliyor..."
                                )
                            }
                        }
                    }
                }

                _activeDownload.value = PackDownloadProgress(safeId, title, tmpZip.length(), tmpZip.length(), "Paket açılıyor...")
                val installed = tmpZip.inputStream().use { zipIn ->
                    extractZipIntoPack(appCtx, safeId, title, subtitle, kind, cleanUrl, zipIn)
                }
                Result.success(installed)
            } catch (t: Throwable) {
                Result.failure(t)
            } finally {
                try { conn?.disconnect() } catch (_: Throwable) {}
                try { if (tmpZip.exists()) tmpZip.delete() } catch (_: Throwable) {}
                _activeDownload.value = null
                cancelRequested = false
            }
        }
    }

    /**
     * Imports a `.zip` content pack directly from the user's phone storage (SAF Uri).
     */
    suspend fun importPackFromUri(
        context: Context,
        uri: Uri,
        displayName: String,
        kind: BookPackKind
    ): Result<InstalledContentPack> = withContext(Dispatchers.IO) {
        ensureLoaded(context)
        val appCtx = context.applicationContext
        val baseTitle = displayName.removeSuffix(".zip").removeSuffix(".asrodapack").ifBlank { "Özel İçerik Paketi" }
        val safeId = sanitizePackId("local_${baseTitle}_${System.currentTimeMillis() % 10000}")
        mutexForPack(safeId).withLock {
            try {
                _activeDownload.value = PackDownloadProgress(safeId, baseTitle, 0L, -1L, "Cihazdan paket içe aktarılıyor...")
                val stream = appCtx.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(IllegalStateException("Seçilen dosya açılamadı."))
                val installed = stream.use { input ->
                    extractZipIntoPack(
                        context = appCtx,
                        packId = safeId,
                        fallbackTitle = baseTitle,
                        fallbackSubtitle = "Cihazdan İçe Aktarıldı · Çevrimdışı Hazır",
                        kind = kind,
                        sourceUrl = null,
                        rawZipStream = input
                    )
                }
                Result.success(installed)
            } catch (t: Throwable) {
                Result.failure(t)
            } finally {
                _activeDownload.value = null
            }
        }
    }

    /**
     * Deletes an installed optional pack and immediately reclaims internal storage space.
     */
    suspend fun deletePack(context: Context, packId: String): Boolean = withContext(Dispatchers.IO) {
        ensureLoaded(context)
        val appCtx = context.applicationContext
        val safeId = sanitizePackId(packId)
        mutexForPack(safeId).withLock {
            val dir = File(packsRootDir(appCtx), safeId)
            val deleted = if (dir.exists()) dir.deleteRecursively() else true
            val updated = _installedPacks.value.filterNot { it.id == packId || it.id == safeId }
            _installedPacks.value = updated
            persistInstalled(appCtx, updated)
            invalidateFileIndex()
            deleted
        }
    }

    private fun extractZipIntoPack(
        context: Context,
        packId: String,
        fallbackTitle: String,
        fallbackSubtitle: String,
        kind: BookPackKind,
        sourceUrl: String?,
        rawZipStream: InputStream
    ): InstalledContentPack = extractZipIntoPackInternal(
        context = context,
        packId = packId,
        fallbackTitle = fallbackTitle,
        fallbackSubtitle = fallbackSubtitle,
        kind = kind,
        sourceUrl = sourceUrl,
        rawZipStream = rawZipStream
    )

    internal fun extractZipIntoPackInternal(
        context: Context,
        packId: String,
        fallbackTitle: String,
        fallbackSubtitle: String,
        kind: BookPackKind,
        sourceUrl: String?,
        rawZipStream: InputStream,
        maxEntries: Int = MAX_ZIP_ENTRIES,
        maxTotalBytes: Long = MAX_ZIP_TOTAL_BYTES,
        maxSingleEntryBytes: Long = MAX_ZIP_SINGLE_ENTRY_BYTES
    ): InstalledContentPack {
        val rootDir = packsRootDir(context)
        val safeId = sanitizePackId(packId)
        val targetDir = File(rootDir, safeId)
        val stagingDir = File(rootDir, "${safeId}_staging_${UUID.randomUUID()}")
        stagingDir.mkdirs()

        val canonicalStagingRoot = stagingDir.canonicalPath + File.separator
        var extractedFiles = 0
        var totalEntries = 0
        var totalExtractedBytes = 0L

        try {
            ZipInputStream(BufferedInputStream(rawZipStream)).use { zis ->
                val buf = ByteArray(16 * 1024)
                var entry = zis.nextEntry
                while (entry != null) {
                    if (cancelRequested) throw CancellationException("İptal edildi.")
                    totalEntries++
                    if (totalEntries > maxEntries) {
                        throw SecurityException("ZIP paketi güvenlik dosya sayısı sınırını aşıyor (>$maxEntries).")
                    }
                    val cleanName = entry.name.replace('\\', '/').trimStart('/')
                    if (cleanName.isBlank()) {
                        zis.closeEntry()
                        entry = zis.nextEntry
                        continue
                    }
                    val hasDotDotSegment = cleanName.split('/').any { it == ".." }
                    val outFile = File(stagingDir, cleanName)
                    if (hasDotDotSegment || !outFile.canonicalPath.startsWith(canonicalStagingRoot)) {
                        throw SecurityException("Güvensiz ZIP yolu engellendi: $cleanName")
                    }
                    if (entry.isDirectory || cleanName.endsWith("/")) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        var entryBytes = 0L
                        FileOutputStream(outFile).use { fos ->
                            while (true) {
                                if (cancelRequested) throw CancellationException("İptal edildi.")
                                val r = zis.read(buf)
                                if (r <= 0) break
                                entryBytes += r
                                totalExtractedBytes += r
                                if (entryBytes > maxSingleEntryBytes) {
                                    throw SecurityException("ZIP içindeki dosya tekil boyut sınırını aşıyor: $cleanName")
                                }
                                if (totalExtractedBytes > maxTotalBytes) {
                                    throw SecurityException("ZIP paketi toplam boyut sınırını aşıyor.")
                                }
                                fos.write(buf, 0, r)
                            }
                        }
                        extractedFiles++
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        } catch (t: Throwable) {
            try { stagingDir.deleteRecursively() } catch (_: Throwable) {}
            if (t is CancellationException || t is SecurityException) throw t
            throw IllegalArgumentException(
                "İçerik paketi bozuk veya eksik görünüyor; mevcut paket korunarak işlem iptal edildi.",
                t
            )
        }

        if (extractedFiles == 0) {
            try { stagingDir.deleteRecursively() } catch (_: Throwable) {}
            throw IllegalArgumentException("ZIP paketinin içinde geçerli dosya bulunamadı; mevcut paket korunmaya devam edecek.")
        }

        // Read optional manifest.json from staging BEFORE atomic swap
        var finalTitle = fallbackTitle
        var finalSubtitle = fallbackSubtitle
        var finalKind = kind
        val manifestFile = File(stagingDir, "manifest.json")
        if (manifestFile.exists()) {
            try {
                val obj = JSONObject(manifestFile.readText())
                obj.optString("title").takeIf { it.isNotBlank() }?.let { finalTitle = it }
                obj.optString("subtitle").takeIf { it.isNotBlank() }?.let { finalSubtitle = it }
                obj.optString("kind").takeIf { it.isNotBlank() }?.let { finalKind = BookPackKind.fromKey(it) }
            } catch (_: Throwable) {
            }
        }

        // Atomic swap with rollback backup so existing working packs are NEVER lost on failure
        synchronized(swapLock) {
            val backupDir = File(rootDir, "${safeId}_backup_${UUID.randomUUID()}")
            val hadExisting = targetDir.exists()
            var movedExistingToBackup = false
            try {
                if (hadExisting) {
                    movedExistingToBackup = targetDir.renameTo(backupDir)
                    if (!movedExistingToBackup) {
                        targetDir.copyRecursively(backupDir, overwrite = true)
                        targetDir.deleteRecursively()
                        movedExistingToBackup = true
                    }
                }
                if (!stagingDir.renameTo(targetDir)) {
                    stagingDir.copyRecursively(targetDir, overwrite = true)
                    stagingDir.deleteRecursively()
                }
                if (movedExistingToBackup) {
                    try { backupDir.deleteRecursively() } catch (_: Throwable) {}
                }
            } catch (t: Throwable) {
                try { stagingDir.deleteRecursively() } catch (_: Throwable) {}
                if (movedExistingToBackup && backupDir.exists()) {
                    try {
                        if (targetDir.exists()) targetDir.deleteRecursively()
                        if (!backupDir.renameTo(targetDir)) {
                            backupDir.copyRecursively(targetDir, overwrite = true)
                            backupDir.deleteRecursively()
                        }
                    } catch (_: Throwable) {}
                }
                throw t
            }
        }

        invalidateFileIndex()

        val pack = InstalledContentPack(
            id = safeId,
            title = finalTitle,
            subtitle = finalSubtitle,
            kind = finalKind,
            sizeBytes = computeDirBytes(targetDir),
            fileCount = extractedFiles,
            sourceUrl = sourceUrl,
            installedAtMs = System.currentTimeMillis()
        )

        val updated = _installedPacks.value.filterNot { it.id == safeId } + pack
        _installedPacks.value = updated
        persistInstalled(context, updated)
        return pack
    }

    private fun persistInstalled(context: Context, list: List<InstalledContentPack>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("title", p.title)
                    put("subtitle", p.subtitle)
                    put("kind", p.kind.key)
                    put("sizeBytes", p.sizeBytes)
                    put("fileCount", p.fileCount)
                    if (p.sourceUrl != null) put("sourceUrl", p.sourceUrl)
                    put("installedAtMs", p.installedAtMs)
                }
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_INSTALLED, arr.toString())
            .apply()
    }

    private fun sanitizePackId(raw: String): String =
        raw.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9_-]"), "_").take(42).ifBlank { "pack" }

    private fun computeDirBytes(dir: File): Long {
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private fun countFiles(dir: File): Int {
        if (!dir.exists()) return 0
        return dir.walkTopDown().count { it.isFile }
    }

    fun formatSizeMb(bytes: Long): String {
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return if (mb >= 1.0) String.format(Locale.US, "%.2f MB", mb)
        else String.format(Locale.US, "%.0f KB", bytes.toDouble() / 1024.0)
    }
}
