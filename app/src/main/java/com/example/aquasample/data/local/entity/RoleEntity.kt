package com.example.aquasample.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class RoleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String, // OPERADOR, ANALISTA, ADMINISTRADOR
    val descripcion: String
)
