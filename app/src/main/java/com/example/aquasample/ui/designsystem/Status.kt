package com.example.aquasample.ui.designsystem

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aquasample.ui.theme.Spacing
import com.example.aquasample.ui.theme.statusColors

/**
 * Estados de revisión de una muestra. Los valores del dominio se guardan como
 * texto en Room; esta enumeración centraliza su representación visual.
 */
enum class SampleStatus(val label: String) {
    PENDIENTE("Pendiente"),
    COMPLETADO("Completado"),
    VALIDADO("Validado"),
    RECHAZADO("Rechazado"),
    DESCONOCIDO("Sin estado");

    companion object {
        fun from(raw: String?): SampleStatus = when (raw?.trim()?.uppercase()) {
            "PENDIENTE" -> PENDIENTE
            "COMPLETADO" -> COMPLETADO
            "VALIDADO" -> VALIDADO
            "RECHAZADO" -> RECHAZADO
            else -> DESCONOCIDO
        }
    }
}

/** Etiqueta compacta de estado con punto de color. */
@Composable
fun StatusPill(status: SampleStatus, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val semantic = MaterialTheme.statusColors
    val (container, content) = when (status) {
        SampleStatus.PENDIENTE -> semantic.warningContainer to semantic.onWarningContainer
        SampleStatus.COMPLETADO -> semantic.infoContainer to semantic.onInfoContainer
        SampleStatus.VALIDADO -> semantic.successContainer to semantic.onSuccessContainer
        SampleStatus.RECHAZADO -> scheme.errorContainer to scheme.onErrorContainer
        SampleStatus.DESCONOCIDO -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Dot(color = content, size = 6.dp)
            Spacer(Modifier.width(6.dp))
            Text(text = status.label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Indicador de sincronización (offline-first). */
@Composable
fun SyncPill(synced: Boolean, modifier: Modifier = Modifier) {
    val semantic = MaterialTheme.statusColors
    val scheme = MaterialTheme.colorScheme
    val container = if (synced) semantic.successContainer else semantic.warningContainer
    val content = if (synced) semantic.onSuccessContainer else semantic.onWarningContainer

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (synced) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = content
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (synced) "Sincronizado" else "Pendiente",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
