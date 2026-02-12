package com.loanfinancial.lofi.domain.model

/**
 * Domain model for loan draft
 */
data class LoanDraft(
    val id: String,
    val amount: Long?,
    val tenor: Int?,
    val purpose: String?,
    val downPayment: Long?,
    val latitude: Double?,
    val longitude: Double?,
    val isBiometricVerified: Boolean,
    // Employment Info
    val jobType: String? = null,
    val companyName: String? = null,
    val jobPosition: String? = null,
    val workDurationMonths: Int? = null,
    val workAddress: String? = null,
    val officePhoneNumber: String? = null,
    val declaredIncome: Long? = null,
    val additionalIncome: Long? = null,
    val npwpNumber: String? = null,
    // Emergency Contact
    val emergencyContactName: String? = null,
    val emergencyContactRelation: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactAddress: String? = null,
    // Bank Info
    val bankName: String? = null,
    val bankBranch: String? = null,
    val accountNumber: String? = null,
    val accountHolderName: String? = null,
    val documentPaths: Map<String, String>?,
    val interestRate: Double?,
    val adminFee: Double?,
    val isAgreementChecked: Boolean,
    val currentStep: DraftStep,
    val status: DraftStatus,
    val isSynced: Boolean,
    val serverLoanId: String?,
    val documentUploadStatus: Map<String, String>? = null,
    val uploadQueueIds: List<String>? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class DraftStep {
    BASIC_INFO,
    EMPLOYMENT_INFO,
    EMERGENCY_CONTACT,
    BANK_INFO,
    DOCUMENTS,
    PREVIEW,
    TNC,
    COMPLETED,
}

enum class DraftStatus {
    DRAFT, // Initial state
    IN_PROGRESS, // User is actively working on it
    COMPLETED, // All steps done, ready to submit
    SYNCED, // Successfully submitted to server
    FAILED, // Submission failed after retries
}
