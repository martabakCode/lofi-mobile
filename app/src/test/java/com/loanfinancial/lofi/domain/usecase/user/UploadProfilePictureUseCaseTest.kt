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
import java.io.File

@ExperimentalCoroutinesApi
class UploadProfilePictureUseCaseTest {
    private lateinit var repository: IUserRepository
    private lateinit var useCase: UploadProfilePictureUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = UploadProfilePictureUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository updateProfilePicture`() =
        runTest {
            // Arrange
            val file = mockk<File>()
            val expectedData = mockk<UserUpdateData>(relaxed = true)

            every { repository.updateProfilePicture(file) } returns flowOf(Resource.Success(expectedData))

            // Act & Assert
            useCase(file).test {
                val result = awaitItem()
                assertEquals(Resource.Success(expectedData), result)
                awaitComplete()
            }

            verify(exactly = 1) { repository.updateProfilePicture(file) }
        }
}
