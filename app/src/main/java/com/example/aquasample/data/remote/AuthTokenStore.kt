package com.example.aquasample.data.remote

import android.content.Context

/** Guarda el JWT del backend para adjuntarlo a las llamadas autenticadas. */
object AuthTokenStore {

    private const val PREFS = "aquasample_auth"
    private const val KEY_TOKEN = "jwt"

    private var preferences: android.content.SharedPreferences? = null

    fun init(context: Context) {
        preferences = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
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
}
