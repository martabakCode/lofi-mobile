package com.loanfinancial.lofi.data.remote.api

import com.loanfinancial.lofi.core.network.BaseResponse
import com.loanfinancial.lofi.data.model.dto.DownloadDocumentResponse
import com.loanfinancial.lofi.data.model.dto.PresignUploadRequest
import com.loanfinancial.lofi.data.model.dto.PresignUploadResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url

interface DocumentApi {
    @POST("documents/presign-upload")
    suspend fun requestPresignUpload(
        @Body request: PresignUploadRequest
    ): Response<BaseResponse<PresignUploadResponse>>

    @PUT
    suspend fun uploadToPresignedUrl(
        @Url uploadUrl: String,
        @Body file: RequestBody
    ): Response<Unit>

    @GET("documents/{id}/download")
    suspend fun downloadDocument(
        @Path("id") id: String,
    ): Response<BaseResponse<DownloadDocumentResponse>>
}
