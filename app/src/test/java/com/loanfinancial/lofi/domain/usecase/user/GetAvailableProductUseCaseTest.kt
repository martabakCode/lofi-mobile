package com.loanfinancial.lofi.domain.usecase.user

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.AvailableProductDto
import com.loanfinancial.lofi.domain.repository.IProductRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class GetAvailableProductUseCaseTest {
    private lateinit var repository: IProductRepository
    private lateinit var useCase: GetAvailableProductUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetAvailableProductUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository getAvailableProduct`() =
        runTest {
            // Arrange
            val expectedData = mockk<AvailableProductDto>(relaxed = true)

            every { repository.getAvailableProduct() } returns flowOf(Resource.Success(expectedData))

            // Act & Assert
            useCase().test {
                val result = awaitItem()
                assertEquals(Resource.Success(expectedData), result)
                awaitComplete()
            }

            verify(exactly = 1) { repository.getAvailableProduct() }
        }
}
