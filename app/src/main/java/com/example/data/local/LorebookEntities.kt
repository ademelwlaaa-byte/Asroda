package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Dinamik Lorebook (Evren Ansiklopedisi) paketi.
 * Birden fazla RP karakterine paylaşılabilir şekilde bağlanabilir; 100% cihaz-içi Room saklama.
 */
@Entity(tableName = "lorebooks")
data class LorebookEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val tokenBudget: Int = 750,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Tekil Lorebook ansiklopedi maddesi.
 * [isConstant] = true ise her tur enjekte edilir; false ise [keywords] son mesajlarda
 * (veya 1 adımlı zincirleme tetiklemede) geçtiğinde öncelik sırasına göre enjekte edilir.
 */
@Entity(
    tableName = "lorebook_entries",
    indices = [Index(value = ["lorebookId"])]
)
data class LorebookEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val lorebookId: String,
    val title: String,
    val keywords: String = "",
    val content: String = "",
    val category: String = "genel",
    val priority: Int = 5,
    val isConstant: Boolean = false,
    val isEnabled: Boolean = true,
    val scanDepth: Int = 4,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Çoktan-çoğa (N:M) Bot <-> Lorebook bağlantısı.
 * Aynı evren ansiklopedisi birden fazla RP botuna tek dokunuşla bağlanabilir.
 */
@Entity(
    tableName = "bot_lorebook_links",
    primaryKeys = ["botId", "lorebookId"],
    indices = [
        Index(value = ["botId"]),
        Index(value = ["lorebookId"])
    ]
)
data class BotLorebookLinkEntity(
    val botId: String,
    val lorebookId: String,
    val linkedAt: Long = System.currentTimeMillis()
)
