package com.example

import com.example.util.ProviderCooldownFormatter
import com.example.util.StoryBookmarkExporter
import com.example.util.StoryBookmarkStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V81MegaBatchUpgradeTest {

    @Test
    fun storyBookmarkExporter_formatsNumberedMarkdownWithSnippets() {
        val now = 1_700_000_000_000L
        val bookmarks = listOf(
            StoryBookmarkStore.Bookmark(
                id = "bm1",
                messageId = "m1",
                title = "Kulede İhanet",
                snippet = "Kılıcını çekip gölgelerin arasına daldı.",
                createdAtMs = now - 5 * 60_000L
            )
        )
        val md = StoryBookmarkExporter.formatMarkdown("Aiden", bookmarks, nowMs = now)
        assertTrue(md.contains("# Aiden — Kayıtlı Sahne Yer İmleri"))
        assertTrue(md.contains("1. **Kulede İhanet**"))
        assertTrue(md.contains("5 dk önce"))
        assertTrue(md.contains("Kılıcını çekip"))
    }

    @Test
    fun providerCooldownFormatter_formatsReadySecondsAndMinutes() {
        assertEquals("Hazır", ProviderCooldownFormatter.formatRemaining(0L))
        assertEquals("45 sn", ProviderCooldownFormatter.formatRemaining(45_000L))
        assertEquals("2 dk", ProviderCooldownFormatter.formatRemaining(120_000L))
        assertEquals("2 dk 15 sn", ProviderCooldownFormatter.formatRemaining(135_000L))
    }
}
