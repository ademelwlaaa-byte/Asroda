package com.example.util

import java.util.Locale

/**
 * Estimates the typical context window size for popular LLM model identifiers
 * so Custom API users can quickly judge suitability for long novel sessions.
 */
object CustomApiModelContextEstimator {

    fun estimateContextLabel(modelId: String): String? {
        val m = modelId.trim().lowercase(Locale.ROOT)
        if (m.isEmpty()) return null
        return when {
            m.contains("1m") || m.contains("gemini-2") || m.contains("gemini-1.5") || m.contains("gemini-3") || m.contains("gemini-4") || m.contains("llama-4") ->
                "📚 1M bağlam"
            m.contains("200k") || m.contains("claude-3") || m.contains("claude-4") || m.contains("claude-5") || m.contains("sonnet") || m.contains("opus") || m.contains("haiku") ->
                "📚 200K bağlam"
            m.contains("128k") || m.contains("gpt-4o") || m.contains("gpt-4-turbo") || m.contains("gpt-5") || m.contains("llama-3.3") || m.contains("llama-3.1") || m.contains("qwen2.5") || m.contains("qwen3") || m.contains("deepseek") || m.contains("mistral-large") ->
                "📚 128K bağlam"
            m.contains("64k") || m.contains("mixtral") ->
                "📚 64K bağlam"
            m.contains("32k") || m.contains("gemma-2") || m.contains("gemma-3") || m.contains("mistral-7b") ->
                "📚 32K bağlam"
            m.contains("8k") || m.contains("llama-3-8b") ->
                "📚 8K bağlam"
            else -> null
        }
    }
}
