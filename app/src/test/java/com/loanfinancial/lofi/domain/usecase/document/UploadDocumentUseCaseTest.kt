package com.loanfinancial.lofi.domain.usecase.document

import com.loanfinancial.lofi.core.common.result.BaseResult
import com.loanfinancial.lofi.core.media.DocumentType
import com.loanfinancial.lofi.data.model.dto.DocumentUploadResult
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class UploadDocumentUseCaseTest {

    private lateinit var repository: IDocumentRepository
    private lateinit var useCase: UploadDocumentUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = UploadDocumentUseCase(repository)
    }

    @Test
    fun `invoke should emit loading then result from repository`() = runTest {
        // Arrange
        val loanId = "loan_123"
        val filePath = "/path/to/file.jpg"
        val documentType = DocumentType.KTP
        val expectedResult = BaseResult.Success(
            DocumentUploadResult(
                documentType = documentType,
                documentId = "doc_1",
                objectKey = "key_1",
                isUploaded = true
            )
        )
        
        coEvery { repository.uploadDocument(loanId, filePath, documentType) } returns expectedResult

        // Act
        val results = useCase(loanId, filePath, documentType).toList()

        // Assert
        assertEquals(2, results.size)
        assertTrue(results[0] is BaseResult.Loading)
        assertEquals(expectedResult, results[1])
        
        coVerify(exactly = 1) { repository.uploadDocument(loanId, filePath, documentType) }
    }
}
