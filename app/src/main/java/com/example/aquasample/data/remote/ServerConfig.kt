package com.example.aquasample.data.remote

import android.content.Context
import android.content.SharedPreferences

/**
 * Dirección del backend configurable desde la app.
 *
 * Por defecto apunta al backend público de Render. En una instalación nueva no
 * hace falta configurar nada; en una actualización se migran automáticamente las
 * URLs antiguas de desarrollo (localhost, IP LAN, túneles temporales).
 */
object ServerConfig {

    const val DEFAULT_BASE_URL = "https://aquasample-backend.onrender.com/"
    private const val PREFS = "aquasample_settings"
    private const val KEY_BASE_URL = "api_base_url"
    private const val KEY_RENDER_MIGRATED = "render_url_migrated"

    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        migrateLegacyUrl()
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

    private fun migrateLegacyUrl() {
        if (preferences.getBoolean(KEY_RENDER_MIGRATED, false)) return

        val saved = preferences.getString(KEY_BASE_URL, null)
        val target = if (saved.isNullOrBlank() || isLegacyHost(saved)) {
            DEFAULT_BASE_URL
        } else {
            saved
        }
        preferences.edit()
            .putString(KEY_BASE_URL, target)
            .putBoolean(KEY_RENDER_MIGRATED, true)
            .apply()
    }

    private fun isLegacyHost(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("127.0.0.1") ||
            lower.contains("localhost") ||
            lower.contains("10.0.2.2") ||
            lower.contains("192.168.") ||
            lower.contains("://10.") ||
            lower.contains(".trycloudflare.com") ||
            lower.contains("serveousercontent.com") ||
            lower.contains("localhost.run")
    }
}
