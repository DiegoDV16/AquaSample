package com.example.aquasample.ui.history

import com.example.aquasample.ui.model.SampleSummary
import java.util.Calendar

/** Reglas de filtrado del historial, aisladas para poder probarlas. */
object HistoryFilter {

    fun matches(
        summary: SampleSummary,
        filters: HistoryFilters,
        now: Long = System.currentTimeMillis()
    ): Boolean {
        val query = filters.query.trim()
        val matchesQuery = query.isEmpty() ||
            summary.id.contains(query, ignoreCase = true) ||
            summary.location.contains(query, ignoreCase = true) ||
            summary.operator.contains(query, ignoreCase = true)

        val matchesStatus = filters.status == null || summary.status == filters.status
        val matchesCentro = filters.centroId == null || summary.centroId == filters.centroId
        val matchesTren = filters.tren == null || summary.tren == filters.tren

        val matchesPeriod = when (filters.period) {
            HistoryPeriod.ALL -> true
            HistoryPeriod.TODAY -> isSameDay(summary.timestamp, now)
            else -> summary.timestamp >= startOfDayMillis(filters.period.daysBack ?: 0, now)
        }

        return matchesQuery && matchesStatus && matchesPeriod && matchesCentro && matchesTren
    }

    fun isSameDay(first: Long, second: Long): Boolean {
        val firstCalendar = Calendar.getInstance().apply { timeInMillis = first }
        val secondCalendar = Calendar.getInstance().apply { timeInMillis = second }
        return firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR) &&
            firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR)
    }

    fun startOfDayMillis(daysBack: Int, now: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -daysBack)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
