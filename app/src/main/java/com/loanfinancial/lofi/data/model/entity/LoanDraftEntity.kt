package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to store loan application drafts for multi-step offline-first flow.
 * 
 * Flow: Apply (BASIC_INFO) -> Document (DOCUMENTS) -> Preview (PREVIEW) -> TnC (TNC) -> Submit
 */
@Entity(tableName = "loan_drafts")
data class LoanDraftEntity(
    @PrimaryKey
    val id: String,
    
    val customerId: String? = null,
    
    // Basic loan info (from Apply step)
    val amount: Long? = null,
    val tenor: Int? = null,
    val purpose: String? = null,
    val downPayment: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isBiometricVerified: Boolean = false,

    // Employment Info
    val declaredIncome: Long? = null,
    val npwpNumber: String? = null,
    val jobType: String? = null,
    val companyName: String? = null,
    val jobPosition: String? = null,
    val workDurationMonths: Int? = null,
    val workAddress: String? = null,
    val officePhoneNumber: String? = null,
    val additionalIncome: Long? = null,

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
    
    // Document paths (from Document step) - stored as JSON string
    val documentPaths: String? = null, // Map<DocumentType, String> as JSON
    
    // Preview step data
    val interestRate: Double? = null,
    val adminFee: Double? = null,
    
    // TnC step data
    val isAgreementChecked: Boolean = false,
    
    // Current step in the flow
    val currentStep: String, // BASIC_INFO, DOCUMENTS, PREVIEW, TNC, COMPLETED
    
    // Step data as JSON for flexibility
    val stepData: String? = null, // Additional step-specific data as JSON
    
    // Status tracking
    val status: String, // DRAFT, IN_PROGRESS, COMPLETED, SYNCED, FAILED
    
    // Server sync tracking
    val isSynced: Boolean = false,
    val serverLoanId: String? = null,
    
    // New fields for offline-first document tracking
    val documentUploadStatus: String? = null, // Map<String, String> as JSON
    val uploadQueueIds: String? = null, // List<String> as JSON
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Enum representing the steps in loan application flow
 */
enum class DraftStep {
    BASIC_INFO,   // ApplyLoanScreen
    DOCUMENTS,    // DocumentUploadScreen
    PREVIEW,      // LoanPreviewScreen
    TNC,          // LoanTnCScreen
    COMPLETED     // Submitted
}

/**
 * Enum representing draft status
 */
enum class DraftStatus {
    DRAFT,        // Initial state
    IN_PROGRESS,  // User is actively working on it
    COMPLETED,    // All steps done, ready to submit
    SYNCED,       // Successfully submitted to server
    FAILED        // Submission failed after retries
}
