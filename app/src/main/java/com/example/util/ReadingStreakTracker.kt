package com.example.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Privacy-first local reading & roleplay streak tracker.
 * Records unique calendar days the user interacts with Asroda and computes current/longest streaks.
 */
object ReadingStreakTracker {

    private const val PREFS_NAME = "asroda_reading_streak_v1"
    private const val KEY_LAST_DAY = "last_active_day"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_LONGEST_STREAK = "longest_streak"
    private const val KEY_TOTAL_DAYS = "total_active_days"

    data class StreakState(
        val currentStreakDays: Int,
        val longestStreakDays: Int,
        val totalActiveDays: Int,
        val lastActiveDayKey: String
    )

    fun dayKeyForEpochMs(epochMs: Long): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        return fmt.format(Date(epochMs))
    }

    fun recordActivity(context: Context, nowMs: Long = System.currentTimeMillis()): StreakState {
        val today = dayKeyForEpochMs(nowMs)
        val yesterday = dayKeyForEpochMs(nowMs - 86_400_000L)
        return try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastDay = prefs.getString(KEY_LAST_DAY, "").orEmpty()
            var current = prefs.getInt(KEY_CURRENT_STREAK, 0)
            var longest = prefs.getInt(KEY_LONGEST_STREAK, 0)
            var total = prefs.getInt(KEY_TOTAL_DAYS, 0)

            if (lastDay != today) {
                current = if (lastDay == yesterday) current + 1 else 1
                total += 1
                if (current > longest) longest = current
                prefs.edit()
                    .putString(KEY_LAST_DAY, today)
                    .putInt(KEY_CURRENT_STREAK, current)
                    .putInt(KEY_LONGEST_STREAK, longest)
                    .putInt(KEY_TOTAL_DAYS, total)
                    .apply()
            }
            StreakState(current, longest, total, today)
        } catch (_: Exception) {
            StreakState(1, 1, 1, today)
        }
    }

    fun getState(context: Context): StreakState {
        return try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            StreakState(
                currentStreakDays = prefs.getInt(KEY_CURRENT_STREAK, 0),
                longestStreakDays = prefs.getInt(KEY_LONGEST_STREAK, 0),
                totalActiveDays = prefs.getInt(KEY_TOTAL_DAYS, 0),
                lastActiveDayKey = prefs.getString(KEY_LAST_DAY, "").orEmpty()
            )
        } catch (_: Exception) {
            StreakState(0, 0, 0, "")
        }
    }
}
