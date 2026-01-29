package com.loanfinancial.lofi.core.util

import com.loanfinancial.lofi.BuildConfig

object UrlUtil {
    fun getFullProfileUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return if (url.startsWith("http")) {
            url
        } else {
            val baseUrl = BuildConfig.BASE_URL.trimEnd('/')
            val path = url.trimStart('/')
            "$baseUrl/$path"
        }
    }
}
