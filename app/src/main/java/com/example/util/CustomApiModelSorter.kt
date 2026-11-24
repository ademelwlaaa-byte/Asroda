package com.example.util

import java.util.Locale

/**
 * Sorts fetched Custom API model IDs so free and flagship conversational models
 * surface first in the model picker.
 */
object CustomApiModelSorter {

    fun sortForPicker(models: List<String>): List<String> {
        fun priority(modelId: String): Int {
            val m = modelId.lowercase(Locale.ROOT)
            var p = 0
            if (m.endsWith(":free")) p += 40
            if (m.contains("deepseek") || m.contains("gemini") || m.contains("claude") || m.contains("gpt-4") || m.contains("llama-3.3") || m.contains("qwen")) {
                p += 25
            }
            if (m.contains("instruct") || m.contains("chat") || m.contains("flash")) p += 10
            return p
        }
        return models
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedWith(compareByDescending<String> { priority(it) }.thenBy { it.lowercase(Locale.ROOT) })
    }
}
