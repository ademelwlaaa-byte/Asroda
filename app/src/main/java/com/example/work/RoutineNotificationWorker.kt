package com.example.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.EmochiRepository
import com.example.util.AppFeatureFlags
import kotlinx.coroutines.CancellationException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class RoutineNotificationWorker(
    private val ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    companion object {
        private const val TAG = "RoutineNotifWorker"
        private const val CHANNEL_ID = "routine_reminders"
        const val WORK_TAG = "routine_notification_daily"
        private const val PREFS_NAME = "emochi_routine_notif_prefs"
        private const val NOTIF_ID_BASE = 9100
        private val TR_LOCALE = Locale("tr", "TR")

        fun isQuietHours(hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): Boolean {
            return hourOfDay >= 23 || hourOfDay < 8
        }

        fun shouldNotifyRoutine(
            botId: String,
            activeBotId: String?,
            isRoutineNotifEnabled: Boolean,
            proactiveEnabled: Boolean = true,
            currentHourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        ): Boolean {
            if (!isRoutineNotifEnabled) return false
            if (!proactiveEnabled) return false
            if (isQuietHours(currentHourOfDay)) return false
            if (botId == activeBotId) return false
            return true
        }

        fun schedule(context: Context) {
            if (!AppFeatureFlags.isRoutineNotifEnabled) return
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            val req = PeriodicWorkRequestBuilder<RoutineNotificationWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(WORK_TAG)
                .build()
            try {
                WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(
                        WORK_TAG,
                        ExistingPeriodicWorkPolicy.KEEP,
                        req
                    )
            } catch (_: IllegalStateException) {
                // WorkManager may be uninitialized in isolated JVM/Robolectric unit tests
            }
        }

        fun cancel(context: Context) {
            try {
                val wm = WorkManager.getInstance(context)
                wm.cancelUniqueWork(WORK_TAG)
                wm.cancelAllWorkByTag(WORK_TAG)
            } catch (_: IllegalStateException) {
            }
        }

        /**
         * API 24+ safe (no java.time) day-of-week matcher supporting BOTH Turkish ("Pazartesi".."Pazar",
         * as stored by WorldStateManager) AND English ("monday".."sunday") values.
         */
        fun matchesDayOfWeek(routineDayOfWeek: String?, calendarDayOfWeek: Int): Boolean {
            if (routineDayOfWeek.isNullOrBlank()) return true
            val norm = routineDayOfWeek.trim().lowercase(TR_LOCALE)
            val expected = when (calendarDayOfWeek) {
                Calendar.MONDAY -> setOf("pazartesi", "monday")
                Calendar.TUESDAY -> setOf("salı", "sali", "tuesday")
                Calendar.WEDNESDAY -> setOf("çarşamba", "carsamba", "wednesday")
                Calendar.THURSDAY -> setOf("perşembe", "persembe", "thursday")
                Calendar.FRIDAY -> setOf("cuma", "friday")
                Calendar.SATURDAY -> setOf("cumartesi", "saturday")
                Calendar.SUNDAY -> setOf("pazar", "sunday")
                else -> emptySet()
            }
            return norm in expected
        }
    }

    override suspend fun doWork(): Result {
        if (!AppFeatureFlags.isRoutineNotifEnabled) return Result.success()
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        if (isQuietHours(currentHour)) return Result.success()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        return try {
            val db = AppDatabase.getInstance(ctx)
            val bots = db.botDao().getAllBotsList()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
            val todayDow = cal.get(Calendar.DAY_OF_WEEK)

            // Real-world daily dedup kept strictly separate from in-story routine.lastMentionedStoryDate!
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            ensureChannel(nm)

            var notifId = NOTIF_ID_BASE
            for (bot in bots) {
                if (!shouldNotifyRoutine(
                        botId = bot.id,
                        activeBotId = EmochiRepository.activeBotId,
                        isRoutineNotifEnabled = AppFeatureFlags.isRoutineNotifEnabled,
                        proactiveEnabled = bot.proactiveEnabled,
                        currentHourOfDay = currentHour
                    )
                ) continue

                val routines = try {
                    db.routineDao().getRoutinesForBot(bot.id)
                } catch (_: Exception) { continue }

                for (routine in routines) {
                    val prefKey = "notified_${routine.id}"
                    if (prefs.getString(prefKey, null) == today) continue
                    if (!matchesDayOfWeek(routine.dayOfWeek, todayDow)) continue

                    val openIntent = Intent(ctx, com.example.MainActivity::class.java).apply {
                        action = "com.example.OPEN_CHAT"
                        putExtra("botId", bot.id)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val pi = PendingIntent.getActivity(
                        ctx,
                        notifId,
                        openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_emochi)
                        .setContentTitle("${bot.aiName} seni bekliyor")
                        .setContentText(routine.routineDescription.take(80))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build()

                    nm.notify(notifId++, notif)
                    prefs.edit().putString(prefKey, today).apply()
                }
            }
            Result.success()
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "Routine notification failed (attempt=$runAttemptCount): ${e.message}")
            if (runAttemptCount < 2) Result.retry() else Result.success()
        }
    }

    private fun ensureChannel(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rutin Hatırlatıcılar",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Günlük rutin etkinlikleri hatırlatır." }
            nm.createNotificationChannel(channel)
        }
    }
}
