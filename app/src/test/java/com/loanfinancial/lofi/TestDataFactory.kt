package com.loanfinancial.lofi

import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.User

object TestDataFactory {
    fun createUser(
        id: String = "user_123",
        email: String = "test@example.com",
        name: String = "Test User",
        phone: String = "+6281234567890",
    ): User =
        User(
            id = id,
            email = email,
            name = name,
            phone = phone,
        )

    fun createLoan(
        id: String = "loan_123",
        amount: Double = 5000000.0,
        tenor: Int = 12,
        purpose: String = "Business",
        status: String = "PENDING",
    ): Loan =
        Loan(
            id = id,
            amount = amount,
            tenor = tenor,
            purpose = purpose,
            status = status,
        )

    fun createLoanList(count: Int = 3): List<Loan> =
        (1..count).map { index ->
            createLoan(
                id = "loan_$index",
                amount = (index * 1000000).toDouble(),
                tenor = index * 6,
                purpose = "Purpose $index",
            )
        }

    const val TEST_ACCESS_TOKEN = "test_access_token_12345"
    const val TEST_REFRESH_TOKEN = "test_refresh_token_67890"
    const val TEST_FCM_TOKEN = "test_fcm_token_abcde"
}
