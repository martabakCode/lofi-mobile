package com.loanfinancial.lofi.data.repository

import com.loanfinancial.lofi.TestDataFactory
import com.loanfinancial.lofi.data.local.datastore.PreferencesManager
import com.loanfinancial.lofi.data.remote.api.UserApi
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest {
    @MockK
    private lateinit var userApi: UserApi

    @MockK
    private lateinit var preferencesManager: PreferencesManager

    private lateinit var repository: IAuthRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = AuthRepositoryImpl(userApi, preferencesManager)
    }

    @Test
    fun `login success should save tokens`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            val accessToken = TestDataFactory.TEST_ACCESS_TOKEN
            val refreshToken = TestDataFactory.TEST_REFRESH_TOKEN

            coEvery {
                userApi.login(any())
            } returns
                Response.success(
                    com.loanfinancial.lofi.data.model.dto.LoginResponse(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        user =
                            com.loanfinancial.lofi.data.model.dto.UserDto(
                                id = "user_123",
                                email = email,
                                name = "Test User",
                            ),
                    ),
                )

            coEvery { preferencesManager.saveAuthTokens(any(), any()) } just runs
            coEvery { preferencesManager.saveUserInfo(any(), any(), any(), any(), any()) } just runs

            val result = repository.login(email, password)

            assertTrue(result is com.loanfinancial.lofi.core.util.Resource.Success)
            coVerify { preferencesManager.saveAuthTokens(accessToken, refreshToken) }
        }

    @Test
    fun `login failure should return error`() =
        runTest {
            val email = "test@example.com"
            val password = "wrongpassword"

            coEvery {
                userApi.login(any())
            } returns
                Response.error(
                    401,
                    okhttp3.ResponseBody.create(null, "Unauthorized"),
                )

            val result = repository.login(email, password)

            assertTrue(result is com.loanfinancial.lofi.core.util.Resource.Error)
        }

    @Test
    fun `logout should clear auth data`() =
        runTest {
            coEvery { preferencesManager.clearAuthData() } just runs

            repository.logout()

            coVerify { preferencesManager.clearAuthData() }
        }

    @Test
    fun `isLoggedIn should return true when token exists`() =
        runTest {
            coEvery { preferencesManager.isLoggedIn() } returns true

            val result = repository.isLoggedIn()

            assertTrue(result)
        }

    @Test
    fun `isLoggedIn should return false when no token`() =
        runTest {
            coEvery { preferencesManager.isLoggedIn() } returns false

            val result = repository.isLoggedIn()

            assertFalse(result)
        }

    @Test
    fun `changePassword success should return success`() =
        runTest {
            val oldPassword = "oldpassword"
            val newPassword = "newpassword"

            coEvery {
                userApi.changePassword(any())
            } returns
                Response.success(
                    com.loanfinancial.lofi.data.model.dto.ChangePasswordResponse(
                        success = true,
                        message = "Password changed successfully",
                    ),
                )

            val result = repository.changePassword(oldPassword, newPassword)

            assertTrue(result is com.loanfinancial.lofi.core.util.Resource.Success)
        }
}
