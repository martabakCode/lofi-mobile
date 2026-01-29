package com.loanfinancial.lofi.domain.model

data class Loan(
    val id: String,
    val customerName: String,
    val product: Product,
    val loanAmount: Long,
    val tenor: Int,
    val loanStatus: String,
    val currentStage: String,
    val submittedAt: String?,
    val loanStatusDisplay: String,
)

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
        loanStatusDisplay =
            when (loanStatus) {
                "DRAFT" -> "Draft"
                "REVIEWED" -> "Reviewed"
                "APPROVED" -> "Approved"
                "DISBURSED" -> "Disbursed"
                else -> loanStatus
            },
    )
