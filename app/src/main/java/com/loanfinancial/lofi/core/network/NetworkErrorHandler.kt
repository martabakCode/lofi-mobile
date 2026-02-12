package com.loanfinancial.lofi.core.network

import com.loanfinancial.lofi.core.common.result.ErrorType
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility object for handling network-related exceptions and converting them
 * to user-friendly ErrorType instances.
 */
object NetworkErrorHandler {
    /**
     * Handles common network exceptions and returns appropriate ErrorType.
     *
     * @param throwable The exception thrown during network operation
     * @return ErrorType representing the specific error condition
     */
    fun handle(throwable: Throwable): ErrorType =
        when (throwable) {
            is SocketTimeoutException -> {
                ErrorType.TimeoutError(
                    message = "Connection timed out. Please check your internet connection and try again.",
                )
            }

            is UnknownHostException -> {
                ErrorType.NetworkError(
                    message = "Unable to connect to server. Please check your internet connection or try again later.",
                )
            }

            is IOException -> {
                ErrorType.NetworkError(
                    message = "Network error occurred. Please check your connection and try again.",
                )
            }

            is HttpException -> {
                handleHttpException(throwable)
            }

            else -> {
                ErrorType.UnknownError(
                    message = throwable.message ?: "An unexpected error occurred. Please try again.",
                )
            }
        }

    /**
     * Handles HTTP-specific exceptions and maps them to appropriate ErrorTypes.
     *
     * @param exception The HttpException thrown by Retrofit
     * @return ErrorType representing the HTTP error
     */
    private fun handleHttpException(exception: HttpException): ErrorType {
        val code = exception.code()
        val message = exception.message ?: "HTTP Error"

        return when (code) {
            400 ->
                ErrorType.ValidationError(
                    fields = mapOf("general" to "Invalid request. Please check your input and try again."),
                )

            401 ->
                ErrorType.Unauthorized(
                    message = "Invalid credentials. Please check your email and password.",
                )

            403 ->
                ErrorType.Forbidden(
                    message = "Access denied. You don't have permission to perform this action.",
                )

            404 ->
                ErrorType.NotFound(
                    message = "The requested resource was not found.",
                )

            408 ->
                ErrorType.TimeoutError(
                    message = "Request timeout. Please try again.",
                )

            in 500..599 ->
                ErrorType.ServerError(
                    code = code,
                    message = "Server error occurred. Please try again later.",
                )

            else ->
                ErrorType.UnknownError(
                    message = "An unexpected error occurred (Code: $code). Please try again.",
                )
        }
    }

    /**
     * Wraps a suspend function with network error handling.
     * Executes the block and catches common network exceptions.
     *
     * @param block The suspend function to execute
     * @return Result containing either the success data or the error Throwable
     */
    suspend fun <T> wrap(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
}

/**
 * Extension function to convert a Throwable to a user-friendly error message.
 *
 * @return A user-friendly error message string
 */
fun Throwable.toUserFriendlyMessage(): String =
    when (this) {
        is SocketTimeoutException -> "Connection timed out. Please check your internet connection and try again."
        is UnknownHostException -> "Unable to connect to server. Please check if the server is running or try again later."
        is IOException -> "Network error occurred. Please check your connection and try again."
        is HttpException -> {
            when (code()) {
                400 -> "Invalid request. Please check your input."
                401 -> "Invalid credentials. Please check your email and password."
                403 -> "Access denied."
                404 -> "Resource not found."
                408 -> "Request timeout. Please try again."
                in 500..599 -> "Server error. Please try again later."
                else -> "An unexpected error occurred. Please try again."
            }
        }
        else -> message ?: "An unexpected error occurred. Please try again."
    }
