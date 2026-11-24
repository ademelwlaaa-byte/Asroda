package com.example.util

import java.util.Calendar

/**
 * Returns atmospheric Turkish time-of-day greeting lines for home & reading headers.
 */
object StoryAmbientGreetingHelper {

    fun greetingForHour(hourOfDay: Int): String {
        val h = hourOfDay.coerceIn(0, 23)
        return when (h) {
            in 5..11 -> "Günaydın • Yeni bir bölüm seni bekliyor"
            in 12..16 -> "İyi günler • Hikayene kaldığın yerden devam et"
            in 17..21 -> "İyi akşamlar • Akşamın sessizliğinde yeni bir sahne"
            else -> "Gece yarısı • Yıldızların altında hikayeni sürdür"
        }
    }

    fun currentGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return greetingForHour(hour)
    }
}
