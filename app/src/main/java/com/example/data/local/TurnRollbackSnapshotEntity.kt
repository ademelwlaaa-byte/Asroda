package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

/**
 * Atomic per-turn rollback ledger capturing exact pre-turn bot/cast state and every
 * memory, RAG fragment, Living Memory, milestone, relation, archive, and emotion side-effect
 * produced by a single turn (main reply + post-turn background memory extraction).
 *
 * Enables 100% lossless rollback whenever a message is edited, regenerated, or deleted.
 */
@Entity(
    tableName = "turn_rollback_snapshots",
    indices = [
        Index(value = ["botId"]),
        Index(value = ["userMessageId"])
    ]
)
data class TurnRollbackSnapshotEntity(
    @PrimaryKey val messageId: String,
    val botId: String,
    val userMessageId: String = "",
    val turnStartedAt: Long = System.currentTimeMillis(),
    val turnCompletedAt: Long = System.currentTimeMillis(),
    val preBotEmotionJson: String = "",
    val preBotPrevEmotionJson: String = "",
    val preWorldAtmosphereJson: String = "",
    val preStoryDayCounter: Long = 1L,
    val preStoryCalendarDate: String = "",
    val preSceneTime: String = "",
    val preCurrentAge: Int = 20,
    val preCastSnapshotJson: String = "{}",
    val createdFactIdsJson: String = "[]",
    val supersededFactsBackupJson: String = "[]",
    val createdEventIdsJson: String = "[]",
    val modifiedEventsBackupJson: String = "[]",
    val createdFragmentIdsJson: String = "[]",
    val createdMilestoneIdsJson: String = "[]",
    val createdRelationIdsJson: String = "[]",
    val createdCastIdsJson: String = "[]",
    val createdArchiveIdsJson: String = "[]",
    val createdEmotionHistoryIdsJson: String = "[]",
    val createdAffectionEventIdsJson: String = "[]"
)

@Dao
interface TurnRollbackSnapshotDao {
    @Upsert
    suspend fun upsert(snapshot: TurnRollbackSnapshotEntity)

    @Query("SELECT * FROM turn_rollback_snapshots WHERE messageId = :messageId LIMIT 1")
    suspend fun getByMessageId(messageId: String): TurnRollbackSnapshotEntity?

    @Query("SELECT * FROM turn_rollback_snapshots WHERE userMessageId = :userMessageId ORDER BY turnStartedAt DESC LIMIT 1")
    suspend fun getByUserMessageId(userMessageId: String): TurnRollbackSnapshotEntity?

    @Query("SELECT * FROM turn_rollback_snapshots WHERE botId = :botId ORDER BY turnStartedAt DESC")
    suspend fun getSnapshotsForBot(botId: String): List<TurnRollbackSnapshotEntity>

    @Query("DELETE FROM turn_rollback_snapshots WHERE messageId = :messageId")
    suspend fun deleteByMessageId(messageId: String)

    @Query("DELETE FROM turn_rollback_snapshots WHERE botId = :botId AND turnStartedAt > :timestamp")
    suspend fun deleteAfterTimestamp(botId: String, timestamp: Long)

    @Query("DELETE FROM turn_rollback_snapshots WHERE botId = :botId")
    suspend fun deleteForBot(botId: String)
}
