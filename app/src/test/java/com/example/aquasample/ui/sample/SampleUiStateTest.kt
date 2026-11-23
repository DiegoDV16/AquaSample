package com.example.aquasample.ui.sample

import org.junit.Assert.assertEquals
import org.junit.Test

class SampleUiStateTest {

    @Test
    fun `promedio usa solo valores numericos`() {
        val matrix = List(30) { "" }.toMutableList()
        matrix[0] = "34.0"
        matrix[1] = "36.0"
        matrix[2] = "38.0"

        val state = SampleUiState(calibreMatrix = matrix)

        assertEquals(36.0, state.averageCalibre, 0.001)
    }

    @Test
    fun `promedio ignora vacios y texto`() {
        val matrix = List(30) { "" }.toMutableList()
        matrix[0] = "30"
        matrix[1] = "abc"
        matrix[2] = "  "

        val state = SampleUiState(calibreMatrix = matrix)

        assertEquals(30.0, state.averageCalibre, 0.001)
    }

    @Test
    fun `promedio sin datos es cero`() {
        assertEquals(0.0, SampleUiState().averageCalibre, 0.001)
    }

    @Test
    fun `conteo asistido refleja las marcas`() {
        val state = SampleUiState(
            countMarks = listOf(CountMark(0.1f, 0.2f), CountMark(0.5f, 0.6f))
        )

        assertEquals(2, state.conteoAsistido)
    }

    @Test
    fun `foto count refleja la lista de fotos`() {
        assertEquals(0, SampleUiState().photoCount)
    }
}
