package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val date: Long,
    val originalText: String,
    val expenses: String, // Store as JSON string, or need TypeConverters
    val mood: String,
    val aiInsight: String,
    val totalAmount: Double,
    val isSynced: Boolean = false
)
