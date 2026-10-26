package com.example.aquasample.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "samples")
data class SampleEntity(
    @PrimaryKey val id: String,
    val fechaHora: String,
    val concesion: String,
    val tren: String,
    val linea: String,
    val tramoLongitudCm: String,
    val conteoManual: String,
    val calibreMatrixJson: String,
    val averageCalibre: Double,
    val observaciones: String,
    val tagsCsv: String,
    val photoCount: Int,
    val operatorAlias: String
)
