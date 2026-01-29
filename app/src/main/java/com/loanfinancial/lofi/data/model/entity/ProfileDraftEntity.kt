package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.loanfinancial.lofi.data.model.dto.UserUpdateRequest

@Entity(tableName = "profile_drafts")
data class ProfileDraftEntity(
    @PrimaryKey
    val userId: String,
    val fullName: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val incomeSource: String = "",
    val incomeType: String = "",
    val monthlyIncome: Double = 0.0,
    val age: Int = 0,
    val nik: String = "",
    val dateOfBirth: String = "",
    val placeOfBirth: String = "",
    val city: String = "",
    val address: String = "",
    val province: String = "",
    val district: String = "",
    val subDistrict: String = "",
    val postalCode: String = "",
    val gender: String = "MALE",
    val maritalStatus: String = "SINGLE",
    val occupation: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
) {
    fun toUpdateRequest(): UserUpdateRequest =
        UserUpdateRequest(
            fullName = fullName,
            phoneNumber = phoneNumber,
            incomeSource = incomeSource,
            incomeType = incomeType,
            monthlyIncome = monthlyIncome,
            nik = nik,
            dateOfBirth = dateOfBirth,
            placeOfBirth = placeOfBirth,
            city = city,
            address = address,
            province = province,
            district = district,
            subDistrict = subDistrict,
            postalCode = postalCode,
            gender = gender,
            maritalStatus = maritalStatus,
            occupation = occupation,
        )
}
