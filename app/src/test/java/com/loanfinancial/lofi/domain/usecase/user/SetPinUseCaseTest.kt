package com.loanfinancial.lofi.domain.usecase.user

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.SetPinRequest
import com.loanfinancial.lofi.domain.repository.IUserRepository
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
class SetPinUseCaseTest {
    private lateinit var repository: IUserRepository
    private lateinit var useCase: SetPinUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SetPinUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository setPin`() =
        runTest {
            // Arrange
            val request = SetPinRequest(pin = "123456", password = "password")
            every { repository.setPin(request) } returns flowOf(Resource.Success(Unit))

            // Act & Assert
            useCase(request).test {
                assertEquals(Resource.Success(Unit), awaitItem())
                awaitComplete()
            }

            verify(exactly = 1) { repository.setPin(request) }
        }
}
