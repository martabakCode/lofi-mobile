package com.loanfinancial.lofi.core.common.result

sealed class ErrorType {
    // Network Errors
    data class NetworkError(
        val message: String,
    ) : ErrorType()

    data class TimeoutError(
        val message: String,
    ) : ErrorType()

    data class ServerError(
        val code: Int,
        val message: String,
    ) : ErrorType()

    // Client Errors
    data class ValidationError(
        val fields: Map<String, String>,
    ) : ErrorType()

    data class Unauthorized(
        val message: String,
    ) : ErrorType()

    data class Forbidden(
        val message: String,
    ) : ErrorType()

    data class NotFound(
        val message: String,
    ) : ErrorType()

    // Local DB Errors
    data class DatabaseError(
        val message: String,
    ) : ErrorType()

    data class CacheError(
        val message: String,
    ) : ErrorType()

    // Business Logic Errors
    data class BusinessError(
        val code: String,
        val message: String,
    ) : ErrorType()

    // Hardware/Permission Errors
    data class PermissionDenied(
        val permission: String,
    ) : ErrorType()

    data class HardwareError(
        val message: String,
    ) : ErrorType()

    data class BiometricError(
        val code: Int,
        val message: String,
    ) : ErrorType()

    data class LocationError(
        val message: String,
    ) : ErrorType()

    // Unknown
    data class UnknownError(
        val message: String,
    ) : ErrorType()

    fun getErrorMessage(): String =
        when (this) {
            is NetworkError -> message
            is TimeoutError -> message
            is ServerError -> "[$code] $message"
            is ValidationError -> fields.values.firstOrNull() ?: "Validation failed"
            is Unauthorized -> message
            is Forbidden -> message
            is NotFound -> message
            is DatabaseError -> message
            is CacheError -> message
            is BusinessError -> "[$code] $message"
            is PermissionDenied -> "Permission denied: $permission"
            is HardwareError -> message
            is BiometricError -> message
            is LocationError -> message
            is UnknownError -> message
        }
}
