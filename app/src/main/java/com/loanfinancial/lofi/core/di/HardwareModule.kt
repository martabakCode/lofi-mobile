package com.loanfinancial.lofi.core.di

import androidx.fragment.app.FragmentActivity
import com.loanfinancial.lofi.core.biometric.BiometricAuthenticator
import com.loanfinancial.lofi.core.biometric.BiometricAuthenticatorImpl
import com.loanfinancial.lofi.core.location.LocationManager
import com.loanfinancial.lofi.core.location.LocationManagerImpl
import com.loanfinancial.lofi.core.media.CameraManager
import com.loanfinancial.lofi.core.media.CameraManagerImpl
import com.loanfinancial.lofi.core.media.UploadManager
import com.loanfinancial.lofi.core.media.UploadManagerImpl
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
        impl: LocationManagerImpl,
    ): LocationManager

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
}
