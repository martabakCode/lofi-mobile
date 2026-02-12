package com.loanfinancial.lofi.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.loanfinancial.lofi.TestDataFactory
import com.loanfinancial.lofi.core.network.ApiService
import com.loanfinancial.lofi.data.local.dao.UserDao
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.model.dto.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest {
    @MockK
    private lateinit var apiService: ApiService

    @MockK
    private lateinit var dataStoreManager: DataStoreManager
    
    @MockK
    private lateinit var userDao: UserDao
    
    @MockK
    private lateinit var firebaseAuth: FirebaseAuth
    
    @MockK
    private lateinit var pinApi: com.loanfinancial.lofi.data.remote.api.PinApi

    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = AuthRepositoryImpl(dataStoreManager, apiService, userDao, firebaseAuth, pinApi)
    }

    @Test
    fun `login success should save tokens`() =
        runTest {
            val email = "test@example.com"
            val password = "password123"
            val accessToken = TestDataFactory.TEST_ACCESS_TOKEN
            val refreshToken = TestDataFactory.TEST_REFRESH_TOKEN
            val request = LoginRequest(email, password, "fcmToken")

            coEvery {
                apiService.login(any())
            } returns
                Response.success(
                    LoginResponse(
                        success = true,
                        message = "Success",
                        data = AuthTokenData(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            expiresIn = 3600,
                            tokenType = "Bearer"
                        ),
                    ),
                )

            coEvery { dataStoreManager.saveAuthTokens(any(), any()) } just runs

            val result = repository.login(request)

            assertTrue(result.isSuccess)
            coVerify { dataStoreManager.saveAuthTokens(accessToken, refreshToken) }
        }

    @Test
    fun `login failure should return error`() =
        runTest {
            val email = "test@example.com"
            val password = "wrongpassword"
            val request = LoginRequest(email, password, "fcmToken")

            coEvery {
                apiService.login(any())
            } returns
                Response.error(
                    401,
                    ResponseBody.create(null, "Unauthorized"),
                )

            val result = repository.login(request)

            assertTrue(result.isFailure)
        }

    @Test
    fun `logout should clear auth data`() =
        runTest {
            coEvery { apiService.logout() } returns Response.success(LogoutResponse(true, "Logged out"))
            coEvery { dataStoreManager.clear() } just runs
            coEvery { userDao.clearUser() } just runs
            every { firebaseAuth.signOut() } just runs

            repository.logout()

            coVerify { dataStoreManager.clear() }
            coVerify { userDao.clearUser() }
            verify { firebaseAuth.signOut() }
        }

    @Test
    fun `changePassword success should return success`() =
        runTest {
            val request = ChangePasswordRequest("old", "new")

            coEvery {
                apiService.changePassword(request)
            } returns
                Response.success(
                    ChangePasswordResponse(
                        success = true,
                        message = "Password changed successfully",
                    ),
                )

            val result = repository.changePassword(request)

            assertTrue(result.isSuccess)
        }
}
