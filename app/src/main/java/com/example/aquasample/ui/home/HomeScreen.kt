package com.example.aquasample.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquasample.data.session.UserRole
import com.example.aquasample.data.session.UserSession
import com.example.aquasample.ui.designsystem.AquaCard
import com.example.aquasample.ui.designsystem.AquaScaffold
import com.example.aquasample.ui.designsystem.AquaTopBar
import com.example.aquasample.ui.designsystem.EmptyState
import com.example.aquasample.ui.designsystem.InfoBanner
import com.example.aquasample.ui.designsystem.KpiCard
import com.example.aquasample.ui.designsystem.MetricRow
import com.example.aquasample.ui.designsystem.MetricTone
import com.example.aquasample.ui.designsystem.SampleStatus
import com.example.aquasample.ui.designsystem.ScreenContentPadding
import com.example.aquasample.ui.designsystem.SectionHeader
import com.example.aquasample.ui.designsystem.StatusPill
import com.example.aquasample.ui.model.SampleSummary
import com.example.aquasample.ui.theme.AquaSampleTheme
import com.example.aquasample.ui.theme.Dimens
import com.example.aquasample.ui.theme.Ocean600
import com.example.aquasample.ui.theme.Ocean800
import com.example.aquasample.ui.theme.Spacing
import com.example.aquasample.ui.theme.ThemeManager
import com.example.aquasample.ui.theme.statusColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    userSession: UserSession,
    onNewSample: () -> Unit,
    onSeeAllSamples: () -> Unit,
    onOpenSample: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        userSession = userSession,
        onNewSample = onNewSample,
        onSeeAllSamples = onSeeAllSamples,
        onOpenSample = onOpenSample,
        onSyncNow = viewModel::syncNow,
        onLogout = onLogout,
        modifier = modifier
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    userSession: UserSession,
    onNewSample: () -> Unit,
    onSeeAllSamples: () -> Unit,
    onOpenSample: (String) -> Unit,
    onSyncNow: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    AquaScaffold(
        modifier = modifier,
        topBar = {
            AquaTopBar(
                title = "AquaSample",
                subtitle = "Panel de terreno · ${userSession.role.displayName}",
                actions = {
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = ScreenContentPadding,
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            item(key = "greeting") {
                GreetingCard(userSession = userSession)
            }

            item(key = "summary-header") {
                SectionHeader(
                    title = "Resumen",
                    subtitle = "Datos guardados en el dispositivo"
                )
            }

            item(key = "metrics-primary") {
                MetricRow {
                    KpiCard(
                        label = "Muestras",
                        value = state.totalSamples.toString(),
                        icon = Icons.Rounded.Science,
                        caption = "registros totales",
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        label = "Calibre prom.",
                        value = state.averageCalibre?.let {
                            String.format(Locale.getDefault(), "%.1f mm", it)
                        } ?: "—",
                        icon = Icons.Rounded.WaterDrop,
                        caption = "promedio general",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item(key = "metrics-secondary") {
                MetricRow {
                    KpiCard(
                        label = "Esta semana",
                        value = state.samplesThisWeek.toString(),
                        icon = Icons.Rounded.CalendarMonth,
                        caption = "nuevas muestras",
                        tone = MetricTone.Success,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        label = "Por validar",
                        value = state.pendingValidation.toString(),
                        icon = Icons.Rounded.PendingActions,
                        caption = "en revisión",
                        tone = if (state.pendingValidation > 0) MetricTone.Warning else MetricTone.Neutral,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (userSession.role == UserRole.OPERADOR) {
                item(key = "new-sample-cta") {
                    NewSampleCard(onClick = onNewSample)
                }
            }

            if (state.pendingSync > 0) {
                item(key = "pending-sync") {
                    PendingSyncCard(count = state.pendingSync, onSyncNow = onSyncNow)
                }
            }

            item(key = "recent-header") {
                SectionHeader(
                    title = "Actividad reciente",
                    subtitle = if (state.recent.isEmpty()) null else "Últimos registros del dispositivo",
                    actionLabel = "Ver todo",
                    onAction = onSeeAllSamples
                )
            }

            if (state.error != null) {
                item(key = "error") {
                    InfoBanner(
                        text = state.error,
                        icon = Icons.Rounded.PendingActions,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (state.recent.isEmpty() && !state.isLoading) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Rounded.History,
                        title = "Aún no hay muestras",
                        message = if (userSession.role == UserRole.OPERADOR) {
                            "Registra la primera muestra de la jornada para comenzar la trazabilidad."
                        } else {
                            "Cuando el equipo registre muestras aparecerán aquí."
                        },
                        actionLabel = if (userSession.role == UserRole.OPERADOR) "Registrar muestra" else null,
                        onAction = if (userSession.role == UserRole.OPERADOR) onNewSample else null
                    )
                }
            } else {
                items(items = state.recent, key = { it.id }) { summary ->
                    RecentSampleCard(
                        summary = summary,
                        onClick = { onOpenSample(summary.id) }
                    )
                }
            }

            item(key = "offline-note") {
                InfoBanner(
                    text = "Tus registros viven en el dispositivo: puedes consultarlos sin conexión.",
                    icon = Icons.Rounded.CloudDone,
                    containerColor = MaterialTheme.statusColors.successContainer,
                    contentColor = MaterialTheme.statusColors.onSuccessContainer
                )
            }
        }
    }
}

@Composable
private fun GreetingCard(
    userSession: UserSession,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.extraLarge
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(Ocean800, Ocean600)))
            .padding(Spacing.xl)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = size.minDimension * 0.55f,
                center = Offset(size.width * 1.02f, size.height * 0.08f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = size.minDimension * 0.4f,
                center = Offset(size.width * 0.08f, size.height * 1.06f)
            )
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(Dimens.avatarMedium)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(userSession.nombreCompleto),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hola, ${firstNameOf(userSession.nombreCompleto)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = userSession.role.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            }

            Spacer(Modifier.height(Spacing.xl))

            Text(
                text = formattedToday(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "Jornada de muestreo en terreno",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun PendingSyncCard(
    count: Int,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = MaterialTheme.statusColors
    AquaCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = semantic.warning
            )
            Spacer(Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (count == 1) {
                        "1 muestra sin sincronizar"
                    } else {
                        "$count muestras sin sincronizar"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Se enviarán automáticamente al recuperar conexión.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onSyncNow) {
                Text("Sincronizar")
            }
        }
    }
}

@Composable
private fun NewSampleCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.large
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.xl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Registrar nueva muestra",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    text = "Ubicación, conteo y evidencia fotográfica",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Box(modifier = Modifier.size(Dimens.avatarMedium), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun RecentSampleCard(
    summary: SampleSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val countLabel = summary.count?.let { "$it individuos" } ?: "Sin conteo"
    val calibreLabel = if (summary.averageCalibre > 0.0) {
        String.format(Locale.getDefault(), "%.1f mm", summary.averageCalibre)
    } else {
        "Sin calibre"
    }

    AquaCard(modifier = modifier, onClick = onClick, contentPadding = PaddingValues(Spacing.lg)) {
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
                    text = "$countLabel · $calibreLabel · ${summary.dateLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

private fun initialsOf(fullName: String): String {
    val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
    val initials = parts.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }
    return if (initials.isEmpty()) "AQ" else initials.joinToString("")
}

private fun firstNameOf(fullName: String): String =
    fullName.trim().split(" ").firstOrNull()?.takeIf { it.isNotBlank() } ?: "operador"

private fun formattedToday(): String {
    val locale = Locale("es", "CL")
    val date = SimpleDateFormat("EEEE d 'de' MMMM", locale).format(Date())
    if (date.isEmpty()) return date
    return date.substring(0, 1).uppercase(locale) + date.substring(1)
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeContentPreview() {
    AquaSampleTheme {
        HomeContent(
            state = HomeUiState(
                isLoading = false,
                totalSamples = 24,
                averageCalibre = 38.4,
                samplesThisWeek = 6,
                pendingValidation = 3,
                validatedSamples = 18,
                recent = listOf(
                    SampleSummary(
                        id = "#M-0024",
                        location = "Línea L-02 · Tren 03",
                        dateLabel = "22/11/2026 09:40",
                        timestamp = 1_763_810_000_000,
                        status = SampleStatus.COMPLETADO,
                        count = 32,
                        averageCalibre = 39.2,
                        synced = true,
                        operator = "Francisca Rojas"
                    ),
                    SampleSummary(
                        id = "#M-0023",
                        location = "Línea L-01 · Tren 01",
                        dateLabel = "21/11/2026 16:05",
                        timestamp = 1_763_720_000_000,
                        status = SampleStatus.PENDIENTE,
                        count = 28,
                        averageCalibre = 37.8,
                        synced = false,
                        operator = "Francisca Rojas"
                    )
                )
            ),
            userSession = UserSession(
                id = "USR-001",
                username = "operador",
                nombreCompleto = "Francisca Rojas",
                email = "operador@aquasample.cl",
                role = UserRole.OPERADOR
            ),
            onNewSample = {},
            onSeeAllSamples = {},
            onOpenSample = {},
            onSyncNow = {},
            onLogout = {}
        )
    }
}
