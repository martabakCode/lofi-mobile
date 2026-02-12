package com.loanfinancial.lofi.ui.features.auth

import android.content.Context
import com.loanfinancial.lofi.core.biometric.BiometricAuthenticator
import com.loanfinancial.lofi.core.biometric.BiometricResult
import com.loanfinancial.lofi.ui.features.auth.biometric.BiometricLoginViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BiometricLoginViewModelTest {
    @MockK
    private lateinit var biometricAuthenticator: BiometricAuthenticator

    @MockK
    private lateinit var context: Context

    private lateinit var viewModel: BiometricLoginViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        every { context.getString(any()) } returns "Test String"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should check biometric availability`() {
        every { biometricAuthenticator.isBiometricAvailable() } returns true
        every { biometricAuthenticator.isBiometricEnrolled() } returns true

        viewModel = BiometricLoginViewModel(biometricAuthenticator, context)

        val state = viewModel.uiState.value
        assertTrue(state.isBiometricAvailable)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `biometric unavailable when not enrolled`() {
        every { biometricAuthenticator.isBiometricAvailable() } returns true
        every { biometricAuthenticator.isBiometricEnrolled() } returns false

        viewModel = BiometricLoginViewModel(biometricAuthenticator, context)

        val state = viewModel.uiState.value
        assertFalse(state.isBiometricAvailable)
    }

    @Test
    fun `authenticate success should update state`() =
        runTest {
            every { biometricAuthenticator.isBiometricAvailable() } returns true
            every { biometricAuthenticator.isBiometricEnrolled() } returns true
            coEvery {
                biometricAuthenticator.authenticate(any(), any(), any(), any())
            } returns flowOf(BiometricResult.Success)

            viewModel = BiometricLoginViewModel(biometricAuthenticator, context)

            viewModel.authenticate()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.biometricSuccess)
            assertFalse(state.isLoading)
        }

    @Test
    fun `authenticate error should update state with error message`() =
        runTest {
            every { biometricAuthenticator.isBiometricAvailable() } returns true
            every { biometricAuthenticator.isBiometricEnrolled() } returns true
            coEvery {
                biometricAuthenticator.authenticate(any(), any(), any(), any())
            } returns flowOf(BiometricResult.Error(1, "Biometric authentication failed"))

            viewModel = BiometricLoginViewModel(biometricAuthenticator, context)

            viewModel.authenticate()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("Biometric authentication failed", state.error)
        }

    @Test
    fun `authenticate cancelled should reset loading`() =
        runTest {
            every { biometricAuthenticator.isBiometricAvailable() } returns true
            every { biometricAuthenticator.isBiometricEnrolled() } returns true
            coEvery {
                biometricAuthenticator.authenticate(any(), any(), any(), any())
            } returns flowOf(BiometricResult.Cancelled)

            viewModel = BiometricLoginViewModel(biometricAuthenticator, context)

            viewModel.authenticate()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.biometricSuccess)
        }

    @Test
    fun `onBiometricSuccessHandled should reset biometricSuccess`() {
        every { biometricAuthenticator.isBiometricAvailable() } returns true
        every { biometricAuthenticator.isBiometricEnrolled() } returns true

        viewModel = BiometricLoginViewModel(biometricAuthenticator, context)

        // Simulate success state
        viewModel.authenticate()

        viewModel.onBiometricSuccessHandled()

        val state = viewModel.uiState.value
        assertFalse(state.biometricSuccess)
    }

    @Test
    fun `onErrorShown should clear error`() {
        every { biometricAuthenticator.isBiometricAvailable() } returns true
        every { biometricAuthenticator.isBiometricEnrolled() } returns true

        viewModel = BiometricLoginViewModel(biometricAuthenticator, context)

        viewModel.onErrorShown()

        val state = viewModel.uiState.value
        assertNull(state.error)
    }
}
