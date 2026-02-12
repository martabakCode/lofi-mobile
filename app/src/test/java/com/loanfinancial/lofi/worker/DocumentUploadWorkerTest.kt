package com.loanfinancial.lofi.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.common.result.ErrorType
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
        MockKAnnotations.init(this)
        
        every { params.inputData.getString(DocumentUploadWorker.KEY_LOAN_DRAFT_ID) } returns "loan_123"
        
        worker = DocumentUploadWorker(
            context = context,
            params = params,
            documentRepository = documentRepository,
            pendingUploadDao = pendingUploadDao
        )
    }

    @Test
    fun `doWork should return failure when no loanDraftId provided`() = runTest {
        // Arrange
        every { params.inputData.getString(DocumentUploadWorker.KEY_LOAN_DRAFT_ID) } returns null
        
        val testWorker = DocumentUploadWorker(
            context = context,
            params = params,
            documentRepository = documentRepository,
            pendingUploadDao = pendingUploadDao
        )
        
        // Act
        val result = testWorker.doWork()
        
        // Assert
        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun `doWork should return success when no pending uploads`() = runTest {
        // Arrange
        coEvery { pendingUploadDao.getPendingForDraft("loan_123") } returns emptyList()
        
        // Act
        val result = worker.doWork()
        
        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork should upload all documents successfully`() = runTest {
        // Arrange
        val upload = createPendingUpload("upload_1", DocumentUploadStatus.PENDING)
        
        coEvery { pendingUploadDao.getPendingForDraft("loan_123") } returns listOf(upload)
        coEvery { pendingUploadDao.updateStatus(any(), any()) } just Runs
        coEvery { pendingUploadDao.updateCompleted(any(), any(), any()) } just Runs
        
        coEvery { 
            documentRepository.uploadDocument(any(), any(), any()) 
        } returns BaseResult.Success(
            DocumentUploadResult(DocumentType.KTP, "doc_123", "key_123", true)
        )
        
        // Act
        val result = worker.doWork()
        
        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { pendingUploadDao.updateCompleted("upload_1", "doc_123", "key_123") }
    }

    @Test
    fun `doWork should retry when some uploads fail with retryable error`() = runTest {
        // Arrange
        val upload = createPendingUpload("upload_1", DocumentUploadStatus.PENDING).copy(
            retryCount = 0
        )
        
        coEvery { pendingUploadDao.getPendingForDraft("loan_123") } returns listOf(upload)
        coEvery { pendingUploadDao.updateStatus(any(), any()) } just Runs
        coEvery { pendingUploadDao.updateRetry(any(), any()) } just Runs
        
        coEvery { 
            documentRepository.uploadDocument(any(), any(), any()) 
        } returns BaseResult.Error(ErrorType.NetworkError("Connection failed"))
        
        // Act
        val result = worker.doWork()
        
        // Assert
        assertEquals(ListenableWorker.Result.retry(), result)
        coVerify { pendingUploadDao.updateRetry("upload_1", 1) }
    }

    @Test
    fun `doWork should fail when max retries reached`() = runTest {
        // Arrange
        val upload = createPendingUpload("upload_1", DocumentUploadStatus.PENDING).copy(
            retryCount = DocumentUploadWorker.MAX_RETRY_COUNT
        )
        
        coEvery { pendingUploadDao.getPendingForDraft("loan_123") } returns listOf(upload)
        coEvery { pendingUploadDao.updateStatus(any(), any()) } just Runs
        coEvery { pendingUploadDao.updateFailed(any(), any(), any()) } just Runs
        
        coEvery { 
            documentRepository.uploadDocument(any(), any(), any()) 
        } returns BaseResult.Error(ErrorType.NetworkError("Connection failed"))
        
        // Act
        val result = worker.doWork()
        
        // Assert
        assertEquals(ListenableWorker.Result.failure(), result)
        coVerify { pendingUploadDao.updateFailed("upload_1", 4, any()) }
    }

    @Test
    fun `doWork should skip completed uploads`() = runTest {
        // Arrange
        val completedUpload = createPendingUpload("upload_1", DocumentUploadStatus.COMPLETED)
        val pendingUpload = createPendingUpload("upload_2", DocumentUploadStatus.PENDING)
        
        coEvery { pendingUploadDao.getPendingForDraft("loan_123") } returns 
            listOf(completedUpload, pendingUpload)
        coEvery { pendingUploadDao.updateStatus(any(), any()) } just Runs
        coEvery { pendingUploadDao.updateCompleted(any(), any(), any()) } just Runs
        
        coEvery { 
            documentRepository.uploadDocument(any(), any(), any()) 
        } returns BaseResult.Success(
            DocumentUploadResult(DocumentType.KTP, "doc_123", "key_123", true)
        )
        
        // Act
        val result = worker.doWork()
        
        // Assert
        assertEquals(ListenableWorker.Result.success(), result)
        // Should only upload the pending one
        coVerify(exactly = 1) { documentRepository.uploadDocument(any(), any(), any()) }
    }

    @Test
    fun `doWork should use compressed file path when available`() = runTest {
        // Arrange
        val upload = createPendingUpload("upload_1", DocumentUploadStatus.PENDING).copy(
            compressedFilePath = "/path/to/compressed.jpg",
            localFilePath = "/path/to/original.jpg"
        )
        
        coEvery { pendingUploadDao.getPendingForDraft("loan_123") } returns listOf(upload)
        coEvery { pendingUploadDao.updateStatus(any(), any()) } just Runs
        coEvery { pendingUploadDao.updateCompleted(any(), any(), any()) } just Runs
        
        coEvery { 
            documentRepository.uploadDocument(any(), any(), any()) 
        } returns BaseResult.Success(
            DocumentUploadResult(DocumentType.KTP, "doc_123", "key_123", true)
        )
        
        // Act
        worker.doWork()
        
        // Assert - Should use compressed path
        coVerify { 
            documentRepository.uploadDocument(
                loanId = any(),
                filePath = "/path/to/compressed.jpg",
                documentType = any()
            )
        }
    }

    @Test
    fun `doWork should handle exception gracefully`() = runTest {
        // Arrange
        coEvery { pendingUploadDao.getPendingForDraft("loan_123") } throws 
            RuntimeException("Database error")
        
        // Act
        val result = worker.doWork()
        
        // Assert
        assertTrue(result is ListenableWorker.Result.Failure)
    }

    @Test
    fun `doWork should process multiple documents with mixed results`() = runTest {
        // Arrange
        val upload1 = createPendingUpload("upload_1", DocumentUploadStatus.PENDING)
        val upload2 = createPendingUpload("upload_2", DocumentUploadStatus.PENDING)
        
        coEvery { pendingUploadDao.getPendingForDraft("loan_123") } returns 
            listOf(upload1, upload2)
        coEvery { pendingUploadDao.updateStatus(any(), any()) } just Runs
        coEvery { pendingUploadDao.updateCompleted(any(), any(), any()) } just Runs
        coEvery { pendingUploadDao.updateRetry(any(), any()) } just Runs
        
        // First upload succeeds, second fails
        coEvery { 
            documentRepository.uploadDocument(any(), "/path/to/upload_1.jpg", any()) 
        } returns BaseResult.Success(
            DocumentUploadResult(DocumentType.KTP, "doc_1", "key_1", true)
        )
        
        coEvery { 
            documentRepository.uploadDocument(any(), "/path/to/upload_2.jpg", any()) 
        } returns BaseResult.Error(ErrorType.NetworkError("Failed"))
        
        // Act
        val result = worker.doWork()
        
        // Assert - Should retry because one failed
        assertEquals(ListenableWorker.Result.retry(), result)
    }

    private fun createPendingUpload(id: String, status: DocumentUploadStatus) = 
        PendingDocumentUploadEntity(
            id = id,
            loanDraftId = "loan_123",
            loanId = null,
            userId = "user_123",
            documentType = DocumentType.KTP.name,
            localFilePath = "/path/to/$id.jpg",
            compressedFilePath = null,
            fileName = "$id.jpg",
            contentType = "image/jpeg",
            originalFileSize = 1024,
            compressedFileSize = 1024,
            isCompressed = false,
            status = status.name,
            documentId = null,
            objectKey = null,
            retryCount = 0,
            lastRetryTime = null,
            failureReason = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
}
