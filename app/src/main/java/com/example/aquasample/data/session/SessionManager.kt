package com.example.aquasample.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.aquasample.data.local.entity.UsuarioEntity
import com.example.aquasample.data.remote.AuthTokenStore
import com.example.aquasample.data.repository.ProfileRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sesión activa de la app. Se persiste en SharedPreferences para sobrevivir a
 * que Android mate el proceso; el JWT se guarda aparte en [AuthTokenStore].
 */
object SessionManager {

    private const val PREFS = "aquasample_session"
    private const val KEY_SESSION = "session"

    private val gson = Gson()
    private var preferences: SharedPreferences? = null

    private val _currentSession = MutableStateFlow<UserSession?>(null)
    val currentSession: StateFlow<UserSession?> = _currentSession.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun init(context: Context) {
        preferences = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = preferences?.getString(KEY_SESSION, null) ?: return
        runCatching { gson.fromJson(json, UserSession::class.java) }
            .getOrNull()
            ?.let { _currentSession.value = it }
    }

    fun loginWithUserEntity(user: UsuarioEntity, role: UserRole) {
        val fullName = user.nombreCompleto.ifBlank { user.nombres }
        setSession(
            UserSession(
                id = user.id,
                username = user.email.substringBefore("@"),
                nombreCompleto = fullName,
                email = user.email,
                role = role
            )
        )
        ProfileRepository.updateAlias(fullName)
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
        setSession(session)
        ProfileRepository.updateAlias(session.nombreCompleto)
    }

    fun logout() {
        _currentSession.value = null
        preferences?.edit()?.remove(KEY_SESSION)?.apply()
        AuthTokenStore.clear()
    }

    /** El backend rechazó el JWT: cierra la sesión y avisa a la UI. */
    fun onUnauthorized() {
        if (_currentSession.value == null) return
        _currentSession.value = null
        preferences?.edit()?.remove(KEY_SESSION)?.apply()
        AuthTokenStore.clear()
        _message.value = "Tu sesión expiró. Inicia sesión nuevamente."
    }

    fun consumeMessage() {
        _message.value = null
    }

    private fun setSession(session: UserSession) {
        _currentSession.value = session
        preferences?.edit()?.putString(KEY_SESSION, gson.toJson(session))?.apply()
    }
}
