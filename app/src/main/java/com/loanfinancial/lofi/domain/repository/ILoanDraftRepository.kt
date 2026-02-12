package com.loanfinancial.lofi.domain.repository

import com.loanfinancial.lofi.domain.model.DraftStep
import com.loanfinancial.lofi.domain.model.LoanDraft
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing loan application drafts.
 * Supports offline-first multi-step loan application flow.
 */
interface ILoanDraftRepository {
    /**
     * Create a new draft or update existing one
     */
    suspend fun saveDraft(draft: LoanDraft): Result<String>

    /**
     * Get draft by ID
     */
    suspend fun getDraftById(draftId: String): LoanDraft?

    /**
     * Get all active drafts (DRAFT or IN_PROGRESS status)
     */
    fun getAllActiveDrafts(): Flow<List<LoanDraft>>

    /**
     * Get drafts by specific step
     */
    fun getDraftsByStep(step: DraftStep): Flow<List<LoanDraft>>

    /**
     * Update basic info step (ApplyLoanScreen data)
     */
    suspend fun updateBasicInfo(
        draftId: String,
        amount: Long?,
        tenor: Int?,
        purpose: String?,
        downPayment: Long?,
        latitude: Double?,
        longitude: Double?,
        isBiometricVerified: Boolean,
    ): Result<Unit>

    suspend fun updateEmploymentInfo(
        draftId: String,
        jobType: String?,
        companyName: String?,
        jobPosition: String?,
        workDurationMonths: Int?,
        workAddress: String?,
        officePhoneNumber: String?,
        declaredIncome: Long?,
        additionalIncome: Long?,
        npwpNumber: String?,
    ): Result<Unit>

    suspend fun updateEmergencyContact(
        draftId: String,
        name: String?,
        relation: String?,
        phone: String?,
        address: String?,
    ): Result<Unit>

    suspend fun updateBankInfo(
        draftId: String,
        bankName: String?,
        bankBranch: String?,
        accountNumber: String?,
        accountHolderName: String?,
    ): Result<Unit>

    /**
     * Update document paths (DocumentUploadScreen data)
     */
    suspend fun updateDocumentPaths(
        draftId: String,
        documentPaths: Map<String, String>,
    ): Result<Unit>

    /**
     * Update TnC and mark as completed
     */
    suspend fun completeDraft(
        draftId: String,
        isAgreementChecked: Boolean,
    ): Result<Unit>

    /**
     * Delete a draft
     */
    suspend fun deleteDraft(draftId: String)

    /**
     * Delete all drafts (for single-draft enforcement)
     */
    suspend fun deleteAllDrafts()

    /**
     * Update current step of a draft
     */
    suspend fun updateDraftStep(
        draftId: String,
        step: DraftStep,
    )

    /**
     * Get unsynced completed drafts for background sync
     */
    suspend fun getUnsyncedCompletedDrafts(): List<LoanDraft>

    /**
     * Mark draft as synced with server
     */
    suspend fun markAsSynced(
        draftId: String,
        serverLoanId: String?,
    )
}
