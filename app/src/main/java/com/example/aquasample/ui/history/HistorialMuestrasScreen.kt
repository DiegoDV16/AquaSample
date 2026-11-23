package com.example.aquasample.ui.history

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.FileProvider
import com.example.aquasample.ui.designsystem.AquaCard
import com.example.aquasample.ui.designsystem.AquaDropdownField
import com.example.aquasample.ui.designsystem.AquaScaffold
import com.example.aquasample.ui.designsystem.AquaTopBar
import com.example.aquasample.ui.designsystem.EmptyState
import com.example.aquasample.ui.designsystem.SampleStatus
import com.example.aquasample.ui.designsystem.SearchField
import com.example.aquasample.ui.designsystem.StatusPill
import com.example.aquasample.ui.designsystem.SyncPill
import com.example.aquasample.ui.model.SampleSummary
import com.example.aquasample.ui.theme.AquaSampleTheme
import com.example.aquasample.ui.theme.Spacing
import com.example.aquasample.ui.theme.ThemeManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Historial de muestras con búsqueda, filtros por estado y por período, y
 * agrupación por día. Cada tarjeta abre el detalle de la muestra.
 */
@Composable
fun HistorialMuestrasScreen(
    modifier: Modifier = Modifier,
    canCreateSample: Boolean = true,
    onNavigateToNewSample: () -> Unit = {},
    onOpenSample: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showExport by remember { mutableStateOf(false) }

    HistoryContent(
        state = state,
        canCreateSample = canCreateSample,
        onExportClick = { showExport = true },
        onQueryChange = viewModel::onQueryChange,
        onStatusChange = viewModel::onStatusChange,
        onPeriodChange = viewModel::onPeriodChange,
        onCentroChange = viewModel::onCentroChange,
        onTrenChange = viewModel::onTrenChange,
        onClearFilters = viewModel::clearFilters,
        onNavigateToNewSample = onNavigateToNewSample,
        onOpenSample = onOpenSample,
        onLogout = onLogout,
        modifier = modifier
    )

    if (showExport) {
        JourneyExportDialog(
            count = state.items.size,
            onDismiss = { showExport = false },
            onExport = { format ->
                showExport = false
                viewModel.exportJourney(format) { result ->
                    result.onSuccess { file ->
                        shareJourney(context, file)
                    }.onFailure { error ->
                        Toast.makeText(
                            context,
                            "No se pudo exportar: ${error.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryContent(
    state: HistoryUiState,
    canCreateSample: Boolean,
    onExportClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onStatusChange: (SampleStatus?) -> Unit,
    onPeriodChange: (HistoryPeriod) -> Unit,
    onCentroChange: (Long?) -> Unit,
    onTrenChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onNavigateToNewSample: () -> Unit,
    onOpenSample: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()
    val groups = state.items.groupBy { it.dateLabel.substringBefore(' ') }

    AquaScaffold(
        modifier = modifier,
        topBar = {
            AquaTopBar(
                title = "Muestras",
                subtitle = "${state.totalCount} registros en el dispositivo",
                actions = {
                    IconButton(onClick = onExportClick) {
                        Icon(
                            imageVector = Icons.Rounded.FileDownload,
                            contentDescription = "Exportar jornada"
                        )
                    }
                    IconButton(onClick = { ThemeManager.setDarkMode(!isDarkMode) }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = if (isDarkMode) "Activar modo claro" else "Activar modo oscuro"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (canCreateSample) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToNewSample,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Nueva muestra", fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Spacing.xl,
                end = Spacing.xl,
                top = Spacing.sm,
                bottom = if (canCreateSample) 104.dp else Spacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item(key = "search") {
                SearchField(
                    value = state.filters.query,
                    onValueChange = onQueryChange,
                    placeholder = "Buscar por código, línea u operador"
                )
            }

            item(key = "status-filters") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    FilterChip(
                        selected = state.filters.status == null,
                        onClick = { onStatusChange(null) },
                        label = { Text("Todas ${state.totalCount}") },
                        leadingIcon = if (state.filters.status == null) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        } else {
                            null
                        },
                        colors = filterChipColors()
                    )
                    StatusChip(
                        label = "Pendientes",
                        count = state.statusCounts[SampleStatus.PENDIENTE] ?: 0,
                        selected = state.filters.status == SampleStatus.PENDIENTE,
                        onClick = { onStatusChange(SampleStatus.PENDIENTE) }
                    )
                    StatusChip(
                        label = "Completadas",
                        count = state.statusCounts[SampleStatus.COMPLETADO] ?: 0,
                        selected = state.filters.status == SampleStatus.COMPLETADO,
                        onClick = { onStatusChange(SampleStatus.COMPLETADO) }
                    )
                    StatusChip(
                        label = "Validadas",
                        count = state.statusCounts[SampleStatus.VALIDADO] ?: 0,
                        selected = state.filters.status == SampleStatus.VALIDADO,
                        onClick = { onStatusChange(SampleStatus.VALIDADO) }
                    )
                }
            }

            item(key = "period-filters") {
                FilterRow {
                    HistoryPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = state.filters.period == period,
                            onClick = { onPeriodChange(period) },
                            label = { Text(period.label) },
                            colors = filterChipColors()
                        )
                    }
                }
            }

            item(key = "catalog-filters") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AquaDropdownField(
                            label = "Centro",
                            selected = state.centros
                                .firstOrNull { it.id == state.filters.centroId }
                                ?.nombre
                                ?: "Todos",
                            options = listOf("Todos") + state.centros.map { it.nombre },
                            onSelected = { nombre ->
                                onCentroChange(
                                    state.centros.firstOrNull { it.nombre == nombre }?.id
                                )
                            }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AquaDropdownField(
                            label = "Tren",
                            selected = state.filters.tren ?: "Todos",
                            options = listOf("Todos") + state.trenes,
                            onSelected = { tren ->
                                onTrenChange(tren.takeUnless { it == "Todos" })
                            }
                        )
                    }
                }
            }

            item(key = "comparison") {
                ComparisonCard(state.comparison)
            }

            item(key = "result-count") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.items.size == 1) {
                            "1 resultado"
                        } else {
                            "${state.items.size} resultados"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (state.filters.isActive) {
                        TextButton(onClick = onClearFilters) {
                            Text("Limpiar filtros")
                        }
                    }
                }
            }

            if (state.error != null) {
                item(key = "error") {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (!state.isLoading && state.items.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Rounded.History,
                        title = "Sin resultados",
                        message = if (state.totalCount == 0) {
                            "Todavía no hay muestras registradas en este dispositivo."
                        } else {
                            "Ninguna muestra coincide con la búsqueda o los filtros."
                        },
                        actionLabel = if (state.filters.isActive) "Limpiar filtros" else null,
                        onAction = if (state.filters.isActive) onClearFilters else null
                    )
                }
            }

            groups.forEach { (date, samplesOfDay) ->
                item(key = "header-$date") {
                    DateHeader(date = date, count = samplesOfDay.size)
                }
                items(items = samplesOfDay, key = { it.id }) { summary ->
                    HistoryCard(
                        summary = summary,
                        onClick = { onOpenSample(summary.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun StatusChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text("$label $count") },
        colors = filterChipColors()
    )
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
)

@Composable
private fun DateHeader(date: String, count: Int) {
    Column(modifier = Modifier.padding(top = Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = friendlyDate(date),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$count ${if (count == 1) "muestra" else "muestras"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun HistoryCard(summary: SampleSummary, onClick: () -> Unit) {
    val countLabel = summary.count?.let { "$it individuos" } ?: "Sin conteo"
    val calibreLabel = if (summary.averageCalibre > 0.0) {
        String.format(Locale.getDefault(), "%.1f mm", summary.averageCalibre)
    } else {
        "Sin calibre"
    }
    val time = summary.dateLabel.substringAfter(' ', missingDelimiterValue = "")

    AquaCard(onClick = onClick, contentPadding = PaddingValues(Spacing.lg)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = summary.id,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    StatusPill(status = summary.status)
                    if (!summary.synced) {
                        Spacer(Modifier.width(Spacing.xs))
                        SyncPill(synced = false)
                    }
                }
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    text = summary.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$countLabel · $calibreLabel${if (time.isNotBlank()) " · $time" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = summary.operator,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun friendlyDate(dateKey: String): String {
    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    if (dateKey == today) return "Hoy"

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterdayKey = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(yesterday.time)
    if (dateKey == yesterdayKey) return "Ayer"

    return runCatching {
        val parsed = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateKey)
        val formatted = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "CL")).format(parsed ?: Date())
        formatted.substring(0, 1).uppercase(Locale("es", "CL")) + formatted.substring(1)
    }.getOrDefault(dateKey)
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HistoryPreview() {
    AquaSampleTheme {
        HistoryContent(
            state = HistoryUiState(
                isLoading = false,
                filters = HistoryFilters(query = "", period = HistoryPeriod.WEEK),
                totalCount = 8,
                items = listOf(
                    SampleSummary(
                        id = "#M-0008",
                        location = "Línea L-04 · Tren 02",
                        dateLabel = "22/11/2026 08:30",
                        timestamp = 1_763_810_000_000,
                        status = SampleStatus.PENDIENTE,
                        count = 30,
                        averageCalibre = 36.4,
                        synced = false,
                        operator = "Francisca Rojas"
                    )
                ),
                statusCounts = mapOf(
                    SampleStatus.PENDIENTE to 2,
                    SampleStatus.COMPLETADO to 4,
                    SampleStatus.VALIDADO to 2
                )
            ),
            canCreateSample = true,
            onExportClick = {},
            onQueryChange = {},
            onStatusChange = {},
            onPeriodChange = {},
            onCentroChange = {},
            onTrenChange = {},
            onClearFilters = {},
            onNavigateToNewSample = {},
            onOpenSample = {},
            onLogout = {}
        )
    }
}

@Composable
private fun ComparisonCard(comparison: HistoryComparison) {
    AquaCard {
        Text(
            text = "Comparación de períodos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${comparison.currentLabel} vs ${comparison.previousLabel}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            ComparisonMetric(
                title = "Muestras",
                current = comparison.currentCount.toString(),
                previous = comparison.previousCount.toString(),
                delta = comparison.countDeltaPercent
                    ?.let { String.format(Locale.getDefault(), "%+d%%", it) },
                modifier = Modifier.weight(1f)
            )
            ComparisonMetric(
                title = "Calibre promedio",
                current = comparison.currentAverageCalibre
                    ?.let { String.format(Locale.getDefault(), "%.1f mm", it) }
                    ?: "—",
                previous = comparison.previousAverageCalibre
                    ?.let { String.format(Locale.getDefault(), "%.1f mm", it) }
                    ?: "—",
                delta = comparison.calibreDelta
                    ?.let { String.format(Locale.getDefault(), "%+.1f mm", it) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ComparisonMetric(
    title: String,
    current: String,
    previous: String,
    delta: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = current,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Antes: $previous",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (delta != null) {
            Text(
                text = delta,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (delta.startsWith("-")) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

@Composable
private fun JourneyExportDialog(
    count: Int,
    onDismiss: () -> Unit,
    onExport: (JourneyExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exportar jornada") },
        text = {
            Text(
                "Se exportarán $count muestras con los filtros actuales. " +
                    "Elige PDF o Excel (CSV)."
            )
        },
        confirmButton = {
            TextButton(onClick = { onExport(JourneyExportFormat.PDF) }) {
                Text("PDF")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onExport(JourneyExportFormat.CSV) }) {
                    Text("Excel (CSV)")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

private fun shareJourney(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val mime = if (file.extension.equals("csv", ignoreCase = true)) "text/csv" else "application/pdf"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Reporte AquaSample")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
}
