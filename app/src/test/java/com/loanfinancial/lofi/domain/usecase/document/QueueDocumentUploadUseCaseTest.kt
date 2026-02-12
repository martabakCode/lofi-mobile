package com.loanfinancial.lofi.domain.usecase.document

import android.content.Context
import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.worker.DocumentUploadWorker
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class QueueDocumentUploadUseCaseTest {
    private lateinit var repository: IDocumentRepository
    private lateinit var context: Context
    private lateinit var useCase: QueueDocumentUploadUseCase

    @Before
    fun setup() {
        repository = mockk()
        context = mockk(relaxed = true)
        useCase = QueueDocumentUploadUseCase(repository, context)
        mockkObject(DocumentUploadWorker)
    }

    @After
    fun tearDown() {
        unmockkObject(DocumentUploadWorker)
    }

    @Test
    fun `invoke should schedule worker when repository returns success`() =
        runTest {
            // Arrange
            val loanDraftId = "draft_123"
            val filePath = "/path/file.jpg"
            val documentType = DocumentType.KTP

            coEvery { repository.queueDocumentUpload(loanDraftId, filePath, documentType) } returns BaseResult.Success("Success")
            every { DocumentUploadWorker.scheduleForDraft(any(), any()) } just Runs

            // Act
            val result = useCase(loanDraftId, filePath, documentType)

            // Assert
            assertTrue(result is BaseResult.Success)
            coVerify(exactly = 1) { repository.queueDocumentUpload(loanDraftId, filePath, documentType) }
            verify(exactly = 1) { DocumentUploadWorker.scheduleForDraft(context, loanDraftId) }
        }

    @Test
    fun `invoke should not schedule worker when repository returns error`() =
        runTest {
            // Arrange
            val loanDraftId = "draft_123"
            val filePath = "/path/file.jpg"
            val documentType = DocumentType.KTP

            coEvery { repository.queueDocumentUpload(loanDraftId, filePath, documentType) } returns BaseResult.Error("Failed")

            // Act
            val result = useCase(loanDraftId, filePath, documentType)

            // Assert
            assertTrue(result is BaseResult.Error)
            coVerify(exactly = 1) { repository.queueDocumentUpload(loanDraftId, filePath, documentType) }
            verify(exactly = 0) { DocumentUploadWorker.scheduleForDraft(any(), any()) }
        }
}
