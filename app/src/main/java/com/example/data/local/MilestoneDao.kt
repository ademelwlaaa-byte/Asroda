package com.example.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    @Upsert
    suspend fun insertMilestone(milestone: MilestoneEntity)

    @Query("SELECT * FROM milestones WHERE botId = :botId ORDER BY reachedAt ASC")
    suspend fun getMilestonesForBot(botId: String): List<MilestoneEntity>

    @Query("SELECT * FROM milestones WHERE botId = :botId ORDER BY reachedAt ASC")
    fun getMilestonesFlow(botId: String): Flow<List<MilestoneEntity>>

    @Query("SELECT * FROM milestones WHERE botId = :botId ORDER BY reachedAt DESC LIMIT :limit")
    suspend fun getRecentMilestonesForBot(botId: String, limit: Int = 3): List<MilestoneEntity>

    @Query("SELECT type FROM milestones WHERE botId = :botId")
    suspend fun getReachedMilestoneTypes(botId: String): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM milestones WHERE botId = :botId AND type = :milestoneKey)")
    suspend fun hasMilestoneKey(botId: String, milestoneKey: String): Boolean

    @Query("DELETE FROM milestones WHERE botId = :botId AND reachedAt > :timestamp")
    suspend fun deleteMilestonesAfterTimestamp(botId: String, timestamp: Long)

    @Query("DELETE FROM milestones WHERE botId = :botId")
    suspend fun deleteMilestonesForBot(botId: String)
}