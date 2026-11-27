package com.example.util

import android.content.Context

/**
 * Tracks the last successful backup timestamp and advises the user when a fresh backup is due.
 */
object BackupRetentionAdvisor {

    private const val PREFS_NAME = "asroda_backup_advisor_v1"
    private const val KEY_LAST_BACKUP_MS = "last_backup_ms"
    private const val STALE_THRESHOLD_MS = 7L * 86_400_000L
    private val storeLock = Any()

    data class BackupStatus(
        val lastBackupMs: Long,
        val isReminderDue: Boolean,
        val label: String
    )

    private fun prefs(context: Context) =
        (context.applicationContext ?: context).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun recordBackup(context: Context, nowMs: Long = System.currentTimeMillis()) {
        if (nowMs <= 0L) return
        synchronized(storeLock) {
            try {
                prefs(context)
                    .edit()
                    .putLong(KEY_LAST_BACKUP_MS, nowMs)
                    .apply()
            } catch (_: Exception) {
            }
        }
    }

    fun clearRecord(context: Context) {
        synchronized(storeLock) {
            try {
                prefs(context)
                    .edit()
                    .remove(KEY_LAST_BACKUP_MS)
                    .apply()
            } catch (_: Exception) {
            }
        }
    }

    fun getStatus(context: Context, nowMs: Long = System.currentTimeMillis()): BackupStatus {
        val lastMs = try {
            prefs(context).getLong(KEY_LAST_BACKUP_MS, 0L)
        } catch (_: Exception) {
            0L
        }
        if (lastMs <= 0L) {
            return BackupStatus(
                lastBackupMs = 0L,
                isReminderDue = true,
                label = "Henüz yedek alınmadı"
            )
        }
        val safeNow = if (nowMs > 0L) nowMs else System.currentTimeMillis()
        val elapsed = (safeNow - lastMs).coerceAtLeast(0L)
        val rel = MessageTimestampFormatter.formatRelative(lastMs, safeNow)
        val due = elapsed >= STALE_THRESHOLD_MS
        val label = if (due) "Son yedek: $rel (Yeni yedek önerilir)" else "Son yedek: $rel"
        return BackupStatus(lastBackupMs = lastMs, isReminderDue = due, label = label)
    }
}
