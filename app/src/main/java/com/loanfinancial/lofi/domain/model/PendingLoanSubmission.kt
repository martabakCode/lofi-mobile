package com.loanfinancial.lofi.domain.model

import java.util.Date

data class PendingLoanSubmission(
    val loanId: String,
    val loanAmount: Long,
    val tenor: Int,
    val productName: String,
    val status: PendingSubmissionStatus,
    val retryCount: Int,
    val lastRetryTime: Date?,
    val failureReason: String?,
    val createdAt: Date
)
