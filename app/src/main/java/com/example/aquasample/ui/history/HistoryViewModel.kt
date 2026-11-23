package com.example.aquasample.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.appContainer
import com.example.aquasample.data.local.entity.CentroCultivoEntity
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
    val period: HistoryPeriod = HistoryPeriod.ALL,
    val centroId: Long? = null,
    val tren: String? = null
) {
    val isActive: Boolean
        get() = query.isNotBlank() || status != null || period != HistoryPeriod.ALL ||
            centroId != null || tren != null
}

/** Comparación del período elegido contra el período anterior equivalente. */
data class HistoryComparison(
    val currentLabel: String = "",
    val previousLabel: String = "",
    val currentCount: Int = 0,
    val previousCount: Int = 0,
    val currentAverageCalibre: Double? = null,
    val previousAverageCalibre: Double? = null
) {
    val countDeltaPercent: Int?
        get() = if (previousCount == 0) null else ((currentCount - previousCount) * 100) / previousCount

    val calibreDelta: Double?
        get() = if (currentAverageCalibre == null || previousAverageCalibre == null) {
            null
        } else {
            currentAverageCalibre - previousAverageCalibre
        }
}

/** Estado del historial, ya filtrado y con conteos por estado. */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val filters: HistoryFilters = HistoryFilters(),
    val items: List<SampleSummary> = emptyList(),
    val totalCount: Int = 0,
    val statusCounts: Map<SampleStatus, Int> = emptyMap(),
    val centros: List<CentroCultivoEntity> = emptyList(),
    val trenes: List<String> = emptyList(),
    val comparison: HistoryComparison = HistoryComparison(),
    val error: String? = null
)

/**
 * ViewModel del historial. Combina el flujo de Room con los filtros elegidos
 * por el usuario; la pantalla solo dibuja.
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = application.appContainer.muestraRepository
    private val centroRepository = application.appContainer.centroRepository
    private val filters = MutableStateFlow(HistoryFilters())

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.allMuestrasConDetalle,
        filters,
        centroRepository.centrosActivos
    ) { muestras, currentFilters, centros ->
        val all = muestras.map { it.toSampleSummary() }
        val filtered = all.filter { HistoryFilter.matches(it, currentFilters) }

        HistoryUiState(
            isLoading = false,
            filters = currentFilters,
            items = filtered,
            totalCount = all.size,
            statusCounts = all.groupingBy { it.status }.eachCount(),
            centros = centros,
            trenes = all.map { it.tren }.filter { it.isNotBlank() }.distinct().sorted(),
            comparison = buildComparison(all, currentFilters)
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

    fun onCentroChange(centroId: Long?) {
        filters.update { it.copy(centroId = centroId) }
    }

    fun onTrenChange(tren: String?) {
        filters.update { it.copy(tren = tren) }
    }

    fun clearFilters() {
        filters.value = HistoryFilters()
    }

    /**
     * Compara el período seleccionado con el anterior equivalente, aplicando
     * búsqueda, estado, centro y tren, pero sin recortar por fecha.
     */
    private fun buildComparison(
        all: List<SampleSummary>,
        currentFilters: HistoryFilters,
        now: Long = System.currentTimeMillis()
    ): HistoryComparison {
        val base = all.filter {
            HistoryFilter.matches(it, currentFilters.copy(period = HistoryPeriod.ALL), now)
        }

        val days = when (currentFilters.period) {
            HistoryPeriod.TODAY -> 1
            HistoryPeriod.WEEK -> 7
            HistoryPeriod.MONTH -> 30
            HistoryPeriod.ALL -> 30
        }
        val currentStart = HistoryFilter.startOfDayMillis(days, now)
        val previousStart = HistoryFilter.startOfDayMillis(days * 2, now)

        val current = base.filter { it.timestamp >= currentStart }
        val previous = base.filter { it.timestamp in previousStart until currentStart }

        fun average(items: List<SampleSummary>): Double? {
            val values = items.map { it.averageCalibre }.filter { it > 0.0 }
            return if (values.isEmpty()) null else values.average()
        }

        return HistoryComparison(
            currentLabel = when (currentFilters.period) {
                HistoryPeriod.TODAY -> "Hoy"
                HistoryPeriod.WEEK -> "Últimos 7 días"
                HistoryPeriod.MONTH -> "Últimos 30 días"
                HistoryPeriod.ALL -> "Últimos 30 días"
            },
            previousLabel = when (currentFilters.period) {
                HistoryPeriod.TODAY -> "Ayer"
                else -> "Período anterior"
            },
            currentCount = current.size,
            previousCount = previous.size,
            currentAverageCalibre = average(current),
            previousAverageCalibre = average(previous)
        )
    }
}
