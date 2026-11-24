package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LorebookDao {

    // ── Lorebooks ──
    @Query("SELECT * FROM lorebooks ORDER BY updatedAt DESC")
    fun getAllLorebooksFlow(): Flow<List<LorebookEntity>>

    @Query("SELECT * FROM lorebooks ORDER BY updatedAt DESC")
    suspend fun getAllLorebooks(): List<LorebookEntity>

    @Query("SELECT * FROM lorebooks WHERE id = :id LIMIT 1")
    suspend fun getLorebookById(id: String): LorebookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLorebook(lorebook: LorebookEntity)

    @Query("DELETE FROM lorebooks WHERE id = :id")
    suspend fun deleteLorebookById(id: String)

    @Query("DELETE FROM lorebooks")
    suspend fun deleteAllLorebooks()

    // ── Lorebook Entries ──
    @Query("SELECT * FROM lorebook_entries WHERE lorebookId = :lorebookId ORDER BY isConstant DESC, priority DESC, title ASC")
    fun getEntriesForLorebookFlow(lorebookId: String): Flow<List<LorebookEntryEntity>>

    @Query("SELECT * FROM lorebook_entries WHERE lorebookId = :lorebookId ORDER BY isConstant DESC, priority DESC, title ASC")
    suspend fun getEntriesForLorebook(lorebookId: String): List<LorebookEntryEntity>

    @Query("SELECT * FROM lorebook_entries")
    suspend fun getAllEntries(): List<LorebookEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntry(entry: LorebookEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntries(entries: List<LorebookEntryEntity>)

    @Query("DELETE FROM lorebook_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: String)

    @Query("DELETE FROM lorebook_entries WHERE lorebookId = :lorebookId")
    suspend fun deleteEntriesForLorebook(lorebookId: String)

    @Query("DELETE FROM lorebook_entries")
    suspend fun deleteAllEntries()

    // ── Bot <-> Lorebook Links ──
    @Query("SELECT lorebookId FROM bot_lorebook_links WHERE botId = :botId")
    fun getLinkedLorebookIdsFlow(botId: String): Flow<List<String>>

    @Query("SELECT lorebookId FROM bot_lorebook_links WHERE botId = :botId")
    suspend fun getLinkedLorebookIds(botId: String): List<String>

    @Query(
        "SELECT l.* FROM lorebooks l " +
            "INNER JOIN bot_lorebook_links bl ON l.id = bl.lorebookId " +
            "WHERE bl.botId = :botId AND l.isEnabled = 1 " +
            "ORDER BY l.updatedAt DESC"
    )
    suspend fun getEnabledLorebooksForBot(botId: String): List<LorebookEntity>

    @Query("SELECT * FROM bot_lorebook_links")
    suspend fun getAllLinks(): List<BotLorebookLinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkBotToLorebook(link: BotLorebookLinkEntity)

    @Query("DELETE FROM bot_lorebook_links WHERE botId = :botId AND lorebookId = :lorebookId")
    suspend fun unlinkBotFromLorebook(botId: String, lorebookId: String)

    @Query("DELETE FROM bot_lorebook_links WHERE lorebookId = :lorebookId")
    suspend fun deleteLinksForLorebook(lorebookId: String)

    @Query("DELETE FROM bot_lorebook_links WHERE botId = :botId")
    suspend fun deleteLinksForBot(botId: String)

    @Query("DELETE FROM bot_lorebook_links")
    suspend fun deleteAllLinks()
}
