package com.example

import com.example.util.ChatInputQuickSyntaxHelper
import com.example.util.StoryAmbientGreetingHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V92MegaBatchUpgradeTest {

    @Test
    fun chatInputQuickSyntaxHelper_wrapsWithoutDuplicatingDelimiters() {
        assertEquals("*Kılıcını çeker*", ChatInputQuickSyntaxHelper.wrapAction("Kılıcını çeker"))
        assertEquals("*Kılıcını çeker*", ChatInputQuickSyntaxHelper.wrapAction("*Kılıcını çeker*"))
        assertEquals("\"Hazırım\"", ChatInputQuickSyntaxHelper.wrapDialogue("Hazırım"))
        assertEquals("\"Hazırım\"", ChatInputQuickSyntaxHelper.wrapDialogue("\"Hazırım\""))
        assertEquals("[OOC: Sahneyi yavaşlat]", ChatInputQuickSyntaxHelper.wrapOoc("Sahneyi yavaşlat"))
        assertEquals("[OOC: Sahneyi yavaşlat]", ChatInputQuickSyntaxHelper.wrapOoc("[OOC: Sahneyi yavaşlat]"))
    }

    @Test
    fun storyAmbientGreetingHelper_coversAllDayParts() {
        assertTrue(StoryAmbientGreetingHelper.greetingForHour(8).startsWith("Günaydın"))
        assertTrue(StoryAmbientGreetingHelper.greetingForHour(14).startsWith("İyi günler"))
        assertTrue(StoryAmbientGreetingHelper.greetingForHour(19).startsWith("İyi akşamlar"))
        assertTrue(StoryAmbientGreetingHelper.greetingForHour(2).startsWith("Gece yarısı"))
    }
}
