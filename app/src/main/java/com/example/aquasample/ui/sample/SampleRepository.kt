package com.example.aquasample.ui.sample

import android.graphics.Bitmap

data class SavedSample(
    val id: String,
    val concesion: String,
    val tren: String,
    val linea: String,
    val tramoLongitudCm: String,
    val conteoManual: String,
    val calibreMatrix: List<String>,
    val averageCalibre: Double,
    val photoBitmaps: List<Bitmap>,
    val observaciones: String,
    val selectedTags: Set<String>,
    val fechaHora: String
)

object SampleRepository {
    private val _samples = mutableListOf<SavedSample>()
    val samples: List<SavedSample> get() = _samples.toList()

    fun addSample(sample: SavedSample) {
        _samples.add(0, sample)
    }

    fun clear() {
        _samples.clear()
    }
}
