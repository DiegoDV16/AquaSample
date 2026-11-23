package com.example.aquasample.data.report

import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.data.local.entity.MuestraEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyCsvTest {

    private fun detalle(id: String, observaciones: String) = MuestraConDetalle(
        muestra = MuestraEntity(
            id = id,
            concesion = "Concesión Dalcahue #104",
            tren = "Tren 02",
            linea = "Línea L-03",
            conteoManual = "30",
            averageCalibre = 36.25,
            estado = "VALIDADO",
            observaciones = observaciones
        ),
        centroNombre = "Dalcahue",
        operadorNombreCompleto = "Francisca Rojas"
    )

    @Test
    fun `genera encabezado y filas para Excel`() {
        val csv = JourneyCsv.build(
            listOf(
                detalle("#M-0001", "Sin novedad"),
                detalle("#M-0002", "Observación con ; punto y coma")
            )
        )

        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("ID;Fecha;Centro"))
        assertTrue(csv.contains("\"#M-0001\""))
        assertTrue(csv.contains("\"36.25\""))
        assertTrue(csv.contains("\"Observación con ; punto y coma\""))
        assertTrue(csv.lines().count { it.isNotBlank() } == 3)
    }
}
