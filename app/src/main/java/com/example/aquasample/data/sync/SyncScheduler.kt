package com.example.aquasample.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/** Programa la sincronización de muestras con WorkManager. */
object SyncScheduler {

    private const val PERIODIC_WORK = "aquasample-sync-periodic"
    private const val ONE_TIME_WORK = "aquasample-sync-now"

    private fun onlineConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Reintenta cada 15 minutos cuando hay conexión (mínimo permitido por WorkManager). */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<SampleSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(onlineConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Intento inmediato; WorkManager espera a que haya red si no la hay. */
    fun syncNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<SampleSyncWorker>()
            .setConstraints(onlineConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
