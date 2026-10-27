package com.example.aquasample.data.session

import com.example.aquasample.data.local.entity.UsuarioEntity
import com.example.aquasample.ui.sample.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    fun loginWithUserEntity(user: UsuarioEntity, role: UserRole) {
        val fullName = user.nombreCompleto.ifBlank { user.nombres }
        val session = UserSession(
            id = user.id,
            username = user.email.substringBefore("@"),
            nombreCompleto = fullName,
            email = user.email,
            role = role
        )
        _currentSession.value = session
        ProfileRepository.alias = fullName
    }

    fun loginWithRole(role: UserRole, customEmail: String? = null) {
        val session = when (role) {
            UserRole.OPERADOR -> UserSession(
                id = "USR-001",
                username = "operador",
                nombreCompleto = "Operador",
                email = customEmail ?: "operador@aquasample.cl",
                role = UserRole.OPERADOR
            )
            UserRole.ANALISTA -> UserSession(
                id = "USR-002",
                username = "analista",
                nombreCompleto = "Analista",
                email = customEmail ?: "analista@aquasample.cl",
                role = UserRole.ANALISTA
            )
            UserRole.ADMINISTRADOR -> UserSession(
                id = "USR-003",
                username = "admin",
                nombreCompleto = "Administrador",
                email = customEmail ?: "admin@aquasample.cl",
                role = UserRole.ADMINISTRADOR
            )
        }
        _currentSession.value = session
        ProfileRepository.alias = session.nombreCompleto
    }

    fun logout() {
        _currentSession.value = null
    }
}
