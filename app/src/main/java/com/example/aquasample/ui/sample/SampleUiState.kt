package com.example.aquasample.ui.sample

import android.graphics.Bitmap

data class SampleUiState(
    val concesion: String = "Concesión Dalcahue #104",
    val tren: String = "Tren 02",
    val linea: String = "Línea L-04",
    val tramoLongitudCm: String = "150",
    val conteoManual: String = "30",
    val calibreMatrix: List<String> = List(30) { "" },
    val photoBitmaps: List<Bitmap> = emptyList(),
    val observaciones: String = "",
    val selectedTags: Set<String> = setOf("#ValvasSanas", "#SinFouling"),
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false
) {
    val photoCount: Int
        get() = photoBitmaps.size

    val averageCalibre: Double
        get() {
            val numbers = calibreMatrix.mapNotNull { it.toDoubleOrNull() }
            return if (numbers.isNotEmpty()) numbers.average() else 0.0
        }
}
