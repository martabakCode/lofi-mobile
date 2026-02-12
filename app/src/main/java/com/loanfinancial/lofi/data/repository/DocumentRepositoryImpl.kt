package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.common.result.ErrorType
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.model.dto.DocumentUploadResult
import com.loanfinancial.lofi.data.model.dto.PresignUploadRequest
import com.loanfinancial.lofi.data.model.dto.PresignUploadResponse
import com.loanfinancial.lofi.data.remote.api.DocumentApi
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

import com.loanfinancial.lofi.data.model.entity.PendingDocumentUploadEntity
import com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager

class DocumentRepositoryImpl @Inject constructor(
    private val documentApi: DocumentApi,
    private val pendingUploadDao: com.loanfinancial.lofi.data.local.dao.PendingDocumentUploadDao,
    private val cameraManager: CameraManager,
    private val dataStoreManager: DataStoreManager,
) : IDocumentRepository {

    override suspend fun requestPresignUpload(
        loanId: String,
        fileName: String,
        documentType: DocumentType,
        contentType: String
    ): BaseResult<PresignUploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = PresignUploadRequest(
                    loanId = loanId,
                    fileName = fileName,
                    documentType = documentType.backendName,
                    contentType = contentType
                )
                val response = documentApi.requestPresignUpload(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        BaseResult.Success(body.data)
                    } else {
                        BaseResult.Error(
                            ErrorType.BusinessError(
                                code = "API_ERROR",
                                message = body?.message ?: "Unknown API error"
                            )
                        )
                    }
                } else {
                    BaseResult.Error(
                        ErrorType.ServerError(
                            code = response.code(),
                            message = response.message()
                        )
                    )
                }
            } catch (e: Exception) {
                BaseResult.Error(ErrorType.NetworkError(e.message ?: "Network error"))
            }
        }
    }

    override suspend fun uploadFileToUrl(
        uploadUrl: String,
        filePath: String,
        contentType: String
    ): BaseResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext BaseResult.Error(
                        ErrorType.ValidationError(mapOf("file" to "File does not exist"))
                    )
                }

                val mediaType = contentType.toMediaTypeOrNull()
                val requestBody = file.asRequestBody(mediaType)
                
                val response = documentApi.uploadToPresignedUrl(uploadUrl, requestBody)
                
                if (response.isSuccessful) {
                    BaseResult.Success(Unit)
                } else {
                    BaseResult.Error(
                        ErrorType.ServerError(
                            code = response.code(),
                            message = response.message()
                        )
                    )
                }
            } catch (e: Exception) {
                BaseResult.Error(ErrorType.UnknownError(e.message ?: "Upload failed"))
            }
        }
    }

    override suspend fun uploadDocument(
        loanId: String,
        filePath: String,
        documentType: DocumentType
    ): BaseResult<DocumentUploadResult> {
        val file = File(filePath)
        if (!file.exists()) {
            return BaseResult.Error(
                ErrorType.ValidationError(mapOf("file" to "File does not exist"))
            )
        }

        // Default to image/jpeg as CameraManager produces jpg
        val contentType = "image/jpeg"

        return when (val presignResult = requestPresignUpload(loanId, file.name, documentType, contentType)) {
            is BaseResult.Success -> {
                val presignData = presignResult.data
                when (val uploadResult = uploadFileToUrl(presignData.uploadUrl, filePath, contentType)) {
                    is BaseResult.Success -> {
                        BaseResult.Success(
                            DocumentUploadResult(
                                documentType = documentType,
                                documentId = presignData.documentId,
                                objectKey = presignData.objectKey,
                                isUploaded = true
                            )
                        )
                    }
                    is BaseResult.Error -> BaseResult.Error(uploadResult.error)
                    is BaseResult.Loading -> BaseResult.Loading
                }
            }
            is BaseResult.Error -> BaseResult.Error(presignResult.error)
            is BaseResult.Loading -> BaseResult.Loading
        }
    }

    override suspend fun queueDocumentUpload(
        loanDraftId: String,
        filePath: String,
        documentType: DocumentType,
        shouldCompress: Boolean
    ): BaseResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    return@withContext BaseResult.Error(
                        ErrorType.ValidationError(mapOf("file" to "File does not exist"))
                    )
                }

                // Compress if needed
                val finalFilePath = if (shouldCompress && file.length() > 1024 * 1024) { // > 1MB
                    cameraManager.compressImage(filePath, 1024)
                } else {
                    filePath
                }

                val userId = dataStoreManager.getUserId() ?: return@withContext BaseResult.Error(
                    ErrorType.ValidationError(mapOf("user" to "Not logged in"))
                )

                val entity = PendingDocumentUploadEntity(
                    loanDraftId = loanDraftId,
                    userId = userId,
                    documentType = documentType.name,
                    localFilePath = filePath,
                    compressedFilePath = if (finalFilePath != filePath) finalFilePath else null,
                    fileName = file.name,
                    contentType = "image/jpeg",
                    originalFileSize = file.length(),
                    compressedFileSize = if (finalFilePath != filePath) File(finalFilePath).length() else file.length(),
                    isCompressed = finalFilePath != filePath,
                    status = DocumentUploadStatus.PENDING.name
                )

                pendingUploadDao.insertPendingUpload(entity)
                BaseResult.Success(entity.id)
            } catch (e: Exception) {
                BaseResult.Error(ErrorType.UnknownError(e.message ?: "Failed to queue upload"))
            }
        }
    }

    override suspend fun getPendingUploads(loanDraftId: String): List<PendingDocumentUploadEntity> {
        return pendingUploadDao.getPendingForDraft(loanDraftId)
    }

    override fun getPendingUploadsFlow(loanDraftId: String): kotlinx.coroutines.flow.Flow<List<PendingDocumentUploadEntity>> {
        return pendingUploadDao.getPendingForDraftFlow(loanDraftId)
    }

    override suspend fun areAllDocumentsUploaded(loanDraftId: String): Boolean {
        val uploads = pendingUploadDao.getPendingForDraft(loanDraftId)
        if (uploads.isEmpty()) return false
        return uploads.all { it.status == DocumentUploadStatus.COMPLETED.name }
    }
}
