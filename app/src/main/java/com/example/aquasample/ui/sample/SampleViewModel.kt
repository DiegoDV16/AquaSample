package com.example.aquasample.ui.sample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.data.local.database.AppDatabase
import com.example.aquasample.data.local.entity.MuestraEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SampleViewModel(application: Application) : AndroidViewModel(application) {

    private val muestraDao = AppDatabase.getInstance(application).muestraDao()

    private val _uiState = MutableStateFlow(
        SampleUiState(
            calibreMatrix = List(30) { index ->
                when (index) {
                    0 -> "55"; 1 -> "62"; 2 -> "58"; 3 -> "60"; 4 -> "54"
                    5 -> "64"; 6 -> "59"; 7 -> "61"; 8 -> "57"; 9 -> "63"
                    else -> ""
                }
            }
        )
    )
    val uiState: StateFlow<SampleUiState> = _uiState.asStateFlow()

    fun onConcesionChanged(value: String) {
        _uiState.update { it.copy(concesion = value) }
    }

    fun onTrenChanged(value: String) {
        _uiState.update { it.copy(tren = value) }
    }

    fun onLineaChanged(value: String) {
        _uiState.update { it.copy(linea = value) }
    }

    fun onTramoLongitudChanged(value: String) {
        _uiState.update { it.copy(tramoLongitudCm = value) }
    }

    fun onConteoManualChanged(value: String) {
        _uiState.update { it.copy(conteoManual = value) }
    }

    fun onCalibreCellChanged(index: Int, value: String) {
        if (index in 0 until 30) {
            val newList = _uiState.value.calibreMatrix.toMutableList()
            newList[index] = value
            _uiState.update { it.copy(calibreMatrix = newList) }
        }
    }

    fun onObservacionesChanged(value: String) {
        _uiState.update { it.copy(observaciones = value) }
    }

    fun onToggleTag(tag: String) {
        _uiState.update { state ->
            val newTags = state.selectedTags.toMutableSet()
            if (newTags.contains(tag)) newTags.remove(tag) else newTags.add(tag)
            state.copy(selectedTags = newTags)
        }
    }

    fun onAddPhoto() {
        _uiState.update { it.copy(hasPhoto = true, photoCount = it.photoCount + 1) }
    }

    fun onSaveSample() {
        val state = _uiState.value
        val sampleId = "#M-${(9000..9999).random()}"
        val sampleEntity = MuestraEntity(
            id = sampleId,
            concesion = state.concesion,
            tren = state.tren,
            linea = state.linea,
            tramoLongitudCm = state.tramoLongitudCm,
            conteoManual = state.conteoManual,
            calibreMatrix = state.calibreMatrix.filter { it.isNotBlank() },
            averageCalibre = state.averageCalibre,
            photoCount = state.photoCount,
            observaciones = state.observaciones,
            tags = state.selectedTags.toList(),
            fechaHora = "Hoy, " + java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
            estado = "COMPLETADO",
            sincronizado = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true) }
            try {
                muestraDao.insertSample(sampleEntity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
        }
    }

    fun resetSavedSuccess() {
        _uiState.update { it.copy(isSavedSuccess = false) }
    }
}
