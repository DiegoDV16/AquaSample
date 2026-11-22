package com.example.aquasample.ui.profile

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.example.aquasample.data.repository.ProfileRepository
import com.example.aquasample.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Estado editable del perfil. */
data class ProfileUiState(
    val alias: String = "",
    val savedAlias: String = "",
    val photo: Bitmap? = null,
    val nombreCompleto: String = "",
    val email: String = "",
    val roleLabel: String = "",
    val savedConfirmation: Boolean = false
) {
    val hasChanges: Boolean
        get() = alias.trim() != savedAlias.trim() && alias.isNotBlank()
}

/**
 * ViewModel del perfil: mantiene el formulario separado del almacenamiento y
 * confirma los cambios contra [ProfileRepository].
 */
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionManager.currentSession.value

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            alias = ProfileRepository.alias,
            savedAlias = ProfileRepository.alias,
            photo = ProfileRepository.photoBitmap,
            nombreCompleto = session?.nombreCompleto.orEmpty(),
            email = session?.email.orEmpty(),
            roleLabel = session?.role?.displayName.orEmpty()
        )
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onAliasChanged(value: String) {
        _uiState.update { it.copy(alias = value, savedConfirmation = false) }
    }

    fun saveAlias() {
        val alias = _uiState.value.alias.trim()
        if (alias.isBlank()) return
        ProfileRepository.updateAlias(alias)
        _uiState.update {
            it.copy(alias = ProfileRepository.alias, savedAlias = ProfileRepository.alias, savedConfirmation = true)
        }
    }

    fun onPhotoSelected(bitmap: Bitmap?) {
        if (bitmap == null) return
        ProfileRepository.updatePhoto(bitmap)
        _uiState.update { it.copy(photo = bitmap) }
    }

    fun removePhoto() {
        ProfileRepository.updatePhoto(null)
        _uiState.update { it.copy(photo = null) }
    }

    fun consumeConfirmation() {
        _uiState.update { it.copy(savedConfirmation = false) }
    }
}
