package com.example.util

import java.util.Locale

/**
 * Auto-suggests a friendly Turkish/brand display name from a pasted Custom API Base URL.
 */
object CustomApiNameAutoSuggester {

    fun suggestLabel(rawUrl: String): String {
        val info = CustomApiEndpointInfoHelper.inspect(rawUrl)
        val host = info.host
        if (host.isBlank()) return ""
        val sanitizedUrl = CustomApiEndpointInfoHelper.sanitizeUrlForDisplay(rawUrl)
        return when {
            host.contains("openrouter") -> "OpenRouter"
            host.contains("deepseek") -> "DeepSeek"
            host.contains("groq") -> "Groq"
            host.contains("nvidia") -> "NVIDIA NIM"
            host.contains("together") -> "Together AI"
            host.contains("mistral") -> "Mistral AI"
            host.contains("anthropic") -> "Anthropic"
            host.contains("openai") -> "OpenAI"
            host.contains("cerebras") -> "Cerebras"
            host.contains("fireworks") -> "Fireworks AI"
            info.isLocalServer && sanitizedUrl.contains(":11434") -> "Yerel Ollama"
            info.isLocalServer && sanitizedUrl.contains(":1234") -> "LM Studio"
            info.isLocalServer -> "Yerel Sunucu"
            else -> {
                val base = host.removePrefix("api.").removePrefix("www.").substringBefore('.').trim()
                if (base.isBlank()) "Özel API"
                else base.replaceFirstChar { it.titlecase(Locale.ROOT) }
            }
        }
    }
}
