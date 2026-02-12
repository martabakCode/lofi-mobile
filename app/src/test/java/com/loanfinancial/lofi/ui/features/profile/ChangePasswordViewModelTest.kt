package com.loanfinancial.lofi.ui.features.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.loanfinancial.lofi.domain.usecase.auth.ChangePasswordUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var changePasswordUseCase: ChangePasswordUseCase

    private lateinit var viewModel: ChangePasswordViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ChangePasswordViewModel(changePasswordUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() {
        val state = viewModel.uiState.value

        assertEquals("", state.oldPassword)
        assertEquals("", state.newPassword)
        assertEquals("", state.confirmPassword)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
        assertTrue(state.validationErrors.isEmpty())
    }

    @Test
    fun `onOldPasswordChange should update state`() {
        viewModel.onOldPasswordChange("oldPassword123")

        val state = viewModel.uiState.value
        assertEquals("oldPassword123", state.oldPassword)
    }

    @Test
    fun `onNewPasswordChange should update state`() {
        viewModel.onNewPasswordChange("newPassword123")

        val state = viewModel.uiState.value
        assertEquals("newPassword123", state.newPassword)
    }

    @Test
    fun `onConfirmPasswordChange should update state`() {
        viewModel.onConfirmPasswordChange("confirmPassword123")

        val state = viewModel.uiState.value
        assertEquals("confirmPassword123", state.confirmPassword)
    }

    @Test
    fun `submit with empty old password should show validation error`() {
        viewModel.onNewPasswordChange("newPassword123")
        viewModel.onConfirmPasswordChange("newPassword123")

        viewModel.submit()

        val state = viewModel.uiState.value
        assertTrue(state.validationErrors.containsKey("oldPassword"))
        assertFalse(state.isLoading)
    }

    @Test
    fun `submit with short password should show validation error`() {
        viewModel.onOldPasswordChange("oldPassword123")
        viewModel.onNewPasswordChange("short")
        viewModel.onConfirmPasswordChange("short")

        viewModel.submit()

        val state = viewModel.uiState.value
        assertTrue(state.validationErrors.containsKey("newPassword"))
    }

    @Test
    fun `submit with mismatched passwords should show validation error`() {
        viewModel.onOldPasswordChange("oldPassword123")
        viewModel.onNewPasswordChange("newPassword123")
        viewModel.onConfirmPasswordChange("differentPassword")

        viewModel.submit()

        val state = viewModel.uiState.value
        assertTrue(state.validationErrors.containsKey("confirmPassword"))
    }

    @Test
    fun `submit with valid data should call use case`() =
        runTest {
            coEvery {
                changePasswordUseCase(any())
            } returns Result.success(Unit)

            viewModel.onOldPasswordChange("oldPassword123")
            viewModel.onNewPasswordChange("newPassword123")
            viewModel.onConfirmPasswordChange("newPassword123")

            viewModel.submit()
            advanceUntilIdle()

            coVerify { changePasswordUseCase(any()) }
        }

    @Test
    fun `submit success should update isSuccess state`() =
        runTest {
            coEvery {
                changePasswordUseCase(any())
            } returns Result.success(Unit)

            viewModel.onOldPasswordChange("oldPassword123")
            viewModel.onNewPasswordChange("newPassword123")
            viewModel.onConfirmPasswordChange("newPassword123")

            viewModel.submit()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isSuccess)
            assertFalse(state.isLoading)
        }

    @Test
    fun `submit failure should update error state`() =
        runTest {
            coEvery {
                changePasswordUseCase(any())
            } returns Result.failure(Exception("Current password is incorrect"))

            viewModel.onOldPasswordChange("oldPassword123")
            viewModel.onNewPasswordChange("newPassword123")
            viewModel.onConfirmPasswordChange("newPassword123")

            viewModel.submit()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.isSuccess)
            assertNotNull(state.error)
        }
}
