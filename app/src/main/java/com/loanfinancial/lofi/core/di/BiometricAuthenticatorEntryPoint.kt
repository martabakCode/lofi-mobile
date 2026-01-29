package com.loanfinancial.lofi.core.di

import com.loanfinancial.lofi.core.biometric.BiometricAuthenticator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface BiometricAuthenticatorEntryPoint {
    fun biometricAuthenticator(): BiometricAuthenticator
}
