package com.loanfinancial.lofi.domain.usecase.document

import android.content.Context
import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.worker.DocumentUploadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class QueueDocumentUploadUseCase
    @Inject
    constructor(
        private val documentRepository: IDocumentRepository,
        @ApplicationContext private val context: Context,
    ) {
        suspend operator fun invoke(
            loanDraftId: String,
            filePath: String,
            documentType: DocumentType,
        ): BaseResult<String> {
            val result = documentRepository.queueDocumentUpload(loanDraftId, filePath, documentType)
            if (result is BaseResult.Success) {
                // Schedule the background worker to handle the upload
                DocumentUploadWorker.scheduleForDraft(context, loanDraftId)
            }
            return result
        }
    }
