package com.example.aquasample.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MuestraConDetalle(
    @Embedded val muestra: MuestraEntity,
    val centroNombre: String? = null,
    val centroCodigo: String? = null,
    val operadorNombreCompleto: String? = null,
    val operadorEmail: String? = null
)

data class UsuarioConRol(
    @Embedded val usuario: UsuarioEntity,
    @Relation(
        parentColumn = "rolId",
        entityColumn = "id"
    )
    val rol: RoleEntity
)
