package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val originalText: String,
    val parsedExpensesJson: String,
    val mood: String,
    val aiInsight: String,
    val userId: String = "",
    val isSynced: Boolean = false,
    val totalAmount: Double = 0.0,
    val firebaseId: String = "",
    val isParsed: Boolean = true
)
