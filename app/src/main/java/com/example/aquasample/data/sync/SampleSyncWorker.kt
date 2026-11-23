package com.example.aquasample.data.sync

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aquasample.appContainer
import com.example.aquasample.data.notifications.SyncNotifications
import com.example.aquasample.data.remote.ApiClient
import com.example.aquasample.data.remote.toDto
import com.example.aquasample.data.remote.toEntity
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException

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
        var shouldRetry = false
        var lastError: String? = null

        for (sample in pending) {
            try {
                if (sample.eliminado) {
                    val deleted = ApiClient.api.deleteSample(sample.id)
                    if (deleted.isSuccessful || deleted.code() == 404) {
                        repository.markSynced(sample.id)
                    } else {
                        shouldRetry = true
                    }
                    continue
                }

                val remotePaths = uploadPhotos(sample)
                val dto = sample.copy(
                    fotos = remotePaths,
                    photoCount = remotePaths.size
                ).toDto()
                val created = ApiClient.api.create(dto)
                when {
                    created.isSuccessful -> repository.markSynced(sample.id)

                    created.code() == 409 -> {
                        val server = runCatching { ApiClient.api.getById(sample.id) }
                            .getOrNull()
                            ?.takeIf { it.isSuccessful }
                            ?.body()

                        if (server?.eliminado == true) {
                            // El servidor ya la eliminó: gana el tombstone.
                            repository.upsertFromServer(
                                server.toEntity().copy(fotos = sample.fotos, photoCount = sample.photoCount)
                            )
                        } else if (server?.estado == "VALIDADO" && sample.estado != "VALIDADO") {
                            // Conflicto: una muestra validada en el servidor no se sobrescribe.
                            Log.w(TAG, "Conflicto en ${sample.id}: gana la versión validada del servidor")
                            repository.upsertFromServer(
                                server.toEntity().copy(fotos = sample.fotos, photoCount = sample.photoCount)
                            )
                        } else {
                            val updated = ApiClient.api.update(sample.id, dto)
                            if (updated.isSuccessful) {
                                repository.markSynced(sample.id)
                            } else {
                                shouldRetry = true
                            }
                        }
                    }

                    created.code() in 500..599 -> {
                        Log.w(TAG, "Backend 5xx para ${sample.id}: ${created.code()}")
                        lastError = "El servidor falló (${created.code()}). Reintentando."
                        shouldRetry = true
                    }

                    else -> {
                        // 400 u otro error de validación: no reintentar en bucle.
                        Log.w(TAG, "Backend rechazó ${sample.id}: ${created.code()}")
                        lastError = "El servidor rechazó ${sample.id} (${created.code()})."
                    }
                }
            } catch (io: IOException) {
                Log.e(TAG, "Error de red al sincronizar ${sample.id}", io)
                lastError = "No se pudo conectar con el servidor. Revisa la URL y la red."
                shouldRetry = true
            } catch (exception: Exception) {
                Log.e(TAG, "Error al sincronizar ${sample.id}", exception)
                lastError = if (exception is HttpException && exception.code() == 401) {
                    "Sesión expirada. Inicia sesión nuevamente."
                } else {
                    "Error al sincronizar: ${exception.localizedMessage}"
                }
                shouldRetry = true
            }
        }

        val remoteSamples = try {
            ApiClient.api.list()
        } catch (io: IOException) {
            Log.e(TAG, "No se pudo descargar muestras del backend", io)
            lastError = "No se pudo conectar con el servidor. Revisa la URL y la red."
            shouldRetry = true
            null
        } catch (exception: Exception) {
            Log.e(TAG, "Error al descargar muestras", exception)
            lastError = if (exception is HttpException && exception.code() == 401) {
                "Sesión expirada. Inicia sesión nuevamente."
            } else {
                "No se pudieron descargar las muestras: ${exception.localizedMessage}"
            }
            shouldRetry = true
            null
        }

        remoteSamples?.forEach { dto ->
            try {
                val local = repository.getSampleById(dto.id)
                // Una muestra pendiente local siempre gana sobre la copia del servidor.
                if (local != null && !local.sincronizado) return@forEach

                if (dto.estado == "VALIDADO" && local?.estado != "VALIDADO") {
                    SyncNotifications.notifyValidated(application, dto.id)
                }

                val downloaded = downloadPhotos(dto, application, local)
                val entity = dto.toEntity().copy(
                    fotos = downloaded.ifEmpty { local?.fotos ?: emptyList() },
                    photoCount = when {
                        downloaded.isNotEmpty() -> downloaded.size
                        local != null -> local.photoCount
                        else -> dto.photoCount ?: 0
                    }
                )
                repository.upsertFromServer(entity)
            } catch (exception: Exception) {
                Log.w(TAG, "No se pudo importar ${dto.id}: ${exception.localizedMessage}")
            }
        }

        val pendingAfter = runCatching { repository.getPendingSync().size }.getOrDefault(0)
        if (pendingAfter > 0) {
            SyncNotifications.notifyPending(application, pendingAfter)
        } else {
            SyncNotifications.cancelPending(application)
        }

        if (shouldRetry || lastError != null) {
            SyncStatusStore.error(lastError ?: "No se pudo sincronizar. Revisa el servidor.")
        } else {
            SyncStatusStore.success(pendingAfter)
        }

        return if (shouldRetry) Result.retry() else Result.success()
    }

    /** Sube las fotos locales y devuelve las rutas remotas que se guardan en el backend. */
    private suspend fun uploadPhotos(sample: com.example.aquasample.data.local.entity.MuestraEntity): List<String> {
        val remotePaths = mutableListOf<String>()
        for (path in sample.fotos) {
            val file = File(path)
            if (!file.exists() || file.length() == 0L) continue

            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody("image/jpeg".toMediaType())
            )
            val response = ApiClient.api.uploadPhoto(part)
            val uploaded = response.body()
            if (!response.isSuccessful || uploaded == null || uploaded.path.isBlank()) {
                throw IOException("No se pudo subir la foto ${file.name}: ${response.code()}")
            }
            remotePaths.add(uploaded.path)
        }
        return remotePaths
    }

    /** Descarga las fotos remotas a archivos locales para mostrarlas en el detalle. */
    private suspend fun downloadPhotos(
        dto: com.example.aquasample.data.remote.MuestraDto,
        application: Application,
        local: com.example.aquasample.data.local.entity.MuestraEntity?
    ): List<String> {
        val raw = dto.fotos
        if (raw.isNullOrBlank()) return emptyList()

        val remote = runCatching {
            Gson().fromJson(raw, Array<String>::class.java)?.toList().orEmpty()
        }.getOrDefault(emptyList())
        if (remote.isEmpty()) return emptyList()

        val directory = File(application.filesDir, "muestras").apply { mkdirs() }
        val safeId = dto.id.replace("#", "").replace("/", "-")
        val result = mutableListOf<String>()

        remote.forEachIndexed { index, path ->
            val name = path.substringAfterLast('/')
            if (name.isBlank()) return@forEachIndexed

            val response = runCatching { ApiClient.api.downloadPhoto(name) }.getOrNull()
                ?: return@forEachIndexed
            if (!response.isSuccessful) return@forEachIndexed

            val file = File(directory, "remoto_${safeId}_${index + 1}.jpg")
            runCatching {
                response.body()?.use { body ->
                    body.byteStream().use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
            if (file.exists() && file.length() > 0L) {
                result.add(file.absolutePath)
            }
        }
        return result
    }

    private companion object {
        const val TAG = "SampleSyncWorker"
    }
}
