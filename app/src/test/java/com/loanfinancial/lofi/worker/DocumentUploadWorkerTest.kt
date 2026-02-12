package com.loanfinancial.lofi.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.local.dao.PendingDocumentUploadDao
import com.loanfinancial.lofi.data.model.dto.DocumentUploadResult
import com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus
import com.loanfinancial.lofi.data.model.entity.PendingDocumentUploadEntity
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DocumentUploadWorkerTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var params: WorkerParameters

    @MockK
    private lateinit var documentRepository: IDocumentRepository

    @MockK
    private lateinit var pendingUploadDao: PendingDocumentUploadDao

    private lateinit var worker: DocumentUploadWorker

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { params.inputData.getString(DocumentUploadWorker.KEY_LOAN_DRAFT_ID) } returns "loan_123"

        worker =
            DocumentUploadWorker(
                context = context,
                params = params,
                documentRepository = documentRepository,
                pendingUploadDao = pendingUploadDao,
            )
    }

    @Test
    fun `doWork should return failure when no loanDraftId provided`() =
        runTest {
            every { params.inputData.getString(DocumentUploadWorker.KEY_LOAN_DRAFT_ID) } returns null

            val testWorker =
                DocumentUploadWorker(
                    context = context,
                    params = params,
                    documentRepository = documentRepository,
                    pendingUploadDao = pendingUploadDao,
                )

            val result = testWorker.doWork()
            assertEquals(ListenableWorker.Result.failure(), result)
        }

    @Test
    fun `doWork should return success when no pending uploads`() =
        runTest {
            coEvery { pendingUploadDao.getPendingForDraft("loan_123") } returns emptyList()

            val result = worker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
        }

    @Test
    fun `doWork should upload all documents successfully`() =
        runTest {
            val upload = createPendingUpload("upload_1", DocumentUploadStatus.PENDING)

            coEvery { pendingUploadDao.getPendingForDraft("loan_123") } returns listOf(upload)
            coEvery { pendingUploadDao.updateStatus(any(), any()) } just Runs
            coEvery { pendingUploadDao.updateCompleted(any(), any(), any()) } just Runs

            coEvery {
                documentRepository.uploadDocument(any(), any(), any())
            } returns BaseResult.Success(DocumentUploadResult(DocumentType.KTP, "doc_123", "key_123", true))

            val result = worker.doWork()
            assertEquals(ListenableWorker.Result.success(), result)
        }

    private fun createPendingUpload(
        id: String,
        status: DocumentUploadStatus,
    ) =
        PendingDocumentUploadEntity(
            id = id,
            userId = "user_123",
            loanDraftId = "loan_123",
            documentType = DocumentType.KTP.name,
            localFilePath = "/path/to/file.jpg",
            fileName = "file.jpg",
            contentType = "image/jpeg",
            status = status.name,
            retryCount = 0,
        )
}
