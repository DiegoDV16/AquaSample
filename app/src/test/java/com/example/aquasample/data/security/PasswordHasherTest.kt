package com.example.aquasample.data.security

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [32], application = Application::class)
class PasswordHasherTest {

    @Test
    fun `hash y verificacion correcta`() {
        val hash = PasswordHasher.hash("operador123")

        assertTrue(PasswordHasher.isHashed(hash))
        assertTrue(PasswordHasher.verify("operador123", hash))
        assertFalse(PasswordHasher.verify("otra-clave", hash))
    }

    @Test
    fun `cada hash usa salt distinto`() {
        val first = PasswordHasher.hash("admin123")
        val second = PasswordHasher.hash("admin123")

        assertFalse(first == second)
        assertTrue(PasswordHasher.verify("admin123", first))
        assertTrue(PasswordHasher.verify("admin123", second))
    }

    @Test
    fun `texto plano no se considera hash`() {
        assertFalse(PasswordHasher.isHashed("operador123"))
        assertFalse(PasswordHasher.verify("operador123", "operador123"))
    }
}
