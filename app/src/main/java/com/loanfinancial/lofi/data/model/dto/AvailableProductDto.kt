package com.loanfinancial.lofi.data.model.dto

import com.google.gson.annotations.SerializedName

data class AvailableProductDto(
    @SerializedName("productId") val productId: String,
    @SerializedName("productCode") val productCode: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("productLimit") val productLimit: Double,
    @SerializedName("approvedLoanAmount") val approvedLoanAmount: Double,
    @SerializedName("availableAmount") val availableAmount: Double,
    @SerializedName("hasSubmittedLoan") val hasSubmittedLoan: Boolean,
    @SerializedName("lastLoanStatus") val lastLoanStatus: String?,
    @SerializedName("lastLoanSubmittedAt") val lastLoanSubmittedAt: String?,
)
