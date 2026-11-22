package com.example.data.managers

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.EntityRelationEntity
import com.example.data.local.MemoryCheckpointEntity
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MessageEntity
import com.example.data.local.MilestoneEntity
import com.example.data.local.RoutineEntity
import com.example.data.local.SensitiveTriggerEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.repository.BackupImportSummary
import com.example.data.repository.BackupSnapshot
import com.example.data.repository.mergePreservingExistingKeys
import com.example.data.repository.sanitizedForBackup
import com.example.util.SecureApiKeyVault
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Cohesive backup/restore manager extracted from [com.example.data.repository.EmochiRepository].
 * Handles atomic internal auto-backups, legacy external-backup cleanup, v1/v2 JSON round-trip,
 * API key stripping by default, and Keystore-encrypted persistence on restore.
 */
class BackupSnapshotManager(
    private val db: AppDatabase,
    private val context: Context?,
    private val backupAdapter: JsonAdapter<BackupSnapshot>,
    private val onAfterRestore: () -> Unit
) {
    companion object {
        private const val TAG = "BackupSnapshotManager"
        private const val BACKUP_FILE_NAME = "emochi_autobackup.json"
        private const val BACKUP_TMP_FILE_NAME = "emochi_autobackup.json.tmp"
    }

    suspend fun buildBackupSnapshot(includeApiKeys: Boolean): BackupSnapshot {
        val botDao = db.botDao()
        val messageDao = db.messageDao()
        val memoryFactDao = db.memoryFactDao()
        val memoryEventDao = db.memoryEventDao()
        val memoryCheckpointDao = db.memoryCheckpointDao()
        val castMemberDao = db.castMemberDao()
        val emotionDao = db.characterEmotionDao()
        val sensitiveTriggerDao = db.sensitiveTriggerDao()
        val routineDao = db.routineDao()
        val entityRelationDao = db.entityRelationDao()
        val milestoneDao = db.milestoneDao()

        val bots = botDao.getAllBotsList()
        val allMessages = mutableListOf<MessageEntity>()
        val allFacts = mutableListOf<MemoryFactEntity>()
        val allEvents = mutableListOf<MemoryEventEntity>()
        val allCheckpoints = mutableListOf<MemoryCheckpointEntity>()
        val allCast = mutableListOf<CastMemberEntity>()
        val allEmotions = mutableListOf<CharacterEmotionEntity>()
        val allTriggers = mutableListOf<SensitiveTriggerEntity>()
        val allRoutines = mutableListOf<RoutineEntity>()
        val allRelations = mutableListOf<EntityRelationEntity>()
        val allMilestones = mutableListOf<MilestoneEntity>()

        for (b in bots) {
            allMessages.addAll(messageDao.getMessagesForBotList(b.id))
            try { allFacts.addAll(memoryFactDao.getActiveFacts(b.id)) } catch (_: Exception) {}
            try { allEvents.addAll(memoryEventDao.getActiveEvents(b.id)) } catch (_: Exception) {}
            try { allCheckpoints.addAll(memoryCheckpointDao.getCheckpointsForBot(b.id)) } catch (_: Exception) {}
            try { allCast.addAll(castMemberDao.getCastMembersForBot(b.id)) } catch (_: Exception) {}
            try { allEmotions.addAll(emotionDao.getEmotionsForBot(b.id)) } catch (_: Exception) {}
            try { allTriggers.addAll(sensitiveTriggerDao.getTriggersForBot(b.id)) } catch (_: Exception) {}
            try { allRoutines.addAll(routineDao.getRoutinesForBot(b.id)) } catch (_: Exception) {}
            try { allRelations.addAll(entityRelationDao.getRelationsForBot(b.id)) } catch (_: Exception) {}
            try { allMilestones.addAll(milestoneDao.getMilestonesForBot(b.id)) } catch (_: Exception) {}
        }

        val rawSettings = db.userSettingsDao().getUserSettings() ?: UserSettingsEntity()
        val exportedSettings = if (includeApiKeys) {
            SecureApiKeyVault.decryptSettingsKeys(rawSettings)
        } else {
            rawSettings.sanitizedForBackup()
        }

        return BackupSnapshot(
            version = 2,
            exportedAt = System.currentTimeMillis(),
            bots = bots,
            messages = allMessages,
            settings = exportedSettings,
            memoryFacts = allFacts,
            memoryEvents = allEvents,
            memoryCheckpoints = allCheckpoints,
            castMembers = allCast,
            characterEmotions = allEmotions,
            sensitiveTriggers = allTriggers,
            routines = allRoutines,
            entityRelations = allRelations,
            milestones = allMilestones
        )
    }

    suspend fun doActualBackup() = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext
        try {
            // Auto-backup ALWAYS strips API keys and writes atomically to internal filesDir only
            val snapshot = buildBackupSnapshot(includeApiKeys = false)
            val json = backupAdapter.toJson(snapshot)

            val targetFile = File(ctx.filesDir, BACKUP_FILE_NAME)
            val tmpFile = File(ctx.filesDir, BACKUP_TMP_FILE_NAME)
            FileOutputStream(tmpFile).use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
                out.flush()
                try { out.fd.sync() } catch (_: Exception) {}
            }
            if (!tmpFile.renameTo(targetFile)) {
                tmpFile.copyTo(targetFile, overwrite = true)
                tmpFile.delete()
            }

            // Security cleanup: delete any legacy plaintext backup file in external storage
            try {
                ctx.getExternalFilesDir(null)?.let { extDir ->
                    val legacyExternal = File(extDir, BACKUP_FILE_NAME)
                    if (legacyExternal.exists()) {
                        legacyExternal.delete()
                    }
                }
            } catch (_: Exception) {}
        } catch (e: Exception) {
            Log.w(TAG, "Auto-backup write failed: ${e.message}")
        }
    }

    suspend fun autoRestoreFromStorage(): Boolean = withContext(Dispatchers.IO) {
        val ctx = context ?: return@withContext false
        try {
            val internalFile = File(ctx.filesDir, BACKUP_FILE_NAME)
            val legacyExternalFile = ctx.getExternalFilesDir(null)?.let { File(it, BACKUP_FILE_NAME) }
            val backupFile = when {
                internalFile.exists() && internalFile.length() > 0L -> internalFile
                legacyExternalFile != null && legacyExternalFile.exists() && legacyExternalFile.length() > 0L -> legacyExternalFile
                else -> null
            } ?: return@withContext false

            val json = backupFile.readText(Charsets.UTF_8)
            val summary = importDataSnapshot(jsonStr = json, overwriteExisting = false)
            if (backupFile == legacyExternalFile && summary.botsCount > 0) {
                doActualBackup()
            }
            summary.botsCount > 0
        } catch (e: Exception) {
            Log.w(TAG, "Auto-restore failed: ${e.message}")
            false
        }
    }

    suspend fun exportDataSnapshot(includeApiKeys: Boolean = false): String = withContext(Dispatchers.IO) {
        val snapshot = buildBackupSnapshot(includeApiKeys = includeApiKeys)
        backupAdapter.toJson(snapshot)
    }

    suspend fun importDataSnapshot(
        jsonStr: String,
        overwriteExisting: Boolean = false
    ): BackupImportSummary = withContext(Dispatchers.IO) {
        if (jsonStr.isBlank()) {
            throw IllegalArgumentException("Yedek verisi boş.")
        }
        val snapshot = try {
            backupAdapter.fromJson(jsonStr)
        } catch (e: Exception) {
            throw IllegalArgumentException("Geçersiz yedek JSON formatı: ${e.message}", e)
        } ?: throw IllegalArgumentException("Yedek dosyası çözümlenemedi.")

        val botDao = db.botDao()
        val messageDao = db.messageDao()
        val settingsDao = db.userSettingsDao()
        val fragmentDao = db.memoryFragmentDao()
        val memoryFactDao = db.memoryFactDao()
        val memoryEventDao = db.memoryEventDao()
        val memoryCheckpointDao = db.memoryCheckpointDao()
        val castMemberDao = db.castMemberDao()
        val emotionDao = db.characterEmotionDao()
        val sensitiveTriggerDao = db.sensitiveTriggerDao()
        val routineDao = db.routineDao()
        val entityRelationDao = db.entityRelationDao()
        val milestoneDao = db.milestoneDao()

        val existingSettings = try { settingsDao.getUserSettings() } catch (_: Exception) { null }
        // Encrypt any newly imported API keys from the snapshot while preserving existing device keys intact
        val encryptedIncoming = SecureApiKeyVault.encryptSettingsKeys(snapshot.settings)
        val mergedSettings = encryptedIncoming.mergePreservingExistingKeys(existingSettings)

        db.withTransaction {
            if (overwriteExisting) {
                val existingBots = botDao.getAllBotsList()
                for (existingBot in existingBots) {
                    val bid = existingBot.id
                    messageDao.deleteMessagesForBot(bid)
                    memoryFactDao.deleteAllFactsForBot(bid)
                    memoryEventDao.deleteAllEventsForBot(bid)
                    memoryCheckpointDao.deleteCheckpointsForBot(bid)
                    emotionDao.deleteEmotionsForBot(bid)
                    sensitiveTriggerDao.deleteTriggersForBot(bid)
                    routineDao.deleteRoutinesForBot(bid)
                    milestoneDao.deleteMilestonesForBot(bid)
                    castMemberDao.getCastMembersForBot(bid).forEach { castMemberDao.deleteCastMember(it.id) }
                    entityRelationDao.getRelationsForBot(bid).forEach { entityRelationDao.deleteRelation(it.id) }
                    fragmentDao.deleteFragmentsForBot(bid)
                }
                botDao.deleteAllBots()
            }

            for (b in snapshot.bots) botDao.insertOrUpdate(b)
            for (m in snapshot.messages) messageDao.insertMessage(m)
            settingsDao.insertOrUpdate(mergedSettings)

            if (snapshot.memoryFacts.isNotEmpty()) {
                val botsWithFacts = snapshot.memoryFacts.map { it.botId }.toSet()
                if (!overwriteExisting) botsWithFacts.forEach { memoryFactDao.deleteAllFactsForBot(it) }
                memoryFactDao.insertFacts(snapshot.memoryFacts.map { it.copy(id = 0L) })
            }
            if (snapshot.memoryEvents.isNotEmpty()) {
                val botsWithEvents = snapshot.memoryEvents.map { it.botId }.toSet()
                if (!overwriteExisting) botsWithEvents.forEach { memoryEventDao.deleteAllEventsForBot(it) }
                memoryEventDao.insertEvents(snapshot.memoryEvents.map { it.copy(id = 0L) })
            }
            if (snapshot.memoryCheckpoints.isNotEmpty()) {
                val botsWithCheckpoints = snapshot.memoryCheckpoints.map { it.botId }.toSet()
                if (!overwriteExisting) botsWithCheckpoints.forEach { memoryCheckpointDao.deleteCheckpointsForBot(it) }
                for (cp in snapshot.memoryCheckpoints) memoryCheckpointDao.insertCheckpoint(cp.copy(id = 0L))
            }
            if (snapshot.castMembers.isNotEmpty()) {
                val botsWithCast = snapshot.castMembers.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithCast.forEach { bid ->
                        castMemberDao.getCastMembersForBot(bid).forEach { castMemberDao.deleteCastMember(it.id) }
                    }
                }
                for (cm in snapshot.castMembers) castMemberDao.insertCastMember(cm.copy(id = 0L))
            }
            if (snapshot.characterEmotions.isNotEmpty()) {
                val botsWithEmotions = snapshot.characterEmotions.map { it.botId }.toSet()
                if (!overwriteExisting) botsWithEmotions.forEach { emotionDao.deleteEmotionsForBot(it) }
                for (em in snapshot.characterEmotions) emotionDao.insertOrUpdate(em)
            }
            if (snapshot.sensitiveTriggers.isNotEmpty()) {
                val botsWithTriggers = snapshot.sensitiveTriggers.map { it.botId }.toSet()
                if (!overwriteExisting) botsWithTriggers.forEach { sensitiveTriggerDao.deleteTriggersForBot(it) }
                for (tr in snapshot.sensitiveTriggers) sensitiveTriggerDao.insertOrUpdate(tr.copy(id = 0L))
            }
            if (snapshot.routines.isNotEmpty()) {
                val botsWithRoutines = snapshot.routines.map { it.botId }.toSet()
                if (!overwriteExisting) botsWithRoutines.forEach { routineDao.deleteRoutinesForBot(it) }
                routineDao.insertRoutines(snapshot.routines.map { it.copy(id = 0L) })
            }
            if (snapshot.entityRelations.isNotEmpty()) {
                val botsWithRelations = snapshot.entityRelations.map { it.botId }.toSet()
                if (!overwriteExisting) {
                    botsWithRelations.forEach { bid ->
                        entityRelationDao.getRelationsForBot(bid).forEach { entityRelationDao.deleteRelation(it.id) }
                    }
                }
                entityRelationDao.insertRelations(snapshot.entityRelations.map { it.copy(id = 0L) })
            }
            if (snapshot.milestones.isNotEmpty()) {
                for (ms in snapshot.milestones) milestoneDao.insertMilestone(ms)
            }
        }

        onAfterRestore()

        BackupImportSummary(
            version = snapshot.version,
            botsCount = snapshot.bots.size,
            messagesCount = snapshot.messages.size,
            memoryFactsCount = snapshot.memoryFacts.size,
            memoryEventsCount = snapshot.memoryEvents.size,
            castMembersCount = snapshot.castMembers.size,
            milestonesCount = snapshot.milestones.size,
            overwriteExisting = overwriteExisting
        )
    }
}
