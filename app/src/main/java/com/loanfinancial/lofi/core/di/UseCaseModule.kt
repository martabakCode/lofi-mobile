package com.loanfinancial.lofi.core.di

import com.loanfinancial.lofi.domain.repository.IAuthRepository
import com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import com.loanfinancial.lofi.domain.repository.INotificationRepository
import com.loanfinancial.lofi.domain.usecase.auth.GetFirebaseIdTokenUseCase
import com.loanfinancial.lofi.domain.usecase.auth.GoogleAuthUseCase
import com.loanfinancial.lofi.domain.usecase.auth.LoginUseCase
import com.loanfinancial.lofi.domain.usecase.auth.RegisterUseCase
import com.loanfinancial.lofi.domain.usecase.auth.HasPinUseCase
import com.loanfinancial.lofi.domain.usecase.auth.SetPinUseCase
import com.loanfinancial.lofi.domain.usecase.auth.VerifyPinUseCase
import com.loanfinancial.lofi.domain.repository.IProductRepository
import com.loanfinancial.lofi.domain.usecase.user.GetAvailableProductUseCase
import com.loanfinancial.lofi.domain.usecase.user.GetProductsUseCase
import com.loanfinancial.lofi.domain.usecase.loan.CreateLoanUseCase
import com.loanfinancial.lofi.domain.usecase.loan.DeleteAllDraftsUseCase
import com.loanfinancial.lofi.domain.usecase.loan.DeleteLoanDraftUseCase
import com.loanfinancial.lofi.domain.usecase.loan.GetAllLoanDraftsUseCase
import com.loanfinancial.lofi.domain.usecase.loan.GetLoanDetailUseCase
import com.loanfinancial.lofi.domain.usecase.loan.GetLoanDraftByIdUseCase
import com.loanfinancial.lofi.domain.usecase.loan.SaveLoanDraftUseCase
import com.loanfinancial.lofi.domain.usecase.loan.SubmitLoanUseCase
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

    @Provides
    @Singleton
    fun provideHasPinUseCase(repository: IAuthRepository): HasPinUseCase = HasPinUseCase(repository)

    @Provides
    @Singleton
    fun provideSetPinUseCase(repository: IAuthRepository): SetPinUseCase = SetPinUseCase(repository)

    @Provides
    @Singleton
    fun provideVerifyPinUseCase(repository: IAuthRepository): VerifyPinUseCase = VerifyPinUseCase(repository)

    @Provides
    @Singleton
    fun provideGetProductsUseCase(repository: IProductRepository): GetProductsUseCase = GetProductsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetAvailableProductUseCase(repository: IProductRepository): GetAvailableProductUseCase = GetAvailableProductUseCase(repository)

    @Provides
    @Singleton
    fun provideGetLoanDetailUseCase(repository: ILoanRepository): GetLoanDetailUseCase = GetLoanDetailUseCase(repository)

    @Provides
    @Singleton
    fun provideCreateLoanUseCase(repository: ILoanRepository): CreateLoanUseCase = CreateLoanUseCase(repository)

    @Provides
    @Singleton
    fun provideSubmitLoanUseCase(repository: ILoanRepository): SubmitLoanUseCase = SubmitLoanUseCase(repository)

    @Provides
    @Singleton
    fun provideSaveLoanDraftUseCase(repository: ILoanDraftRepository, deleteAllDraftsUseCase: DeleteAllDraftsUseCase): SaveLoanDraftUseCase = SaveLoanDraftUseCase(repository, deleteAllDraftsUseCase)

    @Provides
    @Singleton
    fun provideGetLoanDraftByIdUseCase(repository: ILoanDraftRepository): GetLoanDraftByIdUseCase = GetLoanDraftByIdUseCase(repository)

    @Provides
    @Singleton
    fun provideGetAllLoanDraftsUseCase(repository: ILoanDraftRepository): GetAllLoanDraftsUseCase = GetAllLoanDraftsUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteLoanDraftUseCase(repository: ILoanDraftRepository): DeleteLoanDraftUseCase = DeleteLoanDraftUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteAllDraftsUseCase(repository: ILoanDraftRepository): DeleteAllDraftsUseCase = DeleteAllDraftsUseCase(repository)
}
