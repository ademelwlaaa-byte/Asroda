package com.example.util

/**
 * Translates HTTP error status codes from AI providers into actionable Turkish user guidance.
 */
object ProviderErrorRecoveryAdvisor {

    fun adviseForStatusCode(statusCode: Int?): String {
        return when (statusCode) {
            401, 403 -> "API anahtarın geçersiz veya yetkisiz. Ayarlardan anahtarı yenileyip tekrar test et."
            402 -> "Sağlayıcı bakiyen tükenmiş görünüyor. Ücretsiz bir modele veya yedek sağlayıcıya geç."
            404 -> "Seçili model kodu bulunamadı. Özel API'den aktif model listesini yeniden çek."
            429 -> "Hız/kota sınırına ulaşıldı. Birkaç saniye bekle veya yedek sağlayıcıya geç."
            500, 502, 503, 504 -> "Sağlayıcı sunucusu geçici olarak yanıt vermiyor. Kısa süre sonra tekrar dene."
            else -> "Bağlantı kesildi. İnternet bağlantını ve Base URL adresini kontrol et."
        }
    }
}
