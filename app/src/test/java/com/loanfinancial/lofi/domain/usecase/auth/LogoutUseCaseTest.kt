package com.loanfinancial.lofi.domain.usecase.auth

import com.loanfinancial.lofi.data.local.dao.LoanDao
import com.loanfinancial.lofi.data.local.dao.PendingDocumentUploadDao
import com.loanfinancial.lofi.data.local.dao.PendingLoanSubmissionDao
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.model.dto.LogoutResponse
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LogoutUseCaseTest {
    private lateinit var repository: IAuthRepository
    private lateinit var loanDao: LoanDao
    private lateinit var documentUploadDao: PendingDocumentUploadDao
    private lateinit var pendingSubmissionDao: PendingLoanSubmissionDao
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var useCase: LogoutUseCase

    @Before
    fun setup() {
        repository = mockk()
        loanDao = mockk(relaxed = true)
        documentUploadDao = mockk(relaxed = true)
        pendingSubmissionDao = mockk(relaxed = true)
        dataStoreManager = mockk()

        useCase =
            LogoutUseCase(
                repository,
                loanDao,
                documentUploadDao,
                pendingSubmissionDao,
                dataStoreManager,
            )
    }

    @Test
    fun `invoke should clear local data and call logout when userId exists`() =
        runTest {
            // Arrange
            val userId = "user_123"
            val expectedResponse = LogoutResponse(success = true, message = "Logged out successfully")

            coEvery { dataStoreManager.getUserId() } returns userId
            coEvery { repository.logout() } returns Result.success(expectedResponse)

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(expectedResponse, result.getOrNull())

            coVerify(exactly = 1) { dataStoreManager.getUserId() }
            coVerify(exactly = 1) { loanDao.deleteByUser(userId) }
            coVerify(exactly = 1) { documentUploadDao.deleteByUser(userId) }
            coVerify(exactly = 1) { pendingSubmissionDao.deleteByUser(userId) }
            coVerify(exactly = 1) { repository.logout() }
        }

    @Test
    fun `invoke should not clear local data but call logout when userId is null`() =
        runTest {
            // Arrange
            val expectedResponse = LogoutResponse(success = true, message = "Logged out successfully")

            coEvery { dataStoreManager.getUserId() } returns null
            coEvery { repository.logout() } returns Result.success(expectedResponse)

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isSuccess)
            assertEquals(expectedResponse, result.getOrNull())

            coVerify(exactly = 1) { dataStoreManager.getUserId() }
            coVerify(exactly = 0) { loanDao.deleteByUser(any()) }
            coVerify(exactly = 0) { documentUploadDao.deleteByUser(any()) }
            coVerify(exactly = 0) { pendingSubmissionDao.deleteByUser(any()) }
            coVerify(exactly = 1) { repository.logout() }
        }

    @Test
    fun `invoke should return error when logout fails`() =
        runTest {
            // Arrange
            val userId = "user_123"
            val expectedException = Exception("Logout failed")

            coEvery { dataStoreManager.getUserId() } returns userId
            coEvery { repository.logout() } returns Result.failure(expectedException)

            // Act
            val result = useCase()

            // Assert
            assertTrue(result.isFailure)
            assertEquals(expectedException, result.exceptionOrNull())

            // Based on implementation, data cleanup happens BEFORE logout api call
            coVerify(exactly = 1) { loanDao.deleteByUser(userId) }
            coVerify(exactly = 1) { repository.logout() }
        }
}
