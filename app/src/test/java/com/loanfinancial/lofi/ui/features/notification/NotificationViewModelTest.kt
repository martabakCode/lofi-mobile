package com.loanfinancial.lofi.ui.features.notification

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import com.loanfinancial.lofi.domain.usecase.notification.GetNotificationsUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var getNotificationsUseCase: GetNotificationsUseCase

    private lateinit var viewModel: NotificationViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() {
        every { getNotificationsUseCase() } returns flowOf(Resource.Loading())

        viewModel = NotificationViewModel(getNotificationsUseCase)

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Loading)
    }

    @Test
    fun `fetchNotifications success should update state with notifications`() {
        val notifications =
            listOf(
                NotificationResponse(
                    id = "1",
                    title = "Test Notification",
                    body = "This is a test",
                    type = "LOAN_STATUS",
                    isRead = false,
                    createdAt = System.currentTimeMillis(),
                ),
            )

        every { getNotificationsUseCase() } returns flowOf(Resource.Success(notifications))

        viewModel = NotificationViewModel(getNotificationsUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Success)
        assertEquals(1, (state as UiState.Success).data.size)
    }

    @Test
    fun `fetchNotifications error should update state with error message`() {
        every { getNotificationsUseCase() } returns flowOf(Resource.Error("Failed to load notifications"))

        viewModel = NotificationViewModel(getNotificationsUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is UiState.Error)
        assertEquals("Failed to load notifications", (state as UiState.Error).message)
    }

    @Test
    fun `getNotification should return notification when found`() {
        val notifications =
            listOf(
                NotificationResponse(
                    id = "1",
                    title = "Test Notification",
                    body = "This is a test",
                    type = "LOAN_STATUS",
                    isRead = false,
                    createdAt = System.currentTimeMillis(),
                ),
            )

        every { getNotificationsUseCase() } returns flowOf(Resource.Success(notifications))

        viewModel = NotificationViewModel(getNotificationsUseCase)
        advanceUntilIdle()

        val result = viewModel.getNotification("1")

        assertNotNull(result)
        assertEquals("Test Notification", result?.title)
    }

    @Test
    fun `getNotification should return null when not found`() {
        val notifications =
            listOf(
                NotificationResponse(
                    id = "1",
                    title = "Test Notification",
                    body = "This is a test",
                    type = "LOAN_STATUS",
                    isRead = false,
                    createdAt = System.currentTimeMillis(),
                ),
            )

        every { getNotificationsUseCase() } returns flowOf(Resource.Success(notifications))

        viewModel = NotificationViewModel(getNotificationsUseCase)
        advanceUntilIdle()

        val result = viewModel.getNotification("999")

        assertNull(result)
    }

    @Test
    fun `getNotification should return null when state is Loading`() {
        every { getNotificationsUseCase() } returns flowOf(Resource.Loading())

        viewModel = NotificationViewModel(getNotificationsUseCase)

        val result = viewModel.getNotification("1")

        assertNull(result)
    }

    @Test
    fun `getNotification should return null when state is Error`() {
        every { getNotificationsUseCase() } returns flowOf(Resource.Error("Error"))

        viewModel = NotificationViewModel(getNotificationsUseCase)
        advanceUntilIdle()

        val result = viewModel.getNotification("1")

        assertNull(result)
    }
}
