package com.example.aquasample.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class MarineCoordinatesTest {

    @Test
    fun `cada centro tiene coordenadas propias`() {
        assertEquals(-42.38 to -73.65, MarineCoordinates.forCentro(1L))
        assertEquals(-42.60 to -73.65, MarineCoordinates.forCentro(2L))
        assertEquals(-41.87 to -73.83, MarineCoordinates.forCentro(3L))
    }

    @Test
    fun `centro desconocido usa coordenada de respaldo`() {
        assertEquals(-42.48 to -73.76, MarineCoordinates.forCentro(99L))
        assertEquals(-42.48 to -73.76, MarineCoordinates.forCentro(null))
    }
}
