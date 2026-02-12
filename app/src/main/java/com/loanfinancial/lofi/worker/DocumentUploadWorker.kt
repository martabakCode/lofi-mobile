package com.loanfinancial.lofi.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.local.dao.PendingDocumentUploadDao
import com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DocumentUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val documentRepository: IDocumentRepository,
    private val pendingUploadDao: PendingDocumentUploadDao
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_TAG = "document_upload"
        const val KEY_LOAN_DRAFT_ID = "loan_draft_id"
        const val MAX_RETRY_COUNT = 3
        const val RETRY_INTERVAL_MINUTES = 15L

        fun scheduleForDraft(context: Context, loanDraftId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DocumentUploadWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_LOAN_DRAFT_ID to loanDraftId))
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    RETRY_INTERVAL_MINUTES,
                    TimeUnit.MINUTES
                )
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "upload_$loanDraftId",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val loanDraftId = inputData.getString(KEY_LOAN_DRAFT_ID) ?: return Result.failure()
        val pendingUploads = pendingUploadDao.getPendingForDraft(loanDraftId)
            .filter { it.status != DocumentUploadStatus.COMPLETED.name }

        if (pendingUploads.isEmpty()) return Result.success()

        var allSuccess = true
        var hasRetryableError = false
        
        for (upload in pendingUploads) {
            val success = processUpload(upload)
            if (!success) {
                allSuccess = false
                // Check if this upload can be retried
                if (upload.retryCount < MAX_RETRY_COUNT) {
                    hasRetryableError = true
                }
            }
        }

        return when {
            allSuccess -> Result.success()
            hasRetryableError -> Result.retry()
            else -> Result.failure()
        }
    }

    private suspend fun processUpload(upload: com.loanfinancial.lofi.data.model.entity.PendingDocumentUploadEntity): Boolean {
        pendingUploadDao.updateStatus(upload.id, DocumentUploadStatus.UPLOADING.name)

        // Use compressed file if available
        val filePath = upload.compressedFilePath ?: upload.localFilePath
        
        // Use loanDraftId as temporary loanId if loanId is null (as per plan phase 2)
        val targetLoanId = upload.loanId ?: upload.loanDraftId
        
        val result = documentRepository.uploadDocument(
            loanId = targetLoanId,
            filePath = filePath,
            documentType = DocumentType.valueOf(upload.documentType)
        )

        return when (result) {
            is BaseResult.Success -> {
                pendingUploadDao.updateCompleted(
                    id = upload.id,
                    documentId = result.data.documentId,
                    objectKey = result.data.objectKey
                )
                true
            }
            is BaseResult.Error -> {
                val newRetryCount = upload.retryCount + 1
                if (newRetryCount >= MAX_RETRY_COUNT) {
                    pendingUploadDao.updateFailed(upload.id, newRetryCount, result.error.getErrorMessage())
                } else {
                    pendingUploadDao.updateRetry(upload.id, newRetryCount)
                }
                false
            }
            else -> false
        }
    }
}
