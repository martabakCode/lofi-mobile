package com.loanfinancial.lofi.data.model.dto

import com.google.gson.annotations.SerializedName

data class UserUpdateRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("incomeSource") val incomeSource: String,
    @SerializedName("incomeType") val incomeType: String,
    @SerializedName("monthlyIncome") val monthlyIncome: Double,
    @SerializedName("nik") val nik: String,
    @SerializedName("dateOfBirth") val dateOfBirth: String,
    @SerializedName("placeOfBirth") val placeOfBirth: String,
    @SerializedName("city") val city: String,
    @SerializedName("address") val address: String,
    @SerializedName("province") val province: String,
    @SerializedName("district") val district: String,
    @SerializedName("subDistrict") val subDistrict: String,
    @SerializedName("postalCode") val postalCode: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("maritalStatus") val maritalStatus: String,
    @SerializedName("occupation") val occupation: String,
)

data class UserUpdateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("data") val data: UserUpdateData? = null,
    @SerializedName("errors") val errors: Any? = null,
)

data class UserUpdateData(
    @SerializedName("id") val id: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("profilePictureUrl") val profilePictureUrl: String?,
    @SerializedName("branch") val branch: BranchDto?,
    @SerializedName("biodata") val biodata: UserBiodataDto?,
    @SerializedName("product") val product: ProductDto?,
    @SerializedName("pinSet") val pinSet: Boolean?,
    @SerializedName("profileCompleted") val profileCompleted: Boolean?,
)

data class BranchDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
)

data class UserBiodataDto(
    @SerializedName("incomeSource") val incomeSource: String,
    @SerializedName("incomeType") val incomeType: String,
    @SerializedName("monthlyIncome") val monthlyIncome: Double,
    @SerializedName("nik") val nik: String,
    @SerializedName("dateOfBirth") val dateOfBirth: String,
    @SerializedName("placeOfBirth") val placeOfBirth: String,
    @SerializedName("city") val city: String,
    @SerializedName("address") val address: String,
    @SerializedName("province") val province: String,
    @SerializedName("district") val district: String,
    @SerializedName("subDistrict") val subDistrict: String,
    @SerializedName("postalCode") val postalCode: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("maritalStatus") val maritalStatus: String,
    @SerializedName("education") val education: String,
    @SerializedName("occupation") val occupation: String,
)
