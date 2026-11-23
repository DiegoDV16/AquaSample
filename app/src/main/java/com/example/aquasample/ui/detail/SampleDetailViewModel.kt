package com.example.aquasample.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.appContainer
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.data.report.PdfReportGenerator
import com.example.aquasample.data.repository.MuestraRepository
import com.example.aquasample.data.sync.SyncScheduler
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Estado del detalle de una muestra. */
data class SampleDetailUiState(
    val isLoading: Boolean = true,
    val sample: MuestraEntity? = null,
    val centroNombre: String? = null,
    val centroCodigo: String? = null,
    val operadorNombre: String? = null,
    val operadorEmail: String? = null,
    val photos: List<String> = emptyList(),
    val isUpdating: Boolean = false,
    val isExporting: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel del detalle: carga la muestra con sus relaciones y permite al
 * supervisor marcarla como validada.
 */
class SampleDetailViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: MuestraRepository = application.appContainer.muestraRepository
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SampleDetailUiState())
    val uiState: StateFlow<SampleDetailUiState> = _uiState.asStateFlow()

    private var loadedId: String? = null

    fun load(sampleId: String, force: Boolean = false) {
        if (!force && loadedId == sampleId && _uiState.value.sample != null) return
        loadedId = sampleId
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            runCatching { repository.getMuestraConDetalleById(sampleId) }
                .onSuccess { detalle ->
                    if (detalle == null) {
                        _uiState.update {
                            it.copy(isLoading = false, error = "La muestra no existe en el dispositivo.")
                        }
                        return@onSuccess
                    }
                    val photos = detalle.muestra.fotos
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sample = detalle.muestra,
                            centroNombre = detalle.centroNombre,
                            centroCodigo = detalle.centroCodigo,
                            operadorNombre = detalle.operadorNombreCompleto,
                            operadorEmail = detalle.operadorEmail,
                            photos = photos,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudo cargar la muestra: ${exception.localizedMessage}"
                        )
                    }
                }
        }
    }

    /** Marca la muestra como validada por el supervisor. */
    fun validateSample(onResult: (String?) -> Unit) {
        val sample = _uiState.value.sample ?: return
        _uiState.update { it.copy(isUpdating = true) }

        viewModelScope.launch {
            runCatching { repository.updateEstado(sample.id, "VALIDADO") }
                .onSuccess {
                    _uiState.update { it.copy(isUpdating = false) }
                    load(sample.id, force = true)
                    withContext(Dispatchers.Main) { onResult(null) }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(isUpdating = false) }
                    withContext(Dispatchers.Main) {
                        onResult("No se pudo validar la muestra: ${exception.localizedMessage}")
                    }
                }
        }
    }

    /** Actualiza conteo, calibre y observaciones; deja la muestra pendiente de resincronizar. */
    fun updateSample(
        conteo: String,
        calibres: List<String>,
        observaciones: String,
        onResult: (String?) -> Unit
    ) {
        val sample = _uiState.value.sample ?: return
        val validationError = SampleEditValidator.validate(conteo, calibres)
        if (validationError != null) {
            onResult(validationError)
            return
        }

        _uiState.update { it.copy(isUpdating = true) }
        viewModelScope.launch {
            runCatching {
                repository.updateSample(
                    sample.copy(
                        conteoManual = conteo.toIntOrNull()?.toString().orEmpty(),
                        calibreMatrix = calibres,
                        averageCalibre = SampleEditValidator.average(calibres) ?: 0.0,
                        observaciones = observaciones,
                        sincronizado = false
                    )
                )
                SyncScheduler.syncNow(getApplication())
            }.onSuccess {
                _uiState.update { it.copy(isUpdating = false) }
                load(sample.id, force = true)
                withContext(Dispatchers.Main) { onResult(null) }
            }.onFailure { exception ->
                _uiState.update { it.copy(isUpdating = false) }
                withContext(Dispatchers.Main) {
                    onResult("No se pudo actualizar la muestra: ${exception.localizedMessage}")
                }
            }
        }
    }

    /** Borra lógicamente la muestra y deja que el worker propague la eliminación. */
    fun deleteSample(onResult: (String?) -> Unit) {
        val sample = _uiState.value.sample ?: return
        _uiState.update { it.copy(isUpdating = true) }
        viewModelScope.launch {
            runCatching {
                repository.markDeleted(sample.id)
                SyncScheduler.syncNow(getApplication())
            }.onSuccess {
                _uiState.update { it.copy(isUpdating = false) }
                withContext(Dispatchers.Main) { onResult(null) }
            }.onFailure { exception ->
                _uiState.update { it.copy(isUpdating = false) }
                withContext(Dispatchers.Main) {
                    onResult("No se pudo eliminar la muestra: ${exception.localizedMessage}")
                }
            }
        }
    }

    /** Genera el PDF de la muestra en segundo plano y entrega el archivo. */
    fun exportPdf(onResult: (Result<File>) -> Unit) {
        val state = _uiState.value
        val sample = state.sample ?: return

        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                PdfReportGenerator(getApplication())
                    .generate(sample, state.centroNombre, state.operadorNombre)
            }
            _uiState.update { it.copy(isExporting = false) }
            withContext(Dispatchers.Main) { onResult(result) }
        }
    }
}
