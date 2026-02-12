package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "pending_document_uploads",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index("userId")],
)
data class PendingDocumentUploadEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val loanDraftId: String,
    val loanId: String? = null,
    val documentType: String,
    val localFilePath: String,
    val fileName: String,
    val contentType: String,
    val documentId: String? = null,
    val objectKey: String? = null,
    val status: String = DocumentUploadStatus.PENDING.name,
    val retryCount: Int = 0,
    val lastRetryTime: Long? = null,
    val failureReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val compressedFilePath: String? = null,
    val originalFileSize: Long = 0,
    val compressedFileSize: Long = 0,
    val isCompressed: Boolean = false,
    val cleanupScheduled: Boolean = false,
)

enum class DocumentUploadStatus {
    PENDING, // Waiting to be uploaded
    UPLOADING, // Currently uploading
    COMPLETED, // Successfully uploaded to S3
    FAILED, // Failed after max retries
    CANCELLED, // User cancelled
}
