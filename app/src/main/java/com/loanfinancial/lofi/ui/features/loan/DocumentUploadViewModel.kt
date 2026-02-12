package com.loanfinancial.lofi.ui.features.loan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.common.result.getErrorMessage
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.domain.usecase.document.UploadMultipleDocumentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import android.net.Uri
import javax.inject.Inject

import com.loanfinancial.lofi.domain.usecase.document.QueueDocumentUploadUseCase
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus

@HiltViewModel
class DocumentUploadViewModel @Inject constructor(
    private val queueDocumentUploadUseCase: QueueDocumentUploadUseCase,
    private val documentRepository: IDocumentRepository,
    private val cameraManager: CameraManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUploadUiState())
    val uiState: StateFlow<DocumentUploadUiState> = _uiState.asStateFlow()

    fun observeUploadStatus(loanId: String) {
        viewModelScope.launch {
            documentRepository.getPendingUploadsFlow(loanId).collect { uploads ->
                _uiState.update { state ->
                    val updatedDocuments = state.documents.toMutableMap()
                    uploads.forEach { upload ->
                        val docType = try {
                            DocumentType.valueOf(upload.documentType)
                        } catch (e: Exception) {
                            return@forEach
                        }
                        
                        updatedDocuments[docType] = (updatedDocuments[docType] ?: DocumentState()).copy(
                            filePath = upload.localFilePath,
                            isUploading = upload.status == DocumentUploadStatus.UPLOADING.name,
                            isUploaded = upload.status == DocumentUploadStatus.COMPLETED.name,
                            error = if (upload.status == DocumentUploadStatus.FAILED.name) upload.failureReason else null
                        )
                    }
                    
                    // Check if all required docs have been queued (have a file path)
                    val requiredTypes = listOf(
                        DocumentType.KTP, DocumentType.NPWP, DocumentType.KK,
                        DocumentType.PAYSLIP, DocumentType.PROOFOFRESIDENCE, DocumentType.BANK_STATEMENT
                    )
                    val allQueued = requiredTypes.all { updatedDocuments[it]?.filePath != null }
                    
                    state.copy(
                        documents = updatedDocuments,
                        isAllUploaded = updatedDocuments.isNotEmpty() && 
                                       updatedDocuments.values.all { it.isUploaded },
                        isAllQueued = allQueued
                    )
                }
            }
        }
    }

    fun onDocumentSelected(loanId: String, documentType: DocumentType, filePath: String) {
        _uiState.update { state ->
            val updatedDocuments = state.documents.toMutableMap()
            updatedDocuments[documentType] = updatedDocuments[documentType]?.copy(
                filePath = filePath,
                isUploading = false, // Will be updated by flow
                error = null
            ) ?: DocumentState(filePath = filePath)
            
            // Check if all required docs have been queued
            val requiredTypes = listOf(
                DocumentType.KTP, DocumentType.NPWP, DocumentType.KK,
                DocumentType.PAYSLIP, DocumentType.PROOFOFRESIDENCE, DocumentType.BANK_STATEMENT
            )
            val allQueued = requiredTypes.all { updatedDocuments[it]?.filePath != null }
            
            state.copy(documents = updatedDocuments, isAllQueued = allQueued)
        }
        
        // Queue document for offline upload
        viewModelScope.launch {
            queueDocumentUploadUseCase(loanId, filePath, documentType)
        }
    }

    fun onRetryUpload(loanId: String, documentType: DocumentType) {
        val state = _uiState.value
        val docState = state.documents[documentType]
        if (docState?.filePath != null) {
            viewModelScope.launch {
                queueDocumentUploadUseCase(loanId, docState.filePath, documentType)
            }
        }
    }

    fun createTempFileUri(documentType: DocumentType): Pair<File, Uri> {
        val file = cameraManager.createTempImageFile(documentType)
        val uri = cameraManager.getUriForFile(file)
        return file to uri
    }
}
