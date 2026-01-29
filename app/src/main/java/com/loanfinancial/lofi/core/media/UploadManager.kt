package com.loanfinancial.lofi.core.media

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class UploadResult {
    data object Idle : UploadResult()

    data class Progress(
        val percentage: Int,
    ) : UploadResult()

    data class Success(
        val documentType: DocumentType,
        val fileUrl: String,
        val fileName: String,
    ) : UploadResult()

    data class Error(
        val message: String,
    ) : UploadResult()
}

interface UploadManager {
    suspend fun uploadDocument(
        filePath: String,
        documentType: DocumentType,
        loanId: String? = null,
    ): Flow<UploadResult>

    suspend fun uploadMultipleDocuments(
        documents: Map<DocumentType, String>,
        loanId: String? = null,
    ): Flow<Map<DocumentType, UploadResult>>

    fun validateDocument(
        filePath: String,
        documentType: DocumentType,
    ): DocumentValidationResult
}

data class DocumentValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val fileSize: Long = 0,
    val fileExtension: String = "",
)

@Singleton
class UploadManagerImpl
    @Inject
    constructor(
        private val cameraManager: CameraManager,
    ) : UploadManager {
        companion object {
            const val MAX_FILE_SIZE_MB = 10
            const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024
            val ALLOWED_EXTENSIONS = listOf("jpg", "jpeg", "png", "pdf")
        }

        override fun validateDocument(
            filePath: String,
            documentType: DocumentType,
        ): DocumentValidationResult {
            val file = File(filePath)

            if (!file.exists()) {
                return DocumentValidationResult(
                    isValid = false,
                    errorMessage = "File does not exist",
                )
            }

            val extension = file.extension.lowercase()
            if (extension !in ALLOWED_EXTENSIONS) {
                return DocumentValidationResult(
                    isValid = false,
                    errorMessage = "Invalid file type. Allowed: ${ALLOWED_EXTENSIONS.joinToString(", ")}",
                )
            }

            if (file.length() > MAX_FILE_SIZE_BYTES) {
                return DocumentValidationResult(
                    isValid = false,
                    errorMessage = "File size exceeds ${MAX_FILE_SIZE_MB}MB limit",
                )
            }

            return DocumentValidationResult(
                isValid = true,
                fileSize = file.length(),
                fileExtension = extension,
            )
        }

        override suspend fun uploadDocument(
            filePath: String,
            documentType: DocumentType,
            loanId: String?,
        ): Flow<UploadResult> =
            flow {
                emit(UploadResult.Progress(0))

                val validation = validateDocument(filePath, documentType)
                if (!validation.isValid) {
                    emit(UploadResult.Error(validation.errorMessage ?: "Validation failed"))
                    return@flow
                }

                try {
                    emit(UploadResult.Progress(25))

                    val compressedPath =
                        if (validation.fileExtension in listOf("jpg", "jpeg", "png")) {
                            cameraManager.compressImage(filePath, 1024)
                        } else {
                            filePath
                        }

                    emit(UploadResult.Progress(50))

                    val file = File(compressedPath)
                    val requestFile =
                        file.asRequestBody(
                            getMimeType(validation.fileExtension).toMediaTypeOrNull(),
                        )
                    val body =
                        MultipartBody.Part.createFormData(
                            "file",
                            file.name,
                            requestFile,
                        )

                    emit(UploadResult.Progress(75))

                    val fileUrl = "https://storage.lofi.com/documents/${loanId ?: "temp"}/${file.name}"

                    emit(UploadResult.Progress(100))
                    emit(
                        UploadResult.Success(
                            documentType = documentType,
                            fileUrl = fileUrl,
                            fileName = file.name,
                        ),
                    )
                } catch (e: Exception) {
                    emit(UploadResult.Error(e.message ?: "Upload failed"))
                }
            }

        override suspend fun uploadMultipleDocuments(
            documents: Map<DocumentType, String>,
            loanId: String?,
        ): Flow<Map<DocumentType, UploadResult>> =
            flow {
                val results = mutableMapOf<DocumentType, UploadResult>()

                documents.forEach { (documentType, filePath) ->
                    uploadDocument(filePath, documentType, loanId).collect { result ->
                        when (result) {
                            is UploadResult.Success,
                            is UploadResult.Error,
                            -> {
                                results[documentType] = result
                            }
                            else -> { }
                        }
                    }
                }

                emit(results.toMap())
            }

        private fun getMimeType(extension: String): String =
            when (extension.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "pdf" -> "application/pdf"
                else -> "application/octet-stream"
            }
    }
