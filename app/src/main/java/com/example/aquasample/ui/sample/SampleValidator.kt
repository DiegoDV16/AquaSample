package com.example.aquasample.ui.sample

/**
 * Reglas de validación del asistente de muestras, separadas de la vista y del
 * ViewModel para poder probarlas sin Android.
 */
object SampleValidator {

    const val STEP_LOCATION = 0
    const val STEP_MEASUREMENT = 1
    const val STEP_EVIDENCE = 2

    fun validate(state: SampleUiState, step: Int): String? = when (step) {
        STEP_LOCATION -> validateLocation(
            concesion = state.concesion,
            tren = state.tren,
            linea = state.linea,
            tramo = state.tramoLongitudCm
        )

        STEP_MEASUREMENT -> validateMeasurement(
            conteo = state.conteoManual,
            calibres = state.calibreMatrix
        )

        else -> validateEvidence(state.photoCount)
    }

    fun validateLocation(concesion: String, tren: String, linea: String, tramo: String): String? = when {
        concesion.isBlank() -> "Selecciona la concesión o centro de cultivo."
        tren.isBlank() -> "Selecciona el tren."
        linea.isBlank() -> "Selecciona la línea de cultivo."
        !isPositiveNumber(tramo) -> "Ingresa el largo del tramo en cm (mayor que 0)."
        else -> null
    }

    fun validateMeasurement(conteo: String, calibres: List<String>): String? = when {
        !isPositiveNumber(conteo) -> "Ingresa un conteo de individuos mayor que 0."
        calibres.count { it.toDoubleOrNull() != null } == 0 ->
            "Registra al menos una medición de calibre."
        else -> null
    }

    fun validateEvidence(photoCount: Int): String? =
        if (photoCount <= 0) "Adjunta al menos una fotografía como evidencia." else null

    private fun isPositiveNumber(raw: String): Boolean =
        raw.trim().replace(',', '.').toDoubleOrNull()?.let { it > 0.0 } == true
}
