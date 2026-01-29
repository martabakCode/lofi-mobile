package com.loanfinancial.lofi.ui.features.auth

import app.cash.turbine.test
import com.loanfinancial.lofi.MainDispatcherRule
import com.loanfinancial.lofi.core.util.Resource
import com.loanfinancial.lofi.domain.usecase.auth.LoginUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var loginUseCase: LoginUseCase

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = LoginViewModel(loginUseCase)
    }

    @Test
    fun `initial state should be empty`() =
        runTest {
            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals("", state.email)
                assertEquals("", state.password)
                assertFalse(state.isLoading)
                assertFalse(state.isSuccess)
                assertNull(state.error)
            }
        }

    @Test
    fun `email change should update state`() =
        runTest {
            viewModel.onEmailChange("test@example.com")

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals("test@example.com", state.email)
            }
        }

    @Test
    fun `password change should update state`() =
        runTest {
            viewModel.onPasswordChange("password123")

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals("password123", state.password)
            }
        }

    @Test
    fun `login success should update state to success`() =
        runTest {
            coEvery {
                loginUseCase(any(), any())
            } returns flowOf(Resource.Success(Unit))

            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onLogin()

            viewModel.uiState.test {
                skipItems(2)
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)

                val successState = awaitItem()
                assertTrue(successState.isSuccess)
                assertFalse(successState.isLoading)
            }
        }

    @Test
    fun `login error should show error message`() =
        runTest {
            val errorMessage = "Invalid credentials"
            coEvery {
                loginUseCase(any(), any())
            } returns flowOf(Resource.Error(errorMessage))

            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("wrongpassword")
            viewModel.onLogin()

            viewModel.uiState.test {
                skipItems(2)
                val loadingState = awaitItem()
                assertTrue(loadingState.isLoading)

                val errorState = awaitItem()
                assertEquals(errorMessage, errorState.error)
                assertFalse(errorState.isLoading)
            }
        }

    @Test
    fun `reset should clear state`() =
        runTest {
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("password123")
            viewModel.onReset()

            viewModel.uiState.test {
                val state = awaitItem()
                assertEquals("", state.email)
                assertEquals("", state.password)
                assertFalse(state.isLoading)
                assertFalse(state.isSuccess)
                assertNull(state.error)
            }
        }
}
