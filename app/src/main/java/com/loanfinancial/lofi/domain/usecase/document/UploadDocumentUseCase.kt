package com.loanfinancial.lofi.domain.usecase.document

import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.model.dto.DocumentUploadResult
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UploadDocumentUseCase @Inject constructor(
    private val documentRepository: IDocumentRepository
) {
    suspend operator fun invoke(
        loanId: String,
        filePath: String,
        documentType: DocumentType
    ): Flow<BaseResult<DocumentUploadResult>> = flow {
        emit(BaseResult.Loading)
        val result = documentRepository.uploadDocument(loanId, filePath, documentType)
        emit(result)
    }
}
