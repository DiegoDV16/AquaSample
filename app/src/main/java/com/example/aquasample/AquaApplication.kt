package com.example.aquasample

import android.app.Application
import com.example.aquasample.di.AppContainer

/**
 * Punto de entrada de la aplicación. Expone el contenedor de dependencias al
 * resto de la app sin frameworks de inyección.
 */
class AquaApplication : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
}

/** Acceso al contenedor desde cualquier [Application]. */
val Application.appContainer: AppContainer
    get() = (this as AquaApplication).container
