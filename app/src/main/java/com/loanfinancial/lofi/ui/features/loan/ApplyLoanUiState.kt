package com.loanfinancial.lofi.ui.features.loan

import com.loanfinancial.lofi.core.common.result.ErrorType
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.core.media.UploadResult

sealed class ApplyLoanUiState {
    data object Idle : ApplyLoanUiState()

    data object Loading : ApplyLoanUiState()

    data object BiometricAuthenticating : ApplyLoanUiState()

    data object AutoCapturingBiometric : ApplyLoanUiState()

    data object PinRequired : ApplyLoanUiState()

    data class PinError(
        val message: String,
    ) : ApplyLoanUiState()

    data class PinLocked(
        val message: String,
    ) : ApplyLoanUiState()

    data object CapturingLocation : ApplyLoanUiState()

    data object AutoCapturingLocation : ApplyLoanUiState()

    data class ProfileIncomplete(
        val missingFields: List<String> = emptyList(),
    ) : ApplyLoanUiState()

    data object PinNotSet : ApplyLoanUiState()

    data class UploadingDocuments(
        val currentDocument: DocumentType,
        val progress: Int,
        val totalDocuments: Int,
        val completedDocuments: Int,
    ) : ApplyLoanUiState()

    data class ValidationError(
        val errors: Map<String, String>,
    ) : ApplyLoanUiState()

    data class Success(
        val loanId: String,
        val isDraft: Boolean = false,
    ) : ApplyLoanUiState()

    data class DraftSaved(
        val loanId: String,
        val showDialog: Boolean = true,
    ) : ApplyLoanUiState()

    data class DraftLoaded(
        val draftId: String,
    ) : ApplyLoanUiState()

    data class ReadyForDocumentUpload(
        val loanId: String,
    ) : ApplyLoanUiState()

    data class Error(
        val error: ErrorType,
    ) : ApplyLoanUiState()
}

data class ApplyLoanFormState(
    val amount: String = "",
    val tenor: String = "",
    val purpose: String = "",
    val downPayment: String = "",
    // Employment Info
    val jobType: String = "",
    val companyName: String = "",
    val jobPosition: String = "",
    val workDurationMonths: String = "",
    val workAddress: String = "",
    val officePhoneNumber: String = "",
    val declaredIncome: String = "",
    val additionalIncome: String = "",
    val npwpNumber: String = "",
    // Emergency Contact
    val emergencyContactName: String = "",
    val emergencyContactRelation: String = "",
    val emergencyContactPhone: String = "",
    val emergencyContactAddress: String = "",
    // Bank Info
    val bankName: String = "",
    val bankBranch: String = "",
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val documents: Map<DocumentType, DocumentUploadState> = emptyMap(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isBiometricVerified: Boolean = false,
    val loanId: String = "",
    val draftId: String? = null,
    val currentStep: Int = 1, // 1: Basic, 2: Employment, 3: Emergency, 4: Bank
) {
    fun isValid(): Boolean {
        // Biometric and location are hidden in the UI, so we don't require them
        return amount.isNotBlank() &&
            tenor.isNotBlank() &&
            purpose.isNotBlank() &&
            jobType.isNotBlank() &&
            companyName.isNotBlank() &&
            declaredIncome.isNotBlank() &&
            emergencyContactName.isNotBlank() &&
            emergencyContactPhone.isNotBlank() &&
            bankName.isNotBlank() &&
            accountNumber.isNotBlank()
    }

    fun getValidationErrors(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (amount.isBlank()) errors["amount"] = "Amount is required"
        if (tenor.isBlank()) errors["tenor"] = "Tenor is required"
        if (purpose.isBlank()) errors["purpose"] = "Purpose is required"

        if (currentStep >= 2) {
            if (jobType.isBlank()) errors["jobType"] = "Job Type is required"
            if (companyName.isBlank()) errors["companyName"] = "Company Name is required"
            if (declaredIncome.isBlank()) errors["declaredIncome"] = "Income is required"
        }

        if (currentStep >= 3) {
            if (emergencyContactName.isBlank()) errors["emergencyContactName"] = "Name is required"
            if (emergencyContactPhone.isBlank()) errors["emergencyContactPhone"] = "Phone is required"
        }

        if (currentStep >= 4) {
            if (bankName.isBlank()) errors["bankName"] = "Bank Name is required"
            if (accountNumber.isBlank()) errors["accountNumber"] = "Account Number is required"
        }

        if (currentStep == 4) { // Verification step usually
            // Biometric and location are hidden in the UI, so we don't require them
            // if (!isBiometricVerified) {
            //     errors["biometric"] = "Biometric verification is required"
            // }
            // if (latitude == null || longitude == null) {
            //     errors["location"] = "Location capture is required"
            // }
        }
        return errors
    }
}

data class DocumentUploadState(
    val documentType: DocumentType,
    val filePath: String? = null,
    val fileUrl: String? = null,
    val isUploading: Boolean = false,
    val isUploaded: Boolean = false,
    val uploadProgress: Int = 0,
    val error: String? = null,
) {
    companion object {
        fun fromUploadResult(
            documentType: DocumentType,
            result: UploadResult,
        ): DocumentUploadState =
            when (result) {
                is UploadResult.Success ->
                    DocumentUploadState(
                        documentType = documentType,
                        fileUrl = result.fileUrl,
                        isUploaded = true,
                        uploadProgress = 100,
                    )
                is UploadResult.Error ->
                    DocumentUploadState(
                        documentType = documentType,
                        error = result.message,
                    )
                is UploadResult.Progress ->
                    DocumentUploadState(
                        documentType = documentType,
                        isUploading = true,
                        uploadProgress = result.percentage,
                    )
                else -> DocumentUploadState(documentType = documentType)
            }
    }
}
