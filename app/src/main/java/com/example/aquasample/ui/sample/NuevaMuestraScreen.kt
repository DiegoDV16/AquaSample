package com.example.aquasample.ui.sample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquasample.data.local.entity.CentroCultivoEntity
import com.example.aquasample.data.local.entity.TramoEntity
import com.example.aquasample.ui.camera.CameraCaptureDialog
import com.example.aquasample.ui.designsystem.AquaCard
import com.example.aquasample.ui.designsystem.AquaDropdownField
import com.example.aquasample.ui.designsystem.AquaScaffold
import com.example.aquasample.ui.designsystem.AquaTextField
import com.example.aquasample.ui.designsystem.AquaTopBar
import com.example.aquasample.ui.designsystem.InfoBanner
import com.example.aquasample.ui.designsystem.PhotoViewerDialog
import com.example.aquasample.ui.designsystem.PrimaryButton
import com.example.aquasample.ui.designsystem.SecondaryButton
import com.example.aquasample.ui.designsystem.SectionHeader
import com.example.aquasample.ui.designsystem.StepHeader
import com.example.aquasample.ui.theme.AquaSampleTheme
import com.example.aquasample.ui.theme.Dimens
import com.example.aquasample.ui.theme.Spacing
import java.util.Locale
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val TAGS = listOf("ValvasSanas", "SinFouling", "SustratoAdecuado", "CrecimientoOptimo")

/**
 * Registro de muestra como asistente de tres pasos: ubicación, mediciones y
 * evidencia. Cada paso valida antes de avanzar y el estado se conserva al
 * rotar la pantalla.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NuevaMuestraScreen(
    onSampleSaved: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SampleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val centros by viewModel.centros.collectAsStateWithLifecycle()
    val tramos by viewModel.tramos.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by rememberSaveable { mutableIntStateOf(SampleViewModel.STEP_LOCATION) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var viewerPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var showCounting by remember { mutableStateOf(false) }
    val imeVisible = WindowInsets.isImeVisible

    // Al corregir cualquier dato, el mensaje de error deja de ser válido.
    LaunchedEffect(
        uiState.centroId,
        uiState.tramoId,
        uiState.concesion,
        uiState.tren,
        uiState.linea,
        uiState.tramoLongitudCm,
        uiState.conteoManual,
        uiState.photoBitmaps.size
    ) {
        errorMessage = null
    }

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            viewModel.resetSavedSuccess()
            viewModel.resetForm()
            Toast.makeText(context, "Muestra registrada correctamente", Toast.LENGTH_SHORT).show()
            onSampleSaved()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMarineConditions(latitude = -35.9, longitude = -72.9)
    }

    BackHandler(enabled = step > SampleViewModel.STEP_LOCATION && !imeVisible) { step-- }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    }.getOrNull()
                }
                if (bitmap == null) {
                    errorMessage = "No se pudo leer la imagen seleccionada."
                } else if (!viewModel.onPhotoAdded(bitmap)) {
                    errorMessage = "Puedes adjuntar hasta ${SampleViewModel.MAX_PHOTOS} fotografías."
                }
            }
        }
    }

    val advance: () -> Unit = {
        val validation = viewModel.validateStep(step)
        if (validation != null) {
            errorMessage = validation
        } else {
            errorMessage = null
            if (step < SampleViewModel.STEP_EVIDENCE) {
                step++
            } else {
                viewModel.onSaveSample { message -> errorMessage = message }
            }
        }
    }

    AquaScaffold(
        modifier = modifier.imePadding(),
        topBar = {
            AquaTopBar(
                title = "Nueva muestra",
                subtitle = "Registro de terreno",
                onBack = { if (step > SampleViewModel.STEP_LOCATION) step-- else onClose() }
            )
        },
        bottomBar = {
            WizardBottomBar(
                step = step,
                isSaving = uiState.isSaving,
                errorMessage = errorMessage,
                onCancel = onClose,
                onBack = { step-- },
                onAdvance = advance
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            StepHeader(
                currentStep = step + 1,
                totalSteps = SampleViewModel.STEP_COUNT,
                title = stepTitle(step),
                subtitle = stepSubtitle(step)
            )

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val forward = targetState > initialState
                    val offset = if (forward) 1 else -1
                    (
                        fadeIn(tween(220)) +
                            slideInHorizontally(tween(260)) { width -> offset * width / 10 }
                        ) togetherWith (
                        fadeOut(tween(140)) +
                            slideOutHorizontally(tween(220)) { width -> -offset * width / 10 }
                        )
                },
                label = "wizard-step"
            ) { currentStep ->
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                    when (currentStep) {
                        SampleViewModel.STEP_LOCATION -> LocationStep(
                            state = uiState,
                            centros = centros,
                            tramos = tramos,
                            onCentroSelected = viewModel::onCentroSelected,
                            onTramoSelected = viewModel::onTramoSelected
                        )

                        SampleViewModel.STEP_MEASUREMENT -> MeasurementStep(
                            state = uiState,
                            onConteoChanged = viewModel::onConteoManualChanged,
                            onCalibreChanged = viewModel::onCalibreCellChanged
                        )

                        else -> EvidenceStep(
                            state = uiState,
                            onTakePhoto = { showCamera = true },
                            onPickPhoto = { galleryLauncher.launch("image/*") },
                            onRemovePhoto = viewModel::onRemovePhoto,
                            onOpenPhoto = { viewerPhoto = it },
                            onOpenCounting = { showCounting = true },
                            onClearCounting = { viewModel.onCountMarksChanged(emptyList()) },
                            onObservacionesChanged = viewModel::onObservacionesChanged,
                            onToggleTag = viewModel::onToggleTag
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
        }
    }

    viewerPhoto?.let { bitmap ->
        PhotoViewerDialog(model = bitmap, onDismiss = { viewerPhoto = null })
    }

    if (showCamera) {
        CameraCaptureDialog(
            onDismiss = { showCamera = false },
            onCaptured = { file ->
                showCamera = false
                val bitmap = decodeSampledBitmap(file, maxDimension = 1600)
                if (bitmap == null) {
                    errorMessage = "No se pudo procesar la fotografía."
                } else if (!viewModel.onPhotoCaptured(bitmap)) {
                    errorMessage = "Puedes adjuntar hasta ${SampleViewModel.MAX_PHOTOS} fotografías."
                }
                file.delete()
            }
        )
    }

    if (showCounting && uiState.photoBitmaps.isNotEmpty()) {
        CountingDialog(
            bitmap = uiState.photoBitmaps.first(),
            initialMarks = uiState.countMarks,
            onDismiss = { showCounting = false },
            onConfirm = { marks ->
                viewModel.onCountMarksChanged(marks)
                showCounting = false
            }
        )
    }
}

/** Decodifica la foto de CameraX con muestreo para no cargar el archivo completo en memoria. */
private fun decodeSampledBitmap(file: File, maxDimension: Int): Bitmap? {
    if (!file.exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxDimension || bounds.outHeight / sampleSize > maxDimension) {
        sampleSize *= 2
    }
    return BitmapFactory.decodeFile(
        file.absolutePath,
        BitmapFactory.Options().apply { inSampleSize = sampleSize }
    )
}

@Composable
private fun LocationStep(
    state: SampleUiState,
    centros: List<CentroCultivoEntity>,
    tramos: List<TramoEntity>,
    onCentroSelected: (CentroCultivoEntity) -> Unit,
    onTramoSelected: (TramoEntity) -> Unit
) {
    val tramosDelCentro = tramos.filter { it.centroId == state.centroId }
    val tramoSeleccionado = tramosDelCentro.firstOrNull { it.id == state.tramoId }

    AquaCard {
        AquaDropdownField(
            label = "Concesión / Centro",
            selected = state.concesion,
            options = centros.map { it.nombre },
            onSelected = { nombre ->
                centros.firstOrNull { it.nombre == nombre }?.let(onCentroSelected)
            },
            placeholder = "Seleccionar centro"
        )
        Spacer(Modifier.height(Spacing.md))
        AquaDropdownField(
            label = "Tramo",
            selected = tramoSeleccionado?.let { "${it.tren} · ${it.linea}" }.orEmpty(),
            options = tramosDelCentro.map { "${it.tren} · ${it.linea}" },
            onSelected = { etiqueta ->
                tramosDelCentro
                    .firstOrNull { "${it.tren} · ${it.linea}" == etiqueta }
                    ?.let(onTramoSelected)
            },
            placeholder = when {
                state.centroId == null -> "Primero elige un centro"
                tramosDelCentro.isEmpty() -> "El centro no tiene tramos activos"
                else -> "Seleccionar tramo"
            },
            enabled = state.centroId != null && tramosDelCentro.isNotEmpty()
        )

        if (tramoSeleccionado != null) {
            Spacer(Modifier.height(Spacing.md))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GridOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Column {
                        Text(
                            text = "Largo del tramo: ${tramoSeleccionado.longitudCm} cm",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tramoSeleccionado.tren} · ${tramoSeleccionado.linea}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    InfoBanner(
        text = "La muestra queda asociada a tu usuario y a la fecha de la jornada.",
        icon = Icons.Rounded.Info
    )

    MarineConditionsCard(state = state)
}

/** Tarjeta con las condiciones marinas actuales (oleaje, temperatura y viento). */
@Composable
private fun MarineConditionsCard(state: SampleUiState) {
    AquaCard {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = "Condiciones Marinas (Open-Meteo)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (state.isLoadingMarine) {
                Text(
                    text = "Consultando condiciones marinas…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (state.marineCurrent != null) {
                Text("Oleaje (m): ${state.marineCurrent.waveHeight ?: 0.0}")
                Text("Temperatura del mar: ${state.marineCurrent.seaSurfaceTemperature ?: 0.0} °C")
                Text("Viento: ${state.marineCurrent.windSpeed10m ?: 0.0} m/s")
            }

            state.marineError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeasurementStep(
    state: SampleUiState,
    onConteoChanged: (String) -> Unit,
    onCalibreChanged: (Int, String) -> Unit
) {
    AquaCard {
        AquaTextField(
            value = state.conteoManual,
            onValueChange = onConteoChanged,
            label = "Conteo manual (individuos)",
            placeholder = "30",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText = "Total de individuos contados en el tramo"
        )
    }

    CalibreSummary(state = state)

    AquaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Matriz de calibre (mm)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "6 × 5",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "Puedes completar solo las mediciones que alcances a tomar.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.md))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            for (row in 0 until 6) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    for (column in 0 until 5) {
                        val index = row * 5 + column
                        CalibreCell(
                            index = index,
                            value = state.calibreMatrix[index],
                            onValueChange = { onCalibreChanged(index, it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    InfoBanner(
        text = "El promedio se calcula automáticamente con las celdas que completes.",
        icon = Icons.Rounded.Info
    )
}

@Composable
private fun CalibreCell(
    index: Int,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(50.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
        placeholder = {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = MaterialTheme.shapes.small,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun CalibreSummary(state: SampleUiState) {
    val filled = state.calibreMatrix.count { it.toDoubleOrNull() != null }
    val average = state.averageCalibre

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Calibre promedio",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "$filled de 30 mediciones",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }
                Text(
                    text = if (filled == 0) "Sin datos" else String.format(Locale.getDefault(), "%.1f mm", average),
                    style = if (filled == 0) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(Spacing.md))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            ) {
                if (filled > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(filled / 30f)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EvidenceStep(
    state: SampleUiState,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onOpenPhoto: (Bitmap) -> Unit,
    onOpenCounting: () -> Unit,
    onClearCounting: () -> Unit,
    onObservacionesChanged: (String) -> Unit,
    onToggleTag: (String) -> Unit
) {
    AquaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fotografías",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = CircleShape,
                color = if (state.photoCount > 0) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Text(
                    text = "${state.photoCount}/${SampleViewModel.MAX_PHOTOS}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        val rows = state.photoBitmaps.chunked(3)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            rows.forEachIndexed { rowIndex, photos ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    photos.forEachIndexed { columnIndex, bitmap ->
                        val index = rowIndex * 3 + columnIndex
                        PhotoTile(
                            bitmap = bitmap,
                            onOpen = { onOpenPhoto(bitmap) },
                            onRemove = { onRemovePhoto(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(3 - photos.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            if (state.photoCount < SampleViewModel.MAX_PHOTOS) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AddPhotoTile(onClick = onTakePhoto, modifier = Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(Spacing.md))

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SecondaryButton(
                text = "Galería",
                icon = Icons.Rounded.PhotoLibrary,
                onClick = onPickPhoto,
                modifier = Modifier.weight(1f)
            )
            SecondaryButton(
                text = "Cámara",
                icon = Icons.Rounded.CameraAlt,
                onClick = onTakePhoto,
                modifier = Modifier.weight(1f)
            )
        }
    }

    AquaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.TouchApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Conteo asistido",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (state.conteoAsistido == 0) {
                        "Marca los individuos sobre la primera foto para contarlos."
                    } else {
                        "${state.conteoAsistido} individuos marcados en la foto."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.conteoAsistido > 0) {
                TextButton(onClick = onClearCounting) {
                    Text("Limpiar")
                }
            }
        }
        Spacer(Modifier.height(Spacing.md))
        SecondaryButton(
            text = if (state.conteoAsistido == 0) "Contar sobre la foto" else "Revisar marcas",
            icon = Icons.Rounded.TouchApp,
            onClick = onOpenCounting,
            enabled = state.photoCount > 0
        )
    }

    AquaCard {
        Text(
            text = "Observaciones de terreno",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(Spacing.sm))
        AquaTextField(
            value = state.observaciones,
            onValueChange = onObservacionesChanged,
            label = "Notas",
            placeholder = "Estado de la línea, presencia de fouling, incidencias…",
            singleLine = false
        )
        Spacer(Modifier.height(Spacing.md))
        Text(
            text = "Etiquetas rápidas",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.xs))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            TAGS.forEach { tag ->
                val selected = state.selectedTags.contains(tag)
                FilterChip(
                    selected = selected,
                    onClick = { onToggleTag(tag) },
                    label = { Text("#$tag", style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }

    SampleSummaryCard(state = state)
}

@Composable
private fun PhotoTile(
    bitmap: Bitmap,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onOpen)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Fotografía de la muestra",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Spacing.xxs)
                .size(28.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.9f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Eliminar fotografía",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outline = MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Añadir foto",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SampleSummaryCard(state: SampleUiState) {
    val location = listOf(state.linea, state.tren).filter { it.isNotBlank() }.joinToString(" · ")
    val calibre = String.format(Locale.getDefault(), "%.1f mm", state.averageCalibre)

    AquaCard {
        Text(
            text = "Resumen de la muestra",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(Spacing.sm))
        SummaryRow("Concesión", state.concesion.ifBlank { "Sin definir" })
        SummaryRow("Línea / Tren", location.ifBlank { "Sin definir" })
        SummaryRow("Tramo", if (state.tramoLongitudCm.isBlank()) "Sin definir" else "${state.tramoLongitudCm} cm")
        SummaryRow("Conteo", state.conteoManual.ifBlank { "Sin definir" } + " individuos")
        SummaryRow("Calibre promedio", calibre)
        SummaryRow(
            "Conteo asistido",
            if (state.conteoAsistido == 0) "Sin marcas" else "${state.conteoAsistido} marcas"
        )
        SummaryRow("Fotografías", "${state.photoCount} de ${SampleViewModel.MAX_PHOTOS}")
        state.marineCurrent?.let { marine ->
            Spacer(Modifier.height(Spacing.sm))
            SummaryRow(
                "Condiciones marinas",
                listOfNotNull(
                    marine.waveHeight?.let { "Oleaje: ${it} m" },
                    marine.seaSurfaceTemperature?.let { "Temp. del mar: ${it} °C" },
                    marine.windSpeed10m?.let { "Viento: ${it} m/s" }
                ).joinToString(" · ").ifBlank { "Sin datos" }
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun WizardBottomBar(
    step: Int,
    isSaving: Boolean,
    errorMessage: String?,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onAdvance: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl, vertical = Spacing.md)
        ) {
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(Spacing.lg)
                        )
                        Spacer(Modifier.width(Spacing.sm))
                        Text(text = errorMessage, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SecondaryButton(
                    text = if (step == SampleViewModel.STEP_LOCATION) "Cancelar" else "Atrás",
                    onClick = if (step == SampleViewModel.STEP_LOCATION) onCancel else onBack,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = if (step == SampleViewModel.STEP_EVIDENCE) "Guardar muestra" else "Continuar",
                    onClick = onAdvance,
                    loading = isSaving,
                    icon = if (step == SampleViewModel.STEP_EVIDENCE) Icons.Rounded.Save else null,
                    modifier = Modifier.weight(1.4f)
                )
            }
        }
    }
}

private fun stepTitle(step: Int): String = when (step) {
    SampleViewModel.STEP_LOCATION -> "Ubicación"
    SampleViewModel.STEP_MEASUREMENT -> "Conteo y calibre"
    else -> "Evidencia"
}

private fun stepSubtitle(step: Int): String = when (step) {
    SampleViewModel.STEP_LOCATION -> "Dónde se tomó la muestra"
    SampleViewModel.STEP_MEASUREMENT -> "Qué se midió en terreno"
    else -> "Fotos, notas y revisión final"
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LocationStepPreview() {
    AquaSampleTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            StepHeader(
                currentStep = 1,
                totalSteps = 3,
                title = "Ubicación",
                subtitle = "Paso 1 · Dónde se tomó la muestra"
            )
            val centro = CentroCultivoEntity(
                id = 1,
                codigo = "CONC-104",
                nombre = "Concesión Dalcahue #104",
                comuna = "Dalcahue"
            )
            val tramo = TramoEntity(
                id = 1,
                centroId = 1,
                tren = "Tren 01",
                linea = "Línea L-01",
                longitudCm = 150
            )
            LocationStep(
                state = SampleUiState(
                    centroId = 1,
                    tramoId = 1,
                    concesion = centro.nombre,
                    tren = tramo.tren,
                    linea = tramo.linea,
                    tramoLongitudCm = "150"
                ),
                centros = listOf(centro),
                tramos = listOf(tramo),
                onCentroSelected = {},
                onTramoSelected = {}
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun MeasurementStepPreview() {
    AquaSampleTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            MeasurementStep(
                state = SampleUiState(
                    conteoManual = "28",
                    calibreMatrix = List(30) { index -> if (index < 12) "${34 + index % 6}" else "" }
                ),
                onConteoChanged = {},
                onCalibreChanged = { _, _ -> }
            )
        }
    }
}
