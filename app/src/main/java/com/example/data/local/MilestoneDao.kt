package com.example.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MilestoneDao {
    @Upsert
    suspend fun insertMilestone(milestone: MilestoneEntity)

    @Query("SELECT * FROM milestones WHERE botId = :botId ORDER BY reachedAt ASC")
    suspend fun getMilestonesForBot(botId: String): List<MilestoneEntity>

    @Query("SELECT type FROM milestones WHERE botId = :botId")
    suspend fun getReachedMilestoneTypes(botId: String): List<String>

    @Query("DELETE FROM milestones WHERE botId = :botId AND reachedAt > :timestamp")
    suspend fun deleteMilestonesAfterTimestamp(botId: String, timestamp: Long)

    @Query("DELETE FROM milestones WHERE botId = :botId")
    suspend fun deleteMilestonesForBot(botId: String)
}