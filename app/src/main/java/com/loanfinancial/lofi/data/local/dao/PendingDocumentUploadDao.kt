package com.loanfinancial.lofi.data.local.dao

import androidx.room.*
import com.loanfinancial.lofi.data.model.entity.PendingDocumentUploadEntity

@Dao
interface PendingDocumentUploadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingUpload(entity: PendingDocumentUploadEntity)

    @Query("SELECT * FROM pending_document_uploads WHERE id = :id")
    suspend fun getUploadById(id: String): PendingDocumentUploadEntity?

    @Query("SELECT * FROM pending_document_uploads WHERE loanDraftId = :loanDraftId")
    suspend fun getPendingForDraft(loanDraftId: String): List<PendingDocumentUploadEntity>

    @Query("SELECT * FROM pending_document_uploads WHERE loanDraftId = :loanDraftId")
    fun getPendingForDraftFlow(loanDraftId: String): kotlinx.coroutines.flow.Flow<List<PendingDocumentUploadEntity>>

    @Query("SELECT * FROM pending_document_uploads WHERE status = 'PENDING' OR status = 'FAILED'")
    suspend fun getAllPending(): List<PendingDocumentUploadEntity>

    @Query("UPDATE pending_document_uploads SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE pending_document_uploads SET documentId = :documentId, objectKey = :objectKey, status = 'COMPLETED', updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCompleted(
        id: String, 
        documentId: String, 
        objectKey: String, 
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE pending_document_uploads SET retryCount = :retryCount, lastRetryTime = :lastRetryTime, failureReason = :reason, status = 'FAILED', updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFailed(
        id: String, 
        retryCount: Int, 
        reason: String, 
        lastRetryTime: Long = System.currentTimeMillis(), 
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE pending_document_uploads SET retryCount = :retryCount, lastRetryTime = :lastRetryTime, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRetry(
        id: String, 
        retryCount: Int, 
        lastRetryTime: Long = System.currentTimeMillis(), 
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM pending_document_uploads WHERE status = 'COMPLETED' AND updatedAt < :timestamp AND cleanupScheduled = 0")
    suspend fun getCompletedUploadsOlderThan(timestamp: Long): List<PendingDocumentUploadEntity>

    @Query("UPDATE pending_document_uploads SET cleanupScheduled = 1 WHERE id = :id")
    suspend fun markFilesCleaned(id: String)

    @Query("DELETE FROM pending_document_uploads WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM pending_document_uploads WHERE userId = :userId")
    suspend fun deleteByUser(userId: String)
}
