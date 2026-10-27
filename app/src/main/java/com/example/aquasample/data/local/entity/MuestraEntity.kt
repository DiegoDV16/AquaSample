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
    val concesion: String = "", // Coincide con campo concesion del formulario
    val tren: String = "", // Coincide con campo tren del formulario
    val linea: String = "", // Coincide con campo linea del formulario
    val tramoLongitudCm: String = "", // Coincide con campo tramoLongitudCm del formulario
    val conteoManual: String = "", // Coincide con campo conteoManual del formulario
    val calibreMatrix: List<String> = emptyList(), // Coincide con calibreMatrix (30 mediciones)
    val averageCalibre: Double = 0.0, // Calibre promedio calculado
    val photoCount: Int = 0, // Cantidad de fotos tomadas
    val fotos: List<String> = emptyList(), // Rutas locales de fotos
    val observaciones: String = "", // Observaciones técnicas
    val tags: List<String> = emptyList(), // Lista de tags seleccionados (ej. #ValvasSanas)
    val fechaHora: String = "", // Fecha y hora formateada (dd/MM/yyyy HH:mm)
    val timestamp: Long = System.currentTimeMillis(), // Filtros cronológicos
    val estado: String = "COMPLETADO", // PENDIENTE, COMPLETADO, VALIDADO
    val sincronizado: Boolean = true, // Flag de sincronización local
    val operatorAlias: String = "" // Alias/Nombre rápido del operador
)