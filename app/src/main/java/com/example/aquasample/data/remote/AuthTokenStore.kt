package com.example.aquasample.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Guarda el JWT en SharedPreferences cifradas con Android Keystore.
 * Si el keystore no está disponible (por ejemplo en pruebas), usa el archivo
 * plano anterior para no romper el arranque.
 */
object AuthTokenStore {

    private const val SECURE_PREFS = "aquasample_auth_secure"
    private const val LEGACY_PREFS = "aquasample_auth"
    private const val KEY_TOKEN = "jwt"

    private var preferences: SharedPreferences? = null

    fun init(context: Context) {
        preferences = runCatching { encrypted(context) }.getOrElse {
            context.applicationContext.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
        }
        migrateLegacy(context)
    }

    var token: String?
        get() = preferences?.getString(KEY_TOKEN, null)
        set(value) {
            preferences?.edit()?.apply {
                if (value.isNullOrBlank()) remove(KEY_TOKEN) else putString(KEY_TOKEN, value)
            }?.apply()
        }

    fun clear() {
        token = null
    }

    @Suppress("DEPRECATION")
    private fun encrypted(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            SECURE_PREFS,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun migrateLegacy(context: Context) {
        if (preferences?.getString(KEY_TOKEN, null) != null) return
        val legacy = context.applicationContext
            .getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
        val oldToken = legacy.getString(KEY_TOKEN, null) ?: return
        preferences?.edit()?.putString(KEY_TOKEN, oldToken)?.apply()
        legacy.edit().remove(KEY_TOKEN).apply()
    }
}
