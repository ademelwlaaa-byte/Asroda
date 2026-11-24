package com.example.util

import android.content.Context

/**
 * Tracks the last successful backup timestamp and advises the user when a fresh backup is due.
 */
object BackupRetentionAdvisor {

    private const val PREFS_NAME = "asroda_backup_advisor_v1"
    private const val KEY_LAST_BACKUP_MS = "last_backup_ms"
    private const val STALE_THRESHOLD_MS = 7L * 86_400_000L

    data class BackupStatus(
        val lastBackupMs: Long,
        val isReminderDue: Boolean,
        val label: String
    )

    fun recordBackup(context: Context, nowMs: Long = System.currentTimeMillis()) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_BACKUP_MS, nowMs)
            .apply()
    }

    fun getStatus(context: Context, nowMs: Long = System.currentTimeMillis()): BackupStatus {
        val lastMs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKUP_MS, 0L)
        if (lastMs <= 0L) {
            return BackupStatus(
                lastBackupMs = 0L,
                isReminderDue = true,
                label = "Henüz yedek alınmadı"
            )
        }
        val elapsed = (nowMs - lastMs).coerceAtLeast(0L)
        val rel = MessageTimestampFormatter.formatRelative(lastMs, nowMs)
        val due = elapsed >= STALE_THRESHOLD_MS
        val label = if (due) "Son yedek: $rel (Yeni yedek önerilir)" else "Son yedek: $rel"
        return BackupStatus(lastBackupMs = lastMs, isReminderDue = due, label = label)
    }
}
