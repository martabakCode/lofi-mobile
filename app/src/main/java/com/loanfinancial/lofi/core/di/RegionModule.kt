package com.loanfinancial.lofi.core.di

import com.loanfinancial.lofi.data.remote.api.RegionApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RegionModule {
    @Provides
    @Singleton
    fun provideRegionApi(retrofit: Retrofit): RegionApi = retrofit.create(RegionApi::class.java)
}
