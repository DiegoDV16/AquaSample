package com.example.aquasample.data.repository

import com.example.aquasample.data.local.dao.TramoDao
import com.example.aquasample.data.local.entity.TramoEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TramoRepositoryTest {

    private val dao = mockk<TramoDao>()
    private lateinit var repository: TramoRepository

    @Before
    fun setUp() {
        every { dao.getAllTramos() } returns emptyFlow()
        every { dao.getTramosActivos() } returns emptyFlow()
        repository = TramoRepository(dao)
    }

    @Test
    fun `insertar tramo delega en el dao`() = runBlocking {
        val tramo = TramoEntity(centroId = 1, tren = "Tren 01", linea = "Línea L-01", longitudCm = 150)
        coEvery { dao.insertTramo(tramo) } returns 1L

        assertEquals(1L, repository.insertTramo(tramo))
        coVerify { dao.insertTramo(tramo) }
    }

    @Test
    fun `tramos por centro devuelve el flujo del dao`() = runBlocking {
        val tramo = TramoEntity(id = 3, centroId = 2, tren = "Tren 02", linea = "Línea L-02", longitudCm = 160)
        every { dao.getTramosActivosPorCentro(2) } returns flowOf(listOf(tramo))

        assertEquals(listOf(tramo), repository.tramosActivosPorCentro(2).first())
    }

    @Test
    fun `eliminar tramo delega en el dao`() = runBlocking {
        coEvery { dao.deleteTramoById(5) } returns Unit

        repository.deleteTramoById(5)

        coVerify { dao.deleteTramoById(5) }
    }
}
