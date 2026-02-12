package com.loanfinancial.lofi.domain.repository

import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.model.dto.DocumentUploadResult
import com.loanfinancial.lofi.data.model.dto.PresignUploadResponse

interface IDocumentRepository {
    suspend fun requestPresignUpload(
        loanId: String,
        fileName: String,
        documentType: DocumentType,
        contentType: String
    ): BaseResult<PresignUploadResponse>

    suspend fun uploadFileToUrl(
        uploadUrl: String,
        filePath: String,
        contentType: String
    ): BaseResult<Unit>

    suspend fun uploadDocument(
        loanId: String,
        filePath: String,
        documentType: DocumentType
    ): BaseResult<DocumentUploadResult>

    suspend fun queueDocumentUpload(
        loanDraftId: String,
        filePath: String,
        documentType: DocumentType,
        shouldCompress: Boolean = true
    ): BaseResult<String>

    suspend fun getPendingUploads(loanDraftId: String): List<com.loanfinancial.lofi.data.model.entity.PendingDocumentUploadEntity>
    
    fun getPendingUploadsFlow(loanDraftId: String): kotlinx.coroutines.flow.Flow<List<com.loanfinancial.lofi.data.model.entity.PendingDocumentUploadEntity>>

    suspend fun areAllDocumentsUploaded(loanDraftId: String): Boolean
}
