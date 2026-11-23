package com.example.aquasample.data.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hash local de contraseñas con PBKDF2-HMAC-SHA256.
 * Formato: pbkdf2_sha256$iteraciones$salt$hash (Base64).
 */
object PasswordHasher {

    private const val PREFIX = "pbkdf2_sha256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val derived = derive(password, salt, ITERATIONS)
        return "$PREFIX\$$ITERATIONS\$${encode(salt)}\$${encode(derived)}"
    }

    fun isHashed(stored: String): Boolean = stored.startsWith("$PREFIX\$")

    fun verify(password: String, stored: String): Boolean {
        if (!isHashed(stored)) return false
        val parts = stored.split('$')
        if (parts.size != 4) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = decode(parts[2]) ?: return false
        val expected = decode(parts[3]) ?: return false
        val actual = derive(password, salt, iterations)
        return constantTimeEquals(actual, expected)
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray? =
        runCatching { Base64.decode(value, Base64.NO_WRAP) }.getOrNull()

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (index in a.indices) {
            result = result or (a[index].toInt() xor b[index].toInt())
        }
        return result == 0
    }
}
