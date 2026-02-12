package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.data.local.dao.NotificationDao
import com.loanfinancial.lofi.data.model.entity.NotificationEntity
import com.loanfinancial.lofi.data.remote.api.NotificationApi
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class NotificationRepositoryImplTest {
    @MockK
    private lateinit var notificationApi: NotificationApi

    @MockK
    private lateinit var notificationDao: NotificationDao

    private lateinit var repository: NotificationRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = NotificationRepositoryImpl(notificationApi, notificationDao)
    }

    @Test
    fun `getNotifications returns list from API`() =
        runTest {
            val notifications = listOf(
                NotificationEntity(
                    id = "1",
                    title = "Test Notification",
                    body = "This is a test",
                    type = "LOAN_STATUS",
                    isRead = false,
                    createdAt = System.currentTimeMillis()
                )
            )

            coEvery { notificationDao.getAllNotifications() } returns notifications

            val result = repository.getNotifications()

            assertEquals(1, result.size)
            assertEquals("Test Notification", result[0].title)
        }

    @Test
    fun `markAsRead updates local database`() =
        runTest {
            coEvery { notificationDao.markAsRead("1") } just runs

            repository.markAsRead("1")

            coVerify { notificationDao.markAsRead("1") }
        }

    @Test
    fun `markAllAsRead clears unread count`() =
        runTest {
            coEvery { notificationDao.markAllAsRead() } just runs

            repository.markAllAsRead()

            coVerify { notificationDao.markAllAsRead() }
        }

    @Test
    fun `deleteNotification removes from database`() =
        runTest {
            coEvery { notificationDao.deleteNotification("1") } just runs

            repository.deleteNotification("1")

            coVerify { notificationDao.deleteNotification("1") }
        }

    @Test
    fun `getUnreadCount returns correct count`() =
        runTest {
            coEvery { notificationDao.getUnreadCount() } returns 5

            val result = repository.getUnreadCount()

            assertEquals(5, result)
        }

    @Test
    fun `getNotificationsFlow emits updates`() =
        runTest {
            val notifications = listOf(
                NotificationEntity(
                    id = "1",
                    title = "Flow Test",
                    body = "Testing flow",
                    type = "PROMO",
                    isRead = false,
                    createdAt = System.currentTimeMillis()
                )
            )

            every { notificationDao.getAllNotificationsFlow() } returns flowOf(notifications)

            repository.getNotificationsFlow().collect { result ->
                assertEquals(1, result.size)
                assertEquals("Flow Test", result[0].title)
            }
        }
}
