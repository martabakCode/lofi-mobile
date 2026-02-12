package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.data.local.dao.ProductDao
import com.loanfinancial.lofi.data.model.dto.AvailableProductDto
import com.loanfinancial.lofi.data.model.dto.ProductDto
import com.loanfinancial.lofi.data.model.entity.ProductEntity
import com.loanfinancial.lofi.data.remote.api.LoanProductApi
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class ProductRepositoryImplTest {
    @MockK
    private lateinit var productApi: LoanProductApi

    @MockK
    private lateinit var productDao: ProductDao

    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = ProductRepositoryImpl(productApi, productDao)
    }

    @Test
    fun `getProducts returns products from API`() =
        runTest {
            val products =
                listOf(
                    ProductDto(
                        id = "prod1",
                        name = "Personal Loan",
                        description = "Personal loan description",
                        minAmount = 1000000.0,
                        maxAmount = 50000000.0,
                        minTenure = 6,
                        maxTenure = 48,
                        interestRate = 0.12,
                    ),
                )

            coEvery { productApi.getProducts() } returns Response.success(products)
            coEvery { productDao.insertProducts(any()) } just runs

            val result = repository.getProducts()

            assertEquals(1, result.size)
            assertEquals("Personal Loan", result[0].name)
        }

    @Test
    fun `getProducts caches to local database`() =
        runTest {
            val products =
                listOf(
                    ProductDto(
                        id = "prod1",
                        name = "Personal Loan",
                        description = "Description",
                        minAmount = 1000000.0,
                        maxAmount = 50000000.0,
                        minTenure = 6,
                        maxTenure = 48,
                        interestRate = 0.12,
                    ),
                )

            coEvery { productApi.getProducts() } returns Response.success(products)
            coEvery { productDao.insertProducts(any()) } just runs

            repository.getProducts()

            coVerify { productDao.insertProducts(any()) }
        }

    @Test
    fun `getAvailableProducts returns eligible products`() =
        runTest {
            val availableProducts =
                listOf(
                    AvailableProductDto(
                        productId = "prod1",
                        productName = "Personal Loan",
                        maxLoanAmount = 25000000.0,
                        maxTenure = 36,
                        interestRate = 0.10,
                        eligibleAmount = 15000000.0,
                    ),
                )

            coEvery { productApi.getAvailableProducts("user123") } returns Response.success(availableProducts)

            val result = repository.getAvailableProducts("user123")

            assertEquals(1, result.size)
            assertEquals("Personal Loan", result[0].productName)
        }

    @Test
    fun `getCachedProducts returns from database`() =
        runTest {
            val entities =
                listOf(
                    ProductEntity(
                        id = "prod1",
                        name = "Cached Loan",
                        description = "Cached description",
                        minAmount = 1000000.0,
                        maxAmount = 50000000.0,
                        minTenure = 6,
                        maxTenure = 48,
                        interestRate = 0.12,
                        iconUrl = null,
                        color = "#FF0000",
                    ),
                )

            coEvery { productDao.getAllProducts() } returns entities

            val result = repository.getCachedProducts()

            assertEquals(1, result.size)
            assertEquals("Cached Loan", result[0].name)
        }

    @Test
    fun `clearCache removes all products`() =
        runTest {
            coEvery { productDao.clearAll() } just runs

            repository.clearCache()

            coVerify { productDao.clearAll() }
        }
}
