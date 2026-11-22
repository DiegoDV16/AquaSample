package com.example.aquasample.data.sync

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aquasample.appContainer
import com.example.aquasample.data.remote.ApiClient
import com.example.aquasample.data.remote.toDto
import java.io.IOException

/**
 * Envía al backend las muestras que aún no se han sincronizado.
 *
 * Estrategia: POST para crear; si el backend responde 409 (ya existe) se
 * actualiza con PUT. Los errores de red o 5xx se reintentan con backoff;
 * los 4xx de validación se dejan pendientes para revisión manual.
 */
class SampleSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as? Application ?: return Result.failure()
        val repository = application.appContainer.muestraRepository

        val pending = repository.getPendingSync()
        if (pending.isEmpty()) return Result.success()

        var shouldRetry = false

        for (sample in pending) {
            val dto = sample.toDto()
            try {
                val created = ApiClient.api.create(dto)
                when {
                    created.isSuccessful -> repository.markSynced(sample.id)

                    created.code() == 409 -> {
                        val updated = ApiClient.api.update(sample.id, dto)
                        if (updated.isSuccessful) {
                            repository.markSynced(sample.id)
                        } else {
                            shouldRetry = true
                        }
                    }

                    created.code() in 500..599 -> {
                        Log.w(TAG, "Backend 5xx para ${sample.id}: ${created.code()}")
                        shouldRetry = true
                    }

                    else -> {
                        // 400 u otro error de validación: no reintentar en bucle.
                        Log.w(TAG, "Backend rechazó ${sample.id}: ${created.code()}")
                    }
                }
            } catch (io: IOException) {
                Log.e(TAG, "Error de red al sincronizar ${sample.id}", io)
                shouldRetry = true
            } catch (exception: Exception) {
                Log.e(TAG, "Error al sincronizar ${sample.id}", exception)
                shouldRetry = true
            }
        }

        return if (shouldRetry) Result.retry() else Result.success()
    }

    private companion object {
        const val TAG = "SampleSyncWorker"
    }
}
