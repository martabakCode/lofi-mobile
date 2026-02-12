package com.loanfinancial.lofi.domain.usecase.user

import app.cash.turbine.test
import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
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
class GetProfileDraftUseCaseTest {

    private lateinit var repository: IUserRepository
    private lateinit var useCase: GetProfileDraftUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetProfileDraftUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository getProfileDraft`() = runTest {
        // Arrange
        val userId = "user_123"
        val expectedDraft = ProfileDraftEntity(userId = userId, fullName = "John Doe")
        
        every { repository.getProfileDraft(userId) } returns flowOf(expectedDraft)

        // Act & Assert
        useCase(userId).test {
            assertEquals(expectedDraft, awaitItem())
            awaitComplete()
        }
        
        verify(exactly = 1) { repository.getProfileDraft(userId) }
    }
}
