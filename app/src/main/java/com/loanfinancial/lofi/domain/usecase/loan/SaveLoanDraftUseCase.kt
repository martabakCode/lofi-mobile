package com.loanfinancial.lofi.domain.usecase.loan

import com.loanfinancial.lofi.domain.model.DraftStatus
import com.loanfinancial.lofi.domain.model.DraftStep
import com.loanfinancial.lofi.domain.model.LoanDraft
import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to save a loan draft to local storage.
 * Used for offline-first multi-step loan application flow.
 */
class SaveLoanDraftUseCase
    @Inject
    constructor(
        private val repository: ILoanDraftRepository,
        private val deleteAllDraftsUseCase: DeleteAllDraftsUseCase,
    ) {
        suspend operator fun invoke(
            id: String? = null,
            amount: Long? = null,
            tenor: Int? = null,
            purpose: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
            isBiometricVerified: Boolean = false,
            documentPaths: Map<String, String>? = null,
            interestRate: Double? = null,
            adminFee: Double? = null,
            isAgreementChecked: Boolean = false,
            documentUploadStatus: Map<String, String>? = null,
            uploadQueueIds: List<String>? = null,
            currentStep: DraftStep = DraftStep.BASIC_INFO,
            status: DraftStatus = DraftStatus.DRAFT,
        ): Result<String> {
            val draft =
                LoanDraft(
                    id = id ?: "",
                    amount = amount,
                    tenor = tenor,
                    purpose = purpose,
                    downPayment = null, // Set in specific update methods if needed, or update invoke params
                    latitude = latitude,
                    longitude = longitude,
                    isBiometricVerified = isBiometricVerified,
                    documentPaths = documentPaths,
                    interestRate = interestRate,
                    adminFee = adminFee,
                    isAgreementChecked = isAgreementChecked,
                    currentStep = currentStep,
                    status = status,
                    isSynced = false,
                    serverLoanId = null,
                    documentUploadStatus = documentUploadStatus,
                    uploadQueueIds = uploadQueueIds,
                    createdAt =
                        id?.let { repository.getDraftById(it)?.createdAt ?: System.currentTimeMillis() }
                            ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            return repository.saveDraft(draft)
        }

        suspend fun saveOrUpdateBasicInfo(
            draftId: String?,
            amount: Long?,
            tenor: Int?,
            purpose: String?,
            downPayment: Long?,
            latitude: Double?,
            longitude: Double?,
            isBiometricVerified: Boolean,
        ): Result<String> =
            if (draftId != null) {
                // Update existing draft
                repository.updateBasicInfo(
                    draftId = draftId,
                    amount = amount,
                    tenor = tenor,
                    purpose = purpose,
                    downPayment = downPayment,
                    latitude = latitude,
                    longitude = longitude,
                    isBiometricVerified = isBiometricVerified,
                )
                Result.success(draftId)
            } else {
                // Check if there's an existing active draft to reuse
                val existingDraft = repository.getAllActiveDrafts().first().firstOrNull()
                if (existingDraft != null) {
                    // Update the existing draft instead of creating a new one
                    repository.updateBasicInfo(
                        draftId = existingDraft.id,
                        amount = amount,
                        tenor = tenor,
                        purpose = purpose,
                        downPayment = downPayment,
                        latitude = latitude,
                        longitude = longitude,
                        isBiometricVerified = isBiometricVerified,
                    )
                    Result.success(existingDraft.id)
                } else {
                    // No existing draft, create new one
                    invoke(
                        amount = amount,
                        tenor = tenor,
                        purpose = purpose,
                        latitude = latitude,
                        longitude = longitude,
                        isBiometricVerified = isBiometricVerified,
                        currentStep = DraftStep.BASIC_INFO,
                        status = DraftStatus.IN_PROGRESS,
                    )
                }
            }

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
            currentStep: DraftStep = DraftStep.EMPLOYMENT_INFO,
        ): Result<Unit> {
            val result =
                repository.updateEmploymentInfo(
                    draftId,
                    jobType,
                    companyName,
                    jobPosition,
                    workDurationMonths,
                    workAddress,
                    officePhoneNumber,
                    declaredIncome,
                    additionalIncome,
                    npwpNumber,
                )
            // Update currentStep
            if (result.isSuccess) {
                repository.updateDraftStep(draftId, currentStep)
            }
            return result
        }

        suspend fun updateEmergencyContact(
            draftId: String,
            name: String?,
            relation: String?,
            phone: String?,
            address: String?,
            currentStep: DraftStep = DraftStep.EMERGENCY_CONTACT,
        ): Result<Unit> {
            val result = repository.updateEmergencyContact(draftId, name, relation, phone, address)
            // Update currentStep
            if (result.isSuccess) {
                repository.updateDraftStep(draftId, currentStep)
            }
            return result
        }

        suspend fun updateBankInfo(
            draftId: String,
            bankName: String?,
            bankBranch: String?,
            accountNumber: String?,
            accountHolderName: String?,
            currentStep: DraftStep = DraftStep.BANK_INFO,
        ): Result<Unit> {
            val result = repository.updateBankInfo(draftId, bankName, bankBranch, accountNumber, accountHolderName)
            // Update currentStep
            if (result.isSuccess) {
                repository.updateDraftStep(draftId, currentStep)
            }
            return result
        }

        suspend fun updateDocumentPaths(
            draftId: String,
            documentPaths: Map<String, String>,
        ): Result<Unit> = repository.updateDocumentPaths(draftId, documentPaths)
    }
