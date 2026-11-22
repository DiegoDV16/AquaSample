package com.example.aquasample

import android.app.Application
import com.example.aquasample.data.sync.SyncScheduler
import com.example.aquasample.di.AppContainer

/**
 * Punto de entrada de la aplicación. Expone el contenedor de dependencias al
 * resto de la app sin frameworks de inyección y programa la sincronización.
 */
class AquaApplication : Application() {

    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        SyncScheduler.schedulePeriodic(this)
        SyncScheduler.syncNow(this)
    }
}

/** Acceso al contenedor desde cualquier [Application]. */
val Application.appContainer: AppContainer
    get() = (this as AquaApplication).container
