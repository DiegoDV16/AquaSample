package com.example.aquasample.ui.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginValidatorTest {

    @Test
    fun `correo vacio muestra error`() {
        assertEquals(
            "Por favor ingrese su correo electrónico.",
            LoginValidator.validate("", "clave")
        )
    }

    @Test
    fun `clave vacia muestra error`() {
        assertEquals(
            "Por favor ingrese su contraseña.",
            LoginValidator.validate("operador@aquasample.cl", "")
        )
    }

    @Test
    fun `credenciales completas pasan`() {
        assertNull(LoginValidator.validate("operador@aquasample.cl", "operador123"))
    }
}
