package com.example.aquasample.data.remote

import android.content.Context
import android.content.SharedPreferences

/**
 * Dirección del backend configurable desde la app. Por defecto apunta al PC
 * local con `adb reverse`, pero en un teléfono debe apuntar a la IP del PC en
 * la red WiFi o a una URL pública (idealmente HTTPS).
 */
object ServerConfig {

    const val DEFAULT_BASE_URL = "http://127.0.0.1:8080/"
    private const val PREFS = "aquasample_settings"
    private const val KEY_BASE_URL = "api_base_url"

    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    var baseUrl: String
        get() = if (::preferences.isInitialized) {
            preferences.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        } else {
            DEFAULT_BASE_URL
        }
        set(value) {
            if (::preferences.isInitialized) {
                preferences.edit().putString(KEY_BASE_URL, normalize(value)).apply()
            }
        }

    fun normalize(raw: String): String {
        var url = raw.trim()
        if (url.isEmpty()) return DEFAULT_BASE_URL
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        return if (url.endsWith("/")) url else "$url/"
    }
}
