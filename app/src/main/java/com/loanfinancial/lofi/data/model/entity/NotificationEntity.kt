package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.loanfinancial.lofi.data.model.dto.NotificationResponse
import com.loanfinancial.lofi.data.model.dto.NotificationType

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val referenceId: String?,
    val isRead: Boolean,
    val createdAt: String,
    val link: String?,
) {
    fun toDto(): NotificationResponse =
        NotificationResponse(
            id = id,
            userId = userId,
            title = title,
            body = body,
            type =
                try {
                    NotificationType.valueOf(type)
                } catch (e: Exception) {
                    NotificationType.SYSTEM
                },
            referenceId = referenceId,
            isRead = isRead,
            createdAt = createdAt,
            link = link,
        )
}

fun NotificationResponse.toEntity(): NotificationEntity =
    NotificationEntity(
        id = id,
        userId = userId,
        title = title,
        body = body,
        type = type.name,
        referenceId = referenceId,
        isRead = isRead,
        createdAt = createdAt,
        link = link,
    )
