package com.loanfinancial.lofi.core.di

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.loanfinancial.lofi.core.biometric.BiometricAuthenticator
import com.loanfinancial.lofi.core.biometric.BiometricAuthenticatorImpl
import com.loanfinancial.lofi.core.location.LocationManager
import com.loanfinancial.lofi.core.location.NativeLocationManager
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.core.media.CameraManagerImpl
import com.loanfinancial.lofi.core.media.UploadManager
import com.loanfinancial.lofi.core.media.UploadManagerImpl
import com.loanfinancial.lofi.core.location.LocationFallbackManager
import com.loanfinancial.lofi.core.location.LocationFallbackManagerImpl
import com.loanfinancial.lofi.ui.features.auth.biometric.BiometricLoginViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {
    @Binds
    @Singleton
    abstract fun bindLocationManager(
        impl: NativeLocationManager,
    ): LocationManager

    @Binds
    @Singleton
    abstract fun bindLocationFallbackManager(
        impl: LocationFallbackManagerImpl,
    ): LocationFallbackManager

    @Binds
    @Singleton
    abstract fun bindCameraManager(
        impl: CameraManagerImpl,
    ): CameraManager

    @Binds
    @Singleton
    abstract fun bindUploadManager(
        impl: UploadManagerImpl,
    ): UploadManager
}

@Module
@InstallIn(ActivityComponent::class)
object BiometricModule {
    @Provides
    @ActivityScoped
    fun provideBiometricAuthenticator(
        activity: FragmentActivity,
    ): BiometricAuthenticator = BiometricAuthenticatorImpl(activity)

    @Provides
    fun provideBiometricLoginViewModelFactory(
        biometricAuthenticator: BiometricAuthenticator,
        activity: FragmentActivity,
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(BiometricLoginViewModel::class.java)) {
                    return BiometricLoginViewModel(biometricAuthenticator, activity) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
