package com.example.book

/**
 * Classifies Book Mode numeric character bond scores into evocative Turkish relationship tiers.
 */
object BookBondTierClassifier {

    fun classify(bondScore: Int): String {
        return when {
            bondScore >= 75 -> "💫 Sarsılmaz Bağ"
            bondScore >= 45 -> "✦ Güvenilir Müttefik"
            bondScore >= 15 -> "🤝 Yakın Dost"
            bondScore > -15 -> "• Nötr"
            bondScore > -45 -> "⚡ Gergin"
            else -> "🔥 Düşmanca"
        }
    }
}
