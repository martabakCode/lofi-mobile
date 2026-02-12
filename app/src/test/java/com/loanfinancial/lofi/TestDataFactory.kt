package com.loanfinancial.lofi

import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product

object TestDataFactory {
    fun createLoan(
        id: String = "loan_123",
        amount: Long = 5000000,
        tenor: Int = 12,
        customerName: String = "Test Customer",
        status: String = "SUBMITTED",
        loanStatusDisplay: String = "Menunggu Review",
    ): Loan =
        Loan(
            id = id,
            customerName = customerName,
            product = Product(
                productCode = "CASH_LOAN",
                productName = "Pinjaman Tunai",
                interestRate = 0.05,
            ),
            loanAmount = amount,
            tenor = tenor,
            loanStatus = status,
            currentStage = "SUBMISSION",
            submittedAt = "2024-01-15T10:30:00Z",
            reviewedAt = null,
            approvedAt = null,
            rejectedAt = null,
            disbursedAt = null,
            loanStatusDisplay = loanStatusDisplay,
            slaDurationHours = 24,
            disbursementReference = null
        )

    fun createLoanList(count: Int = 3): List<Loan> =
        (1..count).map { index ->
            createLoan(
                id = "loan_$index",
                amount = (index * 1000000).toLong(),
                tenor = index * 6,
                customerName = "Customer $index",
                status = if (index % 2 == 0) "APPROVED" else "SUBMITTED",
                loanStatusDisplay = if (index % 2 == 0) "Disetujui" else "Menunggu Review"
            )
        }

    const val TEST_ACCESS_TOKEN = "test_access_token_12345"
    const val TEST_REFRESH_TOKEN = "test_refresh_token_67890"
    const val TEST_FCM_TOKEN = "test_fcm_token_abcde"
}
