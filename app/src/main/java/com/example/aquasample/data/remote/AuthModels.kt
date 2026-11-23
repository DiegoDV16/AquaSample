package com.example.aquasample.data.remote

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String = "",
    val id: String = "",
    val email: String = "",
    val nombre: String = "",
    val rolId: Long? = null,
    val expiresInMs: Long = 0
)
