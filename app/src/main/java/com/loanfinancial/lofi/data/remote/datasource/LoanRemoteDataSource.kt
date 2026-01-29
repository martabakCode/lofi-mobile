package com.loanfinancial.lofi.data.remote.datasource

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.core.network.PagingResponse
import com.loanfinancial.lofi.data.model.dto.LoanDto
import com.loanfinancial.lofi.data.remote.api.LoanApi
import retrofit2.Response
import javax.inject.Inject

interface LoanRemoteDataSource {
    suspend fun getMyLoans(
        page: Int,
        size: Int,
        sort: String,
    ): Response<BaseResponse<PagingResponse<LoanDto>>>
}

class LoanRemoteDataSourceImpl
    @Inject
    constructor(
        private val loanApi: LoanApi,
    ) : LoanRemoteDataSource {
        override suspend fun getMyLoans(
            page: Int,
            size: Int,
            sort: String,
        ): Response<BaseResponse<PagingResponse<LoanDto>>> = loanApi.getMyLoans(page, size, sort)
    }
