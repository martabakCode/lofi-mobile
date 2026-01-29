package com.loanfinancial.lofi.ui.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.loanfinancial.lofi.data.model.dto.GoogleAuthRequest
import com.loanfinancial.lofi.data.model.dto.RegisterRequest
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import com.loanfinancial.lofi.domain.usecase.auth.GetFirebaseIdTokenUseCase
import com.loanfinancial.lofi.domain.usecase.auth.GoogleAuthUseCase
import com.loanfinancial.lofi.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel
    @Inject
    constructor(
        private val registerUseCase: RegisterUseCase,
        private val googleAuthUseCase: GoogleAuthUseCase,
        private val getFirebaseIdTokenUseCase: GetFirebaseIdTokenUseCase,
        private val authRepository: IAuthRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
        val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

        fun register(
            fullName: String,
            username: String,
            email: String,
            password: String,
            phoneNumber: String,
        ) {
            _uiState.value = RegisterUiState.Loading
            val request =
                RegisterRequest(
                    fullName = fullName,
                    username = username,
                    email = email,
                    password = password,
                    phoneNumber = phoneNumber,
                )
            viewModelScope.launch {
                val result = registerUseCase(request)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response?.success == true) {
                        _uiState.value = RegisterUiState.Success(response.message)
                    } else {
                        _uiState.value = RegisterUiState.Error(response?.message ?: "Registration failed")
                    }
                } else {
                    _uiState.value = RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            }
        }

        /**
         * Authenticate/Register with Google using idToken
         * This sends the token to backend for verification and token generation
         */
        fun onGoogleRegister(
            idToken: String,
            latitude: Double? = null,
            longitude: Double? = null,
        ) {
            viewModelScope.launch {
                _uiState.value = RegisterUiState.Loading

                // 1. Step: Sign in to Firebase with Google idToken to get Firebase User
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val firebaseResult = authRepository.signInWithCredential(credential)

                if (firebaseResult.isSuccess) {
                    // 2. Step: Get Firebase ID Token
                    val tokenResult = getFirebaseIdTokenUseCase()

                    if (tokenResult.isSuccess) {
                        val firebaseIdToken = tokenResult.getOrThrow()

                        val request =
                            GoogleAuthRequest(
                                idToken = firebaseIdToken,
                                latitude = latitude,
                                longitude = longitude,
                            )

                        // 3. Step: Authenticate with backend using FIREBASE idToken
                        val result = googleAuthUseCase(request)
                        if (result.isSuccess) {
                            val response = result.getOrNull()
                            if (response?.success == true) {
                                _uiState.value = RegisterUiState.Success(response.message)
                            } else {
                                _uiState.value = RegisterUiState.Error(response?.message ?: "Google registration failed")
                            }
                        } else {
                            _uiState.value =
                                RegisterUiState.Error(
                                    result.exceptionOrNull()?.message ?: "Backend Verification Failed",
                                )
                        }
                    } else {
                        _uiState.value = RegisterUiState.Error("Failed to get Firebase token")
                    }
                } else {
                    _uiState.value = RegisterUiState.Error("Firebase Sign In Failed")
                }
            }
        }

        fun onFacebookRegister(accessToken: String) {
            viewModelScope.launch {
                _uiState.value = RegisterUiState.Loading
                val credential = FacebookAuthProvider.getCredential(accessToken)
                val result = authRepository.signInWithCredential(credential)
                if (result.isSuccess) {
                    // Facebook registration still uses Firebase for now
                    // TODO: Implement backend Facebook auth when endpoint is available
                    _uiState.value = RegisterUiState.Success("Registered with Facebook")
                } else {
                    _uiState.value =
                        RegisterUiState.Error(
                            result.exceptionOrNull()?.message ?: "Facebook Registration Failed",
                        )
                }
            }
        }

        fun resetState() {
            _uiState.value = RegisterUiState.Idle
        }
    }

sealed class RegisterUiState {
    object Idle : RegisterUiState()

    object Loading : RegisterUiState()

    data class Success(
        val message: String,
    ) : RegisterUiState()

    data class Error(
        val message: String,
    ) : RegisterUiState()
}
