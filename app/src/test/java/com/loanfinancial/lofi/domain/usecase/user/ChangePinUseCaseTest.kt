package com.loanfinancial.lofi.domain.usecase.user

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.ChangePinRequest
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
class ChangePinUseCaseTest {

    private lateinit var repository: IUserRepository
    private lateinit var useCase: ChangePinUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ChangePinUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository changePin`() = runTest {
        // Arrange
        val request = ChangePinRequest(oldPin = "123456", newPin = "654321")
        every { repository.changePin(request) } returns flowOf(Resource.Success(Unit))

        // Act & Assert
        useCase(request).test {
            assertEquals(Resource.Success(Unit), awaitItem())
            awaitComplete()
        }
        
        verify(exactly = 1) { repository.changePin(request) }
    }
}
