package com.example.aquasample.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.appContainer
import com.example.aquasample.data.local.entity.UsuarioEntity
import com.example.aquasample.data.session.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Cuenta del equipo, lista para pintar en pantalla. */
data class TeamMember(
    val id: String,
    val nombres: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val email: String,
    val role: UserRole,
    val status: String,
    val creadoEn: Long,
    /** Solo se usa para conservar la contraseña al editar; nunca se muestra. */
    val passwordHash: String
) {
    val name: String
        get() = "$nombres $apellidoPaterno $apellidoMaterno".trim().ifBlank { email }
}

data class AdminUiState(
    val isLoading: Boolean = true,
    val members: List<TeamMember> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel de gestión de personal: observa las cuentas de Room y ejecuta las
 * altas, ediciones y bajas contra el repositorio.
 */
class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = application.appContainer.usuarioRepository

    val uiState: StateFlow<AdminUiState> = repository.allUsers
        .map { users -> AdminUiState(isLoading = false, members = users.map { it.toTeamMember() }) }
        .catch { emit(AdminUiState(isLoading = false, error = "No se pudo cargar el personal.")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AdminUiState()
        )

    fun saveUser(user: UsuarioEntity, onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.insertUser(user) }
                .onSuccess { withContext(Dispatchers.Main) { onResult(null) } }
                .onFailure { error ->
                    withContext(Dispatchers.Main) {
                        onResult("No se pudo guardar: ${error.localizedMessage}")
                    }
                }
        }
    }

    fun deleteUser(userId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.deleteUserById(userId) }
                .onSuccess { withContext(Dispatchers.Main) { onResult(null) } }
                .onFailure { error ->
                    withContext(Dispatchers.Main) {
                        onResult("No se pudo eliminar: ${error.localizedMessage}")
                    }
                }
        }
    }

    private fun UsuarioEntity.toTeamMember() = TeamMember(
        id = id,
        nombres = nombres,
        apellidoPaterno = apellidoPaterno,
        apellidoMaterno = apellidoMaterno,
        email = email,
        role = rolId.toRole(),
        status = estado,
        creadoEn = creadoEn,
        passwordHash = passwordHash
    )

    companion object {
        fun Long.toRole(): UserRole = when (this) {
            1L -> UserRole.OPERADOR
            2L -> UserRole.ANALISTA
            else -> UserRole.ADMINISTRADOR
        }

        fun UserRole.toRolId(): Long = when (this) {
            UserRole.OPERADOR -> 1L
            UserRole.ANALISTA -> 2L
            UserRole.ADMINISTRADOR -> 3L
        }
    }
}
