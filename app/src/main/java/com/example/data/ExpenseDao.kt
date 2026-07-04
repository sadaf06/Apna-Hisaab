package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)

    @Query("SELECT * FROM expenses WHERE isSynced = 0")
    suspend fun getUnsyncedExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getExpenseByFirebaseId(firebaseId: String): Expense?

    @Query("SELECT * FROM expenses WHERE timestamp = :timestamp AND description = :description AND amount = :amount LIMIT 1")
    suspend fun getExpenseByContent(timestamp: Long, description: String, amount: Double): Expense?

    @Query("UPDATE expenses SET isSynced = 1, firebaseId = :firebaseId WHERE id = :id")
    suspend fun markSynced(id: Int, firebaseId: String)
}
