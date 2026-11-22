package com.example.aquasample.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.appContainer
import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.data.sync.SyncScheduler
import com.example.aquasample.ui.designsystem.SampleStatus
import com.example.aquasample.ui.model.SampleSummary
import com.example.aquasample.ui.model.toSampleSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Punto de la serie de calibre promedio por día. */
data class CalibrePoint(val label: String, val value: Double)

/** Estado de la pantalla de inicio, derivado de los datos locales. */
data class HomeUiState(
    val isLoading: Boolean = true,
    val totalSamples: Int = 0,
    val averageCalibre: Double? = null,
    val samplesThisWeek: Int = 0,
    val pendingValidation: Int = 0,
    val validatedSamples: Int = 0,
    val pendingSync: Int = 0,
    val recent: List<SampleSummary> = emptyList(),
    val statusCounts: Map<SampleStatus, Int> = emptyMap(),
    val trend: List<CalibrePoint> = emptyList(),
    val error: String? = null
)

/**
 * ViewModel del panel de inicio. Observa Room como única fuente de verdad y
 * calcula los indicadores en memoria; la vista no conoce el DAO.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = application.appContainer.muestraRepository

    val uiState: StateFlow<HomeUiState> = repository.allMuestrasConDetalle
        .map { muestras -> muestras.toUiState() }
        .catch {
            emit(HomeUiState(isLoading = false, error = "No se pudieron cargar los registros."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    private fun List<MuestraConDetalle>.toUiState(): HomeUiState {
        val weekStart = startOfWeekMillis()
        val calibres = mapNotNull { detalle ->
            detalle.muestra.averageCalibre.takeIf { valor -> valor > 0.0 }
        }

        return HomeUiState(
            isLoading = false,
            totalSamples = size,
            averageCalibre = if (calibres.isEmpty()) null else calibres.average(),
            samplesThisWeek = count { it.muestra.timestamp >= weekStart },
            pendingValidation = count {
                SampleStatus.from(it.muestra.estado) == SampleStatus.PENDIENTE
            },
            validatedSamples = count {
                SampleStatus.from(it.muestra.estado) == SampleStatus.VALIDADO
            },
            pendingSync = count { !it.muestra.sincronizado },
            recent = take(4).map { it.toSampleSummary() },
            statusCounts = groupingBy { SampleStatus.from(it.muestra.estado) }.eachCount(),
            trend = buildTrend()
        )
    }

    /** Promedio diario de calibre para los últimos siete días. */
    private fun List<MuestraConDetalle>.buildTrend(): List<CalibrePoint> {
        val formatter = SimpleDateFormat("dd/MM", Locale.getDefault())
        val todayStart = startOfDayMillis()
        val points = mutableListOf<CalibrePoint>()

        for (daysAgo in 6 downTo 0) {
            val start = todayStart - daysAgo * DAY_MILLIS
            val end = start + DAY_MILLIS
            val values = filter { detalle ->
                detalle.muestra.timestamp in start until end &&
                    detalle.muestra.averageCalibre > 0.0
            }.map { it.muestra.averageCalibre }

            if (values.isNotEmpty()) {
                points += CalibrePoint(
                    label = formatter.format(Date(start)),
                    value = values.average()
                )
            }
        }
        return points
    }

    private fun startOfDayMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /** Dispara un intento inmediato de sincronización con el backend. */
    fun syncNow() {
        SyncScheduler.syncNow(getApplication())
    }

    private fun startOfWeekMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
