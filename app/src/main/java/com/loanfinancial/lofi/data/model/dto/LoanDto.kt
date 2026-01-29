package com.loanfinancial.lofi.data.model.dto

import com.google.gson.annotations.SerializedName

data class LoanDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("customerName")
    val customerName: String,
    @SerializedName("product")
    val product: ProductDto,
    @SerializedName("loanAmount")
    val loanAmount: Long,
    @SerializedName("tenor")
    val tenor: Int,
    @SerializedName("loanStatus")
    val loanStatus: String,
    @SerializedName("currentStage")
    val currentStage: String,
    @SerializedName("submittedAt")
    val submittedAt: String?,
    @SerializedName("approvedAt")
    val approvedAt: String?,
    @SerializedName("rejectedAt")
    val rejectedAt: String?,
    @SerializedName("disbursedAt")
    val disbursedAt: String?,
    @SerializedName("documents")
    val documents: List<DocumentDto>?,
    @SerializedName("disbursementReference")
    val disbursementReference: String?,
    @SerializedName("aiAnalysis")
    val aiAnalysis: AiAnalysisDto?,
)

// ProductDto removed and moved to its own file

data class DocumentDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("documentType")
    val documentType: String,
    @SerializedName("uploadedAt")
    val uploadedAt: String,
)

data class AiAnalysisDto(
    @SerializedName("confidence")
    val confidence: Double,
    @SerializedName("summary")
    val summary: String,
    @SerializedName("riskFlags")
    val riskFlags: List<String>?,
    @SerializedName("reviewNotes")
    val reviewNotes: List<String>?,
    @SerializedName("limitations")
    val limitations: List<String>?,
)
