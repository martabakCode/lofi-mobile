package com.loanfinancial.lofi.core.common.result

sealed class BaseResult<out T> {
    data class Success<T>(
        val data: T,
    ) : BaseResult<T>()

    data class Error(
        val error: ErrorType,
    ) : BaseResult<Nothing>()

    object Loading : BaseResult<Nothing>()

    fun isSuccess(): Boolean = this is Success

    fun isError(): Boolean = this is Error

    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data

    fun errorOrNull(): ErrorType? = (this as? Error)?.error

    inline fun <R> map(transform: (T) -> R): BaseResult<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }

    inline fun onSuccess(action: (T) -> Unit): BaseResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (ErrorType) -> Unit): BaseResult<T> {
        if (this is Error) action(error)
        return this
    }

    inline fun onLoading(action: () -> Unit): BaseResult<T> {
        if (this is Loading) action()
        return this
    }
}

fun <T> BaseResult<T>.getErrorMessage(): String? = (this as? BaseResult.Error)?.error?.getErrorMessage()
