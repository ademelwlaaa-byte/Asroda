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
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.EmochiApplication
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.MessageEntity
import com.example.data.repository.EmochiRepository
import kotlinx.coroutines.CancellationException
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

class ProactiveMessageWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "ProactiveMsgWorker"
        const val WORK_NAME = "emochi_proactive_message"
        const val CHANNEL_ID = "emochi_proactive"
        const val CHANNEL_NAME = "Karakter Mesajları"
        const val IDLE_THRESHOLD_MS = 2 * 60 * 60 * 1000L // 2 saat
        const val UNANSWERED_FOLLOWUP_COOLDOWN_MS = 12 * 60 * 60 * 1000L // 12 saat
        const val MAX_CONSECUTIVE_ASSISTANT_MESSAGES = 2

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<ProactiveMessageWorker>(45, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            try {
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            } catch (_: IllegalStateException) {}
        }

        /** Quiet hours: 23:00–08:00 local time — never disturb the user overnight. */
        fun isQuietHours(hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): Boolean {
            return hourOfDay >= 23 || hourOfDay < 8
        }

        /**
         * Pure eligibility gate (easily unit-testable) preventing infinite 45-min spam loops:
         * - Skips quiet hours (23:00–08:00)
         * - Skips disabled bots (`proactiveEnabled == false`) and active open chat (`activeBotId`)
         * - Requires at least 1 message and >= IDLE_THRESHOLD_MS since latest message
         * - Blocks consecutive spam: max 1 unanswered assistant message within 12h, never > 2 consecutive assistant messages
         */
        fun shouldSendProactiveMessage(
            botId: String,
            proactiveEnabled: Boolean,
            activeBotId: String?,
            botLastMessageTimestamp: Long,
            recentMessages: List<MessageEntity>,
            nowMs: Long,
            currentHourOfDay: Int = 14
        ): Boolean {
            if (isQuietHours(currentHourOfDay)) return false
            if (!proactiveEnabled) return false
            if (botId == activeBotId) return false
            if (recentMessages.isEmpty()) return false

            val lastMsg = recentMessages.last()
            val effectiveLastTs = maxOf(botLastMessageTimestamp, lastMsg.timestamp)
            val idleTime = nowMs - effectiveLastTs
            if (idleTime < IDLE_THRESHOLD_MS) return false

            // Count trailing assistant messages to avoid spamming an absent user
            var trailingAssistant = 0
            for (i in recentMessages.lastIndex downTo 0) {
                if (recentMessages[i].role == "assistant") trailingAssistant++ else break
            }
            if (trailingAssistant >= MAX_CONSECUTIVE_ASSISTANT_MESSAGES) return false
            if (trailingAssistant >= 1 && idleTime < UNANSWERED_FOLLOWUP_COOLDOWN_MS) return false

            return true
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Karakterlerinden gelen mesajlar"
                    enableVibration(true)
                }
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(channel)
            }
        }
    }

    override suspend fun doWork(): Result {
        // Sistem #12: Gece rahatsız etmeme koruması (23:00–08:00) ve Android 13+ bildirim izni kontrolü
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (isQuietHours(currentHour)) return Result.success()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val repository = EmochiApplication.getSharedRepository(applicationContext)

            val allBots = db.botDao().getAllBotsList()
            val now = System.currentTimeMillis()

            for (bot in allBots) {
                if (!bot.proactiveEnabled ||
                    bot.id == EmochiRepository.activeBotId ||
                    bot.id.startsWith("book_") ||
                    bot.mode == "book"
                ) continue

                // Load only the latest 5 messages (never entire chat history)
                val recentMessages = db.messageDao().getRecentMessagesForBot(bot.id, limit = 5)
                if (!shouldSendProactiveMessage(
                        botId = bot.id,
                        proactiveEnabled = bot.proactiveEnabled,
                        activeBotId = EmochiRepository.activeBotId,
                        botLastMessageTimestamp = bot.lastMessageTimestamp,
                        recentMessages = recentMessages,
                        nowMs = now,
                        currentHourOfDay = currentHour
                    )
                ) continue

                val idleTime = now - maxOf(bot.lastMessageTimestamp, recentMessages.last().timestamp)
                val proactivePrompt = buildProactivePrompt(bot.aiName, bot.aiPersonality, idleTime)

                try {
                    val replyResult = repository.generateAiReply(
                        bot.copy(aiPersonality = proactivePrompt + "\n\n" + bot.aiPersonality),
                        recentMessages
                    )
                    val replyText = replyResult.replyText.trim()
                    if (replyText.isBlank() ||
                        com.example.data.repository.TurkishLanguageGuard.isEnglishDominant(replyText)
                    ) continue

                    val msgTs = System.currentTimeMillis()
                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        botId = bot.id,
                        role = "assistant",
                        text = replyText,
                        timestamp = msgTs,
                        provider = replyResult.usedProvider
                    )
                    // saveMessage also advances bot.lastMessageTimestamp atomically
                    repository.saveMessage(aiMsg)

                    sendNotification(
                        botId = bot.id,
                        botName = bot.aiName,
                        messagePreview = replyText.take(80)
                    )
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    Log.w(TAG, "Proactive reply skipped for bot=${bot.id}: ${e.message}")
                }
            }

            Result.success()
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "Proactive worker failed (attempt=$runAttemptCount): ${e.message}")
            if (runAttemptCount < 2) Result.retry() else Result.success()
        }
    }

    private fun buildProactivePrompt(name: String, personality: String, idleMs: Long): String {
        val hours = idleMs / (60 * 60 * 1000)
        return """
[PROAKTIF MESAJ - GİZLİ TALİMAT]
Kullanıcı yaklaşık $hours saattir sessiz. Sen $name karakterisin.
Karakterine uygun, kısa, samimi ve %100 DOĞAL TÜRKÇE bir mesaj yaz (asla İngilizce cümle yazma).
Sanki kullanıcıya sohbeti ilk sen başlatıyorsun. Yapay veya zorlama hissettirme.
Maksimum 2 cümle.
        """.trimIndent()
    }

    private fun sendNotification(botId: String, botName: String, messagePreview: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        createNotificationChannel(applicationContext)

        val openIntent = Intent(applicationContext, com.example.MainActivity::class.java).apply {
            action = "com.example.OPEN_CHAT"
            putExtra("botId", botId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            botId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_emochi)
            .setContentTitle(botName)
            .setContentText(messagePreview)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messagePreview))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(botId.hashCode(), notification)
    }
}
