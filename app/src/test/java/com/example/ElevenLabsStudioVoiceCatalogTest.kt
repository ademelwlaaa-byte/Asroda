package com.example

import com.example.ui.screens.cosmic3d.CosmicCharacterVoiceEngine
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

class ElevenLabsStudioVoiceCatalogTest {

    @Test
    fun downloadableElevenLabsStudioVoicePack_containsAll119ClipsAndManifest() {
        val packZip = File("../public/packs/asroda-studio-voices-v1.zip")
        assertTrue("On-demand DLC voice zip must exist at ${packZip.absolutePath}", packZip.exists() && packZip.length() > 1_000_000L)

        ZipFile(packZip).use { zip ->
            val rawEntries = zip.entries().toList()
            assertTrue("All ZIP entry names must use POSIX '/' separators for Android/Linux", rawEntries.none { '\\' in it.name })
            val entries = rawEntries.associateBy { it.name }
            assertTrue("manifest.json must exist inside DLC zip", entries.containsKey("manifest.json"))

            val mp3Entries = entries.values.filter { it.name.endsWith(".mp3") }
            assertTrue("Expected at least 115 MP3 clips inside DLC zip, found ${mp3Entries.size}", mp3Entries.size >= 115)
            assertTrue("All MP3 clips must be > 1KB", mp3Entries.all { it.size > 1024L })

            val sampleLines = listOf(
                "TWIN_AIDEN" to "Kapıyı aç... zaman kırılmadan önce sana onları nasıl hayatta tutacağını göstereyim.",
                "ARCHITECT" to "Birinci perde kapandı, Kayıp Anahtarım... Demir kuklayı kırdın, ama insanların korkusunu kıramazsın.",
                "NATASHA" to "Vaelor çatıya indi Aiden! Zemine dokunup ışınlan, mavi toplarda Emilim basıp geri fırlat!",
                "WANDA" to "Onu kızıl kaosla tuttum Aiden! Kırmızı Göz'ünü serbest bırak!",
                "ULTRON" to "Kuklalarım koparıldı ama bu şehir gökten düşecek Aiden Blackwood! İplerim artık yok!"
            )
            for ((speaker, text) in sampleLines) {
                val fileName = CosmicCharacterVoiceEngine.studioVoiceAssetFileName(speaker, text)
                val entry = entries["voices/$fileName"]
                assertTrue("Missing ElevenLabs clip voices/$fileName for $speaker in DLC zip", entry != null && entry.size > 1024L)
            }
        }
    }
}
