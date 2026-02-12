package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.loanfinancial.lofi.domain.model.Loan
import com.loanfinancial.lofi.domain.model.Product

import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val customerName: String,
    val productCode: String,
    val productName: String,
    val interestRate: Double,
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
    val disbursementReference: String?,
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
        reviewedAt = reviewedAt,
        approvedAt = approvedAt,
        rejectedAt = rejectedAt,
        disbursedAt = disbursedAt,
        loanStatusDisplay = loanStatusDisplay,
        slaDurationHours = slaDurationHours,
        disbursementReference = disbursementReference,
    )

fun Loan.toEntity(userId: String): LoanEntity =
    LoanEntity(
        id = id,
        userId = userId,
        customerName = customerName,
        productCode = product.productCode,
        productName = product.productName,
        interestRate = product.interestRate,
        loanAmount = loanAmount,
        tenor = tenor,
        loanStatus = loanStatus,
        currentStage = currentStage,
        submittedAt = submittedAt,
        reviewedAt = reviewedAt,
        approvedAt = approvedAt,
        rejectedAt = rejectedAt,
        disbursedAt = disbursedAt,
        loanStatusDisplay = loanStatusDisplay,
        slaDurationHours = slaDurationHours,
        disbursementReference = disbursementReference,
    )
