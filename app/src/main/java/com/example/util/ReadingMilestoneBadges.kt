package com.example.util

/**
 * Awards Turkish reader milestone badges based on reading streak & daily story goal activity.
 */
object ReadingMilestoneBadges {

    data class MilestoneBadge(
        val title: String,
        val emoji: String,
        val description: String
    )

    fun resolveBadge(
        streak: ReadingStreakTracker.StreakState,
        dailyProgress: StorySessionGoalStore.DailyGoalProgress? = null
    ): MilestoneBadge {
        return when {
            streak.longestStreakDays >= 30 || streak.totalActiveDays >= 60 ->
                MilestoneBadge("Efsanevi Yazar", "👑", "30+ gün seri veya 60+ aktif gün")
            streak.longestStreakDays >= 14 || streak.totalActiveDays >= 30 ->
                MilestoneBadge("Usta Hikayeci", "📚", "14+ gün seri veya 30+ aktif gün")
            streak.currentStreakDays >= 7 ->
                MilestoneBadge("Haftalık Seri", "🔥", "7 gün kesintisiz hikaye yolculuğu")
            dailyProgress?.goalReached == true ->
                MilestoneBadge("Günlük Hedef Şampiyonu", "✦", "Bugünkü mesaj hedefine ulaştın")
            streak.totalActiveDays >= 3 ->
                MilestoneBadge("Sadık Okur", "✨", "Düzenli hikaye serisi başladı")
            else ->
                MilestoneBadge("İlk Kıvılcım", "🌱", "Asroda maceran yeni başlıyor")
        }
    }

    fun formatCompact(badge: MilestoneBadge): String = "${badge.emoji} ${badge.title}"
}
