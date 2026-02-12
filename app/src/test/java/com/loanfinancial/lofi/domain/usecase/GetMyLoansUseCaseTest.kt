package com.loanfinancial.lofi.domain.usecase

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetMyLoansUseCaseTest {
    private val repository: ILoanRepository = mockk()
    private val dataStoreManager: DataStoreManager = mockk()
    private lateinit var useCase: GetMyLoansUseCase

    @Before
    fun setup() {
        useCase = GetMyLoansUseCase(repository, dataStoreManager)
    }

    @Test
    fun `invoke should emit values from repository when userId exists`() =
        runTest {
            // Arrange
            val userId = "user_123"
            val expectedResource = Resource.Success(emptyList<com.loanfinancial.lofi.domain.model.Loan>())

            coEvery { dataStoreManager.getUserId() } returns userId
            every { repository.getMyLoans(userId, 0, 10, "createdAt,desc") } returns flowOf(expectedResource)

            // Act & Assert
            useCase(0, 10, "createdAt,desc").test {
                assertEquals(expectedResource, awaitItem())
                awaitComplete()
            }

            coVerify(exactly = 1) { dataStoreManager.getUserId() }
            verify(exactly = 1) { repository.getMyLoans(userId, 0, 10, "createdAt,desc") }
        }

    @Test
    fun `invoke should emit Error when user not logged in`() =
        runTest {
            // Arrange
            coEvery { dataStoreManager.getUserId() } returns null

            // Act & Assert
            useCase(0, 10, "createdAt,desc").test {
                val result = awaitItem()
                assert(result is Resource.Error)
                assertEquals("User not logged in", (result as Resource.Error).message)
                awaitComplete()
            }

            coVerify(exactly = 1) { dataStoreManager.getUserId() }
            verify(exactly = 0) { repository.getMyLoans(any(), any(), any(), any()) }
        }
}
