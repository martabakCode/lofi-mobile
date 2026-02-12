package com.loanfinancial.lofi.core.biometric

import com.loanfinancial.lofi.core.network.NetworkManager
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.data.remote.api.PinApi
import com.loanfinancial.lofi.data.remote.api.PinVerificationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BiometricAuthenticatorWithPinFallback @Inject constructor(
    private val biometricAuthenticator: BiometricAuthenticator,
    private val pinApi: PinApi,
    private val dataStoreManager: DataStoreManager,
    private val networkManager: NetworkManager,
) {
    suspend fun authenticateWithFallback(): Flow<AuthResult> = flow {
        if (biometricAuthenticator.isBiometricAvailable() &&
            biometricAuthenticator.isBiometricEnrolled()
        ) {
            // Try biometric first
            biometricAuthenticator.authenticate("Authentication", "Use biometric to verify")
                .collect { result ->
                    emit(result.toAuthResult())
                }
        } else {
            // Fallback to PIN
            emit(AuthResult.PinRequired)
        }
    }

    suspend fun verifyPin(pin: String): Flow<PinVerificationResult> = flow {
        emit(PinVerificationResult.Loading)

        if (!networkManager.isNetworkAvailable()) {
            emit(PinVerificationResult.Error("No internet connection. Please try again."))
            return@flow
        }

        try {
            val request = PinVerificationRequest(pin = pin, purpose = "LOAN_SUBMISSION")
            val response = pinApi.verifyPin(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    val data = body.data
                    if (data.isValid) {
                        emit(PinVerificationResult.Success)
                    } else {
                        if (data.isLocked) {
                            emit(PinVerificationResult.Locked(lockedUntil = data.lockedUntil))
                        } else {
                            emit(PinVerificationResult.Invalid(remainingAttempts = data.remainingAttempts))
                        }
                    }
                } else {
                    emit(PinVerificationResult.Error(body?.message ?: "Verification failed"))
                }
            } else {
                 emit(PinVerificationResult.Error("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(PinVerificationResult.Error(e.message ?: "Network error"))
        }
    }
}

fun BiometricResult.toAuthResult(): AuthResult {
    return when (this) {
        is BiometricResult.Success -> AuthResult.Success
        is BiometricResult.Error -> AuthResult.Error(this.errorMessage)
        is BiometricResult.Cancelled -> AuthResult.Error("Cancelled") // Or handle differently
        BiometricResult.NotAvailable -> AuthResult.PinRequired
        BiometricResult.NotEnrolled -> AuthResult.PinRequired
    }
}

sealed class AuthResult {
    data object Success : AuthResult()
    data object PinRequired : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class PinVerificationResult {
    data object Loading : PinVerificationResult()
    data object Success : PinVerificationResult()
    data class Invalid(val remainingAttempts: Int) : PinVerificationResult()
    data class Locked(val lockedUntil: String?) : PinVerificationResult()
    data class Error(val message: String) : PinVerificationResult()
}
