package com.example.aquasample.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.data.local.database.AppDatabase
import com.example.aquasample.data.local.entity.RoleEntity
import com.example.aquasample.data.local.entity.UsuarioEntity
import com.example.aquasample.data.session.SessionManager
import com.example.aquasample.data.session.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val usuarioDao = AppDatabase.getInstance(application).usuarioDao()

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

        if (inputEmail.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor ingrese su correo electrónico.") }
            return
        }
        if (inputPass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor ingrese su contraseña.") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Buscar usuario real en la base de datos Room SQLite
            var user = usuarioDao.getByEmail(inputEmail)

            // Garantizar que la cuenta Administrador inicial exista en SQLite
            if (user == null && inputEmail.equals("admin@aquasample.cl", ignoreCase = true)) {
                ensureAdminUserExists()
                user = usuarioDao.getByEmail(inputEmail)
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
        val db = AppDatabase.getInstance(getApplication())
        val roles = listOf(
            RoleEntity(id = 1, nombre = "OPERADOR", descripcion = "Operador de Terreno para captura de muestras"),
            RoleEntity(id = 2, nombre = "ANALISTA", descripcion = "Analista de laboratorio e inspección técnica"),
            RoleEntity(id = 3, nombre = "ADMINISTRADOR", descripcion = "Administrador general del sistema y gestión de usuarios")
        )
        val adminUser = UsuarioEntity(
            id = "USR-ADMIN",
            email = "admin@aquasample.cl",
            passwordHash = "admin123",
            nombres = "Administrador",
            apellidoPaterno = "Sistema",
            apellidoMaterno = "",
            rolId = 3,
            estado = "ACTIVO"
        )
        try {
            db.roleDao().insertRoles(roles)
            db.usuarioDao().insertUser(adminUser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetLoginState() {
        _uiState.update { LoginUiState() }
    }
}
