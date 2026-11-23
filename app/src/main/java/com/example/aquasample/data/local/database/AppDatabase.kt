package com.example.aquasample.data.local.database

import android.content.Context

/**
 * Delegado de la base de datos AppDatabase hacia AquaSampleDatabase
 * para asegurar compatibilidad total en todo el proyecto.
 */
object AppDatabase {
    fun getInstance(context: Context): AquaSampleDatabase {
        return AquaSampleDatabase.getInstance(context)
    }
}
