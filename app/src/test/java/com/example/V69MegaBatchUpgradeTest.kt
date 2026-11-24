package com.example

import com.example.util.CustomApiEndpointInfoHelper
import com.example.util.StoryTonePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class V69MegaBatchUpgradeTest {

    @Test
    fun storyTonePresets_providesFourCuratedTurkishDirectives() {
        assertEquals(4, StoryTonePresets.presets.size)
        val cinematic = StoryTonePresets.findById("cinematic")
        assertNotNull(cinematic)
        assertTrue(cinematic!!.directive.contains("Sinematik"))
    }

    @Test
    fun customApiEndpointInfoHelper_classifiesLocalHttpsAndHttpEndpoints() {
        val local = CustomApiEndpointInfoHelper.inspect("http://10.0.2.2:11434/v1")
        assertTrue(local.isLocalServer)
        assertFalse(local.isSecureHttps)
        assertTrue(local.badgeText.startsWith("Yerel Sunucu"))

        val cloud = CustomApiEndpointInfoHelper.inspect("https://openrouter.ai/api/v1")
        assertFalse(cloud.isLocalServer)
        assertTrue(cloud.isSecureHttps)
        assertEquals("openrouter.ai", cloud.host)
        assertTrue(cloud.badgeText.contains("🔒"))

        val insecure = CustomApiEndpointInfoHelper.inspect("http://my-proxy.example.com/v1")
        assertFalse(insecure.isLocalServer)
        assertFalse(insecure.isSecureHttps)
        assertTrue(insecure.badgeText.contains("⚠ HTTP"))
    }
}
