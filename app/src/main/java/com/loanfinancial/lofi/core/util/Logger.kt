package com.loanfinancial.lofi.core.util

import android.util.Log

object Logger {
    fun d(
        tag: String,
        msg: String,
    ) {
        try {
            Log.d(tag, msg)
        } catch (e: RuntimeException) {
            // Ignore for tests if mocking fails
            println("DEBUG: $tag: $msg")
        }
    }

    fun e(
        tag: String,
        msg: String,
    ) {
        try {
            Log.e(tag, msg)
        } catch (e: RuntimeException) {
            System.err.println("ERROR: $tag: $msg")
        }
    }

    fun e(
        tag: String,
        msg: String,
        tr: Throwable?,
    ) {
        try {
            Log.e(tag, msg, tr)
        } catch (e: RuntimeException) {
            System.err.println("ERROR: $tag: $msg")
            tr?.printStackTrace()
        }
    }

    fun w(
        tag: String,
        msg: String,
    ) {
        try {
            Log.w(tag, msg)
        } catch (e: RuntimeException) {
            println("WARN: $tag: $msg")
        }
    }
}
