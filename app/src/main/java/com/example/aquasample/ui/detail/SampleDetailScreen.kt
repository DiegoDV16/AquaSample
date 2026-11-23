package com.example.aquasample.ui.detail

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.ui.designsystem.AquaCard
import com.example.aquasample.ui.designsystem.AquaScaffold
import com.example.aquasample.ui.designsystem.AquaTopBar
import com.example.aquasample.ui.designsystem.EmptyState
import com.example.aquasample.ui.designsystem.PhotoViewerDialog
import com.example.aquasample.ui.designsystem.PrimaryButton
import com.example.aquasample.ui.designsystem.SampleStatus
import com.example.aquasample.ui.designsystem.SectionHeader
import com.example.aquasample.ui.designsystem.StatusPill
import com.example.aquasample.ui.designsystem.SyncPill
import com.example.aquasample.ui.theme.AquaSampleTheme
import com.example.aquasample.ui.theme.Spacing
import com.example.aquasample.ui.theme.statusColors
import java.io.File
import java.util.Locale

/**
 * Detalle completo de una muestra: datos de terreno, mediciones, evidencia y
 * acción de validación para el supervisor.
 */
@Composable
fun SampleDetailScreen(
    sampleId: String,
    canValidate: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SampleDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var viewerPhoto by remember { mutableStateOf<String?>(null) }
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(sampleId) { viewModel.load(sampleId) }

    val exportReport: () -> Unit = {
        viewModel.exportPdf { result ->
            result
                .onSuccess { file -> sharePdf(context, file) }
                .onFailure {
                    Toast.makeText(
                        context,
                        "No se pudo generar el informe",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    AquaScaffold(
        modifier = modifier,
        topBar = {
            AquaTopBar(
                title = "Detalle de muestra",
                subtitle = sampleId,
                onBack = onBack,
                actions = {
                    if (state.sample != null) {
                        IconButton(onClick = { showEdit = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Editar muestra"
                            )
                        }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Eliminar muestra"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(innerPadding))

            state.error != null || state.sample == null -> EmptyState(
                icon = Icons.Rounded.ErrorOutline,
                title = "No se pudo abrir la muestra",
                message = state.error ?: "La muestra no está disponible.",
                modifier = Modifier.padding(innerPadding).padding(Spacing.xl),
                actionLabel = "Reintentar",
                onAction = { viewModel.load(sampleId, force = true) }
            )

            else -> DetailContent(
                state = state,
                canValidate = canValidate,
                onOpenPhoto = { viewerPhoto = it },
                onExportPdf = exportReport,
                onValidate = {
                    viewModel.validateSample { error ->
                        Toast.makeText(
                            context,
                            error ?: "Muestra validada correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    viewerPhoto?.let { path ->
        PhotoViewerDialog(model = File(path), onDismiss = { viewerPhoto = null })
    }

    if (showEdit) {
        state.sample?.let { sample ->
            EditSampleDialog(
                sample = sample,
                onDismiss = { showEdit = false },
                onSave = { conteo, calibres, observaciones ->
                    viewModel.updateSample(conteo, calibres, observaciones) { error ->
                        Toast.makeText(
                            context,
                            error ?: "Muestra actualizada. Sincronizando…",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (error == null) showEdit = false
                    }
                }
            )
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Eliminar muestra") },
            text = {
                Text(
                    "La muestra se eliminará de este teléfono y del backend. " +
                        "La acción se sincronizará automáticamente."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDelete = false
                        viewModel.deleteSample { error ->
                            Toast.makeText(
                                context,
                                error ?: "Muestra eliminada",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (error == null) onBack()
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    state: SampleDetailUiState,
    canValidate: Boolean,
    onOpenPhoto: (String) -> Unit,
    onExportPdf: () -> Unit,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sample = state.sample ?: return
    val status = SampleStatus.from(sample.estado)
    val averageLabel = if (sample.averageCalibre > 0.0) {
        String.format(Locale.getDefault(), "%.1f mm", sample.averageCalibre)
    } else {
        "Sin datos"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        AquaCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(status = status)
                SyncPill(synced = sample.sincronizado)
            }
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = sample.id,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = sample.fechaHora.ifBlank { "Sin fecha registrada" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))
            InfoLine(
                icon = Icons.Rounded.Person,
                text = state.operadorNombre ?: sample.operatorAlias.ifBlank { "Operador" }
            )
            Spacer(Modifier.height(Spacing.xs))
            InfoLine(
                icon = Icons.Rounded.LocationOn,
                text = state.centroNombre ?: sample.concesion.ifBlank { "Sin centro" }
            )
        }

        AquaCard(contentPadding = PaddingValues(Spacing.lg)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricColumn(
                    label = "Conteo",
                    value = sample.conteoManual.ifBlank { "—" },
                    icon = Icons.Rounded.Science,
                    modifier = Modifier.weight(1f)
                )
                MetricDivider()
                MetricColumn(
                    label = "Calibre",
                    value = averageLabel,
                    icon = Icons.Rounded.WaterDrop,
                    modifier = Modifier.weight(1f)
                )
                MetricDivider()
                MetricColumn(
                    label = "Fotos",
                    value = state.photos.size.toString(),
                    icon = Icons.Rounded.PhotoLibrary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.photos.isNotEmpty()) {
            SectionHeader(
                title = "Evidencia fotográfica",
                subtitle = "${state.photos.size} de ${sample.photoCount} fotografías disponibles"
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                itemsIndexed(state.photos) { index, path ->
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onOpenPhoto(path) }
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Fotografía ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        SectionHeader(title = "Ubicación y trazabilidad")
        AquaCard {
            DetailRow("Centro", state.centroNombre ?: sample.concesion.ifBlank { "—" })
            DetailRow("Código centro", state.centroCodigo ?: "—")
            DetailRow("Tren", sample.tren.ifBlank { "—" })
            DetailRow("Línea", sample.linea.ifBlank { "—" })
            DetailRow("Tramo", if (sample.tramoLongitudCm.isBlank()) "—" else "${sample.tramoLongitudCm} cm")
            DetailRow("Operador", state.operadorEmail ?: "—")
            DetailRow(
                "Conteo asistido",
                if (sample.conteoAsistido > 0) {
                    "${sample.conteoAsistido} individuos marcados"
                } else {
                    "Sin marcas"
                }
            )
            DetailRow(
                "Fecha de registro",
                sample.fechaHora.ifBlank { "—" }
            )
            DetailRow(
                "Condiciones marinas (Open-Meteo)",
                listOfNotNull(
                    sample.marineWaveHeight?.let { "Oleaje: ${it} m" },
                    sample.marineSeaSurfaceTemperature?.let { "Temp. del mar: ${it} °C" },
                    sample.marineWindSpeed?.let { "Viento: ${it} m/s" }
                ).joinToString(" · ").ifBlank { "Sin datos" }
            )
        }

        SectionHeader(
            title = "Matriz de calibre",
            subtitle = "Promedio: $averageLabel"
        )
        CalibreMatrixCard(values = sample.calibreMatrix)

        if (sample.observaciones.isNotBlank() || sample.tags.isNotEmpty()) {
            SectionHeader(title = "Observaciones")
            AquaCard {
                if (sample.observaciones.isNotBlank()) {
                    Text(
                        text = sample.observaciones,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "Sin observaciones registradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sample.tags.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.md))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        sample.tags.forEach { tag ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(
                                        horizontal = Spacing.md,
                                        vertical = Spacing.xs
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        SectionHeader(title = "Reporte")
        AquaCard {
            Text(
                text = "Informe de la muestra",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = "PDF con identificación, ubicación, conteo, calibre y la primera fotografía.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.lg))
            PrimaryButton(
                text = "Exportar informe PDF",
                icon = Icons.Rounded.PictureAsPdf,
                loading = state.isExporting,
                onClick = onExportPdf
            )
        }

        when {
            status == SampleStatus.VALIDADO -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.statusColors.successContainer,
                    contentColor = MaterialTheme.statusColors.onSuccessContainer
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(Spacing.xl)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = "Muestra validada por el supervisor.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            canValidate -> AquaCard {
                Text(
                    text = "Revisión del supervisor",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "Al validar, la muestra queda aprobada para los reportes de la jornada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.lg))
                PrimaryButton(
                    text = "Validar muestra",
                    icon = Icons.Rounded.CheckCircle,
                    loading = state.isUpdating,
                    onClick = onValidate
                )
            }
        }

        Spacer(Modifier.height(Spacing.sm))
    }
}

@Composable
private fun InfoLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Spacing.lg),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Spacing.xl),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(56.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.4f)
        )
    }
}

@Composable
private fun CalibreMatrixCard(values: List<String>) {
    AquaCard {
        for (row in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                for (column in 0 until 5) {
                    val index = row * 5 + column
                    val value = values.getOrNull(index).orEmpty()
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = MaterialTheme.shapes.small,
                        color = if (value.isBlank()) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = value.ifBlank { "·" },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.Bold,
                                color = if (value.isBlank()) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.xs))
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = "Cargando muestra…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir informe"))
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun DetailContentPreview() {
    AquaSampleTheme {
        DetailContent(
            state = SampleDetailUiState(
                isLoading = false,
                sample = MuestraEntity(
                    id = "#M-0008",
                    centroId = 1L,
                    operadorId = "USR-001",
                    concesion = "Concesión Dalcahue #104",
                    tren = "Tren 02",
                    linea = "Línea L-04",
                    tramoLongitudCm = "150",
                    conteoManual = "30",
                    calibreMatrix = List(30) { index -> if (index < 18) "${34 + index % 6}" else "" },
                    averageCalibre = 36.4,
                    photoCount = 2,
                    observaciones = "Valvas sanas, sin presencia de fouling en el tramo evaluado.",
                    tags = listOf("ValvasSanas", "SinFouling"),
                    fechaHora = "22/11/2026 08:30",
                    timestamp = 1_763_810_000_000,
                    estado = "PENDIENTE",
                    sincronizado = false,
                    operatorAlias = "Francisca Rojas"
                ),
                centroNombre = "Concesión Dalcahue #104",
                centroCodigo = "CONC-104",
                operadorNombre = "Francisca Rojas Soto",
                operadorEmail = "operador@aquasample.cl"
            ),
            canValidate = true,
            onOpenPhoto = {},
            onExportPdf = {},
            onValidate = {}
        )
    }
}
