package com.example.aquasample.ui.model

import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.ui.designsystem.SampleStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleSummaryTest {

    private val muestra = MuestraEntity(
        id = "#M-0001",
        concesion = "Concesión Dalcahue #104",
        tren = "Tren 02",
        linea = "Línea L-03",
        conteoManual = "30",
        averageCalibre = 36.4,
        fechaHora = "22/11/2026 08:30",
        timestamp = 1_763_810_000_000,
        estado = "COMPLETADO",
        sincronizado = true,
        operatorAlias = "Francisca"
    )

    @Test
    fun `mapea ubicacion operador y estado`() {
        val summary = MuestraConDetalle(
            muestra = muestra,
            operadorNombreCompleto = "Francisca Rojas Soto"
        ).toSampleSummary()

        assertEquals("#M-0001", summary.id)
        assertEquals("Línea L-03 · Tren 02", summary.location)
        assertEquals("Francisca Rojas Soto", summary.operator)
        assertEquals(SampleStatus.COMPLETADO, summary.status)
        assertEquals(30, summary.count)
        assertEquals(36.4, summary.averageCalibre, 0.001)
        assertTrue(summary.synced)
    }

    @Test
    fun `usa concesion cuando no hay tren ni linea`() {
        val summary = MuestraConDetalle(
            muestra = muestra.copy(tren = "", linea = "", operadorId = null)
        ).toSampleSummary()

        assertEquals("Concesión Dalcahue #104", summary.location)
        assertEquals("Francisca", summary.operator)
    }

    @Test
    fun `marca pendiente cuando no esta sincronizada`() {
        val summary = MuestraConDetalle(
            muestra = muestra.copy(sincronizado = false, estado = "PENDIENTE")
        ).toSampleSummary()

        assertFalse(summary.synced)
        assertEquals(SampleStatus.PENDIENTE, summary.status)
    }
}
