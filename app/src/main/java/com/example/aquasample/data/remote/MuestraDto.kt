package com.example.aquasample.data.remote

import com.example.aquasample.data.local.entity.MuestraEntity
import com.google.gson.Gson
import com.google.gson.JsonParser

/**
 * Contrato JSON de una muestra en el backend Spring Boot.
 *
 * Las listas (calibre, fotos y etiquetas) viajan como texto JSON para mantener
 * compatibilidad exacta con lo que guarda Room.
 */
data class MuestraDto(
    val id: String,
    val centroId: Long? = null,
    val tramoId: Long? = null,
    val operadorId: String? = null,
    val concesion: String = "",
    val tren: String? = null,
    val linea: String? = null,
    val tramoLongitudCm: String? = null,
    val conteoManual: Int? = null,
    val calibreMatrix: String? = null,
    val averageCalibre: Double? = null,
    val marcasConteo: String? = null,
    val conteoAsistido: Int? = null,
    val photoCount: Int? = null,
    val fotos: String? = null,
    val observaciones: String? = null,
    val tags: String? = null,
    val fechaHora: String? = null,
    val timestamp: Long? = null,
    val estado: String? = null,
    val sincronizado: Boolean? = null,
    val operatorAlias: String? = null,
    val marineTime: String? = null,
    val marineWaveHeight: Double? = null,
    val marineSeaSurfaceTemperature: Double? = null,
    val marineWindSpeed: Double? = null,
    val updatedAt: Long? = null,
    val eliminado: Boolean? = null
)

private val mapperGson = Gson()

/** Respuesta del backend al subir una foto. */
data class PhotoUploadResponse(val path: String = "")

fun MuestraEntity.toDto(): MuestraDto = MuestraDto(
    id = id,
    centroId = centroId,
    tramoId = tramoId,
    operadorId = operadorId,
    concesion = concesion,
    tren = tren,
    linea = linea,
    tramoLongitudCm = tramoLongitudCm,
    conteoManual = conteoManual.toIntOrNull(),
    calibreMatrix = mapperGson.toJson(calibreMatrix),
    averageCalibre = averageCalibre,
    marcasConteo = marcasConteo,
    conteoAsistido = conteoAsistido,
    photoCount = photoCount,
    fotos = mapperGson.toJson(fotos),
    observaciones = observaciones,
    tags = mapperGson.toJson(tags),
    fechaHora = fechaHora,
    timestamp = timestamp,
    estado = estado,
    sincronizado = true,
    operatorAlias = operatorAlias,
    marineTime = marineTime,
    marineWaveHeight = marineWaveHeight,
    marineSeaSurfaceTemperature = marineSeaSurfaceTemperature,
    marineWindSpeed = marineWindSpeed,
    updatedAt = updatedAt,
    eliminado = eliminado
)

/**
 * Convierte una muestra descargada del backend en entidad local.
 * Las fotos no se descargan: se conservan las del dispositivo al hacer merge.
 */
fun MuestraDto.toEntity(): MuestraEntity = MuestraEntity(
    id = id,
    centroId = centroId,
    tramoId = tramoId,
    operadorId = operadorId,
    concesion = concesion,
    tren = tren.orEmpty(),
    linea = linea.orEmpty(),
    tramoLongitudCm = tramoLongitudCm.orEmpty(),
    conteoManual = conteoManual?.toString().orEmpty(),
    calibreMatrix = parseStringList(calibreMatrix),
    averageCalibre = averageCalibre ?: 0.0,
    marcasConteo = marcasConteo ?: "[]",
    conteoAsistido = conteoAsistido ?: 0,
    photoCount = 0,
    fotos = emptyList(),
    observaciones = observaciones.orEmpty(),
    tags = parseStringList(tags),
    fechaHora = fechaHora.orEmpty(),
    timestamp = timestamp ?: 0L,
    estado = estado ?: "PENDIENTE",
    sincronizado = true,
    operatorAlias = operatorAlias.orEmpty(),
    marineTime = marineTime,
    marineWaveHeight = marineWaveHeight,
    marineSeaSurfaceTemperature = marineSeaSurfaceTemperature,
    marineWindSpeed = marineWindSpeed,
    updatedAt = updatedAt ?: 0L,
    eliminado = eliminado ?: false
)

private fun parseStringList(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    return runCatching {
        JsonParser.parseString(raw).asJsonArray.map { it.asString }
    }.getOrDefault(emptyList())
}
