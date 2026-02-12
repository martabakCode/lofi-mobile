package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "pending_loan_submissions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class PendingLoanSubmissionEntity(
    @PrimaryKey
    val loanId: String,
    val userId: String,
    val customerName: String,
    val productCode: String,
    val productName: String,
    val interestRate: Double,
    val loanAmount: Long,
    val tenor: Int,
    val loanStatus: String,
    val currentStage: String,
    val submittedAt: String?,
    val loanStatusDisplay: String,
    val slaDurationHours: Int?,
    val purpose: String?, // Nullable for backward compact if needed, but we should fill it
    val latitude: Double?,
    val longitude: Double?,
    val serverLoanId: String? = null, // To store ID from server after creation
    // Document paths for re-upload
    val documentPaths: String, // JSON string of Map<DocumentType, String>
    // Submission tracking
    val pendingStatus: String, // PENDING, SUBMITTING, SUCCESS, FAILED
    val retryCount: Int = 0,
    val lastRetryTime: Long? = null,
    val failureReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
