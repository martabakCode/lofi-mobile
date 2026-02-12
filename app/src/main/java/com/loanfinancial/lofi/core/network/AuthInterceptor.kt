package com.loanfinancial.lofi.core.network

import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor
    @Inject
    constructor(
        private val dataStoreManager: DataStoreManager,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val requestUrl = originalRequest.url
            val path = requestUrl.encodedPath
            val baseUrl = com.loanfinancial.lofi.BuildConfig.BASE_URL

            // ONLY add auth header if the request is to our own backend
            val isOurBackend = requestUrl.toString().startsWith(baseUrl)

            // Skip auth header for login/register/google
            if (!isOurBackend ||
                path.contains("auth/login") ||
                path.contains("auth/register") ||
                path.contains("auth/google")) {
                return chain.proceed(originalRequest)
            }

            val token =
                runBlocking {
                    dataStoreManager.tokenFlow.firstOrNull()
                }

            val request =
                originalRequest
                    .newBuilder()
                    .apply {
                        if (!token.isNullOrEmpty()) {
                            addHeader("Authorization", "Bearer $token")
                        }
                    }.build()

            return chain.proceed(request)
        }
    }
