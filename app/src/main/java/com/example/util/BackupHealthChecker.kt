package com.example.util

import org.json.JSONObject

/**
 * Fast pre-flight inspector for Asroda JSON backup files so users can preview exact counts
 * (bots, messages, custom APIs, key inclusion) before confirming Merge or Overwrite restore.
 */
object BackupHealthChecker {

    data class Preview(
        val isValidJson: Boolean,
        val schemaVersion: Int,
        val botCount: Int,
        val messageCount: Int,
        val customProviderCount: Int,
        val includesPlaintextKeys: Boolean
    )

    fun inspectJson(rawJson: String): Preview {
        if (rawJson.isBlank()) {
            return Preview(false, 0, 0, 0, 0, false)
        }
        return try {
            val root = JSONObject(rawJson.trim())
            val version = root.optInt("version", root.optInt("schemaVersion", 1))
            val bots = root.optJSONArray("bots")?.length() ?: 0
            val msgs = root.optJSONArray("messages")?.length() ?: 0
            val customProviders = root.optJSONArray("customProviders")?.length() ?: 0
            val settings = root.optJSONObject("settings")
            val hasKeys = settings != null && listOf("geminiApiKey", "openaiApiKey", "claudeApiKey", "groqApiKey", "openRouterApiKey")
                .any { settings.optString(it).isNotBlank() }
            Preview(
                isValidJson = true,
                schemaVersion = version,
                botCount = bots,
                messageCount = msgs,
                customProviderCount = customProviders,
                includesPlaintextKeys = hasKeys
            )
        } catch (_: Exception) {
            Preview(false, 0, 0, 0, 0, false)
        }
    }
}
