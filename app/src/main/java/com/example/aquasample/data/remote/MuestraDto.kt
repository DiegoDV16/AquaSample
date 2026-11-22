package com.example.aquasample.data.remote

import com.example.aquasample.data.local.entity.MuestraEntity
import com.google.gson.Gson

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
    val operatorAlias: String? = null
)

private val mapperGson = Gson()

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
    operatorAlias = operatorAlias
)
