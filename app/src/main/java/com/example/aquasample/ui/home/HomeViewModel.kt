package com.example.aquasample.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.appContainer
import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.ui.designsystem.SampleStatus
import com.example.aquasample.ui.model.SampleSummary
import com.example.aquasample.ui.model.toSampleSummary
import java.util.Calendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Estado de la pantalla de inicio, derivado de los datos locales. */
data class HomeUiState(
    val isLoading: Boolean = true,
    val totalSamples: Int = 0,
    val averageCalibre: Double? = null,
    val samplesThisWeek: Int = 0,
    val pendingValidation: Int = 0,
    val validatedSamples: Int = 0,
    val recent: List<SampleSummary> = emptyList(),
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
            recent = take(4).map { it.toSampleSummary() }
        )
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
}
