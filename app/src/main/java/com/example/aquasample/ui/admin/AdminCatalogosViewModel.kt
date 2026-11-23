package com.example.aquasample.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.appContainer
import com.example.aquasample.data.local.entity.CentroCultivoEntity
import com.example.aquasample.data.local.entity.TramoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AdminCatalogosUiState(
    val isLoading: Boolean = true,
    val centros: List<CentroCultivoEntity> = emptyList(),
    val tramos: List<TramoEntity> = emptyList()
)

/**
 * Catálogos operativos: centros de cultivo y sus tramos. Las bajas son lógicas
 * (estado INACTIVO) para no romper muestras históricas que los referencian.
 */
class AdminCatalogosViewModel(application: Application) : AndroidViewModel(application) {

    private val centroRepository = application.appContainer.centroRepository
    private val tramoRepository = application.appContainer.tramoRepository

    val uiState: StateFlow<AdminCatalogosUiState> =
        combine(centroRepository.allCentros, tramoRepository.allTramos) { centros, tramos ->
            AdminCatalogosUiState(isLoading = false, centros = centros, tramos = tramos)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AdminCatalogosUiState()
        )

    fun saveCentro(centro: CentroCultivoEntity, onResult: (String?) -> Unit) {
        if (centro.codigo.isBlank() || centro.nombre.isBlank() || centro.comuna.isBlank()) {
            onResult("Completa código, nombre y comuna.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val duplicate = runCatching {
                centroRepository.getCentroByCodigo(centro.codigo.trim())
            }.getOrNull()

            if (duplicate != null && duplicate.id != centro.id) {
                withContext(Dispatchers.Main) {
                    onResult("Ya existe un centro con el código ${centro.codigo}.")
                }
                return@launch
            }

            runCatching {
                centroRepository.insertCentro(
                    centro.copy(
                        codigo = centro.codigo.trim(),
                        nombre = centro.nombre.trim(),
                        comuna = centro.comuna.trim()
                    )
                )
            }.onSuccess {
                withContext(Dispatchers.Main) { onResult(null) }
            }.onFailure { error ->
                withContext(Dispatchers.Main) {
                    onResult("No se pudo guardar el centro: ${error.localizedMessage}")
                }
            }
        }
    }

    fun toggleCentro(centro: CentroCultivoEntity, onResult: (String?) -> Unit) {
        val nuevoEstado = if (centro.estado == "ACTIVO") "INACTIVO" else "ACTIVO"
        saveCentro(centro.copy(estado = nuevoEstado), onResult)
    }

    fun saveTramo(tramo: TramoEntity, onResult: (String?) -> Unit) {
        val error = when {
            tramo.centroId <= 0 -> "Selecciona el centro del tramo."
            tramo.tren.isBlank() -> "Ingresa el tren."
            tramo.linea.isBlank() -> "Ingresa la línea."
            tramo.longitudCm <= 0 -> "El largo del tramo debe ser mayor que 0."
            else -> null
        }
        if (error != null) {
            onResult(error)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                tramoRepository.insertTramo(
                    tramo.copy(
                        tren = tramo.tren.trim(),
                        linea = tramo.linea.trim()
                    )
                )
            }.onSuccess {
                withContext(Dispatchers.Main) { onResult(null) }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    onResult("No se pudo guardar el tramo: ${it.localizedMessage}")
                }
            }
        }
    }

    fun toggleTramo(tramo: TramoEntity, onResult: (String?) -> Unit) {
        val nuevoEstado = if (tramo.estado == "ACTIVO") "INACTIVO" else "ACTIVO"
        saveTramo(tramo.copy(estado = nuevoEstado), onResult)
    }
}
