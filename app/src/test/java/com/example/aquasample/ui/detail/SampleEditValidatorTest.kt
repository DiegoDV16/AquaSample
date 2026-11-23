package com.example.aquasample.ui.detail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SampleEditValidatorTest {

    @Test
    fun `rechaza conteo cero o invalido`() {
        assertEquals(
            "Ingresa un conteo mayor que 0.",
            SampleEditValidator.validate("0", listOf("34.5"))
        )
        assertEquals(
            "Ingresa un conteo mayor que 0.",
            SampleEditValidator.validate("abc", listOf("34.5"))
        )
    }

    @Test
    fun `rechaza matriz sin calibres`() {
        assertEquals(
            "Registra al menos una medición de calibre.",
            SampleEditValidator.validate("30", List(30) { "" })
        )
    }

    @Test
    fun `acepta coma decimal y calcula promedio`() {
        assertNull(SampleEditValidator.validate("30", listOf("34,5", "35.5")))
        assertEquals(35.0, SampleEditValidator.average(listOf("34,5", "35.5")) ?: 0.0, 0.001)
    }

    @Test
    fun `promedio nulo sin valores`() {
        assertNull(SampleEditValidator.average(listOf("", "abc")))
    }
}
