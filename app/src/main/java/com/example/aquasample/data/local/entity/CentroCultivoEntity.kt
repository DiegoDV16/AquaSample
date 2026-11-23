package com.example.aquasample.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "centros_cultivo")
data class CentroCultivoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String, // ej. "CONC-104"
    val nombre: String, // ej. "Concesión Dalcahue #104"
    val comuna: String, // ej. "Dalcahue", "Castro"
    val estado: String = "ACTIVO" // "ACTIVO", "INACTIVO"
)
