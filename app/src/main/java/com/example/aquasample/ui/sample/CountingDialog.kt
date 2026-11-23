package com.example.aquasample.ui.sample

import android.graphics.Bitmap
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aquasample.ui.theme.Spacing
import kotlin.math.hypot
import kotlin.math.min

/**
 * Conteo asistido sobre una fotografía: se tocan los individuos para marcarlos,
 * se toca una marca para quitarla y el contador se actualiza en vivo.
 * Las coordenadas se guardan normalizadas (0..1) respecto de la imagen.
 */
@Composable
fun CountingDialog(
    bitmap: Bitmap,
    initialMarks: List<CountMark>,
    onDismiss: () -> Unit,
    onConfirm: (List<CountMark>) -> Unit
) {
    val marks = remember { mutableStateListOf<CountMark>().apply { addAll(initialMarks) } }
    val density = LocalDensity.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Conteo asistido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Toca cada individuo; toca una marca para quitarla.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    Text(
                        text = marks.size.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar conteo",
                        tint = Color.White
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm)
            ) {
                val imageWidth = bitmap.width.toFloat()
                val imageHeight = bitmap.height.toFloat()
                val boxWidth = constraints.maxWidth.toFloat()
                val boxHeight = constraints.maxHeight.toFloat()
                val scale = min(boxWidth / imageWidth, boxHeight / imageHeight)
                val drawWidth = imageWidth * scale
                val drawHeight = imageHeight * scale
                val left = (boxWidth - drawWidth) / 2f
                val top = (boxHeight - drawHeight) / 2f
                val markerRadius = with(density) { 13.dp.toPx() }
                val removeRadius = with(density) { 30.dp.toPx() }
                val textPaint = remember {
                    Paint().apply {
                        color = android.graphics.Color.WHITE
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        textSize = with(density) { 11.sp.toPx() }
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { tap ->
                                val insideX = tap.x in left..(left + drawWidth)
                                val insideY = tap.y in top..(top + drawHeight)
                                if (!insideX || !insideY) return@detectTapGestures

                                val normalizedX = (tap.x - left) / drawWidth
                                val normalizedY = (tap.y - top) / drawHeight
                                val nearIndex = marks.indexOfFirst { mark ->
                                    hypot(
                                        (mark.x - normalizedX) * drawWidth,
                                        (mark.y - normalizedY) * drawHeight
                                    ) < removeRadius
                                }

                                if (nearIndex >= 0) {
                                    marks.removeAt(nearIndex)
                                } else {
                                    marks.add(CountMark(normalizedX, normalizedY))
                                }
                            }
                        }
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Fotografía para conteo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        marks.forEachIndexed { index, mark ->
                            val center = Offset(
                                x = left + mark.x * drawWidth,
                                y = top + mark.y * drawHeight
                            )
                            drawCircle(
                                color = Color(0xFF0A7EA4),
                                radius = markerRadius,
                                center = center
                            )
                            drawCircle(
                                color = Color.White,
                                radius = markerRadius,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "${index + 1}",
                                center.x,
                                center.y + markerRadius * 0.35f,
                                textPaint
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { if (marks.isNotEmpty()) marks.removeAt(marks.lastIndex) },
                    enabled = marks.isNotEmpty()
                ) {
                    Text("Deshacer", color = Color.White)
                }
                TextButton(
                    onClick = { marks.clear() },
                    enabled = marks.isNotEmpty()
                ) {
                    Text("Limpiar", color = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { onConfirm(marks.toList()) },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Listo (${marks.size})")
                }
            }
        }
    }
}
