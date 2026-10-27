package com.example.aquasample.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "muestras",
    foreignKeys = [
        ForeignKey(
            entity = CentroCultivoEntity::class,
            parentColumns = ["id"],
            childColumns = ["centroId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["operadorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["centroId"]),
        Index(value = ["operadorId"]),
        Index(value = ["timestamp"])
    ]
)
data class MuestraEntity(
    @PrimaryKey
    val id: String, // ej. "#M-0001"
    val centroId: Long? = null,
    val operadorId: String? = null,
    val concesion: String,
    val tren: String,
    val linea: String,
    val tramoLongitudCm: String,
    val conteoManual: String,
    val calibreMatrix: List<String>,
    val averageCalibre: Double, // Calibre promedio calculado
    val photoCount: Int = 0, // Cantidad de fotos tomadas
    val fotos: List<String> = emptyList(), // Rutas locales de fotos
    val observaciones: String = "", // Observaciones técnicas
    val tags: List<String> = emptyList(), // Lista de tags seleccionados (ej. #ValvasSanas)
    val fechaHora: String, // Fecha y hora formateada (dd/MM/yyyy HH:mm)
    val timestamp: Long = System.currentTimeMillis(), // Filtros cronológicos
    val estado: String = "COMPLETADO", // PENDIENTE, COMPLETADO, VALIDADO
    val sincronizado: Boolean = true, // Flag de sincronización local
    val operatorAlias: String = "" // Alias/Nombre rápido del operador
)
