package com.example.aquasample.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    fun loginWithRole(role: UserRole, customEmail: String? = null) {
        val session = when (role) {
            UserRole.OPERADOR -> UserSession(
                id = "USR-001",
                username = "operador",
                nombreCompleto = "Juan Pérez",
                email = customEmail ?: "operador@aquasample.cl",
                role = UserRole.OPERADOR
            )
            UserRole.ANALISTA -> UserSession(
                id = "USR-002",
                username = "analista",
                nombreCompleto = "María González",
                email = customEmail ?: "analista@aquasample.cl",
                role = UserRole.ANALISTA
            )
            UserRole.ADMINISTRADOR -> UserSession(
                id = "USR-003",
                username = "admin",
                nombreCompleto = "Carlos Soto",
                email = customEmail ?: "admin@aquasample.cl",
                role = UserRole.ADMINISTRADOR
            )
        }
        _currentSession.value = session
    }

    fun logout() {
        _currentSession.value = null
    }
}
