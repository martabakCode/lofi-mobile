package com.loanfinancial.lofi.core.common.result

sealed class OperationResult<out T> {
    data class Success<T>(
        val data: T,
        val source: DataSource = DataSource.REMOTE,
    ) : OperationResult<T>()

    data class Error(
        val error: ErrorType,
        val source: DataSource = DataSource.REMOTE,
    ) : OperationResult<Nothing>()

    object Loading : OperationResult<Nothing>()

    enum class DataSource { REMOTE, LOCAL }
}
