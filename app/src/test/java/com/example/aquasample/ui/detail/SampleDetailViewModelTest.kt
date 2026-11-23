package com.example.aquasample.ui.detail

import android.app.Application
import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.data.repository.MuestraRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SampleDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val application = mockk<Application>(relaxed = true)
    private val repository = mockk<MuestraRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun detalle(estado: String = "COMPLETADO") = MuestraConDetalle(
        muestra = MuestraEntity(
            id = "#M-0001",
            concesion = "Dalcahue",
            tren = "Tren 02",
            linea = "Línea L-03",
            conteoManual = "30",
            averageCalibre = 36.4,
            estado = estado,
            fotos = listOf("/tmp/foto.jpg"),
            sincronizado = true
        ),
        centroNombre = "Dalcahue",
        centroCodigo = "CONC-104",
        operadorNombreCompleto = "Francisca Rojas",
        operadorEmail = "operador@aquasample.cl"
    )

    @Test
    fun `carga la muestra con sus relaciones`() = runTest(dispatcher) {
        coEvery { repository.getMuestraConDetalleById("#M-0001") } returns detalle()
        val viewModel = SampleDetailViewModel(application, repository)

        viewModel.load("#M-0001")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("#M-0001", state.sample?.id)
        assertEquals("Dalcahue", state.centroNombre)
        assertEquals("operador@aquasample.cl", state.operadorEmail)
        assertEquals(1, state.photos.size)
        assertNull(state.error)
    }

    @Test
    fun `informa cuando la muestra no existe`() = runTest(dispatcher) {
        coEvery { repository.getMuestraConDetalleById("#M-9999") } returns null
        val viewModel = SampleDetailViewModel(application, repository)

        viewModel.load("#M-9999")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.error?.contains("no existe") == true)
    }

    @Test
    fun `valida la muestra y recarga`() = runTest(dispatcher) {
        coEvery { repository.getMuestraConDetalleById("#M-0001") } returns detalle()
        coEvery { repository.updateEstado("#M-0001", "VALIDADO") } returns 1
        val viewModel = SampleDetailViewModel(application, repository)
        viewModel.load("#M-0001")
        advanceUntilIdle()

        var result: String? = "sin-llamar"
        viewModel.validateSample { result = it }
        advanceUntilIdle()

        assertNull(result)
        assertTrue(viewModel.uiState.value.isUpdating.not())
    }
}
