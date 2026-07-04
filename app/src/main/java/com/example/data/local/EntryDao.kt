package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllEntries(userId: String): Flow<List<EntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity): Long

    @Query("SELECT * FROM entries WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsyncedEntries(userId: String): List<EntryEntity>

    @Query("UPDATE entries SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
    
    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteEntry(id: Int)
}
