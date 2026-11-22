package com.example.aquasample.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.appContainer
import com.example.aquasample.ui.designsystem.SampleStatus
import com.example.aquasample.ui.model.SampleSummary
import com.example.aquasample.ui.model.toSampleSummary
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Rangos de fecha disponibles en el historial. */
enum class HistoryPeriod(val label: String, val daysBack: Int?) {
    ALL("Todo", null),
    TODAY("Hoy", 0),
    WEEK("7 días", 7),
    MONTH("30 días", 30)
}

/** Filtros activos del historial. */
data class HistoryFilters(
    val query: String = "",
    val status: SampleStatus? = null,
    val period: HistoryPeriod = HistoryPeriod.ALL
) {
    val isActive: Boolean
        get() = query.isNotBlank() || status != null || period != HistoryPeriod.ALL
}

/** Estado del historial, ya filtrado y con conteos por estado. */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val filters: HistoryFilters = HistoryFilters(),
    val items: List<SampleSummary> = emptyList(),
    val totalCount: Int = 0,
    val statusCounts: Map<SampleStatus, Int> = emptyMap(),
    val error: String? = null
)

/**
 * ViewModel del historial. Combina el flujo de Room con los filtros elegidos
 * por el usuario; la pantalla solo dibuja.
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = application.appContainer.muestraRepository
    private val filters = MutableStateFlow(HistoryFilters())

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.allMuestrasConDetalle,
        filters
    ) { muestras, currentFilters ->
        val all = muestras.map { it.toSampleSummary() }
        HistoryUiState(
            isLoading = false,
            filters = currentFilters,
            items = all.filter { it.matches(currentFilters) },
            totalCount = all.size,
            statusCounts = all.groupingBy { it.status }.eachCount()
        )
    }
        .catch {
            emit(
                HistoryUiState(
                    isLoading = false,
                    error = "No se pudo cargar el historial."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )

    fun onQueryChange(query: String) {
        filters.update { it.copy(query = query) }
    }

    fun onStatusChange(status: SampleStatus?) {
        filters.update { it.copy(status = status) }
    }

    fun onPeriodChange(period: HistoryPeriod) {
        filters.update { it.copy(period = period) }
    }

    fun clearFilters() {
        filters.value = HistoryFilters()
    }

    private fun SampleSummary.matches(filters: HistoryFilters): Boolean {
        val query = filters.query.trim()
        val matchesQuery = query.isEmpty() ||
            id.contains(query, ignoreCase = true) ||
            location.contains(query, ignoreCase = true) ||
            operator.contains(query, ignoreCase = true)

        val matchesStatus = filters.status == null || status == filters.status

        val matchesPeriod = when (filters.period) {
            HistoryPeriod.ALL -> true
            HistoryPeriod.TODAY -> isSameDay(timestamp, System.currentTimeMillis())
            else -> timestamp >= startOfDayMillis(daysBack = filters.period.daysBack ?: 0)
        }

        return matchesQuery && matchesStatus && matchesPeriod
    }

    private fun isSameDay(first: Long, second: Long): Boolean {
        val firstCalendar = Calendar.getInstance().apply { timeInMillis = first }
        val secondCalendar = Calendar.getInstance().apply { timeInMillis = second }
        return firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR) &&
            firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun startOfDayMillis(daysBack: Int): Long {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysBack)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
