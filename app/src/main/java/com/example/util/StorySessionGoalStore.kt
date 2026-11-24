package com.example.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tracks the user's daily interactive story message goal (default 15 turns/day)
 * alongside [ReadingStreakTracker].
 */
object StorySessionGoalStore {

    private const val PREFS_NAME = "asroda_daily_story_goal_v1"
    private const val KEY_DAY = "goal_day"
    private const val KEY_COUNT = "goal_count"
    private const val KEY_TARGET = "goal_target"

    val GOAL_PRESETS = listOf(10, 15, 30, 50)
    const val DEFAULT_GOAL = 15

    data class DailyGoalProgress(
        val sentToday: Int,
        val dailyGoal: Int,
        val percentComplete: Int,
        val goalReached: Boolean
    )

    private fun todayKey(nowMs: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(nowMs))
    }

    fun getTodayProgress(context: Context, nowMs: Long = System.currentTimeMillis()): DailyGoalProgress {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = todayKey(nowMs)
        val savedDay = prefs.getString(KEY_DAY, "") ?: ""
        val count = if (savedDay == today) prefs.getInt(KEY_COUNT, 0).coerceAtLeast(0) else 0
        val goal = prefs.getInt(KEY_TARGET, DEFAULT_GOAL).coerceIn(5, 200)
        val pct = ((count * 100f) / goal).toInt().coerceIn(0, 100)
        return DailyGoalProgress(
            sentToday = count,
            dailyGoal = goal,
            percentComplete = pct,
            goalReached = count >= goal
        )
    }

    fun recordTurn(context: Context, nowMs: Long = System.currentTimeMillis()): DailyGoalProgress {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = todayKey(nowMs)
        val savedDay = prefs.getString(KEY_DAY, "") ?: ""
        val nextCount = if (savedDay == today) prefs.getInt(KEY_COUNT, 0) + 1 else 1
        prefs.edit()
            .putString(KEY_DAY, today)
            .putInt(KEY_COUNT, nextCount)
            .apply()
        return getTodayProgress(context, nowMs)
    }

    fun setDailyGoal(context: Context, target: Int, nowMs: Long = System.currentTimeMillis()): DailyGoalProgress {
        val safe = target.coerceIn(5, 200)
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_TARGET, safe)
            .apply()
        return getTodayProgress(context, nowMs)
    }

    fun nextGoalPreset(currentGoal: Int): Int {
        val idx = GOAL_PRESETS.indexOf(currentGoal)
        return if (idx < 0) GOAL_PRESETS.first() else GOAL_PRESETS[(idx + 1) % GOAL_PRESETS.size]
    }
}
