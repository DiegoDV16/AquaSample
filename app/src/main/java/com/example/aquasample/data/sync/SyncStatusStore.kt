package com.example.aquasample.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Último resultado del worker, para mostrarlo en Inicio cuando algo falla. */
data class SyncStatus(
    val message: String? = null,
    val isError: Boolean = false,
    val updatedAt: Long = 0
)

object SyncStatusStore {

    private val _state = MutableStateFlow(SyncStatus())
    val state: StateFlow<SyncStatus> = _state.asStateFlow()

    fun success(pending: Int) {
        _state.value = SyncStatus(
            message = if (pending == 0) "Todo sincronizado" else "$pending pendiente(s)",
            isError = false,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun error(message: String) {
        _state.value = SyncStatus(
            message = message,
            isError = true,
            updatedAt = System.currentTimeMillis()
        )
    }
}
