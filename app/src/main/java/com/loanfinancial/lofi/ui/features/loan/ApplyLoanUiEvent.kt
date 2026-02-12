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

    data class DownPaymentChanged(
        val downPayment: String,
    ) : ApplyLoanUiEvent()

    // Employment Info
    data class JobTypeChanged(
        val jobType: String,
    ) : ApplyLoanUiEvent()

    data class CompanyNameChanged(
        val companyName: String,
    ) : ApplyLoanUiEvent()

    data class JobPositionChanged(
        val jobPosition: String,
    ) : ApplyLoanUiEvent()

    data class WorkDurationMonthsChanged(
        val months: String,
    ) : ApplyLoanUiEvent()

    data class WorkAddressChanged(
        val address: String,
    ) : ApplyLoanUiEvent()

    data class OfficePhoneNumberChanged(
        val phone: String,
    ) : ApplyLoanUiEvent()

    data class DeclaredIncomeChanged(
        val income: String,
    ) : ApplyLoanUiEvent()

    data class AdditionalIncomeChanged(
        val income: String,
    ) : ApplyLoanUiEvent()

    data class NpwpNumberChanged(
        val npwp: String,
    ) : ApplyLoanUiEvent()

    // Emergency Contact
    data class EmergencyContactNameChanged(
        val name: String,
    ) : ApplyLoanUiEvent()

    data class EmergencyContactRelationChanged(
        val relation: String,
    ) : ApplyLoanUiEvent()

    data class EmergencyContactPhoneChanged(
        val phone: String,
    ) : ApplyLoanUiEvent()

    data class EmergencyContactAddressChanged(
        val address: String,
    ) : ApplyLoanUiEvent()

    // Bank Info
    data class BankNameChanged(
        val bank: String,
    ) : ApplyLoanUiEvent()

    data class BankBranchChanged(
        val branch: String,
    ) : ApplyLoanUiEvent()

    data class AccountNumberChanged(
        val number: String,
    ) : ApplyLoanUiEvent()

    data class AccountHolderNameChanged(
        val name: String,
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

    data object BiometricAuthenticate : ApplyLoanUiEvent()

    data object AutoBiometricAuthenticate : ApplyLoanUiEvent()

    data object CaptureLocation : ApplyLoanUiEvent()

    data object AutoCaptureLocation : ApplyLoanUiEvent()

    data class PinSubmitted(
        val pin: String,
    ) : ApplyLoanUiEvent()

    data object SubmitClicked : ApplyLoanUiEvent()

    object SaveAsDraftClicked : ApplyLoanUiEvent()

    object CancelClicked : ApplyLoanUiEvent()

    object RetryClicked : ApplyLoanUiEvent()

    object ResetClicked : ApplyLoanUiEvent()

    object NextStepClicked : ApplyLoanUiEvent()

    object PreviousStepClicked : ApplyLoanUiEvent()

    object ProceedToDocumentUpload : ApplyLoanUiEvent()
}
