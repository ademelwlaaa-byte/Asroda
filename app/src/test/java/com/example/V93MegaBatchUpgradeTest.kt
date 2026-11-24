package com.example

import com.example.util.ProviderErrorRecoveryAdvisor
import org.junit.Assert.assertTrue
import org.junit.Test

class V93MegaBatchUpgradeTest {

    @Test
    fun providerErrorRecoveryAdvisor_returnsActionableTurkishHints() {
        assertTrue(ProviderErrorRecoveryAdvisor.adviseForStatusCode(401).contains("API anahtarın"))
        assertTrue(ProviderErrorRecoveryAdvisor.adviseForStatusCode(402).contains("bakiyen"))
        assertTrue(ProviderErrorRecoveryAdvisor.adviseForStatusCode(404).contains("model kodu"))
        assertTrue(ProviderErrorRecoveryAdvisor.adviseForStatusCode(429).contains("Hız/kota"))
        assertTrue(ProviderErrorRecoveryAdvisor.adviseForStatusCode(503).contains("sunucusu"))
        assertTrue(ProviderErrorRecoveryAdvisor.adviseForStatusCode(null).contains("Bağlantı"))
    }
}
