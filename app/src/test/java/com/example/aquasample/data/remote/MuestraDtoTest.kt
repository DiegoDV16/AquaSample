package com.example.aquasample.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuestraDtoTest {

    private val dto = MuestraDto(
        id = "#M-9000",
        centroId = 1,
        tramoId = 2,
        operadorId = "USR-001",
        concesion = "Concesión Dalcahue #104",
        tren = "Tren 02",
        linea = "Línea L-03",
        tramoLongitudCm = "145",
        conteoManual = 30,
        calibreMatrix = "[\"36.1\",\"36.4\",\"37.0\"]",
        averageCalibre = 36.5,
        marcasConteo = "[{\"x\":0.1,\"y\":0.2}]",
        conteoAsistido = 1,
        photoCount = 2,
        fotos = "[\"/otro/telefono/foto.jpg\"]",
        observaciones = "Descargada",
        tags = "[\"ValvasSanas\"]",
        fechaHora = "22/11/2026 09:00",
        timestamp = 1_763_810_000_000,
        estado = "VALIDADO",
        sincronizado = true,
        operatorAlias = "Francisca"
    )

    @Test
    fun `convierte listas y conserva el estado del servidor`() {
        val entity = dto.toEntity()

        assertEquals("#M-9000", entity.id)
        assertEquals(listOf("36.1", "36.4", "37.0"), entity.calibreMatrix)
        assertEquals(listOf("ValvasSanas"), entity.tags)
        assertEquals("VALIDADO", entity.estado)
        assertEquals(36.5, entity.averageCalibre, 0.001)
        assertTrue(entity.sincronizado)
    }

    @Test
    fun `no importa rutas de fotos de otro telefono`() {
        val entity = dto.toEntity()

        assertTrue(entity.fotos.isEmpty())
        assertEquals(0, entity.photoCount)
    }

    @Test
    fun `tolera json invalido`() {
        val entity = dto.copy(calibreMatrix = "no-json", tags = null).toEntity()

        assertTrue(entity.calibreMatrix.isEmpty())
        assertTrue(entity.tags.isEmpty())
    }

    @Test
    fun `mapea tombstone y version de modificacion`() {
        val entity = dto.copy(eliminado = true, updatedAt = 123456789L).toEntity()

        assertTrue(entity.eliminado)
        assertEquals(123456789L, entity.updatedAt)
    }
}
