package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.BotEntity
import com.example.data.local.MessageEntity
import com.example.data.repository.EmochiRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized, crash-free (FileProvider + SAF) chat & backup exporter.
 *
 * - Supports `.txt`, `.md` (literary novel format), and `.json`.
 * - Never sends huge chat transcripts or backup JSON via `Intent.EXTRA_TEXT` (avoids
 *   Android Binder `TransactionTooLargeException`). Always writes to `cacheDir/exports/`
 *   and shares via `FileProvider` `Intent.EXTRA_STREAM`.
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

    private val trLocale: Locale = Locale.forLanguageTag("tr-TR")

    private fun formatDateTime(timestampMs: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", trLocale)
        return sdf.format(Date(timestampMs))
    }

    private fun formatFileStamp(timestampMs: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        return sdf.format(Date(timestampMs))
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

    fun buildExportContent(
        bot: BotEntity,
        messages: List<MessageEntity>,
        format: ExportFormat,
        dateRange: DateRange,
        nowMs: Long = System.currentTimeMillis()
    ): String {
        val filtered = filterByRange(messages, dateRange, nowMs)
        return when (format) {
            ExportFormat.TEXT -> buildTextExport(bot, filtered, nowMs)
            ExportFormat.MARKDOWN -> buildMarkdownExport(bot, filtered, nowMs)
            ExportFormat.JSON -> buildJsonExport(bot, filtered, nowMs)
        }
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
     * Writes formatted chat export directly to a user-selected SAF [Uri].
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
            val content = buildExportContent(bot, messages, format, dateRange)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
                stream.flush()
            } ?: return false
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Shares the chat export safely via `FileProvider` (`Intent.EXTRA_STREAM`) so arbitrarily
     * long conversations never hit `TransactionTooLargeException`.
     */
    fun export(
        context: Context,
        bot: BotEntity,
        messages: List<MessageEntity>,
        format: ExportFormat,
        dateRange: DateRange
    ): File {
        cleanupOldExports(context)
        val content = buildExportContent(bot, messages, format, dateRange)
        val fileName = suggestedFileName(bot, format)
        val cacheDir = File(context.cacheDir, EXPORTS_DIR_NAME).also { it.mkdirs() }
        val file = File(cacheDir, fileName)
        file.writeText(content, Charsets.UTF_8)

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
        cleanupOldExports(context)
        val cacheDir = File(context.cacheDir, EXPORTS_DIR_NAME).also { it.mkdirs() }
        val safeFileName = if (fileName.endsWith(".json")) fileName else "$fileName.json"
        val file = File(cacheDir, safeFileName)
        file.writeText(jsonContent, Charsets.UTF_8)

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

    private fun buildTextExport(bot: BotEntity, messages: List<MessageEntity>, nowMs: Long): String {
        val displayTitle = if (bot.mode == "universe") bot.universeName.ifBlank { bot.aiName } else bot.aiName
        val userLabel = bot.userCharName.ifBlank { "Sen" }
        val sb = StringBuilder()
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine("  Emochi — $displayTitle")
        sb.appendLine("  Dışa aktarıldı: ${formatDateTime(nowMs)}")
        sb.appendLine("  Toplam mesaj: ${messages.size}")
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine()
        for (msg in messages) {
            val sender = if (msg.role == "assistant") displayTitle else userLabel
            val time = formatDateTime(msg.timestamp)
            val starPrefix = if (msg.isStarred) "★ " else ""
            sb.appendLine("[$time] $starPrefix$sender")
            sb.appendLine(EmochiRepository.cleanEmotionTags(msg.text))
            sb.appendLine()
        }
        return sb.toString()
    }

    private fun buildMarkdownExport(bot: BotEntity, messages: List<MessageEntity>, nowMs: Long): String {
        val displayTitle = if (bot.mode == "universe") bot.universeName.ifBlank { bot.aiName } else bot.aiName
        val userLabel = bot.userCharName.ifBlank { "Sen" }
        val sb = StringBuilder()
        sb.appendLine("# ✦ $displayTitle")
        sb.appendLine()
        sb.appendLine("> **Dışa Aktarım Tarihi:** ${formatDateTime(nowMs)}  ")
        sb.appendLine("> **Toplam Sahne / Mesaj:** ${messages.size}")
        if (bot.scenario.isNotBlank()) {
            sb.appendLine("> **Senaryo:** ${bot.scenario.replace("\n", " ").take(240)}")
        }
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
        for (msg in messages) {
            val sender = if (msg.role == "assistant") displayTitle else userLabel
            val time = formatDateTime(msg.timestamp)
            val starMark = if (msg.isStarred) " ★" else ""
            val reactionMark = if (msg.reaction.isNotBlank()) " · ${msg.reaction}" else ""
            sb.appendLine("### $sender$starMark")
            sb.appendLine("*$time$reactionMark*")
            sb.appendLine()
            val cleanText = EmochiRepository.cleanEmotionTags(msg.text)
            if (msg.role == "user") {
                cleanText.lines().forEach { line ->
                    sb.appendLine("> $line")
                }
            } else {
                sb.appendLine(cleanText)
            }
            sb.appendLine()
            sb.appendLine("---")
            sb.appendLine()
        }
        return sb.toString()
    }

    private fun buildJsonExport(bot: BotEntity, messages: List<MessageEntity>, nowMs: Long): String {
        val displayTitle = if (bot.mode == "universe") bot.universeName.ifBlank { bot.aiName } else bot.aiName
        val root = JSONObject()
        root.put("exportedAt", formatDateTime(nowMs))
        root.put("exportedAtEpochMs", nowMs)
        root.put("botId", bot.id)
        root.put("botName", displayTitle)
        root.put("botMode", bot.mode)
        root.put("userCharName", bot.userCharName)
        root.put("messageCount", messages.size)
        val arr = JSONArray()
        for (msg in messages) {
            val obj = JSONObject()
            obj.put("id", msg.id)
            obj.put("role", msg.role)
            obj.put("text", EmochiRepository.cleanEmotionTags(msg.text))
            obj.put("timestamp", msg.timestamp)
            obj.put("timestampFormatted", formatDateTime(msg.timestamp))
            obj.put("provider", msg.provider ?: "")
            obj.put("isStarred", msg.isStarred)
            obj.put("reaction", msg.reaction)
            arr.put(obj)
        }
        root.put("messages", arr)
        return root.toString(2)
    }
}