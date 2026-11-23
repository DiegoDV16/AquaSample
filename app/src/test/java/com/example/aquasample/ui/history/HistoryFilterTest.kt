package com.example.aquasample.ui.history

import com.example.aquasample.ui.designsystem.SampleStatus
import com.example.aquasample.ui.model.SampleSummary
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryFilterTest {

    private val now = System.currentTimeMillis()

    private fun summary(
        id: String = "#M-0001",
        timestamp: Long = now,
        status: SampleStatus = SampleStatus.COMPLETADO,
        location: String = "Línea L-01 · Tren 01",
        operator: String = "Francisca Rojas",
        centroId: Long? = 1L,
        tren: String = "Tren 01"
    ) = SampleSummary(
        id = id,
        location = location,
        dateLabel = "22/11/2026 08:30",
        timestamp = timestamp,
        status = status,
        count = 30,
        averageCalibre = 36.4,
        synced = true,
        operator = operator,
        centroId = centroId,
        tren = tren
    )

    private fun daysAgo(days: Int): Long = HistoryFilter.startOfDayMillis(days, now) + 3_600_000

    @Test
    fun `periodo hoy solo incluye el dia actual`() {
        val filters = HistoryFilters(period = HistoryPeriod.TODAY)

        assertTrue(HistoryFilter.matches(summary(timestamp = now), filters, now))
        assertFalse(HistoryFilter.matches(summary(timestamp = daysAgo(1)), filters, now))
    }

    @Test
    fun `periodo semana incluye los ultimos siete dias`() {
        val filters = HistoryFilters(period = HistoryPeriod.WEEK)

        assertTrue(HistoryFilter.matches(summary(timestamp = daysAgo(2)), filters, now))
        assertFalse(HistoryFilter.matches(summary(timestamp = daysAgo(10)), filters, now))
    }

    @Test
    fun `periodo mes incluye los ultimos treinta dias`() {
        val filters = HistoryFilters(period = HistoryPeriod.MONTH)

        assertTrue(HistoryFilter.matches(summary(timestamp = daysAgo(20)), filters, now))
        assertFalse(HistoryFilter.matches(summary(timestamp = daysAgo(40)), filters, now))
    }

    @Test
    fun `periodo todo incluye cualquier fecha`() {
        val filters = HistoryFilters(period = HistoryPeriod.ALL)

        assertTrue(HistoryFilter.matches(summary(timestamp = daysAgo(200)), filters, now))
    }

    @Test
    fun `busqueda coincide por id ubicacion u operador sin distinguir mayusculas`() {
        val filters = HistoryFilters(query = "l-01")

        assertTrue(HistoryFilter.matches(summary(location = "Línea L-01"), filters, now))
        assertTrue(HistoryFilter.matches(summary(id = "#M-0001"), HistoryFilters(query = "m-0001"), now))
        assertTrue(HistoryFilter.matches(summary(operator = "Francisca"), HistoryFilters(query = "FRANCISCA"), now))
        assertFalse(HistoryFilter.matches(summary(), HistoryFilters(query = "inexistente"), now))
    }

    @Test
    fun `filtro por estado`() {
        val filters = HistoryFilters(status = SampleStatus.PENDIENTE)

        assertTrue(HistoryFilter.matches(summary(status = SampleStatus.PENDIENTE), filters, now))
        assertFalse(HistoryFilter.matches(summary(status = SampleStatus.COMPLETADO), filters, now))
    }

    @Test
    fun `filtros se combinan`() {
        val filters = HistoryFilters(
            query = "L-01",
            status = SampleStatus.VALIDADO,
            period = HistoryPeriod.WEEK
        )

        assertTrue(
            HistoryFilter.matches(
                summary(timestamp = daysAgo(1), status = SampleStatus.VALIDADO, location = "Línea L-01"),
                filters,
                now
            )
        )
        assertFalse(
            HistoryFilter.matches(
                summary(timestamp = daysAgo(1), status = SampleStatus.PENDIENTE, location = "Línea L-01"),
                filters,
                now
            )
        )
    }

    @Test
    fun `filtra por centro y tren`() {
        val filters = HistoryFilters(centroId = 2L, tren = "Tren 03")

        assertTrue(
            HistoryFilter.matches(
                summary(centroId = 2L, tren = "Tren 03"),
                filters,
                now
            )
        )
        assertFalse(
            HistoryFilter.matches(
                summary(centroId = 1L, tren = "Tren 03"),
                filters,
                now
            )
        )
        assertFalse(
            HistoryFilter.matches(
                summary(centroId = 2L, tren = "Tren 01"),
                filters,
                now
            )
        )
    }

    @Test
    fun `usa la hora actual cuando no se inyecta`() {        assertTrue(
            HistoryFilter.matches(
                summary(timestamp = System.currentTimeMillis()),
                HistoryFilters(period = HistoryPeriod.TODAY)
            )
        )
    }

    @Test
    fun `inicio del dia sin hora explicita es anterior a ahora`() {
        val start = HistoryFilter.startOfDayMillis(1)

        assertTrue(start < System.currentTimeMillis())
    }
}
