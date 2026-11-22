package com.example.aquasample.ui.model

import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.ui.designsystem.SampleStatus

/**
 * Proyección ligera de una muestra para listados (inicio e historial).
 * Evita que las pantallas conozcan la entidad de Room.
 */
data class SampleSummary(
    val id: String,
    val location: String,
    val dateLabel: String,
    val timestamp: Long,
    val status: SampleStatus,
    val count: Int?,
    val averageCalibre: Double,
    val synced: Boolean,
    val operator: String
)

fun MuestraConDetalle.toSampleSummary(): SampleSummary {
    val muestra = this.muestra
    val location = buildString {
        val linea = muestra.linea.ifBlank { null }
        val tren = muestra.tren.ifBlank { null }
        if (linea != null) append(linea)
        if (linea != null && tren != null) append(" · ")
        if (tren != null) append(tren)
        if (isEmpty()) append(muestra.concesion.ifBlank { "Sin ubicación" })
    }

    return SampleSummary(
        id = muestra.id,
        location = location,
        dateLabel = muestra.fechaHora.ifBlank { "Sin fecha" },
        timestamp = muestra.timestamp,
        status = SampleStatus.from(muestra.estado),
        count = muestra.conteoManual.toIntOrNull(),
        averageCalibre = muestra.averageCalibre,
        synced = muestra.sincronizado,
        operator = operadorNombreCompleto?.ifBlank { null }
            ?: muestra.operatorAlias.ifBlank { "Operador" }
    )
}
