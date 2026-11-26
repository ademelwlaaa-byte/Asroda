package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Real in-app APK updater backed by the public, source-free GitHub Releases repository
 * (`ademelwlaaa-byte/Asroda-Releases`). Downloads signed APKs with live byte progress
 * and launches Android's native package installer via FileProvider.
 */
object AppUpdateManager {

    const val RELEASES_REPO = "ademelwlaaa-byte/Asroda-Releases"
    const val LATEST_RELEASE_API_URL = "https://api.github.com/repos/$RELEASES_REPO/releases/latest"

    data class ReleaseInfo(
        val tagName: String,
        val versionName: String,
        val title: String,
        val releaseNotes: String,
        val apkDownloadUrl: String,
        val apkSizeBytes: Long,
        val htmlUrl: String
    )

    sealed class CheckResult {
        data class UpdateAvailable(val release: ReleaseInfo) : CheckResult()
        data class UpToDate(val currentVersion: String, val latestVersion: String) : CheckResult()
        data class Error(val message: String) : CheckResult()
    }

    /**
     * Compares dotted numeric version strings (e.g. "v12.0" vs "11.9" -> >0, "11.10" vs "11.9" -> >0).
     */
    fun compareVersions(remoteRaw: String, currentRaw: String): Int {
        val remoteParts = parseVersionParts(remoteRaw)
        val currentParts = parseVersionParts(currentRaw)
        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r != c) return r.compareTo(c)
        }
        return 0
    }

    internal fun parseVersionParts(raw: String): List<Int> {
        val cleaned = raw.trim().removePrefix("v").removePrefix("V")
        return cleaned.split('.', '-', '_')
            .mapNotNull { token -> token.takeWhile { it.isDigit() }.toIntOrNull() }
            .ifEmpty { listOf(0) }
    }

    internal fun parseLatestReleaseJson(jsonText: String): ReleaseInfo? {
        val obj = JSONObject(jsonText)
        val tagName = obj.optString("tag_name", "").trim()
        if (tagName.isBlank()) return null
        val versionName = tagName.removePrefix("v").removePrefix("V").trim()
        val title = obj.optString("name", "").trim().ifBlank { "Asroda v$versionName" }
        val notes = obj.optString("body", "").trim()
            .ifBlank { "Yeni Asroda sürümü yayınlandı. Hata düzeltmeleri ve iyileştirmeler içerir." }
        val htmlUrl = obj.optString("html_url", "https://github.com/$RELEASES_REPO/releases/latest").trim()

        val assets = obj.optJSONArray("assets")
        var apkUrl = ""
        var apkSize = 0L
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val a = assets.optJSONObject(i) ?: continue
                val name = a.optString("name", "")
                val dl = a.optString("browser_download_url", "").trim()
                if (name.endsWith(".apk", ignoreCase = true) && dl.startsWith("https://", ignoreCase = true)) {
                    apkUrl = dl
                    apkSize = a.optLong("size", 0L)
                    break
                }
            }
        }
        if (apkUrl.isBlank()) return null
        return ReleaseInfo(
            tagName = tagName,
            versionName = versionName,
            title = title,
            releaseNotes = notes,
            apkDownloadUrl = apkUrl,
            apkSizeBytes = apkSize,
            htmlUrl = htmlUrl
        )
    }

    suspend fun checkForUpdate(currentVersionName: String): CheckResult = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(LATEST_RELEASE_API_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Asroda-Android/$currentVersionName")
            }
            val code = conn.responseCode
            if (code == 404) {
                return@withContext CheckResult.UpToDate(currentVersionName, currentVersionName)
            }
            if (code !in 200..299) {
                return@withContext CheckResult.Error("Güncelleme sunucusu yanıt vermedi (HTTP $code).")
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val release = parseLatestReleaseJson(body)
                ?: return@withContext CheckResult.Error("Yayınlanmış geçerli bir APK bulunamadı.")

            if (compareVersions(release.versionName, currentVersionName) > 0) {
                CheckResult.UpdateAvailable(release)
            } else {
                CheckResult.UpToDate(currentVersionName, release.versionName)
            }
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            CheckResult.Error("İnternet bağlantısı veya sunucu hatası: ${t.message ?: "Bilinmeyen hata"}")
        } finally {
            try { conn?.disconnect() } catch (_: Throwable) {}
        }
    }

    suspend fun downloadApk(
        context: Context,
        release: ReleaseInfo,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val updatesDir = File(context.applicationContext.cacheDir, "updates").apply { mkdirs() }
        // Clean older cached update APKs before downloading
        updatesDir.listFiles()?.forEach { old -> try { old.delete() } catch (_: Throwable) {} }

        val safeTag = release.tagName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val targetFile = File(updatesDir, "Asroda-$safeTag.apk")
        var conn: HttpURLConnection? = null
        try {
            var currentUrl = release.apkDownloadUrl
            var redirects = 0
            while (redirects < 6) {
                val c = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Asroda-Updater/12.0")
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
                    throw IllegalStateException("İndirme hatası (HTTP $code)")
                }
            }
            val activeConn = conn ?: throw IllegalStateException("APK indirme bağlantısı kurulamadı.")
            val total = activeConn.contentLengthLong.takeIf { it > 0L } ?: release.apkSizeBytes

            activeConn.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buf = ByteArray(16 * 1024)
                    var downloaded = 0L
                    var lastReport = 0L
                    while (true) {
                        val read = input.read(buf)
                        if (read <= 0) break
                        output.write(buf, 0, read)
                        downloaded += read
                        val now = System.currentTimeMillis()
                        if (now - lastReport >= 100L) {
                            lastReport = now
                            onProgress(downloaded, total)
                        }
                    }
                    onProgress(downloaded, if (total > 0L) total else downloaded)
                }
            }
            if (!targetFile.exists() || targetFile.length() < 100_000L) {
                throw IllegalStateException("İndirilen APK dosyası eksik veya bozuk.")
            }
            Result.success(targetFile)
        } catch (t: Throwable) {
            try { if (targetFile.exists()) targetFile.delete() } catch (_: Throwable) {}
            if (t is CancellationException) throw t
            Result.failure(t)
        } finally {
            try { conn?.disconnect() } catch (_: Throwable) {}
        }
    }

    /**
     * Launches Android's native package installer for [apkFile].
     * Returns true if the installer opened, or false if the user was redirected to grant
     * "Install unknown apps" permission first.
     */
    fun launchApkInstaller(context: Context, apkFile: File): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            val permIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(permIntent)
            return false
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
        return true
    }

    fun formatSizeMb(bytes: Long): String {
        if (bytes <= 0L) return ""
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
