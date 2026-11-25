package com.example

import com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ElevenLabsStudioVoiceCatalogTest {

    @Test
    fun bundledElevenLabsStudioVoices_existAndMatchDeterministicHashes() {
        val assetDir = File("src/main/assets/${CosmicCharacterVoiceEngine.STUDIO_VOICES_ASSET_DIR}")
        assertTrue("studio_voices asset dir must exist", assetDir.exists() && assetDir.isDirectory)

        val mp3Files = assetDir.listFiles { f -> f.isFile && f.name.endsWith(".mp3") }.orEmpty()
        assertTrue("Expected at least 115 bundled ElevenLabs MP3 files, found ${mp3Files.size}", mp3Files.size >= 115)
        assertTrue("All bundled MP3 files must be non-empty (>1KB)", mp3Files.all { it.length() > 1024L })

        val sampleLines = listOf(
            "TWIN_AIDEN" to "Kapıyı aç... zaman kırılmadan önce sana onları nasıl hayatta tutacağını göstereyim.",
            "ARCHITECT" to "Birinci perde kapandı, Kayıp Anahtarım... Demir kuklayı kırdın, ama insanların korkusunu kıramazsın.",
            "NATASHA" to "Vaelor çatıya indi Aiden! Zemine dokunup ışınlan, mavi toplarda Emilim basıp geri fırlat!",
            "WANDA" to "Onu kızıl kaosla tuttum Aiden! Kırmızı Göz'ünü serbest bırak!",
            "ULTRON" to "Kuklalarım koparıldı ama bu şehir gökten düşecek Aiden Blackwood! İplerim artık yok!"
        )
        for ((speaker, text) in sampleLines) {
            val fileName = CosmicCharacterVoiceEngine.studioVoiceAssetFileName(speaker, text)
            val file = File(assetDir, fileName)
            assertTrue("Missing bundled ElevenLabs clip for $speaker ($fileName)", file.exists() && file.length() > 1024L)
        }
    }
}
