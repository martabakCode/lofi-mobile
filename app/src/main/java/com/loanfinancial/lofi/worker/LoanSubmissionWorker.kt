package com.loanfinancial.lofi.worker

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.core.notification.LoanSubmissionNotificationManager
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.dao.PendingLoanSubmissionDao
import com.loanfinancial.lofi.data.model.entity.PendingLoanSubmissionEntity
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import kotlin.math.round

@HiltWorker
class
LoanSubmissionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val loanRepository: ILoanRepository,
    private val documentRepository: IDocumentRepository,
    private val pendingSubmissionDao: PendingLoanSubmissionDao,
    private val notificationManager: LoanSubmissionNotificationManager
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "LoanSubmissionWorker"
        const val WORK_TAG = "loan_submission"
        const val KEY_LOAN_ID = "loan_id"
        const val MAX_RETRY_COUNT = 3
        const val RETRY_INTERVAL_HOURS = 2L

        fun schedule(context: Context, loanId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<LoanSubmissionWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_LOAN_ID to loanId))
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    RETRY_INTERVAL_HOURS,
                    TimeUnit.HOURS
                )
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "submit_$loanId",
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }

        fun scheduleImmediate(context: Context, loanId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<LoanSubmissionWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_LOAN_ID to loanId))
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "submit_$loanId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun scheduleWithDelay(context: Context, loanId: String, delayHours: Long) {
            val workRequest = OneTimeWorkRequestBuilder<LoanSubmissionWorker>()
                .setInitialDelay(delayHours, TimeUnit.HOURS)
                .setInputData(workDataOf(KEY_LOAN_ID to loanId))
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "submit_$loanId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val inputLoanId = inputData.getString(KEY_LOAN_ID) ?: run {
            Log.e(TAG, "No loanId provided in inputData")
            return Result.failure()
        }

        Log.d(TAG, "Starting work for loanId: $inputLoanId")

        // Update status to SUBMITTING
        pendingSubmissionDao.updateSubmissionStatus(
            loanId = inputLoanId,
            status = "SUBMITTING",
            timestamp = System.currentTimeMillis(),
            reason = null
        )

        return try {
            val submission = pendingSubmissionDao.getById(inputLoanId)
            Log.d(TAG, "Retrieved submission: loanId=${submission.loanId}, serverLoanId=${submission.serverLoanId}, pendingStatus=${submission.pendingStatus}")

            // 1. Create loan on server if not exists
            val targetLoanId = if (submission.serverLoanId.isNullOrEmpty()) {
                Log.d(TAG, "No serverLoanId found, creating loan on server...")
                createLoanOnServer(submission)
            } else {
                Log.d(TAG, "Using existing serverLoanId: ${submission.serverLoanId}")
                submission.serverLoanId
            }

            // 2. Upload documents using the SERVER ID
            Log.d(TAG, "Uploading documents for targetLoanId: $targetLoanId")
            uploadDocuments(submission, targetLoanId)

            // 3. Wait a moment for server to process documents before submitting
            Log.d(TAG, "Waiting for server to process documents...")
            kotlinx.coroutines.delay(2000)

            // 4. Submit loan using the SERVER ID (only if loan is in DRAFT status)
            Log.d(TAG, "Checking loan status before submit for targetLoanId: $targetLoanId")
            
            // First, get the loan details to check its status
            val loanDetail = loanRepository.getLoanDetail(targetLoanId)
                .filter { it !is Resource.Loading }
                .first()
            
            val shouldSubmit = when (loanDetail) {
                is Resource.Success -> {
                    loanDetail.data?.loanStatus != "SUBMITTED"
                }
                else -> true // If we can't get details, try to submit anyway
            }
            
            if (shouldSubmit) {
                Log.d(TAG, "Submitting loan with targetLoanId: $targetLoanId")
                val result = loanRepository.submitLoan(targetLoanId)
                    .filter { it !is Resource.Loading }
                    .first()

                if (result is Resource.Success) {
                    Log.d(TAG, "Loan submission successful for loanId: $inputLoanId")
                    // Success - update status and notify
                    pendingSubmissionDao.updateSubmissionStatus(
                        loanId = inputLoanId,
                        status = "SUCCESS",
                        timestamp = System.currentTimeMillis(),
                        reason = null
                    )
                    notificationManager.showSuccessNotification(inputLoanId)
                    Result.success()
                } else {
                    val message = if (result is Resource.Error) result.message else "Unknown error"
                    Log.e(TAG, "Loan submission failed: $message")
                    handleFailure(inputLoanId, message)
                }
            } else {
                // Loan is already submitted, update status to SUCCESS
                Log.d(TAG, "Loan is already submitted, marking as SUCCESS for loanId: $inputLoanId")
                pendingSubmissionDao.updateSubmissionStatus(
                    loanId = inputLoanId,
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis(),
                    reason = null
                )
                notificationManager.showSuccessNotification(inputLoanId)
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during loan submission: ${e.message}", e)
            handleFailure(inputLoanId, e.message)
        }
    }

    private suspend fun createLoanOnServer(submission: PendingLoanSubmissionEntity): String {
        Log.d(TAG, "Creating loan on server with amount=${submission.loanAmount}, tenor=${submission.tenor}")
        
        // Round coordinates to 6 decimal places to avoid DB arithmetic overflow
        val roundedLongitude = round((submission.longitude ?: 0.0) * 1_000_000) / 1_000_000
        val roundedLatitude = round((submission.latitude ?: 0.0) * 1_000_000) / 1_000_000
        
        val request = com.loanfinancial.lofi.data.model.dto.CreateLoanRequest(
            loanAmount = submission.loanAmount,
            tenor = submission.tenor,
            purpose = submission.purpose ?: "Loan Application",
            longitude = roundedLongitude,
            latitude = roundedLatitude
        )

        val result = loanRepository.createLoan(request)
            .filter { it !is Resource.Loading }
            .first()

        Log.d(TAG, "createLoan result: ${result::class.simpleName}")
        
        if (result is Resource.Success && result.data?.id != null) {
            val serverId = result.data.id
            Log.d(TAG, "Loan created successfully on server with ID: $serverId")
            // Update entity with server ID so we don't recreate on retry
            val updated = submission.copy(serverLoanId = serverId)
            pendingSubmissionDao.update(updated)
            Log.d(TAG, "Updated submission with serverLoanId: $serverId")
            return serverId
        } else {
            val msg = if (result is Resource.Error) result.message else "Failed to create loan on server"
            Log.e(TAG, "Failed to create loan on server: $msg")
            throw Exception(msg)
        }
    }

    private suspend fun uploadDocuments(submission: PendingLoanSubmissionEntity, targetLoanId: String) {
        Log.d(TAG, "Checking document uploads for targetLoanId: $targetLoanId")
        
        // Check uploads for targetLoanId
        val pendingUploads = documentRepository.getPendingUploads(targetLoanId)
        Log.d(TAG, "Found ${pendingUploads.size} pending uploads for targetLoanId: $targetLoanId")
        
        if (pendingUploads.isEmpty()) {
            Log.d(TAG, "No pending uploads found, checking documentPaths...")
            val type = object : TypeToken<Map<String, String>>() {}.type
            val documents: Map<String, String> = try {
                Gson().fromJson(submission.documentPaths, type)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse documentPaths JSON: ${e.message}")
                emptyMap()
            }
            
            Log.d(TAG, "Parsed ${documents.size} documents from documentPaths")
            
            if (documents.isNotEmpty()) {
                documents.forEach { (typeStr, path) ->
                    Log.d(TAG, "Processing document: type=$typeStr, path=$path")
                    val docType = try {
                        DocumentType.valueOf(typeStr)
                    } catch (e: IllegalArgumentException) {
                        Log.w(TAG, "Unknown document type: $typeStr")
                        return@forEach
                    }
                    // Queue upload for the TARGET ID (Server ID)
                    val result = documentRepository.queueDocumentUpload(targetLoanId, path, docType)
                    Log.d(TAG, "Queue document upload result: ${result::class.simpleName}")
                }
                DocumentUploadWorker.scheduleForDraft(applicationContext, targetLoanId)
                Log.d(TAG, "Scheduled DocumentUploadWorker for targetLoanId: $targetLoanId")
                throw Exception("Documents queued for upload. Waiting for completion.")
            }
            Log.d(TAG, "No documents to upload")
            return
        }

        val allUploaded = pendingUploads.all { it.status == com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus.COMPLETED.name }
        Log.d(TAG, "All documents uploaded: $allUploaded")
        
        if (!allUploaded) {
            val failedCount = pendingUploads.count { it.status == com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus.FAILED.name }
            val completedCount = pendingUploads.count { it.status == com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus.COMPLETED.name }
            val pendingCount = pendingUploads.count { it.status == com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus.PENDING.name }
            
            Log.d(TAG, "Document upload status: completed=$completedCount, failed=$failedCount, pending=$pendingCount")
            
            if (failedCount > 0) {
                DocumentUploadWorker.scheduleForDraft(applicationContext, targetLoanId)
            }
            
            throw Exception("Waiting for documents: $completedCount/${pendingUploads.size} uploaded. $failedCount failed.")
        }
        
        Log.d(TAG, "All documents are uploaded successfully")
    }

    private suspend fun handleFailure(loanId: String, reason: String?): Result {
        Log.e(TAG, "Handling failure for loanId: $loanId, reason: $reason")
        
        val submission = pendingSubmissionDao.getById(loanId)
        val newRetryCount = submission.retryCount + 1
        Log.d(TAG, "Current retry count: ${submission.retryCount}, new retry count: $newRetryCount, max: $MAX_RETRY_COUNT")

        return if (newRetryCount >= MAX_RETRY_COUNT) {
            // Max retries reached - mark as failed
            Log.e(TAG, "Max retry count reached. Marking as FAILED.")
            pendingSubmissionDao.updateSubmissionStatus(
                loanId = loanId,
                status = "FAILED",
                retryCount = newRetryCount,
                timestamp = System.currentTimeMillis(),
                reason = reason ?: "Unknown error"
            )
            notificationManager.showFailureNotification(loanId, reason)
            Result.failure()
        } else {
            // Schedule retry
            Log.d(TAG, "Scheduling retry for loanId: $loanId in $RETRY_INTERVAL_HOURS hours")
            pendingSubmissionDao.updateSubmissionStatus(
                loanId = loanId,
                status = "PENDING",
                retryCount = newRetryCount,
                timestamp = System.currentTimeMillis(),
                reason = reason
            )
            scheduleWithDelay(applicationContext, loanId, RETRY_INTERVAL_HOURS)
            Result.retry()
        }
    }
}
