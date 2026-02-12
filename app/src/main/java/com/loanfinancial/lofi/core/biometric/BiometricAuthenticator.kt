package com.loanfinancial.lofi.core.biometric

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

sealed class BiometricResult {
    data object Success : BiometricResult()

    data class Error(
        val errorCode: Int,
        val errorMessage: String,
    ) : BiometricResult()

    data object Cancelled : BiometricResult()

    data object NotAvailable : BiometricResult()

    data object NotEnrolled : BiometricResult()
}

interface BiometricAuthenticator {
    suspend fun authenticate(
        title: String,
        subtitle: String = "",
        description: String = "",
        negativeButtonText: String = "Cancel",
    ): Flow<BiometricResult>

    fun isBiometricAvailable(): Boolean

    fun isBiometricEnrolled(): Boolean
}

@Singleton
class BiometricAuthenticatorImpl
    @Inject
    constructor(
        private val activity: FragmentActivity,
    ) : BiometricAuthenticator {
        private val biometricManager by lazy {
            androidx.biometric.BiometricManager.from(activity)
        }

        override fun isBiometricAvailable(): Boolean =
            biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK,
            ) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS

        override fun isBiometricEnrolled(): Boolean {
            val canAuthenticate =
                biometricManager.canAuthenticate(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK,
                )
            return canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
        }

        override suspend fun authenticate(
            title: String,
            subtitle: String,
            description: String,
            negativeButtonText: String,
        ): Flow<BiometricResult> =
            flow {
                if (!isBiometricAvailable()) {
                    emit(BiometricResult.NotAvailable)
                    return@flow
                }

                if (!isBiometricEnrolled()) {
                    emit(BiometricResult.NotEnrolled)
                    return@flow
                }

                val promptInfo =
                    BiometricPrompt.PromptInfo
                        .Builder()
                        .setTitle(title)
                        .setSubtitle(subtitle)
                        .setDescription(description)
                        .setNegativeButtonText(negativeButtonText)
                        .setAllowedAuthenticators(
                            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK,
                        ).build()

                val result =
                    kotlinx.coroutines.suspendCancellableCoroutine<BiometricResult> { continuation ->
                        val biometricPrompt =
                            BiometricPrompt(
                                activity,
                                activity.mainExecutor,
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationSucceeded(
                                        result: BiometricPrompt.AuthenticationResult,
                                    ) {
                                        continuation.resume(BiometricResult.Success)
                                    }

                                    override fun onAuthenticationError(
                                        errorCode: Int,
                                        errString: CharSequence,
                                    ) {
                                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                                            errorCode == BiometricPrompt.ERROR_USER_CANCELED
                                        ) {
                                            continuation.resume(BiometricResult.Cancelled)
                                        } else {
                                            continuation.resume(
                                                BiometricResult.Error(
                                                    errorCode,
                                                    errString.toString(),
                                                ),
                                            )
                                        }
                                    }

                                    override fun onAuthenticationFailed() {
                                        // Do nothing, wait for error or success
                                    }
                                },
                            )

                        biometricPrompt.authenticate(promptInfo)

                        continuation.invokeOnCancellation {
                            biometricPrompt.cancelAuthentication()
                        }
                    }

                emit(result)
            }
    }
