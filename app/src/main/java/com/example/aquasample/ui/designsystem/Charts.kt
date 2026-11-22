package com.example.aquasample.ui.designsystem

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquasample.ui.theme.statusColors
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/** Punto de una serie para los gráficos del panel. */
data class ChartPoint(val label: String, val value: Float)

/**
 * Gráfico de línea con área para mostrar la evolución del calibre promedio.
 * Se dibuja con Canvas para no agregar dependencias de gráficos.
 */
@Composable
fun TrendChart(points: List<ChartPoint>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sin datos suficientes para graficar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = lineColor.copy(alpha = 0.14f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val density = LocalDensity.current
    val textPaint = remember(labelColor) {
        Paint().apply {
            color = labelColor.toArgb()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = with(density) { 9.sp.toPx() }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
    ) {
        textPaint.color = labelColor.toArgb()
        val left = 6.dp.toPx()
        val right = size.width - 6.dp.toPx()
        val top = 12.dp.toPx()
        val bottom = size.height - 26.dp.toPx()
        val width = right - left
        val height = bottom - top

        val values = points.map { it.value }
        val minValue = values.minOrNull() ?: 0f
        val maxValue = values.maxOrNull() ?: 1f
        val range = (maxValue - minValue).coerceAtLeast(1f)

        repeat(3) { index ->
            val y = top + height * index / 2f
            drawLine(
                color = gridColor,
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val offsets = points.mapIndexed { index, point ->
            val x = if (points.size == 1) left + width / 2f else left + width * index / (points.size - 1)
            val ratio = (point.value - minValue) / range
            val y = bottom - height * ratio
            Offset(x, y)
        }

        val areaPath = Path().apply {
            moveTo(offsets.first().x, bottom)
            offsets.forEach { lineTo(it.x, it.y) }
            lineTo(offsets.last().x, bottom)
            close()
        }
        drawPath(areaPath, color = fillColor)

        val linePath = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            offsets.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(linePath, color = lineColor, style = Stroke(width = 2.5.dp.toPx()))

        offsets.forEachIndexed { index, offset ->
            drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = offset)
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = offset
            )
            drawContext.canvas.nativeCanvas.drawText(
                points[index].label,
                offset.x,
                size.height - 8.dp.toPx(),
                textPaint
            )
        }
    }
}

/** Distribución de muestras por estado: barra segmentada y leyenda. */
@Composable
fun StatusDistribution(
    counts: Map<SampleStatus, Int>,
    modifier: Modifier = Modifier
) {
    val entries = counts
        .filterValues { it > 0 }
        .toList()
        .sortedBy { it.first.ordinal }

    if (entries.isEmpty()) {
        Text(
            text = "Sin datos para mostrar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
        ) {
            entries.forEach { (status, count) ->
                Box(
                    modifier = Modifier
                        .weight(count.toFloat())
                        .fillMaxHeight()
                        .background(statusColor(status))
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        entries.forEach { (status, count) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor(status))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = String.format(Locale.getDefault(), "%d", count),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun statusColor(status: SampleStatus): Color {
    val semantic = MaterialTheme.statusColors
    return when (status) {
        SampleStatus.PENDIENTE -> semantic.warning
        SampleStatus.COMPLETADO -> semantic.info
        SampleStatus.VALIDADO -> semantic.success
        SampleStatus.RECHAZADO -> MaterialTheme.colorScheme.error
        SampleStatus.DESCONOCIDO -> MaterialTheme.colorScheme.outline
    }
}
