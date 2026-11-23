package com.example.aquasample.data.backup

import android.content.Context
import android.net.Uri
import com.example.aquasample.data.local.entity.CentroCultivoEntity
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.data.local.entity.TramoEntity
import com.example.aquasample.di.AppContainer
import com.google.gson.Gson
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Contenido del respaldo. Las fotos se guardan como entradas del ZIP. */
data class BackupPayload(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val muestras: List<MuestraEntity> = emptyList(),
    val centros: List<CentroCultivoEntity> = emptyList(),
    val tramos: List<TramoEntity> = emptyList()
)

data class BackupResult(
    val imported: Int,
    val skipped: Int,
    val photos: Int,
    val error: String? = null
)

/**
 * Exporta e importa un respaldo .aquabackup (ZIP con backup.json y fotos).
 * La importación no pisa muestras existentes: solo agrega las que faltan.
 */
class BackupManager(
    private val context: Context,
    private val container: AppContainer
) {

    private val gson = Gson()

    suspend fun export(): File = withContext(Dispatchers.IO) {
        val muestraDao = container.database.muestraDao()
        val centroDao = container.database.centroCultivoDao()
        val tramoDao = container.database.tramoDao()

        val directory = File(context.cacheDir, "respaldos").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val file = File(directory, "AquaSample_Respaldo_$stamp.aquabackup")

        ZipOutputStream(BufferedOutputStream(file.outputStream())).use { zip ->
            val samples = muestraDao.getAllSamplesForBackup().map { sample ->
                val entries = mutableListOf<String>()
                sample.fotos.forEachIndexed { index, path ->
                    val photo = File(path)
                    if (photo.isFile) {
                        val extension = photo.extension.ifBlank { "jpg" }
                        val entryName = "fotos/${safeName(sample.id)}_${index + 1}.$extension"
                        zip.putNextEntry(ZipEntry(entryName))
                        photo.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                        entries.add(entryName)
                    }
                }
                sample.copy(fotos = entries)
            }

            val payload = BackupPayload(
                muestras = samples,
                centros = centroDao.getAllCentrosOnce(),
                tramos = tramoDao.getAllTramosOnce()
            )
            zip.putNextEntry(ZipEntry("backup.json"))
            zip.write(gson.toJson(payload).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        file
    }

    suspend fun import(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        val temp = File(context.cacheDir, "respaldo_import").apply {
            deleteRecursively()
            mkdirs()
        }
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: return@withContext BackupResult(0, 0, 0, "No se pudo abrir el archivo.")
            input.use { stream ->
                ZipInputStream(BufferedInputStream(stream)).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        if (!entry.isDirectory) {
                            val destination = File(temp, entry.name).canonicalFile
                            if (destination.path.startsWith(temp.canonicalPath)) {
                                destination.parentFile?.mkdirs()
                                destination.outputStream().use { output -> zip.copyTo(output) }
                            }
                        }
                        zip.closeEntry()
                    }
                }
            }

            val jsonFile = File(temp, "backup.json")
            if (!jsonFile.isFile) {
                return@withContext BackupResult(0, 0, 0, "El respaldo no contiene backup.json.")
            }

            val payload = runCatching {
                gson.fromJson(jsonFile.readText(Charsets.UTF_8), BackupPayload::class.java)
            }.getOrNull() ?: return@withContext BackupResult(0, 0, 0, "backup.json inválido.")
            if (payload.version != 1) {
                return@withContext BackupResult(0, 0, 0, "Versión de respaldo no soportada.")
            }

            val centroDao = container.database.centroCultivoDao()
            val tramoDao = container.database.tramoDao()
            val usuarioDao = container.database.usuarioDao()
            val muestraDao = container.database.muestraDao()

            payload.centros.forEach {
                if (centroDao.getCentroById(it.id) == null) centroDao.insertCentro(it)
            }
            payload.tramos.forEach {
                if (tramoDao.getTramoById(it.id) == null) tramoDao.insertTramo(it)
            }

            val photoDirectory = File(context.filesDir, "muestras").apply { mkdirs() }
            var imported = 0
            var skipped = 0
            var photos = 0

            payload.muestras.forEach { sample ->
                if (muestraDao.getSampleById(sample.id) != null) {
                    skipped++
                    return@forEach
                }

                val restoredPhotos = sample.fotos.mapNotNull { entry ->
                    val source = File(temp, entry)
                    if (!source.isFile) return@mapNotNull null
                    val destination = File(
                        photoDirectory,
                        "import_${safeName(sample.id)}_${source.name}"
                    )
                    source.copyTo(destination, overwrite = true)
                    photos++
                    destination.absolutePath
                }

                val centroExists = sample.centroId?.let { centroDao.getCentroById(it) != null } == true
                val operadorExists = sample.operadorId?.let { usuarioDao.getById(it) != null } == true

                muestraDao.insertSample(
                    sample.copy(
                        centroId = if (centroExists) sample.centroId else null,
                        operadorId = if (operadorExists) sample.operadorId else null,
                        fotos = restoredPhotos,
                        photoCount = restoredPhotos.size,
                        sincronizado = false,
                        eliminado = false,
                        updatedAt = 0
                    )
                )
                imported++
            }

            BackupResult(imported, skipped, photos)
        } catch (exception: Exception) {
            BackupResult(0, 0, 0, exception.localizedMessage ?: "No se pudo importar el respaldo.")
        } finally {
            temp.deleteRecursively()
        }
    }

    private fun safeName(value: String): String =
        value.replace(Regex("[^A-Za-z0-9._-]"), "_")
}
