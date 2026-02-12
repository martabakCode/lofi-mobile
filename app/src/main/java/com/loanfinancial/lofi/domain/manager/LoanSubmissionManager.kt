package com.loanfinancial.lofi.domain.manager

import com.loanfinancial.lofi.domain.model.LoanSubmissionData
import com.loanfinancial.lofi.domain.model.PendingLoanSubmission
import kotlinx.coroutines.flow.Flow

interface LoanSubmissionManager {
    // Submit loan when offline - saves to DB and schedules worker
    suspend fun submitLoanOffline(loanData: LoanSubmissionData): Result<String>

    // Get all pending submissions
    fun getPendingSubmissions(): Flow<List<PendingLoanSubmission>>

    // Retry a failed submission
    suspend fun retrySubmission(loanId: String): Result<Unit>

    // Cancel a pending submission
    suspend fun cancelSubmission(loanId: String)

    // Trigger all pending submissions (e.g. on network restored)
    suspend fun triggerPendingSubmissions()
}
