package com.example

import com.example.data.managers.ProviderFailureType
import com.example.data.managers.ProviderHealthState
import com.example.data.repository.ProviderFallbackLogEntry
import com.example.util.ApiDiagnosticsFormatter
import com.example.util.NarrativeTextFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V56MegaBatchUpgradeTest {

    @Test
    fun narrativeTextFormatter_splitsLongWallOfTextIntoReadableParagraphs() {
        val wall = "Yağmur Manhattan'ın karanlık çatılarında şiddetle yağıyordu ve rüzgar pelerinini savuruyordu. " +
            "\"Burada ne işin var Aiden?\" dedi Natasha gözlerini kısmadan. " +
            "*Aiden sessizce bir adım öne çıktı ve elindeki parlayan küreyi havaya kaldırdı.* " +
            "\"Zaman dokusu kırılmak üzere, bunu tek başıma durduramam.\" diye fısıldadı karanlığa doğru. " +
            "Uzakta gök gürledi ve kuledeki tüm acil durum ışıkları kırmızıya döndü."
        val formatted = NarrativeTextFormatter.formatWallOfText(wall)
        assertTrue("Long single-block text should be split into multiple paragraphs", formatted.contains("\n\n"))

        val alreadyFormatted = "İlk paragraf.\n\nİkinci paragraf."
        assertEquals(alreadyFormatted, NarrativeTextFormatter.formatWallOfText(alreadyFormatted))
    }

    @Test
    fun apiDiagnosticsFormatter_generatesSanitizedReadableReport() {
        val health = mapOf(
            "groq" to ProviderHealthState(
                providerKey = "groq",
                totalSuccessCount = 5,
                totalFailureCount = 1,
                lastLatencyMs = 420L,
                lastFailureType = ProviderFailureType.RATE_LIMIT
            )
        )
        val logs = listOf(
            ProviderFallbackLogEntry(
                providerName = "groq",
                status = "SUCCESS",
                statusCode = 200,
                layer = 1
            )
        )
        val report = ApiDiagnosticsFormatter.formatReport("5.6", health, logs, nowMs = 1_000L)
        assertTrue(report.contains("Asroda v5.6"))
        assertTrue(report.contains("groq: HAZIR (420ms)"))
        assertTrue(report.contains("SUCCESS (HTTP 200)"))
    }
}
