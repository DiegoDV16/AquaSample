package com.example.aquasample.ui.sample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.data.local.database.AppDatabase
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.data.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SampleViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).muestraDao()

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
        val currentState = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true) }
            delay(500)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val now = Date()
            val fechaHora = dateFormat.format(now)
            val last = dao.getLastSample()
            val nextNum = (last?.id?.substringAfterLast("-")?.toIntOrNull() ?: 0) + 1
            val id = String.format(Locale.getDefault(), "#M-%04d", nextNum)

            val currentSession = SessionManager.currentSession.value
            val operatorId = currentSession?.id ?: "USR-001"
            val operatorAlias = currentSession?.nombreCompleto ?: "Operador"

            val entity = MuestraEntity(
                id = id,
                centroId = 1L,
                operadorId = operatorId,
                concesion = currentState.concesion.ifBlank { "Concesión Dalcahue #104" },
                tren = currentState.tren.ifBlank { "Tren 01" },
                linea = currentState.linea.ifBlank { "Línea L-01" },
                tramoLongitudCm = currentState.tramoLongitudCm.ifBlank { "150" },
                conteoManual = currentState.conteoManual.ifBlank { "30" },
                calibreMatrix = currentState.calibreMatrix.filter { it.isNotBlank() },
                averageCalibre = currentState.averageCalibre,
                photoCount = currentState.photoCount,
                fotos = emptyList(),
                observaciones = currentState.observaciones,
                tags = currentState.selectedTags.toList(),
                fechaHora = fechaHora,
                timestamp = now.time,
                estado = "COMPLETADO",
                sincronizado = true,
                operatorAlias = operatorAlias
            )

            // Inserción directa en la base de datos de Room
            dao.insertSample(entity)

            // Resetea el estado y marca guardado exitoso
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isSavedSuccess = true
                )
            }
        }
    }

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val now = Date()
            val fechaHora = dateFormat.format(now)
            val last = dao.getLastSample()
            val nextNum = (last?.id?.substringAfterLast("-")?.toIntOrNull() ?: 0) + 1
            val id = String.format(Locale.getDefault(), "#M-%04d", nextNum)

            val currentSession = SessionManager.currentSession.value
            val operatorId = currentSession?.id ?: "USR-001"
            val operatorAlias = currentSession?.nombreCompleto ?: "Operador"

            val entity = MuestraEntity(
                id = id,
                centroId = 1L,
                operadorId = operatorId,
                concesion = currentState.concesion.ifBlank { "Concesión Dalcahue #104" },
                tren = currentState.tren.ifBlank { "Tren 01" },
                linea = currentState.linea.ifBlank { "Línea L-01" },
                tram