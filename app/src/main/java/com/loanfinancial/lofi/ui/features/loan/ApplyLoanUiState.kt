package com.loanfinancial.lofi.ui.features.loan

import com.loanfinancial.lofi.core.common.result.ErrorType
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.core.media.UploadResult

sealed class ApplyLoanUiState {
    data object Idle : ApplyLoanUiState()

    data object Loading : ApplyLoanUiState()

    data object BiometricAuthenticating : ApplyLoanUiState()

    data object CapturingLocation : ApplyLoanUiState()

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
    ) : ApplyLoanUiState()

    data class Error(
        val error: ErrorType,
    ) : ApplyLoanUiState()
}

data class ApplyLoanFormState(
    val amount: String = "",
    val tenor: String = "",
    val purpose: String = "",
    val documents: Map<DocumentType, DocumentUploadState> = emptyMap(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isBiometricVerified: Boolean = false,
) {
    fun isValid(): Boolean =
        amount.isNotBlank() &&
            tenor.isNotBlank() &&
            purpose.isNotBlank() &&
            documents[DocumentType.KTP]?.isUploaded == true &&
            documents[DocumentType.SELFIE]?.isUploaded == true

    fun getValidationErrors(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (amount.isBlank()) errors["amount"] = "Amount is required"
        if (tenor.isBlank()) errors["tenor"] = "Tenor is required"
        if (purpose.isBlank()) errors["purpose"] = "Purpose is required"
        if (documents[DocumentType.KTP]?.isUploaded != true) {
            errors["ktp"] = "KTP document is required"
        }
        if (documents[DocumentType.SELFIE]?.isUploaded != true) {
            errors["selfie"] = "Selfie document is required"
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
