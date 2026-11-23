package com.example.aquasample.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aquasample.ui.theme.Spacing
import com.example.aquasample.ui.theme.statusColors

/** Tono semántico de una métrica. */
enum class MetricTone { Primary, Success, Warning, Neutral }

/**
 * Tarjeta de indicador: icono tonal, etiqueta y valor numérico protagonista.
 */
@Composable
fun KpiCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    caption: String? = null,
    tone: MetricTone = MetricTone.Primary
) {
    val scheme = MaterialTheme.colorScheme
    val semantic = MaterialTheme.statusColors
    val (container, content) = when (tone) {
        MetricTone.Primary -> scheme.primaryContainer to scheme.onPrimaryContainer
        MetricTone.Success -> semantic.successContainer to semantic.onSuccessContainer
        MetricTone.Warning -> semantic.warningContainer to semantic.onWarningContainer
        MetricTone.Neutral -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = scheme.outlineVariant.copy(alpha = 0.75f)
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant
                )
                Surface(shape = CircleShape, color = container, contentColor = content) {
                    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = content
                        )
                    }
                }
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Fila de métricas con separación uniforme. */
@Composable
fun MetricRow(modifier: Modifier = Modifier, content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        content = content
    )
}
