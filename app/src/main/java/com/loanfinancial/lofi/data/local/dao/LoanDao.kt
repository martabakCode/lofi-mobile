package com.loanfinancial.lofi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.loanfinancial.lofi.data.model.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE userId = :userId")
    fun getLoansByUser(userId: String): Flow<List<LoanEntity>>

    @Query("DELETE FROM loans WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)

    @androidx.room.Transaction
    suspend fun clearAndInsertByUser(
        userId: String,
        loans: List<LoanEntity>,
    ) {
        deleteByUser(userId)
        insertLoans(loans)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()
}
