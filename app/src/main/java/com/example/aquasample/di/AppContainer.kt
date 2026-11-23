package com.example.aquasample.di

import android.content.Context
import com.example.aquasample.data.local.database.AquaSampleDatabase
import com.example.aquasample.data.repository.CentroCultivoRepository
import com.example.aquasample.data.repository.MuestraRepository
import com.example.aquasample.data.repository.TramoRepository
import com.example.aquasample.data.repository.UsuarioRepository

/**
 * Contenedor de dependencias de la aplicación.
 *
 * Se prefiere este contenedor manual (en lugar de Hilt) para mantener el
 * proyecto liviano y fácil de explicar en la defensa: una sola instancia de la
 * base de datos y un repositorio por agregado.
 */
class AppContainer(context: Context) {

    val database: AquaSampleDatabase = AquaSampleDatabase.getInstance(context)

    val muestraRepository: MuestraRepository by lazy {
        MuestraRepository(database.muestraDao())
    }

    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(database.usuarioDao())
    }

    val centroRepository: CentroCultivoRepository by lazy {
        CentroCultivoRepository(database.centroCultivoDao())
    }

    val tramoRepository: TramoRepository by lazy {
        TramoRepository(database.tramoDao())
    }
}
