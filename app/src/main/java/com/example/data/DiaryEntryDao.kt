package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryEntryDao {
    @Query("SELECT * FROM diary_entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllDiaryEntries(userId: String): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntry): Long

    @Update
    suspend fun updateDiaryEntry(entry: DiaryEntry)

    @Query("SELECT * FROM diary_entries WHERE id = :id LIMIT 1")
    suspend fun getDiaryEntryById(id: Int): DiaryEntry?

    @Query("SELECT * FROM diary_entries WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsyncedDiaryEntries(userId: String): List<DiaryEntry>

    @Query("SELECT * FROM diary_entries WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getEntryByFirebaseId(firebaseId: String): DiaryEntry?

    @Query("SELECT * FROM diary_entries WHERE timestamp = :timestamp AND originalText = :originalText LIMIT 1")
    suspend fun getEntryByContent(timestamp: Long, originalText: String): DiaryEntry?

    @Query("UPDATE diary_entries SET isSynced = 1, firebaseId = :firebaseId WHERE id = :id")
    suspend fun markSynced(id: Int, firebaseId: String)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteDiaryEntryById(id: Int)

    @Query("DELETE FROM diary_entries WHERE firebaseId = :firebaseId")
    suspend fun deleteDiaryEntryByFirebaseId(firebaseId: String)

    @Query("DELETE FROM diary_entries WHERE userId = :userId")
    suspend fun deleteAllDiaryEntries(userId: String)
}
