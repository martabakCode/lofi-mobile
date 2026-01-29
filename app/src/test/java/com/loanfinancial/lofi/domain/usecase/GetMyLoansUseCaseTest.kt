package com.loanfinancial.lofi.domain.usecase

import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetMyLoansUseCaseTest {
    private val repository: ILoanRepository = mockk()
    private lateinit var useCase: GetMyLoansUseCase

    @Before
    fun setup() {
        useCase = GetMyLoansUseCase(repository)
    }

    @Test
    fun `invoke should call repository`() {
        // Arrange
        val expectedFlow = flowOf(Resource.Success(emptyList<com.loanfinancial.lofi.domain.model.Loan>()))
        every { repository.getMyLoans(0, 10, "createdAt") } returns expectedFlow

        // Act
        val actualFlow = useCase(0, 10, "createdAt")

        // Assert
        assertEquals(expectedFlow, actualFlow)
    }
}
