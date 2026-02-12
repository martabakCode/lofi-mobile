package com.loanfinancial.lofi

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class LofiApplication :
    Application(),
    ImageLoaderFactory,
    androidx.work.Configuration.Provider {
    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    @Inject
    lateinit var networkSyncTriggerManager: com.loanfinancial.lofi.core.network.NetworkSyncTriggerManager

    override fun onCreate() {
        super.onCreate()
        // Start network sync monitoring
        networkSyncTriggerManager.startMonitoring()
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .okHttpClient(okHttpClient)
            .build()

    override val workManagerConfiguration: androidx.work.Configuration
        get() =
            androidx.work.Configuration
                .Builder()
                .setWorkerFactory(workerFactory)
                .build()
}
