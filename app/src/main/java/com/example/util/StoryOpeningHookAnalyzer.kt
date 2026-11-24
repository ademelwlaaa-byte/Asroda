package com.example.util

import com.example.data.local.MessageEntity

/**
 * Classifies the literary hook style of the story's opening assistant message.
 */
object StoryOpeningHookAnalyzer {

    fun classifyOpening(messages: List<MessageEntity>): String {
        val opening = messages.firstOrNull { it.role == "assistant" && it.text.isNotBlank() }?.text?.trim()
            ?: return "🌱 Açılış Bekleniyor"
        val hasQuote = opening.contains('"') || opening.contains('“') || opening.contains('«')
        val hasAction = opening.contains('*')
        val hasQuestion = opening.contains('?')
        val wordCount = opening.split(Regex("\\s+")).count { it.isNotBlank() }

        return when {
            hasQuote && hasAction && wordCount >= 20 -> "🎬 Sinematik Açılış"
            hasQuote -> "💬 Diyalogla Giriş"
            hasQuestion && wordCount >= 12 -> "❓ Merak Uyandıran Giriş"
            hasAction || wordCount >= 35 -> "🌌 Atmosferik Başlangıç"
            else -> "📝 Sade Giriş"
        }
    }
}
