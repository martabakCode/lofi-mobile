package com.loanfinancial.lofi.core.di

import com.loanfinancial.lofi.data.local.datasource.LoanLocalDataSource
import com.loanfinancial.lofi.data.local.datasource.LoanLocalDataSourceImpl
import com.loanfinancial.lofi.data.remote.datasource.LoanRemoteDataSource
import com.loanfinancial.lofi.data.remote.datasource.LoanRemoteDataSourceImpl
import com.loanfinancial.lofi.data.remote.datasource.NotificationRemoteDataSource
import com.loanfinancial.lofi.data.remote.datasource.NotificationRemoteDataSourceImpl
import com.loanfinancial.lofi.data.repository.AuthRepositoryImpl
import com.loanfinancial.lofi.data.repository.DocumentRepositoryImpl
import com.loanfinancial.lofi.data.repository.LoanRepositoryImpl
import com.loanfinancial.lofi.data.repository.NotificationRepositoryImpl
import com.loanfinancial.lofi.data.repository.ProductRepositoryImpl
import com.loanfinancial.lofi.data.repository.RegionRepositoryImpl
import com.loanfinancial.lofi.data.repository.UserRepositoryImpl
import com.loanfinancial.lofi.domain.repository.IAuthRepository
import com.loanfinancial.lofi.domain.repository.IDocumentRepository
import com.loanfinancial.lofi.domain.repository.ILoanRepository
import com.loanfinancial.lofi.domain.repository.INotificationRepository
import com.loanfinancial.lofi.domain.repository.IProductRepository
import com.loanfinancial.lofi.domain.repository.IRegionRepository
import com.loanfinancial.lofi.domain.repository.IUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLoanRemoteDataSource(
        impl: LoanRemoteDataSourceImpl,
    ): LoanRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindLoanLocalDataSource(
        impl: LoanLocalDataSourceImpl,
    ): LoanLocalDataSource

    @Binds
    @Singleton
    abstract fun bindLoanRepository(
        impl: LoanRepositoryImpl,
    ): ILoanRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRemoteDataSource(
        impl: NotificationRemoteDataSourceImpl,
    ): NotificationRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl,
    ): INotificationRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl,
    ): IUserRepository

    @Binds
    @Singleton
    abstract fun bindRegionRepository(
        impl: RegionRepositoryImpl,
    ): IRegionRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl,
    ): IProductRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        impl: DocumentRepositoryImpl,
    ): IDocumentRepository

    @Binds
    @Singleton
    abstract fun bindLoanSubmissionManager(
        impl: com.loanfinancial.lofi.data.manager.LoanSubmissionManagerImpl,
    ): com.loanfinancial.lofi.domain.manager.LoanSubmissionManager

    @Binds
    @Singleton
    abstract fun bindLoanDraftRepository(
        impl: com.loanfinancial.lofi.data.repository.LoanDraftRepositoryImpl,
    ): com.loanfinancial.lofi.domain.repository.ILoanDraftRepository
}
