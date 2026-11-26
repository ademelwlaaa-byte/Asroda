package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.EmochiRepository
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized, streaming, OOM-safe & crash-free (FileProvider + SAF) chat & backup exporter.
 *
 * - Streams `.txt`, `.md` (literary novel format), and `.json` message-by-message through a
 *   [BufferedWriter] so 1,500+ message chats never allocate giant monolithic strings in RAM.
 * - Never sends chat transcripts or backup JSON via `Intent.EXTRA_TEXT` (avoids Android Binder
 *   `TransactionTooLargeException`). Always writes to `cacheDir/exports/` and shares via
 *   `FileProvider` `Intent.EXTRA_STREAM`.
 * - Automatically purges stale temporary export files older than 24 hours.
 */
object ChatExporter {

    enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
        TEXT("txt", "text/plain", "Metin (.txt)"),
        MARKDOWN("md", "text/markdown", "Roman (.md)"),
        JSON("json", "application/json", "Veri (.json)")
    }

    enum class DateRange(val label: String) {
        ALL("Tümü"),
        LAST_24_HOURS("24 Saat"),
        LAST_7_DAYS("1 Hafta"),
        LAST_30_DAYS("1 Ay")
    }

    private const val EXPORTS_DIR_NAME = "exports"
    private const val DEFAULT_STALE_EXPORT_AGE_MS = 24L * 60L * 60L * 1000L
    private const val BUFFER_SIZE_BYTES = 16 * 1024

    private val trLocale: Locale = Locale.forLanguageTag("tr-TR")

    private fun createDateTimeFormatter(): SimpleDateFormat =
        SimpleDateFormat("dd.MM.yyyy HH:mm", trLocale)

    private fun formatDateTime(timestampMs: Long): String =
        createDateTimeFormatter().format(Date(timestampMs))

    private fun formatFileStamp(timestampMs: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        return sdf.format(Date(timestampMs))
    }

    /**
     * Sanitizes any user- or caller-supplied filename against path traversal (`../`, slashes, NUL)
     * and normalizes it to a safe single-segment filename inside `cacheDir/exports/`.
     */
    fun sanitizeExportFileName(rawFileName: String, requiredExtension: String = "json", fallbackBase: String = "emochi_yedek"): String {
        val ext = requiredExtension.trimStart('.').lowercase(Locale.US)
        val leaf = rawFileName
            .replace('\\', '/')
            .substringAfterLast('/')
            .replace("\u0000", "")
            .trim()
        val withoutExt = if (ext.isNotEmpty() && leaf.lowercase(Locale.US).endsWith(".$ext")) {
            leaf.dropLast(ext.length + 1)
        } else {
            leaf
        }
        val cleanedBase = withoutExt
            .replace(Regex("""\.\.+"""), "_")
            .replace(Regex("""[^a-zA-Z0-9çğıöşüÇĞİÖŞÜ._-]+"""), "_")
            .trim('.', '_', '-', ' ')
            .take(64)
            .ifBlank { "${fallbackBase}_${formatFileStamp()}" }
        return if (ext.isNotEmpty()) "$cleanedBase.$ext" else cleanedBase
    }

    private fun resolveUniqueExportFile(cacheDir: File, desiredName: String): File {
        val canonicalDir = cacheDir.canonicalFile
        var candidate = File(canonicalDir, desiredName)
        if (!candidate.canonicalPath.startsWith(canonicalDir.canonicalPath + File.separator)) {
            candidate = File(canonicalDir, sanitizeExportFileName(desiredName))
        }
        if (!candidate.exists()) return candidate
        val dotIdx = candidate.name.lastIndexOf('.')
        val stem = if (dotIdx > 0) candidate.name.substring(0, dotIdx) else candidate.name
        val ext = if (dotIdx > 0) candidate.name.substring(dotIdx) else ""
        for (suffix in 1..999) {
            val alt = File(canonicalDir, "${stem}_$suffix$ext")
            if (!alt.exists()) return alt
        }
        return File(canonicalDir, "${stem}_${System.nanoTime()}$ext")
    }

    fun filterByRange(
        messages: List<MessageEntity>,
        range: DateRange,
        nowMs: Long = System.currentTimeMillis()
    ): List<MessageEntity> {
        return when (range) {
            DateRange.ALL -> messages
            DateRange.LAST_24_HOURS -> messages.filter { nowMs - it.timestamp <= 24L * 60L * 60L * 1000L }
            DateRange.LAST_7_DAYS -> messages.filter { nowMs - it.timestamp <= 7L * 24L * 60L * 60L * 1000L }
            DateRange.LAST_30_DAYS -> messages.filter { nowMs - it.timestamp <= 30L * 24L * 60L * 60L * 1000L }
        }
    }

    fun suggestedFileName(
        bot: BotEntity,
        format: ExportFormat,
        nowMs: Long = System.currentTimeMillis()
    ): String {
        val rawTitle = if (bot.mode == "universe") bot.universeName.ifBlank { bot.aiName } else bot.aiName
        val safeTitle = rawTitle
            .ifBlank { "sohbet" }
            .take(24)
            .replace(Regex("[^a-zA-Z0-9çğıöşüÇĞİÖŞÜ_-]+"), "_")
            .trim('_')
            .ifBlank { "sohbet" }
        return "emochi_${safeTitle}_${formatFileStamp(nowMs)}.${format.extension}"
    }

    /**
     * Streams formatted export directly into any [Writer] (file, SAF stream, or in-memory writer)
     * one message at a time with O(1) memory overhead.
     */
    fun writeExportToWriter(
        writer: Writer,
        bot: BotEntity,
        messages: List<MessageEntity>,
        format: ExportFormat,
        dateRange: DateRange,
        nowMs: Long = System.currentTimeMillis()
    ) {
        val filtered = filterByRange(messages, dateRange, nowMs)
        val buffered = if (writer is BufferedWriter) writer else BufferedWriter(writer, BUFFER_SIZE_BYTES)
        when (format) {
            ExportFormat.TEXT -> streamTextExport(buffered, bot, filtered, nowMs)
            ExportFormat.MARKDOWN -> streamMarkdownExport(buffered, bot, filtered, nowMs)
            ExportFormat.JSON -> streamJsonExport(buffered, bot, filtered, nowMs)
        }
        buffered.flush()
    }

    fun buildExportContent(
        bot: BotEntity,
        messages: List<MessageEntity>,
        format: ExportFormat,
        dateRange: DateRange,
        nowMs: Long = System.currentTimeMillis()
    ): String {
        val sw = StringWriter()
        writeExportToWriter(sw, bot, messages, format, dateRange, nowMs)
        return sw.toString()
    }

    /**
     * Streams chat export directly to a file inside `cacheDir/exports/` without holding the entire
     * output string in RAM. Also cleans up stale export files older than 24 hours.
     */
    fun exportToFile(
        context: Context,
        bot: BotEntity,
        messages: List<MessageEntity>,
        format: ExportFormat,
        dateRange: DateRange,
        nowMs: Long = System.currentTimeMillis()
    ): File {
        cleanupOldExports(context, nowMs = nowMs)
        val fileName = sanitizeExportFileName(
            suggestedFileName(bot, format, nowMs),
            requiredExtension = format.extension,
            fallbackBase = "emochi_sohbet"
        )
        val cacheDir = File(context.cacheDir, EXPORTS_DIR_NAME).also { it.mkdirs() }
        val file = resolveUniqueExportFile(cacheDir, fileName)
        BufferedWriter(OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8), BUFFER_SIZE_BYTES).use { writer ->
            writeExportToWriter(writer, bot, messages, format, dateRange, nowMs)
        }
        return file
    }

    /**
     * Deletes temporary files inside `cacheDir/exports/` older than [maxAgeMs] (default 24h).
     */
    fun cleanupOldExports(
        context: Context,
        maxAgeMs: Long = DEFAULT_STALE_EXPORT_AGE_MS,
        nowMs: Long = System.currentTimeMillis()
    ): Int {
        val dir = File(context.cacheDir, EXPORTS_DIR_NAME)
        if (!dir.exists() || !dir.isDirectory) return 0
        var removed = 0
        dir.listFiles()?.forEach { file ->
            if (file.isFile && (nowMs - file.lastModified()) > maxAgeMs) {
                if (file.delete()) removed++
            }
        }
        return removed
    }

    /**
     * Streams formatted chat export directly to a user-selected SAF [Uri] using [BufferedWriter].
     */
    fun exportToUri(
        context: Context,
        uri: Uri,
        bot: BotEntity,
        messages: List<MessageEntity>,
        format: ExportFormat,
        dateRange: DateRange
    ): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                BufferedWriter(OutputStreamWriter(stream, Charsets.UTF_8), BUFFER_SIZE_BYTES).use { writer ->
                    writeExportToWriter(writer, bot, messages, format, dateRange)
                }
            } ?: return false
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Shares the chat export safely via `FileProvider` (`Intent.EXTRA_STREAM`) using streaming file I/O
     * so arbitrarily long conversations never hit OOM or `TransactionTooLargeException`.
     */
    fun export(
        context: Context,
        bot: BotEntity,
        messages: List<MessageEntity>,
        format: ExportFormat,
        dateRange: DateRange
    ): File {
        val file = exportToFile(context, bot, messages, format, dateRange)
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val displayTitle = if (bot.mode == "universe") bot.universeName.ifBlank { bot.aiName } else bot.aiName

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Emochi — $displayTitle sohbeti")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Sohbeti Paylaş").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        return file
    }

    /**
     * Writes a JSON payload safely into `cacheDir/exports/` with path-traversal protection
     * and collision-safe unique file naming.
     */
    fun writeJsonToExportFile(
        context: Context,
        fileName: String,
        jsonContent: String
    ): File {
        cleanupOldExports(context)
        val cacheDir = File(context.cacheDir, EXPORTS_DIR_NAME).also { it.mkdirs() }
        val safeFileName = sanitizeExportFileName(fileName, requiredExtension = "json", fallbackBase = "emochi_yedek")
        val file = resolveUniqueExportFile(cacheDir, safeFileName)
        BufferedWriter(OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8), BUFFER_SIZE_BYTES).use { writer ->
            writer.write(jsonContent)
            writer.flush()
        }
        return file
    }

    /**
     * Shares a backup JSON string safely as a `.json` file via `FileProvider` (`Intent.EXTRA_STREAM`)
     * instead of `Intent.EXTRA_TEXT`.
     */
    fun shareJsonFile(
        context: Context,
        fileName: String,
        jsonContent: String,
        subject: String = "Emochi Yedeği",
        chooserTitle: String = "Yedeği Paylaş"
    ): File {
        val file = writeJsonToExportFile(context, fileName, jsonContent)

        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        return file
    }

    private fun streamTextExport(
        out: BufferedWriter,
        bot: BotEntity,
        messages: List<MessageEntity>,
        nowMs: Long
    ) {
        val displayTitle = if (bot.mode == "universe") bot.universeName.ifBlank { bot.aiName } else bot.aiName
        val userLabel = bot.userCharName.ifBlank { "Sen" }
        val fmt = createDateTimeFormatter()

        out.write("═══════════════════════════════════════\n")
        out.write("  Emochi — $displayTitle\n")
        out.write("  Dışa aktarıldı: ${fmt.format(Date(nowMs))}\n")
        out.write("  Toplam mesaj: ${messages.size}\n")
        out.write("═══════════════════════════════════════\n\n")

        for (msg in messages) {
            val sender = if (msg.role == "assistant") displayTitle else userLabel
            val time = fmt.format(Date(msg.timestamp))
            val starPrefix = if (msg.isStarred) "★ " else ""
            out.write("[$time] $starPrefix$sender\n")
            out.write(EmochiRepository.cleanEmotionTags(msg.text))
            out.write("\n\n")
        }
    }

    private fun streamMarkdownExport(
        out: BufferedWriter,
        bot: BotEntity,
        messages: List<MessageEntity>,
        nowMs: Long
    ) {
        val displayTitle = if (bot.mode == "universe") bot.universeName.ifBlank { bot.aiName } else bot.aiName
        val userLabel = bot.userCharName.ifBlank { "Sen" }
        val fmt = createDateTimeFormatter()

        out.write("# ✦ $displayTitle\n\n")
        out.write("> **Dışa Aktarım Tarihi:** ${fmt.format(Date(nowMs))}  \n")
        out.write("> **Toplam Sahne / Mesaj:** ${messages.size}\n")
        if (bot.scenario.isNotBlank()) {
            out.write("> **Senaryo:** ${bot.scenario.replace("\n", " ").take(240)}\n")
        }
        out.write("\n---\n\n")

        for (msg in messages) {
            val sender = if (msg.role == "assistant") displayTitle else userLabel
            val time = fmt.format(Date(msg.timestamp))
            val starMark = if (msg.isStarred) " ★" else ""
            val reactionMark = if (msg.reaction.isNotBlank()) " · ${msg.reaction}" else ""
            out.write("### $sender$starMark\n")
            out.write("*$time$reactionMark*\n\n")
            val cleanText = EmochiRepository.cleanEmotionTags(msg.text)
            if (msg.role == "user") {
                cleanText.lines().forEach { line ->
                    out.write("> $line\n")
                }
            } else {
                out.write(cleanText)
                out.write("\n")
            }
            out.write("\n---\n\n")
        }
    }

    private fun streamJsonExport(
        out: BufferedWriter,
        bot: BotEntity,
        messages: List<MessageEntity>,
        nowMs: Long
    ) {
        val displayTitle = if (bot.mode == "universe") bot.universeName.ifBlank { bot.aiName } else bot.aiName
        val fmt = createDateTimeFormatter()

        out.write("{\n")
        out.write("  \"version\": 2,\n")
        out.write("  \"exportedAt\": ${JSONObject.quote(fmt.format(Date(nowMs)))},\n")
        out.write("  \"exportedAtEpochMs\": $nowMs,\n")
        out.write("  \"botId\": ${JSONObject.quote(bot.id)},\n")
        out.write("  \"botName\": ${JSONObject.quote(displayTitle)},\n")
        out.write("  \"botMode\": ${JSONObject.quote(bot.mode)},\n")
        out.write("  \"userCharName\": ${JSONObject.quote(bot.userCharName)},\n")
        out.write("  \"messageCount\": ${messages.size},\n")
        out.write("  \"messages\": [")
        if (messages.isNotEmpty()) out.write("\n")

        messages.forEachIndexed { index, msg ->
            val obj = JSONObject()
            obj.put("id", msg.id)
            obj.put("role", msg.role)
            obj.put("text", EmochiRepository.cleanEmotionTags(msg.text))
            obj.put("timestamp", msg.timestamp)
            obj.put("timestampFormatted", fmt.format(Date(msg.timestamp)))
            obj.put("provider", msg.provider ?: "")
            obj.put("isStarred", msg.isStarred)
            obj.put("reaction", msg.reaction)
            out.write("    ")
            out.write(obj.toString())
            if (index < messages.lastIndex) out.write(",")
            out.write("\n")
        }
        out.write(if (messages.isNotEmpty()) "  ]\n}\n" else "]\n}\n")
    }
}