package com.loanfinancial.lofi.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.loanfinancial.lofi.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val createdAt: String,
    val roles: List<String> = emptyList(),
) {
    fun toDomain(): User =
        User(
            id = id,
            fullName = fullName,
            username = username,
            email = email,
            phoneNumber = phoneNumber,
            createdAt = createdAt,
            roles = roles,
        )
}
