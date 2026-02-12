package com.loanfinancial.lofi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.loanfinancial.lofi.data.model.entity.PendingLoanSubmissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingLoanSubmissionDao {
    @Query("SELECT * FROM pending_loan_submissions WHERE pendingStatus IN ('PENDING', 'SUBMITTING', 'FAILED') ORDER BY createdAt DESC")
    fun getPendingSubmissions(): Flow<List<PendingLoanSubmissionEntity>>

    @Query("SELECT * FROM pending_loan_submissions WHERE userId = :userId AND pendingStatus IN ('PENDING', 'SUBMITTING', 'FAILED') ORDER BY createdAt DESC")
    fun getPendingSubmissionsByUser(userId: String): Flow<List<PendingLoanSubmissionEntity>>

    @Query("DELETE FROM pending_loan_submissions WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)

    @Query("SELECT * FROM pending_loan_submissions WHERE pendingStatus IN ('PENDING', 'FAILED')")
    suspend fun getAllPendingSubmissionsSync(): List<PendingLoanSubmissionEntity>

    @Query("SELECT * FROM pending_loan_submissions WHERE pendingStatus = 'PENDING' AND (lastRetryTime IS NULL OR :currentTime - lastRetryTime >= :minInterval)")
    suspend fun getRetryableSubmissions(currentTime: Long, minInterval: Long): List<PendingLoanSubmissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(submission: PendingLoanSubmissionEntity)

    @Update
    suspend fun update(submission: PendingLoanSubmissionEntity)

    @Query("UPDATE pending_loan_submissions SET pendingStatus = :status, retryCount = :retryCount, lastRetryTime = :timestamp, failureReason = :reason WHERE loanId = :loanId")
    suspend fun updateSubmissionStatus(loanId: String, status: String, retryCount: Int, timestamp: Long, reason: String?)

    @Query("UPDATE pending_loan_submissions SET pendingStatus = :status, lastRetryTime = :timestamp, failureReason = :reason WHERE loanId = :loanId")
    suspend fun updateSubmissionStatus(loanId: String, status: String, timestamp: Long, reason: String?)

    @Query("SELECT * FROM pending_loan_submissions WHERE loanId = :loanId")
    suspend fun getById(loanId: String): PendingLoanSubmissionEntity

    @Query("DELETE FROM pending_loan_submissions WHERE loanId = :loanId")
    suspend fun delete(loanId: String)

    // Cancel submission - update status to CANCELLED
    @Query("UPDATE pending_loan_submissions SET pendingStatus = 'CANCELLED', failureReason = 'Cancelled by user' WHERE loanId = :loanId AND pendingStatus IN ('PENDING', 'FAILED')")
    suspend fun cancelSubmission(loanId: String): Int

    // Get cancellable submissions (PENDING or FAILED only)
    @Query("SELECT * FROM pending_loan_submissions WHERE loanId = :loanId AND pendingStatus IN ('PENDING', 'FAILED')")
    suspend fun getCancellableSubmission(loanId: String): PendingLoanSubmissionEntity?
}
