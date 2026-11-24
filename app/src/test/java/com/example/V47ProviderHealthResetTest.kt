package com.example

import com.example.data.managers.ProviderFailureType
import com.example.data.managers.ProviderHealthTracker
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V47ProviderHealthResetTest {

    @After
    fun tearDown() {
        ProviderHealthTracker.resetAll()
    }

    @Test
    fun resetHealthAndResetAll_clearsActiveProviderCooldownsImmediately() {
        ProviderHealthTracker.resetAll()
        ProviderHealthTracker.recordFailure("custom_42", ProviderFailureType.RATE_LIMIT, "HTTP 429 Too Many Requests")
        val afterFail = ProviderHealthTracker.healthSnapshot.value["custom_42"]
        assertTrue(
            "429 failure should place provider in cooldown",
            afterFail != null && afterFail.cooldownUntilMs > System.currentTimeMillis()
        )

        ProviderHealthTracker.resetHealth("custom_42")
        val afterSingleReset = ProviderHealthTracker.healthSnapshot.value["custom_42"]
        assertFalse(
            "Single-provider reset should immediately clear cooldown",
            afterSingleReset != null && afterSingleReset.cooldownUntilMs > System.currentTimeMillis()
        )

        ProviderHealthTracker.recordFailure("groq", ProviderFailureType.RATE_LIMIT, "HTTP 429 rate limit")
        ProviderHealthTracker.resetAll()
        assertTrue(
            "resetAll should clear all tracked provider states",
            ProviderHealthTracker.healthSnapshot.value.isEmpty()
        )
    }
}
