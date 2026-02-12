package com.loanfinancial.lofi.domain.model

/**
 * Data class representing a loan step in the application process
 */
data class LoanStep(
    val status: String,
    val label: String,
    val date: String?,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
)

data class Loan(
    val id: String,
    val customerName: String,
    val product: Product,
    val loanAmount: Long,
    val tenor: Int,
    val loanStatus: String,
    val currentStage: String,
    val submittedAt: String?,
    val reviewedAt: String?,
    val approvedAt: String?,
    val rejectedAt: String?,
    val disbursedAt: String?,
    val loanStatusDisplay: String,
    val slaDurationHours: Int?,
    val pendingStatus: PendingSubmissionStatus? = null,
    val failureReason: String? = null,
    val disbursementReference: String? = null,
) {
    /**
     * Get all loan steps with their status and dates
     */
    fun getLoanSteps(): List<LoanStep> {
        val steps = mutableListOf<LoanStep>()

        // Step 1: Submitted
        steps.add(
            LoanStep(
                status = "SUBMITTED",
                label = "Pengajuan Dikirim",
                date = submittedAt,
                isCompleted = submittedAt != null,
                isCurrent = loanStatus == "SUBMITTED" || loanStatus == "DRAFT",
            ),
        )

        // Step 2: Review
        steps.add(
            LoanStep(
                status = "REVIEWED",
                label = "Sedang Ditinjau",
                date = reviewedAt,
                isCompleted = reviewedAt != null || approvedAt != null || disbursedAt != null,
                isCurrent = loanStatus == "REVIEWED" || loanStatus == "IN_REVIEW",
            ),
        )

        // Step 3: Approval
        steps.add(
            LoanStep(
                status = "APPROVED",
                label = if (rejectedAt != null) "Pengajuan Ditolak" else "Pengajuan Disetujui",
                date = approvedAt ?: rejectedAt,
                isCompleted = approvedAt != null || rejectedAt != null,
                isCurrent = loanStatus == "APPROVED" || loanStatus == "REJECTED",
            ),
        )

        // Step 4: Disbursement (only if approved)
        if (approvedAt != null || disbursedAt != null) {
            steps.add(
                LoanStep(
                    status = "DISBURSED",
                    label = "Dana Dicairkan",
                    date = disbursedAt,
                    isCompleted = disbursedAt != null,
                    isCurrent = loanStatus == "DISBURSED",
                ),
            )
        }

        return steps
    }
}

data class Product(
    val productCode: String,
    val productName: String,
    val interestRate: Double,
)

fun com.loanfinancial.lofi.data.model.dto.LoanDto.toDomain(): Loan =
    Loan(
        id = id,
        customerName = customerName,
        product =
            Product(
                productCode = product.productCode,
                productName = product.productName,
                interestRate = product.interestRate,
            ),
        loanAmount = loanAmount,
        tenor = tenor,
        loanStatus = loanStatus,
        currentStage = currentStage,
        submittedAt = submittedAt,
        reviewedAt = null, // Will be populated from API if available
        approvedAt = approvedAt,
        rejectedAt = rejectedAt,
        disbursedAt = disbursedAt,
        loanStatusDisplay =
            when (loanStatus) {
                "DRAFT" -> "Draft"
                "SUBMITTED" -> "Menunggu Review"
                "REVIEWED", "IN_REVIEW" -> "Sedang Ditinjau"
                "APPROVED" -> "Disetujui"
                "REJECTED" -> "Ditolak"
                "DISBURSED" -> "Dana Dicairkan"
                else -> loanStatus
            },
        slaDurationHours = slaDurationHours,
        disbursementReference = disbursementReference,
    )

/**
 * Extension function to parse and format loan dates
 */
fun String?.formatLoanDate(): String? {
    if (this == null) return null
    return try {
        // Try parsing ISO 8601 format
        val inputFormatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
        val outputFormatter =
            java.time.format.DateTimeFormatter
                .ofPattern("dd MMMM yyyy, HH:mm")
        val dateTime = java.time.LocalDateTime.parse(this, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        // If parsing fails, return original string
        this
    }
}
