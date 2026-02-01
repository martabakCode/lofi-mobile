package com.loanfinancial.lofi.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.loanfinancial.lofi.core.network.ApiService
import com.loanfinancial.lofi.data.local.dao.UserDao
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.model.dto.*
import com.loanfinancial.lofi.data.model.entity.UserEntity
import com.loanfinancial.lofi.domain.model.User
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        private val dataStoreManager: DataStoreManager,
        private val apiService: ApiService,
        private val userDao: UserDao,
        private val firebaseAuth: FirebaseAuth,
    ) : IAuthRepository {
        override suspend fun login(request: LoginRequest): Result<LoginResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.login(request)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success && body.data != null) {
                            dataStoreManager.saveAuthTokens(body.data.accessToken, body.data.refreshToken)
                            Result.success(body)
                        } else {
                            Result.failure(Exception(body.message))
                        }
                    } else {
                        Result.failure(Exception("Login failed: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getUserInfo(): Result<UserResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.getUserInfo()
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success && body.data != null) {
                            val data = body.data
                            val existingUser = userDao.getUser().firstOrNull()

                            val newUser =
                                UserEntity(
                                    id = data.id,
                                    fullName = existingUser?.fullName ?: "",
                                    username = data.username,
                                    email = data.email,
                                    phoneNumber = existingUser?.phoneNumber ?: "",
                                    createdAt = existingUser?.createdAt ?: "",
                                    roles = data.roles,
                                )
                            userDao.insertUser(newUser)
                            Result.success(body)
                        } else {
                            Result.failure(Exception(body.message))
                        }
                    } else {
                        Result.failure(Exception("Failed to get user info: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun register(request: RegisterRequest): Result<RegisterResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.register(request)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success && body.data != null) {
                            // Save access and refresh tokens from registration response
                            dataStoreManager.saveAuthTokens(body.data.accessToken, body.data.refreshToken)
                            Result.success(body)
                        } else {
                            Result.failure(Exception(body.message))
                        }
                    } else {
                        Result.failure(Exception("Registration failed: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun logout(): Result<LogoutResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.logout()
                    if (response.isSuccessful && response.body() != null) {
                        dataStoreManager.clear()
                        userDao.clearUser()
                        // Also sign out from Firebase
                        firebaseAuth.signOut()
                        Result.success(response.body()!!)
                    } else {
                        Result.failure(Exception("Logout failed: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun changePassword(request: ChangePasswordRequest): Result<ChangePasswordResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.changePassword(request)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success) {
                            Result.success(body)
                        } else {
                            Result.failure(Exception(body.message))
                        }
                    } else {
                        Result.failure(Exception("Change password failed: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun signInWithCredential(credential: AuthCredential): Result<FirebaseUser> =
            withContext(Dispatchers.IO) {
                try {
                    val authResult = firebaseAuth.signInWithCredential(credential).await()
                    if (authResult.user != null) {
                        Result.success(authResult.user!!)
                    } else {
                        Result.failure(Exception("Firebase user is null"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun googleAuth(request: GoogleAuthRequest): Result<GoogleAuthResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.googleAuth(request)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success && body.data != null) {
                            // Save access and refresh tokens from Google auth response
                            dataStoreManager.saveAuthTokens(body.data.accessToken, body.data.refreshToken)
                            Result.success(body)
                        } else {
                            Result.failure(Exception(body.message))
                        }
                    } else {
                        Result.failure(Exception("Google authentication failed: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getFirebaseIdToken(): Result<String> =
            withContext(Dispatchers.IO) {
                try {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val tokenResult = user.getIdToken(false).await()
                        val token = tokenResult.token
                        if (token != null) {
                            Result.success(token)
                        } else {
                            Result.failure(Exception("Firebase ID Token is null"))
                        }
                    } else {
                        Result.failure(Exception("No Firebase user signed in"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override fun getUser(): Flow<User?> = userDao.getUser().map { it?.toDomain() }
    }
