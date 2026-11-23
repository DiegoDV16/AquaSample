package com.example.aquasample

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import com.example.aquasample.data.remote.ServerConfig
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
        ServerConfig.init(this)
        SyncScheduler.schedulePeriodic(this)
        SyncScheduler.syncNow(this)

        // Intento inmediato cuando vuelve la conexión (WiFi o datos móviles).
        runCatching {
            getSystemService(ConnectivityManager::class.java)?.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        SyncScheduler.syncNow(this@AquaApplication)
                    }
                }
            )
        }
    }
}

/** Acceso al contenedor desde cualquier [Application]. */
val Application.appContainer: AppContainer
    get() = (this as AquaApplication).container
