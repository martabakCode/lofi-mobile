package com.loanfinancial.lofi.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDataStoreManager(
        @ApplicationContext context: Context,
    ): DataStoreManager = DataStoreManager(context)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
