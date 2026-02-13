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
                    LoanEntity(
                        id = "1",
                        userId = "User",
                        customerName = "Test User",
                        productCode = "P1",
                        productName = "Prod 1",
                        interestRate = 1.5,
                        loanAmount = 1000,
                        tenor = 12,
                        loanStatus = "DRAFT",
                        currentStage = "STAGE",
                        submittedAt = null,
                        reviewedAt = null,
                        approvedAt = null,
                        rejectedAt = null,
                        disbursedAt = null,
                        loanStatusDisplay = "Draft",
                        slaDurationHours = null,
                        disbursementReference = null,
                    ),
                )
            val remoteDto =
                LoanDto(
                    id = "1",
                    customerId = "C1",
                    customerName = "User",
                    product = ProductDto("P1", "P1", "Prod 1", null, 1.5, null, null, null, null, null, null),
                    loanAmount = 1000,
                    tenor = 12,
                    loanStatus = "DRAFT",
                    currentStage = "STAGE",
                    submittedAt = null,
                    approvedAt = null,
                    rejectedAt = null,
                    disbursedAt = null,
                    documents = null,
                    disbursementReference = null,
                    aiAnalysis = null,
                    slaDurationHours = null,
                )
            val remoteResponse = BaseResponse(true, "Success", PagingResponse(listOf(remoteDto), mockk()))

            every { localDataSource.getAllLoans() } returns flowOf(cachedEntities)
            coEvery { remoteDataSource.getMyLoans(any(), any(), any()) } returns Response.success(remoteResponse)
            coEvery { localDataSource.deleteAllLoans() } returns Unit
            coEvery { localDataSource.insertLoans(any()) } returns Unit

            // Act & Assert
            repository.getMyLoans("user_123", 0, 10, "createdAt").test {
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
