package com.loanfinancial.lofi.core.di

import android.content.Context
import android.util.Log
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.loanfinancial.lofi.BuildConfig
import com.loanfinancial.lofi.core.network.ApiService
import com.loanfinancial.lofi.core.network.AuthInterceptor
import com.loanfinancial.lofi.data.remote.api.DocumentApi
import com.loanfinancial.lofi.data.remote.api.LoanApi
import com.loanfinancial.lofi.data.remote.api.LoanProductApi
import com.loanfinancial.lofi.data.remote.api.NotificationApi
import com.loanfinancial.lofi.data.remote.api.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        val certificatePinner =
            CertificatePinner
                .Builder()
                // .add("api.yourdomain.com", "sha256/your_certificate_pin_here")
                .build()

        return OkHttpClient
            .Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    val chuckerCollector = ChuckerCollector(
                        context = context,
                        showNotification = true,
                        retentionPeriod = RetentionManager.Period.ONE_HOUR,
                    )
                    
                    val chuckerInterceptor = ChuckerInterceptor.Builder(context)
                        .collector(chuckerCollector)
                        .maxContentLength(250_000L)
                        .redactHeaders("Auth-Token", "Bearer")
                        .alwaysReadResponseBody(true)
                        .build()
                        
                    addInterceptor(chuckerInterceptor)
                }
            }.certificatePinner(certificatePinner)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        Log.d("NetworkModule", "Initializing Retrofit with BASE_URL: ${BuildConfig.BASE_URL}")
        return Retrofit
            .Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideLoanApi(retrofit: Retrofit): LoanApi = retrofit.create(LoanApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideLoanProductApi(retrofit: Retrofit): LoanProductApi = retrofit.create(LoanProductApi::class.java)

    @Provides
    @Singleton
    fun provideDocumentApi(retrofit: Retrofit): DocumentApi = retrofit.create(DocumentApi::class.java)

    @Provides
    @Singleton
    fun providePinApi(retrofit: Retrofit): com.loanfinancial.lofi.data.remote.api.PinApi =
        retrofit.create(com.loanfinancial.lofi.data.remote.api.PinApi::class.java)

    @Provides
    @Singleton
    fun provideNetworkManager(
        @ApplicationContext context: Context,
    ): com.loanfinancial.lofi.core.network.NetworkManager =
        com.loanfinancial.lofi.core.network
            .NetworkManagerImpl(context)

    @Provides
    @Singleton
    fun provideNetworkSyncTriggerManager(
        networkManager: com.loanfinancial.lofi.core.network.NetworkManager,
        loanSubmissionManager: com.loanfinancial.lofi.domain.manager.LoanSubmissionManager,
        notificationRepository: com.loanfinancial.lofi.domain.repository.INotificationRepository,
    ): com.loanfinancial.lofi.core.network.NetworkSyncTriggerManager =
        com.loanfinancial.lofi.core.network.NetworkSyncTriggerManagerImpl(
            networkManager,
            loanSubmissionManager,
            notificationRepository,
        )
}
