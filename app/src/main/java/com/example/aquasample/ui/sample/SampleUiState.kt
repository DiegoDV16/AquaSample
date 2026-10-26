package com.example.aquasample.ui.sample

import android.graphics.Bitmap

data class SampleUiState(
    val concesion: String = "",
    val tren: String = "",
    val linea: String = "",
    val tramoLongitudCm: String = "",
    val conteoManual: String = "",
    val calibreMatrix: List<String> = List(30) { "" },
    val photoBitmaps: List<Bitmap> = emptyList(),
    val observaciones: String = "",
    val selectedTags: Set<String> = emptySet(),
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
