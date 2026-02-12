package com.loanfinancial.lofi.ui.features.loan

import app.cash.turbine.test
import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.model.entity.DocumentUploadStatus
import com.loanfinancial.lofi.data.model.entity.PendingDocumentUploadEntity
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.domain.usecase.document.QueueDocumentUploadUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class DocumentUploadViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var queueDocumentUploadUseCase: QueueDocumentUploadUseCase

    @MockK
    private lateinit var documentRepository: IDocumentRepository

    @MockK
    private lateinit var cameraManager: CameraManager

    private lateinit var viewModel: DocumentUploadViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel =
            DocumentUploadViewModel(
                queueDocumentUploadUseCase,
                documentRepository,
                cameraManager,
            )
    }

    @Test
    fun `initial state should be empty`() =
        runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state.documents.isEmpty())
                assertFalse(state.isAllUploaded)
                assertFalse(state.isAllQueued)
            }
        }

    @Test
    fun `observeUploadStatus should update state from repository`() =
        runTest {
            // Arrange
            val uploads =
                listOf(
                    createUploadEntity(DocumentType.KTP.name, DocumentUploadStatus.COMPLETED),
                    createUploadEntity(DocumentType.NPWP.name, DocumentUploadStatus.UPLOADING),
                )

            every { documentRepository.getPendingUploadsFlow("loan_123") } returns
                flowOf(uploads)

            // Act
            viewModel.observeUploadStatus("loan_123")

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(2, state.documents.size)
                assertTrue(state.documents[DocumentType.KTP]?.isUploaded == true)
                assertTrue(state.documents[DocumentType.NPWP]?.isUploading == true)
            }
        }

    @Test
    fun `onDocumentSelected should update state and queue upload`() =
        runTest {
            // Arrange
            val filePath = "/path/to/ktp.jpg"
            coEvery {
                queueDocumentUploadUseCase(any(), any(), any())
            } returns BaseResult.Success("upload_id")

            // Act
            viewModel.onDocumentSelected("loan_123", DocumentType.KTP, filePath)

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(filePath, state.documents[DocumentType.KTP]?.filePath)
                assertNull(state.documents[DocumentType.KTP]?.error)
            }

            coVerify {
                queueDocumentUploadUseCase("loan_123", filePath, DocumentType.KTP)
            }
        }

    @Test
    fun `onDocumentSelected should handle all required document types`() =
        runTest {
            // Arrange
            coEvery { queueDocumentUploadUseCase(any(), any(), any()) } returns
                BaseResult.Success("upload_id")

            val requiredTypes =
                listOf(
                    DocumentType.KTP,
                    DocumentType.NPWP,
                    DocumentType.KK,
                    DocumentType.PAYSLIP,
                    DocumentType.PROOFOFRESIDENCE,
                    DocumentType.BANK_STATEMENT,
                )

            // Act - Upload all required documents
            requiredTypes.forEachIndexed { index, type ->
                viewModel.onDocumentSelected("loan_123", type, "/path/to/$type.jpg")
            }

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals(6, state.documents.size)
                assertTrue(state.isAllQueued)
            }
        }

    @Test
    fun `onRetryUpload should re-queue failed upload`() =
        runTest {
            // Arrange
            val filePath = "/path/to/ktp.jpg"

            // First, set initial state with a failed upload
            coEvery { queueDocumentUploadUseCase(any(), any(), any()) } returns
                BaseResult.Success("upload_id")
            viewModel.onDocumentSelected("loan_123", DocumentType.KTP, filePath)

            // Now retry
            coEvery {
                queueDocumentUploadUseCase("loan_123", filePath, DocumentType.KTP)
            } returns BaseResult.Success("upload_id_retry")

            // Act
            viewModel.onRetryUpload("loan_123", DocumentType.KTP)

            // Assert
            coVerify(exactly = 2) {
                queueDocumentUploadUseCase("loan_123", filePath, DocumentType.KTP)
            }
        }

    @Test
    fun `createTempFileUri should delegate to cameraManager`() =
        runTest {
            // Arrange
            val mockFile = mockk<File>()
            val mockUri = mockk<android.net.Uri>()

            every { cameraManager.createTempImageFile(DocumentType.KTP) } returns mockFile
            every { cameraManager.getUriForFile(mockFile) } returns mockUri

            // Act
            val result = viewModel.createTempFileUri(DocumentType.KTP)

            // Assert
            assertEquals(mockFile, result.first)
            assertEquals(mockUri, result.second)
        }

    @Test
    fun `isAllUploaded should be true when all documents completed`() =
        runTest {
            // Arrange
            val uploads =
                listOf(
                    createUploadEntity(DocumentType.KTP.name, DocumentUploadStatus.COMPLETED),
                    createUploadEntity(DocumentType.NPWP.name, DocumentUploadStatus.COMPLETED),
                    createUploadEntity(DocumentType.KK.name, DocumentUploadStatus.COMPLETED),
                )

            every { documentRepository.getPendingUploadsFlow("loan_123") } returns
                flowOf(uploads)

            // Act
            viewModel.observeUploadStatus("loan_123")

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertTrue(state.isAllUploaded)
            }
        }

    @Test
    fun `isAllUploaded should be false when some documents pending`() =
        runTest {
            // Arrange
            val uploads =
                listOf(
                    createUploadEntity(DocumentType.KTP.name, DocumentUploadStatus.COMPLETED),
                    createUploadEntity(DocumentType.NPWP.name, DocumentUploadStatus.PENDING),
                )

            every { documentRepository.getPendingUploadsFlow("loan_123") } returns
                flowOf(uploads)

            // Act
            viewModel.observeUploadStatus("loan_123")

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertFalse(state.isAllUploaded)
            }
        }

    @Test
    fun `should handle upload error state`() =
        runTest {
            // Arrange
            val uploads =
                listOf(
                    createUploadEntity(
                        DocumentType.KTP.name,
                        DocumentUploadStatus.FAILED,
                        "Network error",
                    ),
                )

            every { documentRepository.getPendingUploadsFlow("loan_123") } returns
                flowOf(uploads)

            // Act
            viewModel.observeUploadStatus("loan_123")

            // Assert
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals("Network error", state.documents[DocumentType.KTP]?.error)
            }
        }

    private fun createUploadEntity(
        type: String,
        status: DocumentUploadStatus,
        failureReason: String? = null,
    ) = PendingDocumentUploadEntity(
        id = "upload_123",
        loanDraftId = "loan_123",
        loanId = null,
        userId = "user_123",
        documentType = type,
        localFilePath = "/path/to/doc.jpg",
        compressedFilePath = null,
        fileName = "doc.jpg",
        contentType = "image/jpeg",
        originalFileSize = 1024,
        compressedFileSize = 1024,
        isCompressed = false,
        status = status.name,
        documentId = if (status == DocumentUploadStatus.COMPLETED) "doc_123" else null,
        objectKey = if (status == DocumentUploadStatus.COMPLETED) "key_123" else null,
        retryCount = 0,
        lastRetryTime = null,
        failureReason = failureReason,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
    )
}
