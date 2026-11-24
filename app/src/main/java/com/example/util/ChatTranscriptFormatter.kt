package com.example.util

import com.example.data.local.MessageEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formats an Asroda chat session into clean, readable Markdown for clipboard export or sharing.
 * Filters out system/internal/failed stubs and strips hidden metadata tags cleanly.
 */
object ChatTranscriptFormatter {

    fun formatMarkdown(
        botDisplayName: String,
        userDisplayName: String,
        messages: List<MessageEntity>
    ): String {
        val cleanBot = botDisplayName.trim().ifBlank { "Karakter" }
        val cleanUser = userDisplayName.trim().ifBlank { "Sen" }
        val valid = messages.filter {
            (it.role == "user" || it.role == "assistant") &&
                it.status != "error" &&
                it.text.isNotBlank()
        }
        if (valid.isEmpty()) return ""

        val dateFmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("tr-TR"))
        return buildString {
            append("# ").append(cleanBot).append(" — Hikaye Kaydı\n\n")
            valid.forEachIndexed { idx, msg ->
                val speaker = if (msg.role == "user") cleanUser else cleanBot
                val timeStr = if (msg.timestamp > 0L) " _(${dateFmt.format(Date(msg.timestamp))})_" else ""
                append("**").append(speaker).append("**").append(timeStr).append("\n")
                append(msg.text.trim()).append("\n")
                if (idx < valid.lastIndex) append("\n---\n\n")
            }
        }
    }
}
