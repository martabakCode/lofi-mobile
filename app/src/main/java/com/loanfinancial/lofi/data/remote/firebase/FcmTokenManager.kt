package com.loanfinancial.lofi.data.remote.firebase

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface IFcmTokenManager {
    suspend fun getToken(): String?
}

@Singleton
class FcmTokenManager
    @Inject
    constructor() : IFcmTokenManager {
        override suspend fun getToken(): String? =
            try {
                FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
    }
