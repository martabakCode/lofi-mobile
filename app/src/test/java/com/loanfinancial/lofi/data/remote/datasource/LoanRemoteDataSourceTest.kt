package com.loanfinancial.lofi.data.remote.datasource

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.core.network.PagingResponse
import com.loanfinancial.lofi.data.model.dto.LoanDto
import com.loanfinancial.lofi.data.remote.api.LoanApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class LoanRemoteDataSourceTest {
    private val loanApi: LoanApi = mockk()
    private lateinit var dataSource: LoanRemoteDataSource

    @Before
    fun setup() {
        dataSource = LoanRemoteDataSourceImpl(loanApi)
    }

    @Test
    fun `getMyLoans should return response from api`() =
        runBlocking {
            // Arrange
            val expectedResponse: Response<BaseResponse<PagingResponse<LoanDto>>> = mockk()
            coEvery { loanApi.getMyLoans(0, 10, "createdAt") } returns expectedResponse

            // Act
            val actualResponse = dataSource.getMyLoans(0, 10, "createdAt")

            // Assert
            assertEquals(expectedResponse, actualResponse)
        }
}
