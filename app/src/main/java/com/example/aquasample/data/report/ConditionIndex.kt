package com.example.aquasample.data.report

import kotlin.math.roundToInt

/**
 * Índice de condición calculado sobre el calibre promedio.
 * 40 mm se toma como referencia = 100 %.
 */
object ConditionIndex {

    private const val REFERENCE_MM = 40.0

    fun calculate(averageCalibre: Double): Int? {
        if (averageCalibre <= 0.0) return null
        return ((averageCalibre / REFERENCE_MM) * 100).roundToInt().coerceIn(0, 100)
    }

    fun label(averageCalibre: Double): String =
        calculate(averageCalibre)?.let { "$it %" } ?: "—"
}
