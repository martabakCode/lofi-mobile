package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.loanfinancial.lofi.data.model.dto.ProductDto

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val productCode: String,
    val productName: String,
    val description: String?,
    val interestRate: Double,
    val adminFee: Double?,
    val minTenor: Int?,
    val maxTenor: Int?,
    val minLoanAmount: Double?,
    val maxLoanAmount: Double?,
    val isActive: Boolean?,
) {
    fun toDto(): ProductDto =
        ProductDto(
            id = id,
            productCode = productCode,
            productName = productName,
            description = description,
            interestRate = interestRate,
            adminFee = adminFee,
            minTenor = minTenor,
            maxTenor = maxTenor,
            minLoanAmount = minLoanAmount,
            maxLoanAmount = maxLoanAmount,
            isActive = isActive,
        )
}

fun ProductDto.toEntity(): ProductEntity =
    ProductEntity(
        id = id,
        productCode = productCode,
        productName = productName,
        description = description,
        interestRate = interestRate,
        adminFee = adminFee,
        minTenor = minTenor,
        maxTenor = maxTenor,
        minLoanAmount = minLoanAmount,
        maxLoanAmount = maxLoanAmount,
        isActive = isActive,
    )
