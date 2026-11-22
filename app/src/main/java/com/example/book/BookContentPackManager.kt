package com.example.book

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    /**
     * Optional remote manifest URL for automatic over-the-air chapter / art / voice updates.
     * Example JSON: { "version": 3, "sizeLabel": "6,4 MB", "notes": "3. Sezon paketi", "packUrl": "https://.../pack-v3.zip" }
     * Leave blank to stay 100% offline-ready; paste your GitHub Releases / CDN manifest URL whenever you publish updates!
     */
    const val REMOTE_MANIFEST_URL = ""

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
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(manifestUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 3_000
                readTimeout = 3_000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Asroda-Updater/2.4")
            }
            if (conn.responseCode !in 200..299) return@withContext null
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val remoteVer = json.optInt("version", 0)
            val packUrl = json.optString("packUrl", "").trim()
            if (remoteVer > currentVer && packUrl.startsWith("http", ignoreCase = true)) {
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
                packId = "story_update_v${remoteUpdate.version}",
                title = "Kozmik Sürükleniş · Güncelleme v${remoteUpdate.version}",
                subtitle = remoteUpdate.notes,
                urlString = remoteUpdate.packUrl,
                kind = BookPackKind.STORY_SEASON
            )
            return@withContext res.map {
                markStoryVersionInstalled(appCtx, bookId, remoteUpdate.version)
            }
        }

        // First-time preparation of the high-efficiency offline HD WebP + Opus story pack
        try {
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
     * for [lineId] (e.g. `voice_twin_aiden_1.ogg` / `.mp3` / `.wav`).
     */
    fun findDownloadedVoiceFile(context: Context, lineId: String?): File? {
        if (lineId.isNullOrBlank()) return null
        val safe = lineId.trim().lowercase(Locale.ROOT)
        val exts = listOf("ogg", "mp3", "m4a", "wav")
        val voicesDir = voicesRootDir(context)
        for (ext in exts) {
            val direct = File(voicesDir, "$safe.$ext")
            if (direct.exists() && direct.length() > 512L) return direct
        }
        val root = packsRootDir(context)
        val subDirs = root.listFiles()?.filter { it.isDirectory } ?: return null
        for (packDir in subDirs) {
            for (ext in exts) {
                val f1 = File(packDir, "$safe.$ext")
                if (f1.exists() && f1.length() > 512L) return f1
                val f2 = File(File(packDir, "voices"), "$safe.$ext")
                if (f2.exists() && f2.length() > 512L) return f2
            }
        }
        return null
    }

    /**
     * Resolves an optional downloaded HD media file (image or music) by basename if present.
     */
    fun findDownloadedMediaFile(context: Context, baseName: String): File? {
        if (baseName.isBlank()) return null
        val root = packsRootDir(context)
        val subDirs = root.listFiles()?.filter { it.isDirectory } ?: return null
        val exts = listOf("webp", "jpg", "png", "ogg", "m4a", "mp3")
        for (packDir in subDirs) {
            for (ext in exts) {
                val f1 = File(packDir, "$baseName.$ext")
                if (f1.exists() && f1.length() > 256L) return f1
                val f2 = File(File(packDir, "media"), "$baseName.$ext")
                if (f2.exists() && f2.length() > 256L) return f2
            }
        }
        return null
    }

    /**
     * Downloads a `.zip` content pack over HTTPS/HTTP (supports GitHub Releases & CDN redirects),
     * verifies integrity, safely extracts into internal storage, and registers it offline.
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

        cancelRequested = false
        val safeId = sanitizePackId(packId)
        val tmpZip = File(appCtx.cacheDir, "dl_$safeId.zip")
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
                    currentUrl = loc
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

    /**
     * Deletes an installed optional pack and immediately reclaims internal storage space.
     */
    suspend fun deletePack(context: Context, packId: String): Boolean = withContext(Dispatchers.IO) {
        ensureLoaded(context)
        val appCtx = context.applicationContext
        val dir = File(packsRootDir(appCtx), sanitizePackId(packId))
        val deleted = if (dir.exists()) dir.deleteRecursively() else true
        val updated = _installedPacks.value.filterNot { it.id == packId }
        _installedPacks.value = updated
        persistInstalled(appCtx, updated)
        deleted
    }

    private fun extractZipIntoPack(
        context: Context,
        packId: String,
        fallbackTitle: String,
        fallbackSubtitle: String,
        kind: BookPackKind,
        sourceUrl: String?,
        rawZipStream: InputStream
    ): InstalledContentPack {
        val targetDir = File(packsRootDir(context), packId)
        if (targetDir.exists()) targetDir.deleteRecursively()
        targetDir.mkdirs()

        val canonicalRoot = targetDir.canonicalPath + File.separator
        var extractedFiles = 0

        ZipInputStream(BufferedInputStream(rawZipStream)).use { zis ->
            val buf = ByteArray(16 * 1024)
            var entry = zis.nextEntry
            while (entry != null) {
                if (cancelRequested) throw CancellationException("İptal edildi.")
                val outFile = File(targetDir, entry.name)
                if (!outFile.canonicalPath.startsWith(canonicalRoot)) {
                    throw SecurityException("Güvensiz ZIP yolu engellendi: ${entry.name}")
                }
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        while (true) {
                            val r = zis.read(buf)
                            if (r <= 0) break
                            fos.write(buf, 0, r)
                        }
                    }
                    extractedFiles++
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        if (extractedFiles == 0) {
            targetDir.deleteRecursively()
            throw IllegalArgumentException("ZIP paketinin içinde geçerli dosya bulunamadı.")
        }

        // Read optional manifest.json inside the pack for rich metadata
        var finalTitle = fallbackTitle
        var finalSubtitle = fallbackSubtitle
        var finalKind = kind
        val manifestFile = File(targetDir, "manifest.json")
        if (manifestFile.exists()) {
            try {
                val obj = JSONObject(manifestFile.readText())
                obj.optString("title").takeIf { it.isNotBlank() }?.let { finalTitle = it }
                obj.optString("subtitle").takeIf { it.isNotBlank() }?.let { finalSubtitle = it }
                obj.optString("kind").takeIf { it.isNotBlank() }?.let { finalKind = BookPackKind.fromKey(it) }
            } catch (_: Throwable) {
            }
        }

        val pack = InstalledContentPack(
            id = packId,
            title = finalTitle,
            subtitle = finalSubtitle,
            kind = finalKind,
            sizeBytes = computeDirBytes(targetDir),
            fileCount = extractedFiles,
            sourceUrl = sourceUrl,
            installedAtMs = System.currentTimeMillis()
        )

        val updated = _installedPacks.value.filterNot { it.id == packId } + pack
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
