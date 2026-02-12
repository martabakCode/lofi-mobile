package com.loanfinancial.lofi.domain.usecase.user

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.UserUpdateData
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
class GetUserProfileUseCaseTest {

    private lateinit var repository: IUserRepository
    private lateinit var useCase: GetUserProfileUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetUserProfileUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository getUserProfile`() = runTest {
        // Arrange
        val expectedData = mockk<UserUpdateData>(relaxed = true)
        
        every { repository.getUserProfile() } returns flowOf(Resource.Success(expectedData))

        // Act & Assert
        useCase().test {
            val result = awaitItem()
            assertEquals(Resource.Success(expectedData), result)
            awaitComplete()
        }
        
        verify(exactly = 1) { repository.getUserProfile() }
    }
}
