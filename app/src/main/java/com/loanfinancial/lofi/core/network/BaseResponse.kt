package com.loanfinancial.lofi.core.network

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("errors")
    val errors: Any? = null,
)

data class Meta(
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("totalItems")
    val totalItems: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
)

data class PagingResponse<T>(
    @SerializedName("items")
    val items: List<T>,
    @SerializedName("meta")
    val meta: Meta,
)

fun <T> BaseResponse<T>.toOperationResult(): com.loanfinancial.lofi.core.common.result.OperationResult<T> {
    return if (success && data != null) {
        com.loanfinancial.lofi.core.common.result.OperationResult.Success(data, com.loanfinancial.lofi.core.common.result.OperationResult.DataSource.REMOTE)
    } else {
        com.loanfinancial.lofi.core.common.result.OperationResult.Error(
            com.loanfinancial.lofi.core.common.result.ErrorType.BusinessError(
                code = "API_ERROR",
                message = message
            ),
            com.loanfinancial.lofi.core.common.result.OperationResult.DataSource.REMOTE
        )
    }
}
