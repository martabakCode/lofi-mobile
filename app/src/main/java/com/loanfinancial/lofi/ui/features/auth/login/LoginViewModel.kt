package com.loanfinancial.lofi.ui.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.loanfinancial.lofi.data.local.datastore.PreferencesManager
import com.loanfinancial.lofi.data.model.dto.GoogleAuthRequest
import com.loanfinancial.lofi.data.model.dto.LoginRequest
import com.loanfinancial.lofi.data.remote.firebase.IFcmTokenManager
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import com.loanfinancial.lofi.domain.usecase.auth.GetFirebaseIdTokenUseCase
import com.loanfinancial.lofi.domain.usecase.auth.GoogleAuthUseCase
import com.loanfinancial.lofi.domain.usecase.auth.LoginUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val showEnableBiometricDialog: Boolean = false,
)

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val loginUseCase: LoginUseCase,
        private val getUserUseCase: GetUserUseCase,
        private val googleAuthUseCase: GoogleAuthUseCase,
        private val getFirebaseIdTokenUseCase: GetFirebaseIdTokenUseCase,
        private val authRepository: IAuthRepository,
        private val fcmTokenManager: IFcmTokenManager,
        private val preferencesManager: PreferencesManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

        init {
            checkBiometricStatus()
        }

        private fun checkBiometricStatus() {
            viewModelScope.launch {
                val isEnabled = preferencesManager.isBiometricEnabled()
                _uiState.update { it.copy(isBiometricEnabled = isEnabled) }
            }
        }

        fun onEmailChange(email: String) {
            _uiState.update { it.copy(email = email, emailError = null, loginError = null) }
        }

        fun onPasswordChange(password: String) {
            _uiState.update { it.copy(password = password, passwordError = null, loginError = null) }
        }

        fun onLoginClick() {
            if (validate()) {
                performLogin()
            }
        }

        private fun validate(): Boolean {
            var isValid = true
            val currentState = _uiState.value
            var emailError: String? = null
            var passwordError: String? = null

            if (currentState.email.isBlank()) {
                emailError = "Email cannot be empty"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(currentState.email)
                    .matches()
            ) {
                emailError = "Invalid email format"
                isValid = false
            }

            if (currentState.password.isBlank()) {
                passwordError = "Password cannot be empty"
                isValid = false
            } else if (currentState.password.length < 6) {
                passwordError = "Password must be at least 6 characters"
                isValid = false
            }

            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return isValid
        }

        private fun performLogin() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, loginError = null) }

                val fcmToken = fcmTokenManager.getToken() ?: ""

                val request =
                    LoginRequest(
                        email = _uiState.value.email,
                        password = _uiState.value.password,
                        fcmToken = fcmToken,
                    )

                // 1. Attempt Login
                val loginResult = loginUseCase(request)

                if (loginResult.isSuccess) {
                    // 2. Fetch User Info to check Role
                    val userResult = getUserUseCase()

                    if (userResult.isSuccess) {
                        val user = userResult.getOrNull()?.data
                        if (user != null && user.roles.contains("ROLE_CUSTOMER")) {
                            // 3a. Role is valid
                            _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
                        } else {
                            // 3b. Role invalid - Logout and show error
                            authRepository.logout()
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    loginError = "Unauthorized: Access restricted to Customers only.",
                                )
                            }
                        }
                    } else {
                        // Failed to get user info
                        authRepository.logout()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loginError = "Failed to verify user role.",
                            )
                        }
                    }
                } else {
                    val errorMessage = loginResult.exceptionOrNull()?.message ?: "Login failed"
                    _uiState.update { it.copy(isLoading = false, loginError = errorMessage) }
                }
            }
        }

        fun onLoginSuccessHandled() {
            _uiState.update { it.copy(isLoginSuccessful = false) }
        }

        fun onEnableBiometric() {
            viewModelScope.launch {
                preferencesManager.setBiometricEnabled(true)
                _uiState.update {
                    it.copy(
                        showEnableBiometricDialog = false,
                        isBiometricEnabled = true,
                    )
                }
            }
        }

        fun onSkipBiometric() {
            _uiState.update { it.copy(showEnableBiometricDialog = false) }
        }

        fun shouldShowEnableBiometricDialog() {
            _uiState.update { it.copy(showEnableBiometricDialog = true) }
        }

        fun onErrorShown() {
            _uiState.update { it.copy(loginError = null) }
        }

        /**
         * Authenticate with Google using idToken
         * Sends the token to backend for verification and token generation
         */
        fun onGoogleLogin(
            idToken: String,
            latitude: Double? = null,
            longitude: Double? = null,
        ) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, loginError = null) }

                // 1. Step: Sign in to Firebase with Google idToken to get Firebase User
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val firebaseResult = authRepository.signInWithCredential(credential)

                if (firebaseResult.isSuccess) {
                    // 2. Step: Get Firebase ID Token (this is what Backend Admin SDK verifies)
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
                        val googleAuthResult = googleAuthUseCase(request)

                        if (googleAuthResult.isSuccess) {
                            // 4. Step: Fetch User Info to check Role
                            val userResult = getUserUseCase()

                            if (userResult.isSuccess) {
                                val user = userResult.getOrNull()?.data
                                if (user != null && user.roles.contains("ROLE_CUSTOMER")) {
                                    _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
                                } else {
                                    authRepository.logout()
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            loginError = "Unauthorized: Access restricted to Customers only.",
                                        )
                                    }
                                }
                            } else {
                                authRepository.logout()
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        loginError = "Failed to verify user role.",
                                    )
                                }
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    loginError = googleAuthResult.exceptionOrNull()?.message ?: "Backend Verification Failed",
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                loginError = "Failed to get Firebase token: ${tokenResult.exceptionOrNull()?.message}",
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginError = "Firebase Sign In Failed: ${firebaseResult.exceptionOrNull()?.message}",
                        )
                    }
                }
            }
        }

        fun onFacebookLogin(accessToken: String) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, loginError = null) }
                val credential = FacebookAuthProvider.getCredential(accessToken)
                val result = authRepository.signInWithCredential(credential)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginError = result.exceptionOrNull()?.message ?: "Facebook Login Failed",
                        )
                    }
                }
            }
        }
    }
