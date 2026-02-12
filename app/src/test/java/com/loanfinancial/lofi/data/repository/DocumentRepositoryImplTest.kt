package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.local.dao.PendingDocumentUploadDao
import com.loanfinancial.lofi.data.model.dto.PresignUploadResponse
import com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus
import com.loanfinancial.lofi.data.model.entity.PendingDocumentUploadEntity
import com.loanfinancial.lofi.data.remote.api.DocumentApi
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class DocumentRepositoryImplTest {
    @MockK
    private lateinit var documentApi: DocumentApi

    @MockK
    private lateinit var pendingUploadDao: PendingDocumentUploadDao

    @MockK
    private lateinit var cameraManager: com.loanfinancial.lofi.core.media.CameraManager

    @MockK
    private lateinit var dataStoreManager: com.loanfinancial.lofi.data.local.datastore.DataStoreManager

    private lateinit var repository: DocumentRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = DocumentRepositoryImpl(documentApi, pendingUploadDao, cameraManager, dataStoreManager)
    }

    @Test
    fun `requestPresignUpload success should return presign data`() =
        runTest {
            val responseData = PresignUploadResponse(
                uploadUrl = "https://presigned.url",
                documentId = "doc123",
                objectKey = "loans/loan123/doc123.jpg",
            )
            val response = BaseResponse(
                success = true,
                message = "Success",
                data = responseData
            )

            coEvery { documentApi.requestPresignUpload(any()) } returns Response.success(response)

            val result =
                repository.requestPresignUpload(
                    loanId = "loan123",
                    fileName = "test.jpg",
                    documentType = DocumentType.KTP,
                    contentType = "image/jpeg",
                )

            assertTrue(result is BaseResult.Success)
            coVerify { documentApi.requestPresignUpload(any()) }
        }

    @Test
    fun `requestPresignUpload failure should return error`() =
        runTest {
            coEvery {
                documentApi.requestPresignUpload(any())
            } returns Response.error(500, "Server Error".toResponseBody())

            val result =
                repository.requestPresignUpload(
                    loanId = "loan123",
                    fileName = "test.jpg",
                    documentType = DocumentType.KTP,
                    contentType = "image/jpeg",
                )

            assertTrue(result is BaseResult.Error)
        }

    @Test
    fun `uploadDocument success should return upload result`() =
        runTest {
            val presignResponse =
                BaseResponse(
                    success = true,
                    message = "Success",
                    data =
                        PresignUploadResponse(
                            uploadUrl = "https://presigned.url",
                            documentId = "doc123",
                            objectKey = "loans/loan123/doc123.jpg",
                        ),
                )

            coEvery { documentApi.requestPresignUpload(any()) } returns Response.success(presignResponse)
            coEvery { documentApi.uploadToPresignedUrl(any(), any()) } returns Response.success(Unit)

            val result =
                repository.uploadDocument(
                    loanId = "loan123",
                    filePath = "/test/file.jpg",
                    documentType = DocumentType.KTP,
                )

            assertTrue(result is BaseResult.Success)
        }

    @Test
    fun `queueDocumentUpload success should return queue id`() =
        runTest {
            val entity =
                PendingDocumentUploadEntity(
                    id = "queue123",
                    loanDraftId = "loan123",
                    userId = "user123",
                    documentType = DocumentType.KTP.name,
                    localFilePath = "/test/file.jpg",
                    fileName = "test.jpg",
                    contentType = "image/jpeg",
                    originalFileSize = 1024,
                    compressedFileSize = 1024,
                    isCompressed = false,
                    status = DocumentUploadStatus.PENDING.name,
                )

            coEvery { dataStoreManager.getUserId() } returns "user123"
            coEvery { pendingUploadDao.insertPendingUpload(any()) } just Runs

            val result =
                repository.queueDocumentUpload(
                    loanDraftId = "loan123",
                    filePath = "/test/file.jpg",
                    documentType = DocumentType.KTP,
                    shouldCompress = false,
                )

            assertTrue(result is BaseResult.Success)
            coVerify { pendingUploadDao.insertPendingUpload(any()) }
        }

    @Test
    fun `queueDocumentUpload should queue upload with compression`() =
        runTest {
            val entity =
                PendingDocumentUploadEntity(
                    id = "queue123",
                    loanDraftId = "loan123",
                    userId = "user123",
                    documentType = DocumentType.KTP.name,
                    localFilePath = "/test/file.jpg",
                    compressedFilePath = "/test/compressed.jpg",
                    fileName = "test.jpg",
                    contentType = "image/jpeg",
                    originalFileSize = 2048,
                    compressedFileSize = 1024,
                    isCompressed = true,
                    status = DocumentUploadStatus.PENDING.name,
                )

            coEvery { dataStoreManager.getUserId() } returns "user123"
            coEvery { cameraManager.compressImage("/test/file.jpg", 1024) } returns "/test/compressed.jpg"
            coEvery { pendingUploadDao.insertPendingUpload(any()) } just Runs

            val result =
                repository.queueDocumentUpload(
                    loanDraftId = "loan123",
                    filePath = "/test/file.jpg",
                    documentType = DocumentType.KTP,
                    shouldCompress = true,
                )

            assertTrue(result is BaseResult.Success)
        }

    @Test
    fun `areAllDocumentsUploaded should return true when all completed`() =
        runTest {
            val uploads =
                listOf(
                    PendingDocumentUploadEntity(
                        id = "1",
                        loanDraftId = "loan123",
                        userId = "user123",
                        documentType = "KTP",
                        localFilePath = "/test/file1.jpg",
                        fileName = "test1.jpg",
                        contentType = "image/jpeg",
                        originalFileSize = 1024,
                        compressedFileSize = 1024,
                        isCompressed = false,
                        status = DocumentUploadStatus.COMPLETED.name,
                    ),
                    PendingDocumentUploadEntity(
                        id = "2",
                        loanDraftId = "loan123",
                        userId = "user123",
                        documentType = "ID_CARD_BACK",
                        localFilePath = "/test/file2.jpg",
                        fileName = "test2.jpg",
                        contentType = "image/jpeg",
                        originalFileSize = 1024,
                        compressedFileSize = 1024,
                        isCompressed = false,
                        status = DocumentUploadStatus.COMPLETED.name,
                    ),
                )

            coEvery { pendingUploadDao.getPendingForDraft("loan123") } returns uploads

            val result = repository.areAllDocumentsUploaded("loan123")

            assertTrue(result)
        }
}
