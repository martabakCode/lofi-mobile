package com.loanfinancial.lofi.data.repository

import app.cash.turbine.test
import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.core.network.PagingResponse
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.datasource.LoanLocalDataSource
import com.loanfinancial.lofi.data.model.dto.LoanDto
import com.loanfinancial.lofi.data.model.dto.ProductDto
import com.loanfinancial.lofi.data.model.entity.LoanEntity
import com.loanfinancial.lofi.data.remote.datasource.LoanRemoteDataSource
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class LoanRepositoryImplTest {
    private val remoteDataSource: LoanRemoteDataSource = mockk()
    private val localDataSource: LoanLocalDataSource = mockk()
    private lateinit var repository: ILoanRepository

    @Before
    fun setup() {
        repository = LoanRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Test
    fun `getMyLoans should emit loading, then cached data, then remote data`() =
        runBlocking {
            // Arrange
            val cachedEntities =
                listOf(
                    LoanEntity("1", "User", "P1", "Prod 1", 1.5, 1000, 12, "DRAFT", "STAGE", null, "Draft"),
                )
            val remoteDto = LoanDto("1", "C1", "User", ProductDto("P1", "P1", "Prod 1", null, 1.5, null, null, null, null, null, null), 1000, 12, "DRAFT", "STAGE", null, null, null, null, null, null, null)
            val remoteResponse = BaseResponse(true, "Success", PagingResponse(listOf(remoteDto), mockk()))

            every { localDataSource.getAllLoans() } returns flowOf(cachedEntities)
            coEvery { remoteDataSource.getMyLoans(0, 10, "createdAt") } returns Response.success(remoteResponse)
            coEvery { localDataSource.deleteAllLoans() } returns Unit
            coEvery { localDataSource.insertLoans(any()) } returns Unit

            // Act & Assert
            repository.getMyLoans(0, 10, "createdAt").test {
                assertEquals(Resource.Loading, awaitItem())
                val cachedResult = awaitItem()
                assertTrue(cachedResult is Resource.Success)
                assertEquals("1", (cachedResult as Resource.Success).data[0].id)

                val finalResult = awaitItem()
                assertTrue(finalResult is Resource.Success)
                assertEquals("1", (finalResult as Resource.Success).data[0].id)

                awaitComplete()
            }

            coVerify { localDataSource.deleteAllLoans() }
            coVerify { localDataSource.insertLoans(any()) }
        }
}
