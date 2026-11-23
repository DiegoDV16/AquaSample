package com.example.aquasample.ui.history

import android.app.Application
import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.data.repository.CentroCultivoRepository
import com.example.aquasample.data.repository.MuestraRepository
import com.example.aquasample.ui.designsystem.SampleStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val application = mockk<Application>(relaxed = true)
    private val repository = mockk<MuestraRepository>()
    private val centros = mockk<CentroCultivoRepository>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun detalle(id: String, estado: String, timestamp: Long = System.currentTimeMillis()) =
        MuestraConDetalle(
            muestra = MuestraEntity(
                id = id,
                concesion = "Dalcahue",
                tren = "Tren 02",
                linea = "Línea L-03",
                conteoManual = "30",
                averageCalibre = 36.4,
                estado = estado,
                timestamp = timestamp,
                sincronizado = true
            ),
            centroNombre = "Dalcahue",
            operadorNombreCompleto = "Francisca Rojas"
        )

    private fun viewModel(): HistoryViewModel {
        every { repository.allMuestrasConDetalle } returns flowOf(
            listOf(
                detalle("#M-0001", "VALIDADO"),
                detalle("#M-0002", "PENDIENTE")
            )
        )
        every { centros.centrosActivos } returns flowOf(emptyList())
        return HistoryViewModel(application, repository, centros)
    }

    @Test
    fun `carga y filtra por estado`() = runTest(dispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.items.size)

        viewModel.onStatusChange(SampleStatus.VALIDADO)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals("#M-0001", viewModel.uiState.value.items.first().id)

        viewModel.clearFilters()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.items.size)
    }

    @Test
    fun `filtra por busqueda de texto`() = runTest(dispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onQueryChange("#M-0002")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals("#M-0002", viewModel.uiState.value.items.first().id)
    }

    @Test
    fun `periodo hoy descarta muestras antiguas`() = runTest(dispatcher) {
        every { repository.allMuestrasConDetalle } returns flowOf(
            listOf(
                detalle("#M-0003", "COMPLETADO"),
                detalle("#M-0004", "COMPLETADO", timestamp = 1_000L)
            )
        )
        every { centros.centrosActivos } returns flowOf(emptyList())
        val viewModel = HistoryViewModel(application, repository, centros)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        viewModel.onPeriodChange(HistoryPeriod.TODAY)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals("#M-0003", viewModel.uiState.value.items.first().id)
    }
}
