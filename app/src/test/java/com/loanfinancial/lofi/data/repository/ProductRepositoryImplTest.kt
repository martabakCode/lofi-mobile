package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.core.network.Meta
import com.loanfinancial.lofi.core.network.PagingResponse
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.dao.ProductDao
import com.loanfinancial.lofi.data.local.database.AppDatabase
import com.loanfinancial.lofi.data.model.dto.AvailableProductDto
import com.loanfinancial.lofi.data.model.dto.ProductDto
import com.loanfinancial.lofi.data.model.entity.ProductEntity
import com.loanfinancial.lofi.data.model.entity.toEntity
import com.loanfinancial.lofi.data.remote.api.LoanProductApi
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class ProductRepositoryImplTest {
    @MockK
    private lateinit var api: LoanProductApi

    @MockK
    private lateinit var database: AppDatabase

    @MockK
    private lateinit var productDao: ProductDao

    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { database.productDao() } returns productDao
        repository = ProductRepositoryImpl(api, database)
    }

    @Test
    fun `getProducts should emit loading then local data then remote data`() =
        runTest {
            // Arrange
            val localProducts =
                listOf(
                    ProductEntity(
                        id = "1",
                        productCode = "CODE1",
                        productName = "Local",
                        description = "Desc",
                        interestRate = 0.1,
                        adminFee = 1000.0,
                        minTenor = 1,
                        maxTenor = 12,
                        minLoanAmount = 1000000.0,
                        maxLoanAmount = 5000000.0,
                        isActive = true,
                    ),
                )
            val remoteProducts =
                listOf(
                    ProductDto(
                        id = "1",
                        productCode = "CODE1",
                        productName = "Remote",
                        description = "Desc",
                        interestRate = 0.1,
                        adminFee = 1000.0,
                        minTenor = 1,
                        maxTenor = 12,
                        minLoanAmount = 1000000.0,
                        maxLoanAmount = 5000000.0,
                        isActive = true,
                    ),
                )
            val pagingResponse =
                PagingResponse(
                    items = remoteProducts,
                    meta = Meta(1, 1, 1, 1),
                )
            val baseResponse =
                BaseResponse(
                    success = true,
                    message = "Success",
                    data = pagingResponse,
                )

            coEvery { productDao.getProducts() } returns flowOf(localProducts) andThen flowOf(remoteProducts.map { it.toEntity() })
            coEvery { api.getProducts(any(), any()) } returns Response.success(baseResponse)
            coEvery { productDao.clearAll() } just Runs
            coEvery { productDao.insertAll(any()) } just Runs

            // Act
            val results = repository.getProducts().toList()

            // Assert
            assertTrue(results[0] is Resource.Loading)
            assertTrue(results[1] is Resource.Success)
            assertEquals("Local", (results[1] as Resource.Success).data[0].productName)
            assertTrue(results[2] is Resource.Success)
            assertEquals("Remote", (results[2] as Resource.Success).data[0].productName)
        }

    @Test
    fun `getAvailableProduct should emit loading then remote data`() =
        runTest {
            // Arrange
            val availableProduct =
                AvailableProductDto(
                    productId = "1",
                    productCode = "CODE1",
                    productName = "Available",
                    productLimit = 5000000.0,
                    approvedLoanAmount = 0.0,
                    availableAmount = 5000000.0,
                    hasSubmittedLoan = false,
                    lastLoanStatus = null,
                    lastLoanSubmittedAt = null,
                )
            val baseResponse =
                BaseResponse(
                    success = true,
                    message = "Success",
                    data = availableProduct,
                )

            coEvery { api.getAvailableProduct() } returns Response.success(baseResponse)

            // Act
            val results = repository.getAvailableProduct().toList()

            // Assert
            assertTrue(results[0] is Resource.Loading)
            assertTrue(results[1] is Resource.Success)
            assertEquals("Available", (results[1] as Resource.Success).data.productName)
        }
}
