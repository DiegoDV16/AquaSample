package com.example.aquasample.data.repository

import android.graphics.Bitmap

/**
 * Datos de perfil del usuario en sesión.
 *
 * El alias y la fotografía son preferencias de trabajo en terreno: el alias se
 * usa como firma del operador en cada muestra y la foto solo decora la interfaz.
 * Se mantienen en memoria durante la sesión para no acoplar el login a disco.
 */
object ProfileRepository {

    private const val DEFAULT_ALIAS = "Operador"

    var alias: String = DEFAULT_ALIAS
        private set

    var photoBitmap: Bitmap? = null
        private set

    fun updateAlias(newAlias: String) {
        alias = newAlias.trim().ifBlank { DEFAULT_ALIAS }
    }

    fun updatePhoto(bitmap: Bitmap?) {
        photoBitmap = bitmap
    }

    fun reset() {
        alias = DEFAULT_ALIAS
        photoBitmap = null
    }
}
