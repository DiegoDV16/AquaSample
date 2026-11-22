package com.example.aquasample.ui.sample

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquasample.appContainer
import com.example.aquasample.data.local.entity.CentroCultivoEntity
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.data.local.entity.TramoEntity
import com.example.aquasample.data.repository.ProfileRepository
import com.example.aquasample.data.session.SessionManager
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel del registro de muestras.
 *
 * Mantiene el formulario en un único [SampleUiState] inmutable, valida por
 * etapas y persiste a través de [com.example.aquasample.data.repository.MuestraRepository].
 * Las fotografías se escriben en el almacenamiento interno de la app antes de
 * guardar la muestra, de modo que sobreviven al cierre de la aplicación.
 */
class SampleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = application.appContainer.muestraRepository
    private val centroRepository = application.appContainer.centroRepository
    private val tramoRepository = application.appContainer.tramoRepository

    private val _uiState = MutableStateFlow(SampleUiState())
    val uiState: StateFlow<SampleUiState> = _uiState

    /** Centros de cultivo disponibles, leídos desde Room. */
    val centros: StateFlow<List<CentroCultivoEntity>> = centroRepository.centrosActivos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Tramos activos de todos los centros; la UI filtra por el centro elegido. */
    val tramos: StateFlow<List<TramoEntity>> = tramoRepository.tramosActivos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onCentroSelected(centro: CentroCultivoEntity) {
        _uiState.update {
            it.copy(
                centroId = centro.id,
                concesion = centro.nombre,
                tramoId = null,
                tren = "",
                linea = "",
                tramoLongitudCm = ""
            )
        }
    }

    fun onTramoSelected(tramo: TramoEntity) {
        _uiState.update {
            it.copy(
                tramoId = tramo.id,
                tren = tramo.tren,
                linea = tramo.linea,
                tramoLongitudCm = tramo.longitudCm.toString()
            )
        }
    }

    fun onConteoManualChanged(value: String) {
        _uiState.update { it.copy(conteoManual = value) }
    }

    fun onCalibreCellChanged(index: Int, value: String) {
        if (index !in 0 until CALIBRE_CELLS) return
        _uiState.update { state ->
            val newList = state.calibreMatrix.toMutableList()
            newList[index] = value.filter { it.isDigit() || it == '.' || it == ',' }
            state.copy(calibreMatrix = newList)
        }
    }

    fun onObservacionesChanged(value: String) {
        _uiState.update { it.copy(observaciones = value) }
    }

    /** Reemplaza las marcas de conteo asistido (0..1 respecto de la foto). */
    fun onCountMarksChanged(marks: List<CountMark>) {
        _uiState.update { it.copy(countMarks = marks) }
    }

    fun onToggleTag(tag: String) {
        _uiState.update { state ->
            val newTags = state.selectedTags.toMutableSet()
            if (!newTags.add(tag)) newTags.remove(tag)
            state.copy(selectedTags = newTags)
        }
    }

    fun onPhotoCaptured(bitmap: Bitmap): Boolean = addPhoto(bitmap, atStart = true)

    fun onPhotoAdded(bitmap: Bitmap): Boolean = addPhoto(bitmap, atStart = false)

    fun onRemovePhoto(index: Int) {
        _uiState.update { state ->
            if (index in state.photoBitmaps.indices) {
                val newList = state.photoBitmaps.toMutableList().apply { removeAt(index) }
                state.copy(
                    photoBitmaps = newList,
                    countMarks = if (index == 0) emptyList() else state.countMarks
                )
            } else {
                state
            }
        }
    }

    private fun addPhoto(bitmap: Bitmap, atStart: Boolean): Boolean {
        if (_uiState.value.photoBitmaps.size >= MAX_PHOTOS) return false
        _uiState.update { state ->
            val newList = state.photoBitmaps.toMutableList()
            if (atStart) newList.add(0, bitmap) else newList.add(bitmap)
            state.copy(photoBitmaps = newList)
        }
        return true
    }

    /**
     * Valida la etapa actual y devuelve el primer error encontrado, o null si
     * el paso está completo. La misma función alimenta la UI y los tests.
     */
    fun validateStep(step: Int): String? = SampleValidator.validate(_uiState.value, step)

    /**
     * Guarda la muestra. Las fotos se escriben primero en disco y se guardan
     * sus rutas en la entidad; si algo falla se informa por [onError].
     */
    fun onSaveSample(onError: (String) -> Unit = {}): Boolean {
        val validationError = validateStep(STEP_EVIDENCE)
        if (validationError != null) {
            onError(validationError)
            return false
        }

        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = Date()
                val id = nextSampleId()
                val photoPaths = persistPhotos(id, state.photoBitmaps)
                val session = SessionManager.currentSession.value
                val operatorId = session?.id ?: "USR-001"
                val operatorAlias = ProfileRepository.alias
                    .ifBlank { session?.nombreCompleto ?: "Operador" }

                val entity = MuestraEntity(
                    id = id,
                    centroId = state.centroId,
                    tramoId = state.tramoId,
                    operadorId = operatorId,
                    concesion = state.concesion,
                    tren = state.tren,
                    linea = state.linea,
                    tramoLongitudCm = state.tramoLongitudCm,
                    conteoManual = state.conteoManual,
                    calibreMatrix = state.calibreMatrix,
                    averageCalibre = state.averageCalibre,
                    marcasConteo = Gson().toJson(state.countMarks),
                    conteoAsistido = state.countMarks.size,
                    photoCount = photoPaths.size,
                    fotos = photoPaths,
                    observaciones = state.observaciones,
                    tags = state.selectedTags.toList(),
                    fechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(now),
                    timestamp = now.time,
                    estado = "PENDIENTE",
                    sincronizado = false,
                    operatorAlias = operatorAlias
                )

                repository.insertSample(entity)
                com.example.aquasample.data.sync.SyncScheduler.syncNow(getApplication())

                _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                withContext(Dispatchers.Main) {
                    onError("No se pudo guardar la muestra: ${exception.localizedMessage}")
                }
            }
        }
        return true
    }

    fun resetSavedSuccess() {
        _uiState.update { it.copy(isSavedSuccess = false) }
    }

    /** Reinicia el formulario para registrar otra muestra en la misma sesión. */
    fun resetForm() {
        _uiState.value = SampleUiState()
    }

    private suspend fun nextSampleId(): String {
        val last = repository.getLastSample()
        val number = (last?.id?.substringAfterLast("-")?.toIntOrNull() ?: 0) + 1
        return String.format(Locale.getDefault(), "#M-%04d", number)
    }

    private fun persistPhotos(sampleId: String, photos: List<Bitmap>): List<String> {
        if (photos.isEmpty()) return emptyList()
        val directory = File(getApplication<Application>().filesDir, "muestras").apply { mkdirs() }
        val safeId = sampleId.replace("#", "")

        return photos.mapIndexedNotNull { index, bitmap ->
            runCatching {
                val file = File(directory, "${safeId}_${index + 1}.jpg")
                file.outputStream().use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
                }
                file.absolutePath
            }.getOrNull()
        }
    }

    companion object {
        const val STEP_LOCATION = SampleValidator.STEP_LOCATION
        const val STEP_MEASUREMENT = SampleValidator.STEP_MEASUREMENT
        const val STEP_EVIDENCE = SampleValidator.STEP_EVIDENCE
        const val STEP_COUNT = 3
        const val CALIBRE_CELLS = 30
        const val MAX_PHOTOS = 5
    }
}
