package com.loanfinancial.lofi.data.model.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id") val id: String,
    @SerializedName("productCode") val productCode: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("interestRate") val interestRate: Double,
    @SerializedName("adminFee") val adminFee: Double?,
    @SerializedName("minTenor") val minTenor: Int?,
    @SerializedName("maxTenor") val maxTenor: Int?,
    @SerializedName("minLoanAmount") val minLoanAmount: Double?,
    @SerializedName("maxLoanAmount") val maxLoanAmount: Double?,
    @SerializedName("isActive") val isActive: Boolean?,
)
