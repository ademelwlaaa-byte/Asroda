package com.example.data.managers

/**
 * Curated 1-tap quick-fill presets for Custom (OpenAI / Anthropic-compatible) API endpoints.
 * Saves mobile users from manually typing base URLs while still letting them fetch all live models
 * and choose any main or manual utility model freely.
 */
object CustomApiQuickPresets {

    data class Preset(
        val id: String,
        val chipLabel: String,
        val displayName: String,
        val baseUrl: String,
        val apiFormat: String,
        val suggestedModel: String
    )

    val presets: List<Preset> = listOf(
        Preset(
            id = "openrouter",
            chipLabel = "OpenRouter",
            displayName = "OpenRouter Özel",
            baseUrl = "https://openrouter.ai/api/v1",
            apiFormat = "openai",
            suggestedModel = "deepseek/deepseek-chat-v3-0324"
        ),
        Preset(
            id = "deepseek",
            chipLabel = "DeepSeek",
            displayName = "DeepSeek API",
            baseUrl = "https://api.deepseek.com/v1",
            apiFormat = "openai",
            suggestedModel = "deepseek-chat"
        ),
        Preset(
            id = "groq",
            chipLabel = "Groq",
            displayName = "Groq Hızlı API",
            baseUrl = "https://api.groq.com/openai/v1",
            apiFormat = "openai",
            suggestedModel = "llama-3.3-70b-versatile"
        ),
        Preset(
            id = "nvidia",
            chipLabel = "NVIDIA NIM",
            displayName = "NVIDIA NIM",
            baseUrl = "https://integrate.api.nvidia.com/v1",
            apiFormat = "openai",
            suggestedModel = "meta/llama-3.3-70b-instruct"
        ),
        Preset(
            id = "together",
            chipLabel = "Together",
            displayName = "Together AI",
            baseUrl = "https://api.together.xyz/v1",
            apiFormat = "openai",
            suggestedModel = "meta-llama/Llama-3.3-70B-Instruct-Turbo"
        ),
        Preset(
            id = "ollama",
            chipLabel = "Ollama (Yerel)",
            displayName = "Ollama Yerel Sunucu",
            baseUrl = "http://192.168.1.2:11434/v1",
            apiFormat = "openai",
            suggestedModel = "llama3.1:8b"
        ),
        Preset(
            id = "lmstudio",
            chipLabel = "LM Studio",
            displayName = "LM Studio Yerel",
            baseUrl = "http://192.168.1.2:1234/v1",
            apiFormat = "openai",
            suggestedModel = "local-model"
        )
    )
}
