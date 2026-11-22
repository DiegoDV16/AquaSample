package com.example.aquasample.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.appContainer
import com.example.aquasample.data.local.entity.UsuarioEntity
import com.example.aquasample.data.session.SessionManager
import com.example.aquasample.data.session.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del inicio de sesión. Toda la persistencia pasa por
 * [com.example.aquasample.data.repository.UsuarioRepository].
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioRepository = application.appContainer.usuarioRepository

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

    fun onLoginSubmit() {
        val currentState = _uiState.value
        val inputEmail = currentState.email.trim()
        val inputPass = currentState.password.trim()

        LoginValidator.validate(inputEmail, inputPass)?.let { message ->
            _uiState.update { it.copy(errorMessage = message) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            var user = usuarioRepository.getByEmail(inputEmail)

            // Garantiza que la cuenta administradora inicial exista en SQLite.
            if (user == null && inputEmail.equals(ADMIN_EMAIL, ignoreCase = true)) {
                ensureAdminUserExists()
                user = usuarioRepository.getByEmail(inputEmail)
            }

            if (user == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        password = "",
                        errorMessage = "El correo '$inputEmail' no se encuentra registrado."
                    )
                }
                return@launch
            }

            if (user.passwordHash.trim() != inputPass) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        password = "",
                        errorMessage = "Contraseña incorrecta."
                    )
                }
                return@launch
            }

            if (user.estado.equals("INACTIVO", ignoreCase = true)) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        password = "",
                        errorMessage = "El usuario se encuentra inactivo."
                    )
                }
                return@launch
            }

            val role = when (user.rolId) {
                1L -> UserRole.OPERADOR
                2L -> UserRole.ANALISTA
                3L -> UserRole.ADMINISTRADOR
                else -> UserRole.OPERADOR
            }

            SessionManager.loginWithUserEntity(user, role)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    authenticatedUserRole = role
                )
            }
        }
    }

    private suspend fun ensureAdminUserExists() {
        val adminUser = UsuarioEntity(
            id = "USR-ADMIN",
            email = ADMIN_EMAIL,
            passwordHash = "admin123",
            nombres = "Administrador",
            apellidoPaterno = "Sistema",
            apellidoMaterno = "",
            rolId = 3,
            estado = "ACTIVO"
        )
        runCatching { usuarioRepository.insertUser(adminUser) }
    }

    fun resetLoginState() {
        _uiState.update { LoginUiState() }
    }

    private companion object {
        const val ADMIN_EMAIL = "admin@aquasample.cl"
    }
}
