package com.example.util

/**
 * Auto-suggests a friendly Turkish/brand display name from a pasted Custom API Base URL.
 */
object CustomApiNameAutoSuggester {

    fun suggestLabel(rawUrl: String): String {
        val info = CustomApiEndpointInfoHelper.inspect(rawUrl)
        val host = info.host
        if (host.isBlank()) return ""
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
            info.isLocalServer && rawUrl.contains("11434") -> "Yerel Ollama"
            info.isLocalServer && rawUrl.contains("1234") -> "LM Studio"
            info.isLocalServer -> "Yerel Sunucu"
            else -> host.removePrefix("api.").substringBefore('.').replaceFirstChar { it.uppercase() }
        }
    }
}
