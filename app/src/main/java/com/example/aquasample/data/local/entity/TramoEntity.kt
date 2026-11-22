package com.example.aquasample.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tramo de cultivo: unidad operativa de un centro, definida por tren, línea y
 * largo. Reemplaza el texto libre que antes se escribía en cada muestra.
 */
@Entity(tableName = "tramos", indices = [Index("centroId")])
data class TramoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val centroId: Long,
    val tren: String,
    val linea: String,
    val longitudCm: Int,
    val estado: String = "ACTIVO"
)
