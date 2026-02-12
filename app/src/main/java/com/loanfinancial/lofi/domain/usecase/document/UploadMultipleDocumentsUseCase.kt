package com.loanfinancial.lofi.domain.usecase.document

import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.model.dto.DocumentUploadResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UploadMultipleDocumentsUseCase @Inject constructor(
    private val uploadDocumentUseCase: UploadDocumentUseCase
) {
    suspend operator fun invoke(
        loanId: String,
        documents: Map<DocumentType, String>
    ): Flow<Map<DocumentType, BaseResult<DocumentUploadResult>>> = flow {
        val currentResults = mutableMapOf<DocumentType, BaseResult<DocumentUploadResult>>()
        
        // Initialize all as Loading or just idle? 
        // If we emit Loading for all initially, the UI shows loaders for all.
        documents.keys.forEach { key ->
            currentResults[key] = BaseResult.Loading
        }
        emit(currentResults.toMap())

        // Upload sequentially for simplicity and to avoid overwhelming network/server
        documents.forEach { (type, path) ->
            uploadDocumentUseCase(loanId, path, type).collect { result ->
                currentResults[type] = result
                emit(currentResults.toMap())
            }
        }
    }
}
