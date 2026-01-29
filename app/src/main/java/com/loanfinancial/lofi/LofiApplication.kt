package com.loanfinancial.lofi

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.facebook.FacebookSdk
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class LofiApplication :
    Application(),
    ImageLoaderFactory {
    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        // Only initialize Facebook SDK if app ID is configured
        val facebookAppId = getString(R.string.facebook_app_id)
        if (facebookAppId.isNotEmpty()) {
            FacebookSdk.setApplicationId(facebookAppId)
            FacebookSdk.sdkInitialize(applicationContext)
        }
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .okHttpClient(okHttpClient)
            .build()
}
