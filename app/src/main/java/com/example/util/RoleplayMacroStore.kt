package com.example.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Stores reusable 1-tap Roleplay Quick Macros (custom OOC commands, favorite actions,
 * scene transitions) that insert cleanly into the chat input bar.
 */
object RoleplayMacroStore {

    private const val PREFS_NAME = "asroda_rp_macros_v1"
    private const val KEY_MACROS = "macros_json"
    private const val KEY_INITIALIZED = "macros_initialized"
    internal const val MAX_MACROS = 25
    private val TR_LOCALE = java.util.Locale.forLanguageTag("tr-TR")

    data class Macro(
        val id: String,
        val label: String,
        val text: String
    )

    val defaultMacros: List<Macro> = listOf(
        Macro("def_slow", "Yavaş & Duygusal", "[OOC: Sahneyi acele etmeden, duygusal gerilimi ve iç monologları derinleştirerek sürdür.]"),
        Macro("def_twist", "Sürpriz Dönemeç", "[OOC: Hikayeye tutarlı ama beklenmedik bir olay örgüsü dönüşü ekle.]"),
        Macro("def_dialogue", "Diyalog Odaklı", "[OOC: Uzun betimlemeler yerine keskin ve doğal karakter diyaloglarına odaklan.]")
    )

    fun loadMacros(context: Context): List<Macro> {
        return try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val initialized = prefs.getBoolean(KEY_INITIALIZED, false)
            val raw = prefs.getString(KEY_MACROS, null)
            if (raw.isNullOrBlank()) return if (initialized) emptyList() else defaultMacros
            val arr = JSONArray(raw)
            val list = mutableListOf<Macro>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id").trim()
                val label = obj.optString("label").trim()
                val text = obj.optString("text").trim()
                if (id.isNotBlank() && label.isNotBlank() && text.isNotBlank()) {
                    list.add(Macro(id, label, text))
                }
            }
            if (list.isEmpty() && !initialized) defaultMacros else list
        } catch (_: Exception) {
            defaultMacros
        }
    }

    fun addMacro(context: Context, label: String, text: String): List<Macro> {
        val cleanLabel = label.trim().take(40)
        val cleanText = text.trim().take(600)
        if (cleanLabel.isBlank() || cleanText.isBlank()) return loadMacros(context)
        val uniqueId = "m_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(8)}"
        val updated = (listOf(Macro(uniqueId, cleanLabel, cleanText)) + loadMacros(context))
            .distinctBy { it.label.lowercase(TR_LOCALE) }
            .take(MAX_MACROS)
        persist(context, updated)
        return updated
    }

    fun removeMacro(context: Context, macroId: String): List<Macro> {
        val updated = loadMacros(context).filterNot { it.id == macroId }
        persist(context, updated)
        return updated
    }

    private fun persist(context: Context, macros: List<Macro>) {
        try {
            val arr = JSONArray()
            macros.forEach { m ->
                arr.put(JSONObject().apply {
                    put("id", m.id)
                    put("label", m.label)
                    put("text", m.text)
                })
            }
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_INITIALIZED, true)
                .putString(KEY_MACROS, arr.toString())
                .apply()
        } catch (_: Exception) {}
    }
}
