package com.loanfinancial.lofi.domain.usecase.document

import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.model.dto.DocumentUploadResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UploadMultipleDocumentsUseCaseTest {
    private lateinit var uploadDocumentUseCase: UploadDocumentUseCase
    private lateinit var useCase: UploadMultipleDocumentsUseCase

    @Before
    fun setup() {
        uploadDocumentUseCase = mockk()
        useCase = UploadMultipleDocumentsUseCase(uploadDocumentUseCase)
    }

    @Test
    fun `invoke should upload documents sequentially and update results`() =
        runTest {
            // Arrange
            val loanId = "loan_123"
            val documents =
                mapOf(
                    DocumentType.KTP to "/path/ktp.jpg",
                    DocumentType.NPWP to "/path/npwp.jpg",
                )

            val ktpResult =
                BaseResult.Success(
                    DocumentUploadResult(DocumentType.KTP, "doc_ktp", "key_ktp", true),
                )
            val npwpResult =
                BaseResult.Success(
                    DocumentUploadResult(DocumentType.NPWP, "doc_npwp", "key_npwp", true),
                )

            // uploadDocumentUseCase is a flow, we need to mock it returning a flow
            coEvery { uploadDocumentUseCase(loanId, "/path/ktp.jpg", DocumentType.KTP) } returns flowOf(ktpResult)
            coEvery { uploadDocumentUseCase(loanId, "/path/npwp.jpg", DocumentType.NPWP) } returns flowOf(npwpResult)

            // Act
            val finalResult = useCase(loanId, documents).last()

            // Assert
            assertEquals(2, finalResult.size)
            assertEquals(ktpResult, finalResult[DocumentType.KTP])
            assertEquals(npwpResult, finalResult[DocumentType.NPWP])

            coVerify(exactly = 1) { uploadDocumentUseCase(loanId, "/path/ktp.jpg", DocumentType.KTP) }
            coVerify(exactly = 1) { uploadDocumentUseCase(loanId, "/path/npwp.jpg", DocumentType.NPWP) }
        }
}
