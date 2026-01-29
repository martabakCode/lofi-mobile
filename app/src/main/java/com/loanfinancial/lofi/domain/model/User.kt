package com.loanfinancial.lofi.domain.model

data class User(
    val id: String,
    val fullName: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val createdAt: String,
    val roles: List<String> = emptyList(),
)
