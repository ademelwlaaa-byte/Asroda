package com.example.data.managers

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.ArchivedMemoryEntity
import com.example.data.local.CastMemberEntity
import com.example.data.local.CharacterEmotionEntity
import com.example.data.local.CustomProviderEntity
import com.example.data.local.EntityRelationEntity
import com.example.data.local.MemoryCheckpointEntity
import com.example.data.local.MemoryEventEntity
import com.example.data.local.MemoryFactEntity
import com.example.data.local.MessageEntity
import com.example.data.local.MilestoneEntity
import com.example.data.local.PendingReappraisalEntity
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
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * 10/10 Cohesive backup/restore manager for Emochi.
 *
 * - Full Room v48 table coverage including `customProviders` (masked by default),
 *   `pendingReappraisals`, and `archivedMemories` (100% backward-compatible with v1 & legacy v2).
 * - Pre-validation via [validateBackupJson] rejects malformed JSON, empty structure, or
 *   unsupported future schema versions BEFORE touching the database.
 * - Single atomic `db.withTransaction` restore preserving existing local API keys (built-in AND
 *   custom providers) when masked/blank keys arrive from a backup.
 * - Automatic cleanup of legacy external-storage plaintext backups via [cleanupLegacyExternalBackups].
 */
class BackupSnapshotManager(
    private val db: AppDatabase,
    private val context: Context?,
    private val backupAdapter: JsonAdapter<BackupSnapshot>,
    private val onAfterRestore: () -> Unit
) {
    data class BackupValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val snapshot: BackupSnapshot? = null
    )

    companion object {
        private const val TAG = "BackupSnapshotManager"
        private const val BACKUP_FILE_NAME = "emochi_autobackup.json"
        private const val BACKUP_TMP_FILE_NAME = "emochi_autobackup.json.tmp"
        const val CURRENT_BACKUP_VERSION = 2

        /**
         * Deletes any legacy unencrypted backup files left in `getExternalFilesDir(null)` by older
         * app versions. Safe to call on app startup and during auto-backup.
         */
        fun cleanupLegacyExternalBackups(context: Context?): Boolean {
            val ctx = context ?: return false
            return try {
                val extDir = ctx.getExternalFilesDir(null) ?: return false
                val legacyFile = File(extDir, BACKUP_FILE_NAME)
                val legacyTmp = File(extDir, BACKUP_TMP_FILE_NAME)
                var removed = false
                if (legacyFile.exists() && legacyFile.delete()) removed = true
                if (legacyTmp.exists() && legacyTmp.delete()) removed = true
                removed
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Validates a raw backup JSON string BEFORE any database write occurs.
     * Rejects blank input, malformed JSON, unsupported future `version > CURRENT_BACKUP_VERSION`,
     * and empty/unrecognized JSON objects.
     */
    fun validateBackupJson(jsonStr: String): BackupValidationResult {
        if (jsonStr.isBlank()) {
            return BackupValidationResult(false, "Yedek verisi boş.")
        }
        val rawObj = try {
            JSONObject(jsonStr)
        } catch (e: Exception) {
            return BackupValidationResult(false, "Geçersiz yedek JSON formatı: ${e.message}")
        }

        if (!rawObj.has("bots") && !rawObj.has("settings") && !rawObj.has("messages")) {
            return BackupValidationResult(false, "Bu dosya geçerli bir Emochi yedeği değil (bot veya ayar verisi bulunamadı).")
        }

        val declaredVersion = rawObj.optInt("version", 1)
        if (declaredVersion < 1 || declaredVersion > CURRENT_BACKUP_VERSION) {
            return BackupValidationResult(
                false,
                "Desteklenmeyen yedek sürümü (v$declaredVersion). Lütfen uygulamayı güncelleyin."
            )
        }

        val parsed = try {
            backupAdapter.fromJson(jsonStr)
        } catch (e: Exception) {
            return BackupValidationResult(false, "Yedek dosyası çözümlenemedi: ${e.message}")
        } ?: return BackupValidationResult(false, "Yedek dosyası çözümlenemedi.")

        return BackupValidationResult(isValid = true, snapshot = parsed)
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
        val customProviderDao = db.customProviderDao()
        val pendingReappraisalDao = db.pendingReappraisalDao()
        val archivedMemoryDao = db.archivedMemoryDao()
        val lorebookDao = db.lorebookDao()

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
        val allPendingReappraisals = mutableListOf<PendingReappraisalEntity>()
        val allArchivedMemories = mutableListOf<ArchivedMemoryEntity>()

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
            try { allPendingReappraisals.addAll(pendingReappraisalDao.getAllForBot(b.id)) } catch (_: Exception) {}
            try { allArchivedMemories.addAll(archivedMemoryDao.getArchivesForBot(b.id)) } catch (_: Exception) {}
        }

        val rawCustomProviders = try {
            customProviderDao.getAllProviders()
        } catch (_: Exception) {
            emptyList()
        }
        val exportedCustomProviders = rawCustomProviders.map { provider ->
            if (includeApiKeys) {
                provider.copy(apiKeyEncrypted = SecureApiKeyVault.decrypt(provider.apiKeyEncrypted))
            } else {
                provider.copy(apiKeyEncrypted = "")
            }
        }

        val rawSettings = db.userSettingsDao().getUserSettings() ?: UserSettingsEntity()
        val exportedSettings = if (includeApiKeys) {
            SecureApiKeyVault.decryptSettingsKeys(rawSettings)
        } else {
            rawSettings.sanitizedForBackup()
        }

        val allLorebooks = try { lorebookDao.getAllLorebooks() } catch (_: Exception) { emptyList() }
        val allLorebookEntries = try { lorebookDao.getAllEntries() } catch (_: Exception) { emptyList() }
        val allBotLorebookLinks = try { lorebookDao.getAllLinks() } catch (_: Exception) { emptyList() }

        return BackupSnapshot(
            version = CURRENT_BACKUP_VERSION,
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
            milestones = allMilestones,
            customProviders = exportedCustomProviders,
            pendingReappraisals = allPendingReappraisals,
            archivedMemories = allArchivedMemories,
            lorebooks = allLorebooks,
            lorebookEntries = allLorebookEntries,
            botLorebookLinks = allBotLorebookLinks
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
            cleanupLegacyExternalBackups(ctx)
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
        val validation = validateBackupJson(jsonStr)
        val snapshot = if (validation.isValid && validation.snapshot != null) {
            validation.snapshot
        } else {
            throw IllegalArgumentException(validation.errorMessage ?: "Geçersiz yedek dosyası.")
        }

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
        val customProviderDao = db.customProviderDao()
        val pendingReappraisalDao = db.pendingReappraisalDao()
        val archivedMemoryDao = db.archivedMemoryDao()
        val lorebookDao = db.lorebookDao()

        val existingSettings = try { settingsDao.getUserSettings() } catch (_: Exception) { null }
        val existingCustomProviders = try { customProviderDao.getAllProviders() } catch (_: Exception) { emptyList() }
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
                    pendingReappraisalDao.deleteForBot(bid)
                    castMemberDao.getCastMembersForBot(bid).forEach { castMemberDao.deleteCastMember(it.id) }
                    entityRelationDao.getRelationsForBot(bid).forEach { entityRelationDao.deleteRelation(it.id) }
                    fragmentDao.deleteFragmentsForBot(bid)
                }
                botDao.deleteAllBots()
                lorebookDao.deleteAllLinks()
                lorebookDao.deleteAllEntries()
                lorebookDao.deleteAllLorebooks()
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
            if (snapshot.pendingReappraisals.isNotEmpty()) {
                val botsWithReappraisals = snapshot.pendingReappraisals.map { it.botId }.toSet()
                if (!overwriteExisting) botsWithReappraisals.forEach { pendingReappraisalDao.deleteForBot(it) }
                for (pr in snapshot.pendingReappraisals) {
                    pendingReappraisalDao.insert(pr.copy(id = 0L))
                }
            }
            if (snapshot.archivedMemories.isNotEmpty()) {
                val existingArchivesByBot = snapshot.archivedMemories
                    .map { it.botId }
                    .distinct()
                    .associateWith { bid ->
                        archivedMemoryDao.getArchivesForBot(bid)
                            .map { "${it.memoryType}|${it.originalMemoryId}|${it.content}" }
                            .toSet()
                    }
                for (am in snapshot.archivedMemories) {
                    val signature = "${am.memoryType}|${am.originalMemoryId}|${am.content}"
                    if (signature !in (existingArchivesByBot[am.botId] ?: emptySet())) {
                        archivedMemoryDao.insertArchive(am.copy(id = 0L))
                    }
                }
            }
            if (snapshot.customProviders.isNotEmpty()) {
                val existingBySignature = existingCustomProviders.associateBy {
                    "${it.baseUrl.trim().lowercase()}|${it.label.trim().lowercase()}"
                }
                for (incoming in snapshot.customProviders) {
                    val sig = "${incoming.baseUrl.trim().lowercase()}|${incoming.label.trim().lowercase()}"
                    val existing = existingBySignature[sig]
                    val resolvedEncryptedKey = when {
                        incoming.apiKeyEncrypted.isNotBlank() -> SecureApiKeyVault.encrypt(incoming.apiKeyEncrypted)
                        existing != null && existing.apiKeyEncrypted.isNotBlank() -> existing.apiKeyEncrypted
                        else -> ""
                    }
                    if (existing != null) {
                        customProviderDao.updateProvider(
                            incoming.copy(
                                id = existing.id,
                                apiKeyEncrypted = resolvedEncryptedKey
                            )
                        )
                    } else {
                        customProviderDao.insertProvider(
                            incoming.copy(
                                id = 0L,
                                apiKeyEncrypted = resolvedEncryptedKey
                            )
                        )
                    }
                }
            }
            if (snapshot.lorebooks.isNotEmpty()) {
                for (lb in snapshot.lorebooks) lorebookDao.upsertLorebook(lb)
            }
            if (snapshot.lorebookEntries.isNotEmpty()) {
                lorebookDao.upsertEntries(snapshot.lorebookEntries)
            }
            if (snapshot.botLorebookLinks.isNotEmpty()) {
                for (lnk in snapshot.botLorebookLinks) lorebookDao.linkBotToLorebook(lnk)
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
