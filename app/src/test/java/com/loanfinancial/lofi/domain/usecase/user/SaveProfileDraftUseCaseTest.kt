package com.loanfinancial.lofi.domain.usecase.user

import com.loanfinancial.lofi.data.model.entity.ProfileDraftEntity
import com.loanfinancial.lofi.domain.repository.IUserRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SaveProfileDraftUseCaseTest {

    private lateinit var repository: IUserRepository
    private lateinit var useCase: SaveProfileDraftUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SaveProfileDraftUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository saveProfileDraft`() = runTest {
        // Arrange
        val draft = ProfileDraftEntity(userId = "user_123", fullName = "John")
        coEvery { repository.saveProfileDraft(draft) } just Runs

        // Act
        useCase(draft)

        // Assert
        coVerify(exactly = 1) { repository.saveProfileDraft(draft) }
    }
}
