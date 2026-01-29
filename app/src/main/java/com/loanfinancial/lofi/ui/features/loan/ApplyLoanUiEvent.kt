package com.loanfinancial.lofi.ui.features.loan

import com.loanfinancial.lofi.core.media.DocumentType

sealed class ApplyLoanUiEvent {
    data class AmountChanged(
        val amount: String,
    ) : ApplyLoanUiEvent()

    data class TenorChanged(
        val tenor: String,
    ) : ApplyLoanUiEvent()

    data class PurposeChanged(
        val purpose: String,
    ) : ApplyLoanUiEvent()

    data class DocumentSelected(
        val documentType: DocumentType,
        val filePath: String,
    ) : ApplyLoanUiEvent()

    data class DocumentUploadStarted(
        val documentType: DocumentType,
    ) : ApplyLoanUiEvent()

    data class DocumentUploadCancelled(
        val documentType: DocumentType,
    ) : ApplyLoanUiEvent()

    data class DocumentRemoved(
        val documentType: DocumentType,
    ) : ApplyLoanUiEvent()

    data class CaptureDocument(
        val documentType: DocumentType,
    ) : ApplyLoanUiEvent()

    data class SelectDocumentFromGallery(
        val documentType: DocumentType,
    ) : ApplyLoanUiEvent()

    object BiometricAuthenticate : ApplyLoanUiEvent()

    object CaptureLocation : ApplyLoanUiEvent()

    object SubmitClicked : ApplyLoanUiEvent()

    object CancelClicked : ApplyLoanUiEvent()

    object RetryClicked : ApplyLoanUiEvent()

    object ResetClicked : ApplyLoanUiEvent()
}
