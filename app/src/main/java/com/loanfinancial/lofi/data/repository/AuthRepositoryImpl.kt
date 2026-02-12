package com.loanfinancial.lofi.data.repository

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.loanfinancial.lofi.core.network.ApiService
import com.loanfinancial.lofi.data.local.dao.UserDao
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.model.dto.*
import com.loanfinancial.lofi.data.model.entity.UserEntity
import com.loanfinancial.lofi.data.remote.api.AuthSourceResponse
import com.loanfinancial.lofi.data.remote.api.PinApi
import com.loanfinancial.lofi.data.remote.api.PinStatusResponse
import com.loanfinancial.lofi.data.remote.api.SetGooglePinRequest
import com.loanfinancial.lofi.data.remote.api.UpdateGooglePinRequest
import com.loanfinancial.lofi.domain.model.User
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.ConnectException
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject
    constructor(
        private val dataStoreManager: DataStoreManager,
        private val apiService: ApiService,
        private val userDao: UserDao,
        private val firebaseAuth: FirebaseAuth,
        private val pinApi: PinApi,
    ) : IAuthRepository {
        override suspend fun login(request: LoginRequest): Result<LoginResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.login(request)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success && body.data != null) {
                            dataStoreManager.saveAuthTokens(body.data.accessToken, body.data.refreshToken)
                            dataStoreManager.saveProfileStatus(
                                pinSet = body.data.pinSet,
                                profileCompleted = body.data.profileCompleted,
                            )
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

                            // Save user info to DataStore for GetMyLoansUseCase
                            dataStoreManager.saveUserInfo(
                                userId = data.id,
                                email = data.email,
                                name = data.username,
                            )

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
                            dataStoreManager.saveProfileStatus(
                                pinSet = body.data.pinSet,
                                profileCompleted = body.data.profileCompleted,
                            )
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
                    Log.d("AuthRepositoryImpl", "Starting Google auth to: ${apiService.javaClass.simpleName}")
                    val response = apiService.googleAuth(request)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success && body.data != null) {
                            // Save access and refresh tokens from Google auth response
                            dataStoreManager.saveAuthTokens(body.data.accessToken, body.data.refreshToken)
                            dataStoreManager.saveProfileStatus(
                                pinSet = body.data.pinSet,
                                profileCompleted = body.data.profileCompleted,
                            )
                            Result.success(body)
                        } else {
                            Result.failure(Exception(body.message))
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("AuthRepositoryImpl", "Google authentication failed with code ${response.code()}: $errorBody")
                        Result.failure(Exception("Google authentication failed: ${response.message()} ($errorBody)"))
                    }
                } catch (e: Exception) {
                    Log.e("AuthRepositoryImpl", "Google auth exception: ${e.javaClass.simpleName} - ${e.message}")
                    when (e) {
                        is ConnectException -> {
                            Log.e(
                                "AuthRepositoryImpl",
                                "CONNECT EXCEPTION: Server at 10.10.90.218:8080 is unreachable. " +
                                    "Possible causes: 1) Server is not running, 2) Firewall blocking port 8080, " +
                                    "3) Device is on different network than server",
                            )
                        }
                        is java.net.SocketTimeoutException -> {
                            Log.e("AuthRepositoryImpl", "SOCKET TIMEOUT: Server is not responding in time")
                        }
                        is java.net.UnknownHostException -> {
                            Log.e("AuthRepositoryImpl", "UNKNOWN HOST: Cannot resolve hostname 10.10.90.218")
                        }
                    }
                    Result.failure(e)
                }
            }

        override suspend fun getFirebaseIdToken(): Result<String> =
            withContext(Dispatchers.IO) {
                try {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val tokenResult = user.getIdToken(true).await()
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

        override suspend fun hasPin(): Result<Boolean> =
            withContext(Dispatchers.IO) {
                try {
                    val response = pinApi.hasPin()
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success) {
                            Result.success(body.data ?: false)
                        } else {
                            Result.failure(Exception(body.message))
                        }
                    } else {
                        Result.failure(Exception("Failed to check PIN: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun setPin(pin: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val response =
                        pinApi.setPin(
                            com.loanfinancial.lofi.data.remote.api
                                .SetPinRequest(pin),
                        )
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success) {
                            Result.success(Unit)
                        } else {
                            Result.failure(Exception(body.message))
                        }
                    } else {
                        Result.failure(Exception("Failed to set PIN: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun verifyPin(
            pin: String,
            purpose: String,
        ): Result<com.loanfinancial.lofi.data.remote.api.PinVerificationResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response =
                        pinApi.verifyPin(
                            com.loanfinancial.lofi.data.remote.api
                                .PinVerificationRequest(pin, purpose),
                        )
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success && body.data != null) {
                            Result.success(body.data)
                        } else {
                            Result.failure(Exception(body.message))
                        }
                    } else {
                        Result.failure(Exception("Failed to verify PIN: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override fun getUser(): Flow<User?> = userDao.getUser().map { it?.toDomain() }

        override suspend fun getAuthSource(): Result<AuthSourceResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response = pinApi.getAuthSource()
                    if (response.isSuccessful && response.body()?.success == true) {
                        Result.success(response.body()!!.data!!)
                    } else {
                        Result.failure(Exception(response.body()?.message ?: "Failed to get auth source"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun setGooglePin(pin: String): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val response = pinApi.setGooglePin(SetGooglePinRequest(pin))
                    if (response.isSuccessful && response.body()?.success == true) {
                        dataStoreManager.setPinSet(true)
                        Result.success(Unit)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val baseMessage = response.body()?.message ?: errorBody ?: "Failed to set Google PIN"
                        val errorMessage =
                            when (response.code()) {
                                500 -> "Server error. Please try again later or contact support."
                                400 -> {
                                    // Check if PIN is already set
                                    if (baseMessage.contains("already set", ignoreCase = true) ||
                                        baseMessage.contains("update PIN", ignoreCase = true)
                                    ) {
                                        "PIN is already set. Use update PIN endpoint"
                                    } else {
                                        "Invalid request. Please check your PIN."
                                    }
                                }
                                401 -> "Session expired. Please login again."
                                else -> baseMessage
                            }
                        Result.failure(Exception(errorMessage))
                    }
                } catch (e: Exception) {
                    Result.failure(Exception("Network error: ${e.message}"))
                }
            }

        override suspend fun updateGooglePin(
            oldPin: String,
            newPin: String,
        ): Result<Unit> =
            withContext(Dispatchers.IO) {
                try {
                    val response = pinApi.updateGooglePin(UpdateGooglePinRequest(oldPin, newPin))
                    if (response.isSuccessful && response.body()?.success == true) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(response.body()?.message ?: "Failed to update Google PIN"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        override suspend fun getPinStatus(): Result<PinStatusResponse> =
            withContext(Dispatchers.IO) {
                try {
                    val response = pinApi.getPinStatus()
                    if (response.isSuccessful && response.body()?.success == true) {
                        Result.success(response.body()!!.data!!)
                    } else {
                        Result.failure(Exception(response.body()?.message ?: "Failed to get PIN status"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
    }
