package com.loanfinancial.lofi.data.model.dto

import com.google.gson.annotations.SerializedName
import com.loanfinancial.lofi.core.media.DocumentType

data class PresignUploadRequest(
    @SerializedName("loanId")
    val loanId: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("documentType")
    val documentType: String,
    @SerializedName("contentType")
    val contentType: String,
)

data class PresignUploadResponse(
    @SerializedName("documentId")
    val documentId: String,
    @SerializedName("uploadUrl")
    val uploadUrl: String,
    @SerializedName("objectKey")
    val objectKey: String,
)

data class DocumentUploadResult(
    val documentType: DocumentType,
    val documentId: String,
    val objectKey: String,
    val isUploaded: Boolean,
)

data class DownloadDocumentResponse(
    @SerializedName("downloadUrl")
    val downloadUrl: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("contentType")
    val contentType: String? = null,
)
