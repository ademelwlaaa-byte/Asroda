package com.example.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.EmochiApplication
import com.example.data.local.AppDatabase
import com.example.data.local.ArchivedMemoryEntity
import com.example.data.local.MemoryEventEntity
import com.example.data.repository.EmochiRepository
import com.example.data.repository.LivingMemoryCodec
import com.example.data.repository.MemoryEngine
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

/**
 * Arka planda benzer/yinelenen hafıza kayıtlarını gruplar ve LLM ile birleştirir.
 * - Tetiklenme: 8 saatte bir, pil/ağ uygunsa (EmochiRepository sayacıyla 20 yeni kayıt eşiği).
 * - Benzerlik eşiği: 0.88 — bu eşiğin altındaki çiftler ayrı tutulur.
 * - Birleşme & TTL Arşivleme: isPinned == true ve category == "CORE" kayıtlarına ASLA dokunulmaz.
 * - Her değişiklik sonunda repository.invalidateMemoryCaches(botId) çağrılır.
 * - Ayrıca periyodik bakım olarak 7 teşhis log tablosunu sınırlar (pruneDiagnosticLogs).
 */
class BackgroundMemoryMergeWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "MemoryMergeWorker"
        const val WORK_NAME = "emochi_background_memory_merge"
        const val MERGE_SIMILARITY_THRESHOLD = 0.88f
        const val MAX_MERGES_PER_RUN = 10
        const val MAX_GROUP_SIZE = 5
        const val MAX_SCAN_EVENTS = 120

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<BackgroundMemoryMergeWorker>(
                8, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            try {
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            } catch (_: IllegalStateException) {}
        }

        /**
         * Returns true if this event must NEVER be merged or TTL-archived (pinned, structured LivingMemory, or CORE).
         * Note: `MemoryEventEntity.isBotSaved` defaults to `true` for normal AI-logged events, so it must NOT block merging.
         */
        fun isProtectedEvent(ev: MemoryEventEntity): Boolean {
            val parsed = LivingMemoryCodec.parse(ev.description, ev.importanceScore)
            if (parsed.isStructured) return true
            if (ev.importanceScore >= 90) return true
            val descUpper = ev.description.uppercase(java.util.Locale.ROOT)
            return descUpper.contains("[CORE]") ||
                descUpper.contains("CATEGORY:CORE") ||
                descUpper.contains("PINNED")
        }

        /**
         * Testable single-bot memory maintenance entry point (TTL archival + similarity merge + cache invalidation).
         * Strictly protects `isPinned == true` and `category == "CORE"` facts & events.
         * Returns true if any merge or archival occurred and memory caches were invalidated.
         */
        suspend fun runMaintenanceForBot(
            botId: String,
            repository: EmochiRepository,
            db: AppDatabase,
            customMerger: (suspend (List<MemoryEventEntity>) -> String?)? = null
        ): Boolean {
            var mutated = false

            // 1. Natural Decay TTL archival (protects CORE & isPinned facts/events)
            val bot = db.botDao().getBotById(botId)
            if (bot != null) {
                val msgCount = try { db.messageDao().getMessageCountForBot(botId) } catch (_: Exception) { 0 }
                val archivedBefore = try { db.archivedMemoryDao().getArchivesForBot(botId).size } catch (_: Exception) { 0 }
                try {
                    repository.reapExpiredMemories(botId, bot.storyDayCounter, msgCount)
                } catch (ce: CancellationException) {
                    throw ce
                } catch (_: Exception) {}
                val archivedAfter = try { db.archivedMemoryDao().getArchivesForBot(botId).size } catch (_: Exception) { archivedBefore }
                if (archivedAfter > archivedBefore) {
                    mutated = true
                }
            }

            // 2. Similarity-based merge for plain legacy events (skipping CORE / pinned / structured)
            val activeEvents = db.memoryEventDao().getActiveEvents(botId)
            val candidates = activeEvents
                .asSequence()
                .filter { ev ->
                    !isProtectedEvent(ev) &&
                        !LivingMemoryCodec.parse(ev.description, ev.importanceScore).isStructured
                }
                .take(MAX_SCAN_EVENTS)
                .map { ev ->
                    val vec = repository.jsonToFloatArray(ev.embedding).takeIf { it.isNotEmpty() }
                        ?: MemoryEngine.computeTurkishFeatureEmbedding(ev.description)
                    Pair(ev, vec)
                }
                .toList()

            if (candidates.size >= 2) {
                val settings = if (customMerger == null) {
                    try { repository.getOrCreateSettings() } catch (_: Exception) { null }
                } else null

                var mergeCount = 0
                val merged = mutableSetOf<Long>()

                for (i in candidates.indices) {
                    if (mergeCount >= MAX_MERGES_PER_RUN) break
                    val (ev1, vec1) = candidates[i]
                    if (merged.contains(ev1.id)) continue

                    val similarGroup = mutableListOf<MemoryEventEntity>()
                    for (j in (i + 1) until candidates.size) {
                        if (similarGroup.size >= MAX_GROUP_SIZE - 1) break
                        val (ev2, vec2) = candidates[j]
                        if (merged.contains(ev2.id)) continue
                        if (vec2.size != vec1.size) continue
                        val sim = MemoryEngine.cosineSimilarity(vec1, vec2)
                        if (sim >= MERGE_SIMILARITY_THRESHOLD) {
                            similarGroup.add(ev2)
                        }
                    }

                    if (similarGroup.isEmpty()) continue

                    val group = listOf(ev1) + similarGroup
                    val mergedContent = if (customMerger != null) {
                        customMerger(group)
                    } else if (settings != null) {
                        askLlmToMergeStatic(group, repository, settings)
                    } else null
                    if (mergedContent.isNullOrBlank()) continue

                    val maxImportance = group.maxOf { it.importanceScore }
                    val sourceIds = group.map { it.id }.joinToString(",")
                    val vec = MemoryEngine.computeTurkishFeatureEmbedding(mergedContent)
                    db.memoryEventDao().insertEvent(
                        MemoryEventEntity(
                            botId = botId,
                            description = mergedContent,
                            importanceScore = maxImportance,
                            embedding = repository.floatArrayToJson(vec),
                            isBotSaved = false,
                            storyDayIndex = group.minOf { it.storyDayIndex }
                        )
                    )

                    for (old in group) {
                        db.archivedMemoryDao().insertArchive(
                            ArchivedMemoryEntity(
                                botId = botId,
                                originalMemoryId = old.id,
                                memoryType = "event",
                                content = old.description,
                                archivedReason = "merge:[$sourceIds]"
                            )
                        )
                        db.memoryEventDao().updateEvent(old.copy(supersededBy = -1L))
                    }

                    group.forEach { merged.add(it.id) }
                    mutated = true
                    mergeCount++
                }
            }

            if (mutated) {
                repository.invalidateMemoryCaches(botId)
            }
            return mutated
        }

        private suspend fun askLlmToMergeStatic(
            group: List<MemoryEventEntity>,
            repository: EmochiRepository,
            settings: com.example.data.local.UserSettingsEntity
        ): String? {
            return try {
                val numbered = group.mapIndexed { i, ev -> "${i + 1}. ${ev.description}" }.joinToString("\n")
                val userMessage = "Aşağıdaki hafıza kayıtları aynı konuyu farklı biçimlerde anlatıyor olabilir.\n" +
                    "Eğer bunlar aynı bilgiyi veya olayı anlatıyorsa, tek bir kısa ve olgusal cümle olarak birleştir.\n" +
                    "Eğer farklı olaylarsa sadece 'FARKLI' yaz.\n\n$numbered"

                val text = repository.callUtilityLlm(
                    settings = settings,
                    systemPrompt = "Sen kısa hafıza birleştirici bir asistansın. Yalnızca birleşik cümle veya FARKLI yaz.",
                    userMessage = userMessage,
                    botId = group.first().botId,
                    maxTokens = 180,
                    timeoutMs = 5000L
                ).trim()

                if (text.equals("FARKLI", ignoreCase = true) || text.isBlank()) null
                else text.lines().first().trim().take(300)
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Exception) { null }
        }
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = EmochiApplication.getSharedRepository(applicationContext)

        val bots = try {
            db.botDao().getAllBotsList()
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load bots for memory merge: ${e.message}")
            return if (runAttemptCount < 2) Result.retry() else Result.success()
        }

        for (bot in bots) {
            if (bot.id == EmochiRepository.activeBotId) continue
            try {
                runMaintenanceForBot(bot.id, repository, db)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Log.w(TAG, "Memory merge skipped for bot=${bot.id}: ${e.message}")
            }
        }

        // Periodic SQLite housekeeping: cap unbounded diagnostic log tables
        AppDatabase.pruneDiagnosticLogs(db, maxRowsPerTable = 500)

        return Result.success()
    }
}
