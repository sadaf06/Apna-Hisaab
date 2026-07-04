package com.example.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val diaryEntryDao: DiaryEntryDao
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getAllDiaryEntries(userId: String): Flow<List<DiaryEntry>> = 
        diaryEntryDao.getAllDiaryEntries(userId)

    suspend fun insert(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun delete(id: Int) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun getUnsyncedExpenses(): List<Expense> {
        return expenseDao.getUnsyncedExpenses()
    }

    suspend fun getExpenseByFirebaseId(firebaseId: String): Expense? {
        return expenseDao.getExpenseByFirebaseId(firebaseId)
    }

    suspend fun getExpenseByContent(timestamp: Long, description: String, amount: Double): Expense? {
        return expenseDao.getExpenseByContent(timestamp, description, amount)
    }

    suspend fun markExpenseSynced(id: Int, firebaseId: String) {
        expenseDao.markSynced(id, firebaseId)
    }

    suspend fun insertDiaryEntry(entry: DiaryEntry): Long {
        return diaryEntryDao.insertDiaryEntry(entry)
    }

    suspend fun updateDiaryEntry(entry: DiaryEntry) {
        diaryEntryDao.updateDiaryEntry(entry)
    }

    suspend fun deleteDiaryEntry(id: Int) {
        diaryEntryDao.deleteDiaryEntryById(id)
    }

    suspend fun getDiaryEntryById(id: Int): DiaryEntry? {
        return diaryEntryDao.getDiaryEntryById(id)
    }

    suspend fun getUnsyncedDiaryEntries(userId: String): List<DiaryEntry> {
        return diaryEntryDao.getUnsyncedDiaryEntries(userId)
    }

    suspend fun getEntryByFirebaseId(firebaseId: String): DiaryEntry? {
        return diaryEntryDao.getEntryByFirebaseId(firebaseId)
    }

    suspend fun getEntryByContent(timestamp: Long, originalText: String): DiaryEntry? {
        return diaryEntryDao.getEntryByContent(timestamp, originalText)
    }

    suspend fun markSynced(id: Int, firebaseId: String) {
        diaryEntryDao.markSynced(id, firebaseId)
    }

    suspend fun deleteDiaryEntryByFirebaseId(firebaseId: String) {
        diaryEntryDao.deleteDiaryEntryByFirebaseId(firebaseId)
    }

    suspend fun deleteAllDiaryEntries(userId: String) {
        diaryEntryDao.deleteAllDiaryEntries(userId)
    }
}
