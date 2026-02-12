package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.local.dao.NotificationDao
import com.loanfinancial.lofi.data.local.database.AppDatabase
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import com.loanfinancial.lofi.data.model.dto.NotificationType
import com.loanfinancial.lofi.data.model.entity.NotificationEntity
import com.loanfinancial.lofi.data.model.entity.toEntity
import com.loanfinancial.lofi.data.remote.datasource.NotificationRemoteDataSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class NotificationRepositoryImplTest {
    @MockK
    private lateinit var remoteDataSource: NotificationRemoteDataSource

    @MockK
    private lateinit var database: AppDatabase

    @MockK
    private lateinit var notificationDao: NotificationDao

    private lateinit var repository: NotificationRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { database.notificationDao() } returns notificationDao
        repository = NotificationRepositoryImpl(remoteDataSource, database)
    }

    @Test
    fun `getNotifications should emit loading then local data then remote data`() =
        runTest {
            // Arrange
            val localNotifications = listOf(
                NotificationEntity(
                    id = "1",
                    userId = "user1",
                    title = "Local",
                    body = "Body",
                    type = "SYSTEM",
                    referenceId = null,
                    isRead = false,
                    createdAt = "2024-01-01",
                    link = null
                )
            )
            val remoteNotifications = listOf(
                NotificationResponse(
                    id = "1",
                    userId = "user1",
                    title = "Remote",
                    body = "Body",
                    type = NotificationType.SYSTEM,
                    referenceId = null,
                    isRead = true,
                    createdAt = "2024-01-01",
                    link = null
                )
            )
            val baseResponse = BaseResponse(
                success = true,
                message = "Success",
                data = remoteNotifications
            )

            coEvery { notificationDao.getNotifications() } returns flowOf(localNotifications) andThen flowOf(remoteNotifications.map { it.toEntity() })
            coEvery { remoteDataSource.getNotifications() } returns Response.success(baseResponse)
            coEvery { notificationDao.clearAll() } just Runs
            coEvery { notificationDao.insertAll(any()) } just Runs

            // Act
            val results = repository.getNotifications().toList()

            // Assert
            assertTrue(results[0] is Resource.Loading)
            assertTrue(results[1] is Resource.Success)
            assertEquals("Local", (results[1] as Resource.Success).data[0].title)
            assertTrue(results[2] is Resource.Success)
            assertEquals("Remote", (results[2] as Resource.Success).data[0].title)
            
            coVerify { notificationDao.clearAll() }
            coVerify { notificationDao.insertAll(any()) }
        }

    @Test
    fun `getUnreadCount should delegate to dao`() =
        runTest {
            // Arrange
            val countFlow = flowOf(5)
            every { notificationDao.getUnreadCount() } returns countFlow

            // Act
            val result = repository.getUnreadCount().first()

            // Assert
            assertEquals(5, result)
            verify { notificationDao.getUnreadCount() }
        }

    @Test
    fun `syncNotifications should fetch from remote and save to local`() =
        runTest {
            // Arrange
            val remoteNotifications = listOf(
                NotificationResponse(
                    id = "1",
                    userId = "user1",
                    title = "Remote",
                    body = "Body",
                    type = NotificationType.SYSTEM,
                    referenceId = null,
                    isRead = true,
                    createdAt = "2024-01-01",
                    link = null
                )
            )
            val baseResponse = BaseResponse(
                success = true,
                message = "Success",
                data = remoteNotifications
            )

            coEvery { remoteDataSource.getNotifications() } returns Response.success(baseResponse)
            coEvery { notificationDao.clearAll() } just Runs
            coEvery { notificationDao.insertAll(any()) } just Runs

            // Act
            repository.syncNotifications()

            // Assert
            coVerify { notificationDao.clearAll() }
            coVerify { notificationDao.insertAll(any()) }
        }
}
