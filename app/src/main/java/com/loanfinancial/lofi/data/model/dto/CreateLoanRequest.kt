package com.loanfinancial.lofi.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Request DTO for creating a new loan application (POST /loans)
 */
data class CreateLoanRequest(
    @SerializedName("loanAmount")
    val loanAmount: Long,
    @SerializedName("tenor")
    val tenor: Int,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("declaredIncome")
    val declaredIncome: Long? = null,
    @SerializedName("npwpNumber")
    val npwpNumber: String? = null,
    @SerializedName("jobType")
    val jobType: String? = null,
    @SerializedName("companyName")
    val companyName: String? = null,
    @SerializedName("jobPosition")
    val jobPosition: String? = null,
    @SerializedName("workDurationMonths")
    val workDurationMonths: Int? = null,
    @SerializedName("workAddress")
    val workAddress: String? = null,
    @SerializedName("officePhoneNumber")
    val officePhoneNumber: String? = null,
    @SerializedName("additionalIncome")
    val additionalIncome: Long? = null,
    @SerializedName("emergencyContactName")
    val emergencyContactName: String? = null,
    @SerializedName("emergencyContactRelation")
    val emergencyContactRelation: String? = null,
    @SerializedName("emergencyContactPhone")
    val emergencyContactPhone: String? = null,
    @SerializedName("emergencyContactAddress")
    val emergencyContactAddress: String? = null,
    @SerializedName("downPayment")
    val downPayment: Long? = null,
    @SerializedName("purpose")
    val purpose: String,
    @SerializedName("bankName")
    val bankName: String? = null,
    @SerializedName("bankBranch")
    val bankBranch: String? = null,
    @SerializedName("accountNumber")
    val accountNumber: String? = null,
    @SerializedName("accountHolderName")
    val accountHolderName: String? = null,
)

enum class JobType {
    KARYAWAN,
    WIRASWASTA,
    PENGUSAHA,
    PNS,
    TNI,
    POLRI,
    PROFESIONAL,
    LAINNYA
}
