package com.example.aquasample.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.data.session.SessionManager
import com.example.aquasample.data.session.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onRoleChipSelected(role: UserRole) {
        val defaultEmail = when (role) {
            UserRole.OPERADOR -> "operador@aquasample.cl"
            UserRole.ANALISTA -> "analista@aquasample.cl"
            UserRole.ADMINISTRADOR -> "admin@aquasample.cl"
        }
        val defaultPass = when (role) {
            UserRole.OPERADOR -> "operador123"
            UserRole.ANALISTA -> "analista123"
            UserRole.ADMINISTRADOR -> "admin123"
        }
        _uiState.update {
            it.copy(
                email = defaultEmail,
                password = defaultPass,
                selectedRole = role,
                errorMessage = null
            )
        }
    }

    fun onLoginSubmit() {
        val currentState = _uiState.value
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor ingrese su correo electrónico.") }
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor ingrese su contraseña.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            delay(800)

            val role = when {
                currentState.email.contains("operador", ignoreCase = true) -> UserRole.OPERADOR
                currentState.email.contains("analista", ignoreCase = true) -> UserRole.ANALISTA
                currentState.email.contains("admin", ignoreCase = true) -> UserRole.ADMINISTRADOR
                else -> currentState.selectedRole ?: UserRole.OPERADOR
            }

            SessionManager.loginWithRole(role, currentState.email)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    authenticatedUserRole = role
                )
            }
        }
    }

    fun resetLoginState() {
        _uiState.update { LoginUiState() }
    }
}
