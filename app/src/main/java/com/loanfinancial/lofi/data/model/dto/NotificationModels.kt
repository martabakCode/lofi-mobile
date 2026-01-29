package com.loanfinancial.lofi.data.model.dto

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("type")
    val type: NotificationType,
    @SerializedName("referenceId")
    val referenceId: String?,
    @SerializedName("isRead")
    val isRead: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("link")
    val link: String?,
)

enum class NotificationType {
    @SerializedName("LOAN")
    LOAN,

    @SerializedName("AUTH")
    AUTH,

    @SerializedName("SYSTEM")
    SYSTEM,
}
