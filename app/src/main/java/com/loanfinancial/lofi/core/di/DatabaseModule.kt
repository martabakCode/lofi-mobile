package com.loanfinancial.lofi.core.di

import android.content.Context
import androidx.room.Room
import com.loanfinancial.lofi.data.local.dao.LoanDao
import com.loanfinancial.lofi.data.local.dao.ProfileDraftDao
import com.loanfinancial.lofi.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room
            .databaseBuilder(
                context,
                AppDatabase::class.java,
                "lofi_db",
            ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideLoanDao(database: AppDatabase): LoanDao = database.loanDao()

    @Provides
    fun provideUserDao(database: AppDatabase): com.loanfinancial.lofi.data.local.dao.UserDao = database.userDao()

    @Provides
    fun provideProfileDraftDao(database: AppDatabase): ProfileDraftDao = database.profileDraftDao()
}
