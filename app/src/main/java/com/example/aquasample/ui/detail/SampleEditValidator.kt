package com.example.aquasample.ui.detail

/** Validación pura de la edición de una muestra, reutilizable por UI y ViewModel. */
object SampleEditValidator {

    fun validate(conteo: String, calibres: List<String>): String? {
        val count = conteo.toIntOrNull()
        if (count == null || count <= 0) return "Ingresa un conteo mayor que 0."
        if (values(calibres).isEmpty()) return "Registra al menos una medición de calibre."
        return null
    }

    fun values(calibres: List<String>): List<Double> =
        calibres.mapNotNull { it.replace(',', '.').toDoubleOrNull() }

    fun average(calibres: List<String>): Double? =
        values(calibres).takeIf { it.isNotEmpty() }?.average()
}
