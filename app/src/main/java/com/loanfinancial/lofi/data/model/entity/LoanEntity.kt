package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val customerName: String,
    val productCode: String,
    val productName: String,
    val interestRate: Double,
    val loanAmount: Long,
    val tenor: Int,
    val loanStatus: String,
    val currentStage: String,
    val submittedAt: String?,
    val loanStatusDisplay: String,
)

fun LoanEntity.toDomain(): Loan =
    Loan(
        id = id,
        customerName = customerName,
        product =
            Product(
                productCode = productCode,
                productName = productName,
                interestRate = interestRate,
            ),
        loanAmount = loanAmount,
        tenor = tenor,
        loanStatus = loanStatus,
        currentStage = currentStage,
        submittedAt = submittedAt,
        loanStatusDisplay = loanStatusDisplay,
    )

fun Loan.toEntity(): LoanEntity =
    LoanEntity(
        id = id,
        customerName = customerName,
        productCode = product.productCode,
        productName = product.productName,
        interestRate = product.interestRate,
        loanAmount = loanAmount,
        tenor = tenor,
        loanStatus = loanStatus,
        currentStage = currentStage,
        submittedAt = submittedAt,
        loanStatusDisplay = loanStatusDisplay,
    )
