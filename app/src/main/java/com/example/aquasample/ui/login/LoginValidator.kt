package com.example.aquasample.ui.login

/** Validación de credenciales, independiente de Android para poder testearla. */
object LoginValidator {

    fun validate(email: String, password: String): String? = when {
        email.isBlank() -> "Por favor ingrese su correo electrónico."
        password.isBlank() -> "Por favor ingrese su contraseña."
        else -> null
    }
}
