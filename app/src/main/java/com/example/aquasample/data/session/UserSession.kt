package com.example.aquasample.data.session

enum class UserRole(val displayName: String) {
    OPERADOR("Operador de Terreno"),
    ANALISTA("Analista de Muestras"),
    ADMINISTRADOR("Administrador del Sistema")
}

data class UserSession(
    val id: String,
    val username: String,
    val nombreCompleto: String,
    val email: String,
    val role: UserRole
)
