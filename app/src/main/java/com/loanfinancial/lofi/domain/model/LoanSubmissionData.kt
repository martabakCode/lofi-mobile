package com.loanfinancial.lofi.domain.model

data class LoanSubmissionData(
    val customerName: String,
    val productCode: String,
    val productName: String,
    val interestRate: Double,
    val loanAmount: Long,
    val tenor: Int,
    val purpose: String,
    val downPayment: Long? = null,
    val latitude: Double,
    val longitude: Double,
    // Employment Info
    val jobType: String? = null,
    val companyName: String? = null,
    val jobPosition: String? = null,
    val workDurationMonths: Int? = null,
    val workAddress: String? = null,
    val officePhoneNumber: String? = null,
    val declaredIncome: Long? = null,
    val additionalIncome: Long? = null,
    val npwpNumber: String? = null,
    // Emergency Contact
    val emergencyContactName: String? = null,
    val emergencyContactRelation: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactAddress: String? = null,
    // Bank Info
    val bankName: String? = null,
    val bankBranch: String? = null,
    val accountNumber: String? = null,
    val accountHolderName: String? = null,
    // DocumentType -> FilePath
    val documentPaths: Map<String, String>,
)
