package com.example.aquasample.ui.designsystem

import org.junit.Assert.assertEquals
import org.junit.Test

class SampleStatusTest {

    @Test
    fun `mapea estados conocidos`() {
        assertEquals(SampleStatus.PENDIENTE, SampleStatus.from("PENDIENTE"))
        assertEquals(SampleStatus.COMPLETADO, SampleStatus.from("completado"))
        assertEquals(SampleStatus.VALIDADO, SampleStatus.from(" Validado "))
        assertEquals(SampleStatus.RECHAZADO, SampleStatus.from("RECHAZADO"))
    }

    @Test
    fun `estado desconocido cae en el valor por defecto`() {
        assertEquals(SampleStatus.DESCONOCIDO, SampleStatus.from(null))
        assertEquals(SampleStatus.DESCONOCIDO, SampleStatus.from(""))
        assertEquals(SampleStatus.DESCONOCIDO, SampleStatus.from("OTRO"))
    }
}
