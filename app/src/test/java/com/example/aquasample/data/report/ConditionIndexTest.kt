package com.example.aquasample.data.report

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConditionIndexTest {

    @Test
    fun `sin calibre devuelve nulo`() {
        assertNull(ConditionIndex.calculate(0.0))
        assertNull(ConditionIndex.calculate(-1.0))
        assertEquals("—", ConditionIndex.label(0.0))
    }

    @Test
    fun `calcula porcentaje sobre 40 mm`() {
        assertEquals(50, ConditionIndex.calculate(20.0))
        assertEquals(100, ConditionIndex.calculate(40.0))
        assertEquals(91, ConditionIndex.calculate(36.4))
    }

    @Test
    fun `limita el indice a 100`() {
        assertEquals(100, ConditionIndex.calculate(55.0))
    }
}
