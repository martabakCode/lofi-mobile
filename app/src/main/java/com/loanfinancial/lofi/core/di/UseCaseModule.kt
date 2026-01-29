package com.loanfinancial.lofi.core.di

import com.loanfinancial.lofi.domain.repository.IAuthRepository
import com.loanfinancial.lofi.domain.repository.INotificationRepository
import com.loanfinancial.lofi.domain.usecase.auth.GetFirebaseIdTokenUseCase
import com.loanfinancial.lofi.domain.usecase.auth.GoogleAuthUseCase
import com.loanfinancial.lofi.domain.usecase.auth.LoginUseCase
import com.loanfinancial.lofi.domain.usecase.auth.RegisterUseCase
import com.loanfinancial.lofi.domain.usecase.notification.GetNotificationsUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideLoginUseCase(repository: IAuthRepository): LoginUseCase = LoginUseCase(repository)

    @Provides
    @Singleton
    fun provideGetUserUseCase(repository: IAuthRepository): GetUserUseCase = GetUserUseCase(repository)

    @Provides
    @Singleton
    fun provideGetNotificationsUseCase(repository: INotificationRepository): GetNotificationsUseCase = GetNotificationsUseCase(repository)

    @Provides
    @Singleton
    fun provideGoogleAuthUseCase(repository: IAuthRepository): GoogleAuthUseCase = GoogleAuthUseCase(repository)

    @Provides
    @Singleton
    fun provideRegisterUseCase(repository: IAuthRepository): RegisterUseCase = RegisterUseCase(repository)

    @Provides
    @Singleton
    fun provideGetFirebaseIdTokenUseCase(repository: IAuthRepository): GetFirebaseIdTokenUseCase = GetFirebaseIdTokenUseCase(repository)
}
