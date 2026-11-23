package com.example.aquasample.ui.sample

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SampleValidatorTest {

    @Test
    fun `ubicacion exige concesion`() {
        assertEquals(
            "Selecciona la concesión o centro de cultivo.",
            SampleValidator.validateLocation("", "Tren 01", "Línea L-01", "150")
        )
    }

    @Test
    fun `ubicacion exige tren`() {
        assertEquals(
            "Selecciona el tren.",
            SampleValidator.validateLocation("Concesión", "", "Línea L-01", "150")
        )
    }

    @Test
    fun `ubicacion exige linea`() {
        assertEquals(
            "Selecciona la línea de cultivo.",
            SampleValidator.validateLocation("Concesión", "Tren 01", "", "150")
        )
    }

    @Test
    fun `tramo debe ser mayor que cero`() {
        assertEquals(
            "Ingresa el largo del tramo en cm (mayor que 0).",
            SampleValidator.validateLocation("Concesión", "Tren 01", "Línea L-01", "0")
        )
        assertEquals(
            "Ingresa el largo del tramo en cm (mayor que 0).",
            SampleValidator.validateLocation("Concesión", "Tren 01", "Línea L-01", "abc")
        )
    }

    @Test
    fun `ubicacion valida acepta coma decimal`() {
        assertNull(SampleValidator.validateLocation("Concesión", "Tren 01", "Línea L-01", "150,5"))
    }

    @Test
    fun `conteo debe ser positivo`() {
        assertEquals(
            "Ingresa un conteo de individuos mayor que 0.",
            SampleValidator.validateMeasurement("0", listOf("34.0"))
        )
    }

    @Test
    fun `medicion exige al menos un calibre`() {
        assertEquals(
            "Registra al menos una medición de calibre.",
            SampleValidator.validateMeasurement("30", List(30) { "" })
        )
    }

    @Test
    fun `medicion valida con un calibre`() {
        assertNull(SampleValidator.validateMeasurement("30", listOf("34.5")))
    }

    @Test
    fun `evidencia exige fotografia`() {
        assertEquals(
            "Adjunta al menos una fotografía como evidencia.",
            SampleValidator.validateEvidence(0)
        )
        assertNull(SampleValidator.validateEvidence(1))
    }

    @Test
    fun `ubicacion exige tramo`() {
        assertEquals(
            "Selecciona un tramo del centro.",
            SampleValidator.validate(
                SampleUiState(concesion = "Concesión", tren = "Tren 01", linea = "Línea L-01"),
                SampleValidator.STEP_LOCATION
            )
        )
    }

    @Test
    fun `validate despacha segun el paso`() {
        val state = SampleUiState(
            centroId = 1,
            tramoId = 1,
            concesion = "Concesión",
            tren = "Tren",
            linea = "Línea"
        )

        assertEquals(
            "Ingresa el largo del tramo en cm (mayor que 0).",
            SampleValidator.validate(state, SampleValidator.STEP_LOCATION)
        )
        assertEquals(
            "Ingresa un conteo de individuos mayor que 0.",
            SampleValidator.validate(state, SampleValidator.STEP_MEASUREMENT)
        )
        assertEquals(
            "Adjunta al menos una fotografía como evidencia.",
            SampleValidator.validate(state, SampleValidator.STEP_EVIDENCE)
        )
    }
}
