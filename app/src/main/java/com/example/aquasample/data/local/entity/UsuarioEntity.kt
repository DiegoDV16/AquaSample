package com.example.aquasample.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = RoleEntity::class,
            parentColumns = ["id"],
            childColumns = ["rolId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["rolId"])
    ]
)
data class UsuarioEntity(
    @PrimaryKey
    val id: String, // ej. "USR-001"
    val email: String, // usado para autenticación
    val passwordHash: String,
    val nombres: String, // ej. "Francisca"
    val apellidoPaterno: String, // ej. "Rojas"
    val apellidoMaterno: String, // ej. "Soto"
    val rolId: Long, // FK -> roles.id
    val estado: String = "ACTIVO", // "ACTIVO", "INACTIVO"
    val creadoEn: Long = System.currentTimeMillis()
) {
    val nombreCompleto: String
        get() = "$nombres $apellidoPaterno $apellidoMaterno".trim()
}
