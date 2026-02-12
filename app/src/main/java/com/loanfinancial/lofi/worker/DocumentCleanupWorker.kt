package com.loanfinancial.lofi.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.loanfinancial.lofi.data.local.dao.PendingDocumentUploadDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.util.concurrent.TimeUnit

@HiltWorker
class DocumentCleanupWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val pendingUploadDao: PendingDocumentUploadDao,
    ) : CoroutineWorker(context, params) {
        companion object {
            const val WORK_TAG = "document_cleanup"
            const val CLEANUP_INTERVAL_HOURS = 24L

            fun schedulePeriodic(context: Context) {
                val workRequest =
                    PeriodicWorkRequestBuilder<DocumentCleanupWorker>(
                        CLEANUP_INTERVAL_HOURS,
                        TimeUnit.HOURS,
                    ).build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest,
                )
            }
        }

        override suspend fun doWork(): Result {
            // Clean up completed uploads older than 7 days
            val oldCompletedUploads =
                pendingUploadDao.getCompletedUploadsOlderThan(
                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7),
                )

            for (upload in oldCompletedUploads) {
                // Delete compressed file if it exists
                upload.compressedFilePath?.let {
                    val file = File(it)
                    if (file.exists()) file.delete()
                }

                // Mark as cleaned in DB
                pendingUploadDao.markFilesCleaned(upload.id)
            }

            return Result.success()
        }
    }
