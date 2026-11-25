package com.example.aquasample.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class ServerConfigTest {

    @Test
    fun `agrega http y barra final`() {
        assertEquals("http://192.168.1.50:8080/", ServerConfig.normalize("192.168.1.50:8080"))
    }

    @Test
    fun `respeta https y barra existente`() {
        assertEquals(
            "https://aquasample.onrender.com/",
            ServerConfig.normalize("https://aquasample.onrender.com/")
        )
    }

    @Test
    fun `texto vacio usa el valor por defecto`() {
        assertEquals(ServerConfig.DEFAULT_BASE_URL, ServerConfig.normalize("   "))
    }

    @Test
    fun `el valor por defecto es el backend de Render`() {
        assertEquals("https://aquasample-backend.onrender.com/", ServerConfig.DEFAULT_BASE_URL)
    }
}
