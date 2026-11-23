package com.example.aquasample.data.repository

import com.example.aquasample.data.local.dao.MuestraDao
import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.data.local.entity.MuestraEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MuestraRepositoryTest {

    private val dao = mockk<MuestraDao>()
    private lateinit var repository: MuestraRepository

    @Before
    fun setUp() {
        every { dao.getAllSamples() } returns emptyFlow()
        every { dao.getAllMuestrasConDetalle() } returns emptyFlow()
        repository = MuestraRepository(dao)
    }

    @Test
    fun `marcar sincronizada delega en el dao`() = runBlocking {
        coEvery { dao.markSynced("#M-0001") } returns 1

        val updated = repository.markSynced("#M-0001")

        assertEquals(1, updated)
        coVerify { dao.markSynced("#M-0001") }
    }

    @Test
    fun `obtener pendientes devuelve lo que entrega el dao`() = runBlocking {
        val pendiente = MuestraEntity(id = "#M-0002", concesion = "Concesión Dalcahue #104")
        coEvery { dao.getPendingSync() } returns listOf(pendiente)

        val result = repository.getPendingSync()

        assertEquals(listOf(pendiente), result)
        coVerify { dao.getPendingSync() }
    }

    @Test
    fun `insertar actualizar y eliminar delegan en el dao`() = runBlocking {
        val muestra = MuestraEntity(id = "#M-0003", concesion = "Concesión")
        coEvery { dao.insertSample(muestra) } returns 1L
        coEvery { dao.updateSample(muestra) } returns 1
        coEvery { dao.deleteSample(muestra) } returns 1

        repository.insertSample(muestra)
        repository.updateSample(muestra)
        repository.deleteSample(muestra)

        coVerify { dao.insertSample(muestra) }
        coVerify { dao.updateSample(muestra) }
        coVerify { dao.deleteSample(muestra) }
    }

    @Test
    fun `consultas por id y ultima muestra delegan en el dao`() = runBlocking {
        val muestra = MuestraEntity(id = "#M-0004", concesion = "Concesión")
        coEvery { dao.getSampleById("#M-0004") } returns muestra
        coEvery { dao.getLastSample() } returns muestra

        assertEquals(muestra, repository.getSampleById("#M-0004"))
        assertEquals(muestra, repository.getLastSample())
    }

    @Test
    fun `detalle por id y cambios de estado delegan en el dao`() = runBlocking {
        val detalle = MuestraConDetalle(MuestraEntity(id = "#M-0005", concesion = "Concesión"))
        coEvery { dao.getMuestraConDetalleById("#M-0005") } returns detalle
        coEvery { dao.updateEstado("#M-0005", "VALIDADO") } returns 1
        coEvery { dao.updateEstadoSincronizacion("#M-0005", true, "VALIDADO") } returns 1

        assertEquals(detalle, repository.getMuestraConDetalleById("#M-0005"))
        assertEquals(1, repository.updateEstado("#M-0005", "VALIDADO"))
        repository.updateEstadoSincronizacion("#M-0005", true, "VALIDADO")

        coVerify { dao.updateEstado("#M-0005", "VALIDADO") }
        coVerify { dao.updateEstadoSincronizacion("#M-0005", true, "VALIDADO") }
    }

    @Test
    fun `filtradas devuelve el flujo del dao`() = runBlocking {
        every { dao.getMuestrasFiltradas("L-01", "PENDIENTE") } returns flowOf(emptyList())

        val result = repository.getMuestrasFiltradas("L-01", "PENDIENTE").first()

        assertTrue(result.isEmpty())
        verify { dao.getMuestrasFiltradas("L-01", "PENDIENTE") }
    }
}
