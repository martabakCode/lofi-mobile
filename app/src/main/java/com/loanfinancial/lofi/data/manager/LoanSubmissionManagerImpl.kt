package com.loanfinancial.lofi.data.manager

import android.content.Context
import com.google.gson.Gson
import com.loanfinancial.lofi.data.local.dao.PendingLoanSubmissionDao
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.model.entity.PendingLoanSubmissionEntity
import com.loanfinancial.lofi.domain.manager.LoanSubmissionManager
import com.loanfinancial.lofi.domain.model.LoanSubmissionData
import com.loanfinancial.lofi.domain.model.PendingLoanSubmission
import com.loanfinancial.lofi.domain.model.PendingSubmissionStatus
import com.loanfinancial.lofi.worker.LoanSubmissionWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class LoanSubmissionManagerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val pendingSubmissionDao: PendingLoanSubmissionDao,
        private val dataStoreManager: DataStoreManager,
    ) : LoanSubmissionManager {
        override suspend fun submitLoanOffline(loanData: LoanSubmissionData): Result<String> =
            try {
                val userId = dataStoreManager.getUserId() ?: throw IllegalStateException("User not logged in")
                val loanId = UUID.randomUUID().toString()
                val entity =
                    PendingLoanSubmissionEntity(
                        loanId = loanId,
                        userId = userId,
                        customerName = loanData.customerName,
                        productCode = loanData.productCode,
                        productName = loanData.productName,
                        interestRate = loanData.interestRate,
                        loanAmount = loanData.loanAmount,
                        tenor = loanData.tenor,
                        loanStatus = "SUBMISSION_PENDING",
                        currentStage = "SUBMISSION",
                        submittedAt = null,
                        loanStatusDisplay = "Pending Submission",
                        slaDurationHours = null,
                        purpose = loanData.purpose,
                        latitude = loanData.latitude,
                        longitude = loanData.longitude,
                        documentPaths = Gson().toJson(loanData.documentPaths),
                        pendingStatus = "PENDING",
                        retryCount = 0,
                        lastRetryTime = null,
                        failureReason = null,
                        createdAt = System.currentTimeMillis(),
                    )

                pendingSubmissionDao.insert(entity)
                LoanSubmissionWorker.schedule(context, loanId)

                Result.success(loanId)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override fun getPendingSubmissions(): Flow<List<PendingLoanSubmission>> =
            kotlinx.coroutines.flow.flow {
                val userId = dataStoreManager.getUserId()
                if (userId != null) {
                    emitAll(
                        pendingSubmissionDao.getPendingSubmissionsByUser(userId).map { entities ->
                            entities.map { entity ->
                                PendingLoanSubmission(
                                    loanId = entity.loanId,
                                    loanAmount = entity.loanAmount,
                                    tenor = entity.tenor,
                                    productName = entity.productName,
                                    status =
                                        try {
                                            PendingSubmissionStatus.valueOf(entity.pendingStatus)
                                        } catch (e: IllegalArgumentException) {
                                            PendingSubmissionStatus.PENDING
                                        },
                                    retryCount = entity.retryCount,
                                    lastRetryTime = entity.lastRetryTime?.let { Date(it) },
                                    failureReason = entity.failureReason,
                                    createdAt = Date(entity.createdAt),
                                )
                            }
                        },
                    )
                } else {
                    emit(emptyList())
                }
            }

        override suspend fun retrySubmission(loanId: String): Result<Unit> =
            try {
                pendingSubmissionDao.updateSubmissionStatus(
                    loanId = loanId,
                    status = "PENDING",
                    retryCount = 0, // Reset retry count on manual retry? Or keep it? Plan implicitly resets simply by re-scheduling
                    timestamp = System.currentTimeMillis(),
                    reason = "Manual retry",
                )
                LoanSubmissionWorker.schedule(context, loanId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }

        override suspend fun cancelSubmission(loanId: String) {
            val count = pendingSubmissionDao.cancelSubmission(loanId)
            if (count > 0) {
                // Cancel work
                androidx.work.WorkManager
                    .getInstance(context)
                    .cancelAllWorkByTag("submit_$loanId")
            }
        }

        override suspend fun triggerPendingSubmissions() {
            val pending = pendingSubmissionDao.getAllPendingSubmissionsSync()
            pending.forEach { submission ->
                // Reset failed status to pending for retry
                if (submission.pendingStatus == "FAILED") {
                    pendingSubmissionDao.updateSubmissionStatus(
                        loanId = submission.loanId,
                        status = "PENDING",
                        timestamp = System.currentTimeMillis(),
                        reason = "Network Trigger",
                    )
                }
                LoanSubmissionWorker.scheduleImmediate(context, submission.loanId)
            }
        }
    }
