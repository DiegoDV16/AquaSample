package com.example.aquasample.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class MarineDtoTest {

    @Test
    fun `mapea y conserva los datos marinos`() {
        val dto = MuestraDto(
            id = "#M-9100",
            marineTime = "2026-11-23T05:00",
            marineWaveHeight = 0.4,
            marineSeaSurfaceTemperature = 14.8,
            marineWindSpeed = 6.2
        )

        val entity = dto.toEntity()
        assertEquals("2026-11-23T05:00", entity.marineTime)
        assertEquals(0.4, entity.marineWaveHeight ?: 0.0, 0.001)
        assertEquals(14.8, entity.marineSeaSurfaceTemperature ?: 0.0, 0.001)
        assertEquals(6.2, entity.marineWindSpeed ?: 0.0, 0.001)

        val roundTrip = entity.toDto()
        assertEquals(0.4, roundTrip.marineWaveHeight ?: 0.0, 0.001)
        assertEquals(6.2, roundTrip.marineWindSpeed ?: 0.0, 0.001)
    }
}
