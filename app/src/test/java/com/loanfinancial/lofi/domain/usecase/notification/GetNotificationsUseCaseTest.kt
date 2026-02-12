package com.loanfinancial.lofi.domain.usecase.notification

import app.cash.turbine.test
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import com.loanfinancial.lofi.domain.repository.INotificationRepository
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
class GetNotificationsUseCaseTest {
    private lateinit var repository: INotificationRepository
    private lateinit var useCase: GetNotificationsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetNotificationsUseCase(repository)
    }

    @Test
    fun `invoke should delegate to repository getNotifications`() =
        runTest {
            // Arrange
            val expectedNotifications =
                listOf(
                    mockk<NotificationResponse>(relaxed = true),
                    mockk<NotificationResponse>(relaxed = true),
                )

            every { repository.getNotifications() } returns
                flowOf(
                    Resource.Loading,
                    Resource.Success(expectedNotifications),
                )

            // Act
            useCase().test {
                assertEquals(Resource.Loading, awaitItem())
                val success = awaitItem() as Resource.Success
                assertEquals(expectedNotifications, success.data)
                awaitComplete()
            }

            verify(exactly = 1) { repository.getNotifications() }
        }
}
