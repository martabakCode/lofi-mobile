package com.loanfinancial.lofi.core.di

import com.loanfinancial.lofi.data.remote.firebase.FcmTokenManager
import com.loanfinancial.lofi.data.remote.firebase.IFcmTokenManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {
    @Binds
    @Singleton
    abstract fun bindFcmTokenManager(
        fcmTokenManager: FcmTokenManager,
    ): IFcmTokenManager
}
