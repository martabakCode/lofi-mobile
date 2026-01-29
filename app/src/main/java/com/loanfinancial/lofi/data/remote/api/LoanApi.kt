package com.loanfinancial.lofi.data.remote.api

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.core.network.PagingResponse
import com.loanfinancial.lofi.data.model.dto.LoanDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LoanApi {
    @GET("loans/me")
    suspend fun getMyLoans(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String,
    ): Response<BaseResponse<PagingResponse<LoanDto>>>
}
