package com.example.aquasample.ui.history

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryComparisonTest {

    @Test
    fun `calcula variacion de muestras`() {
        val comparison = HistoryComparison(currentCount = 12, previousCount = 10)

        assertEquals(20, comparison.countDeltaPercent)
    }

    @Test
    fun `sin periodo anterior no hay variacion porcentual`() {
        val comparison = HistoryComparison(currentCount = 5, previousCount = 0)

        assertNull(comparison.countDeltaPercent)
    }

    @Test
    fun `calcula diferencia de calibre`() {
        val comparison = HistoryComparison(
            currentAverageCalibre = 37.0,
            previousAverageCalibre = 35.5
        )

        assertEquals(1.5, comparison.calibreDelta ?: 0.0, 0.001)
    }

    @Test
    fun `sin calibres no hay diferencia`() {
        val comparison = HistoryComparison(currentAverageCalibre = null, previousAverageCalibre = 35.5)

        assertNull(comparison.calibreDelta)
    }
}
