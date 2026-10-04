package com.example.aquasample.ui.login

import com.example.aquasample.data.session.UserRole

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val selectedRole: UserRole? = null,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val authenticatedUserRole: UserRole? = null
)
