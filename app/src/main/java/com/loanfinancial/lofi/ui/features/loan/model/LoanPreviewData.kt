package com.loanfinancial.lofi.ui.features.loan.model

import com.loanfinancial.lofi.core.media.DocumentType

/**
 * Data class representing loan preview data passed between screens
 */
data class LoanPreviewData(
    val amount: String = "",
    val tenor: String = "",
    val purpose: String = "",
    val documents: Map<DocumentType, DocumentPreviewInfo> = emptyMap(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isBiometricVerified: Boolean = false,
    val interestRate: Double = DEFAULT_INTEREST_RATE,
    val adminFee: Double = DEFAULT_ADMIN_FEE,
) {
    companion object {
        const val DEFAULT_INTEREST_RATE = 12.0 // 12% per year
        const val DEFAULT_ADMIN_FEE = 50000.0 // Rp 50.000
        const val DEFAULT_PROVISION_FEE_PERCENT = 1.0 // 1% of loan amount
    }

    val amountValue: Long
        get() = amount.toLongOrNull() ?: 0L

    val tenorValue: Int
        get() = tenor.toIntOrNull() ?: 0

    /**
     * Calculate estimated monthly installment using flat rate formula
     * Formula: (P + (P × r × t)) / t
     * P = Principal, r = monthly interest rate, t = tenor in months
     */
    val estimatedMonthlyInstallment: Long
        get() {
            val principal = amountValue.toDouble()
            val months = tenorValue
            if (principal <= 0 || months <= 0) return 0L

            val monthlyInterestRate = interestRate / 100 / 12
            val totalInterest = principal * monthlyInterestRate * months
            val totalPayment = principal + totalInterest + adminFee + provisionFee
            return (totalPayment / months).toLong()
        }

    /**
     * Calculate provision fee (1% of loan amount)
     */
    val provisionFee: Double
        get() = amountValue * DEFAULT_PROVISION_FEE_PERCENT / 100

    /**
     * Calculate total payment including principal, interest, and fees
     */
    val totalPayment: Long
        get() {
            val principal = amountValue.toDouble()
            val months = tenorValue
            if (principal <= 0 || months <= 0) return 0L

            val monthlyInterestRate = interestRate / 100 / 12
            val totalInterest = principal * monthlyInterestRate * months
            return (principal + totalInterest + adminFee + provisionFee).toLong()
        }

    /**
     * Calculate total interest over the loan period
     */
    val totalInterest: Long
        get() {
            val principal = amountValue.toDouble()
            val months = tenorValue
            if (principal <= 0 || months <= 0) return 0L

            val monthlyInterestRate = interestRate / 100 / 12
            return (principal * monthlyInterestRate * months).toLong()
        }

    fun isValid(): Boolean =
        amount.isNotBlank() &&
            tenor.isNotBlank() &&
            purpose.isNotBlank() &&
            documents[DocumentType.KTP]?.isUploaded == true
}

/**
 * Simplified document info for preview
 */
data class DocumentPreviewInfo(
    val documentType: DocumentType,
    val fileName: String? = null,
    val isUploaded: Boolean = false,
)

/**
 * Convert from ApplyLoanFormState to LoanPreviewData
 */
fun com.loanfinancial.lofi.ui.features.loan.ApplyLoanFormState.toPreviewData(): LoanPreviewData =
    LoanPreviewData(
        amount = amount,
        tenor = tenor,
        purpose = purpose,
        documents =
            documents.mapValues { entry ->
                DocumentPreviewInfo(
                    documentType = entry.key,
                    fileName = entry.value.filePath?.substringAfterLast("/"),
                    isUploaded = entry.value.isUploaded,
                )
            },
        latitude = latitude,
        longitude = longitude,
        isBiometricVerified = isBiometricVerified,
    )
