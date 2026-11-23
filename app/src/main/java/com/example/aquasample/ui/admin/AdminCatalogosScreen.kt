package com.example.aquasample.ui.admin

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquasample.data.local.entity.CentroCultivoEntity
import com.example.aquasample.data.local.entity.TramoEntity
import com.example.aquasample.ui.designsystem.AquaCard
import com.example.aquasample.ui.designsystem.AquaDropdownField
import com.example.aquasample.ui.designsystem.AquaScaffold
import com.example.aquasample.ui.designsystem.AquaTextField
import com.example.aquasample.ui.designsystem.AquaTopBar
import com.example.aquasample.ui.designsystem.PrimaryButton
import com.example.aquasample.ui.designsystem.SecondaryButton
import com.example.aquasample.ui.theme.Spacing

/**
 * Administración de los catálogos operativos: centros de cultivo y tramos.
 * Las bajas son lógicas para no romper las muestras históricas.
 */
@Composable
fun AdminCatalogosScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminCatalogosViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showCentroForm by remember { mutableStateOf(false) }
    var editingCentro by remember { mutableStateOf<CentroCultivoEntity?>(null) }
    var showTramoForm by remember { mutableStateOf(false) }
    var editingTramo by remember { mutableStateOf<TramoEntity?>(null) }

    AquaScaffold(
        modifier = modifier,
        topBar = {
            AquaTopBar(
                title = "Centros y tramos",
                subtitle = "Catálogos operativos",
                onBack = onBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (tab == 0) {
                        editingCentro = null
                        showCentroForm = true
                    } else {
                        editingTramo = null
                        showTramoForm = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text(if (tab == 0) "Nuevo centro" else "Nuevo tramo") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                FilterChip(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    label = { Text("Centros (${state.centros.size})") }
                )
                FilterChip(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    label = { Text("Tramos (${state.tramos.size})") }
                )
            }

            if (tab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.xl,
                        end = Spacing.xl,
                        top = Spacing.sm,
                        bottom = 104.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(state.centros, key = { it.id }) { centro ->
                        CentroCard(
                            centro = centro,
                            onEdit = {
                                editingCentro = centro
                                showCentroForm = true
                            },
                            onToggle = {
                                viewModel.toggleCentro(centro) { error ->
                                    Toast.makeText(
                                        context,
                                        error ?: "Estado del centro actualizado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            } else {
                val centrosPorId = state.centros.associateBy { it.id }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.xl,
                        end = Spacing.xl,
                        top = Spacing.sm,
                        bottom = 104.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(state.tramos, key = { it.id }) { tramo ->
                        TramoCard(
                            tramo = tramo,
                            centroNombre = centrosPorId[tramo.centroId]?.nombre ?: "Centro ${tramo.centroId}",
                            onEdit = {
                                editingTramo = tramo
                                showTramoForm = true
                            },
                            onToggle = {
                                viewModel.toggleTramo(tramo) { error ->
                                    Toast.makeText(
                                        context,
                                        error ?: "Estado del tramo actualizado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCentroForm) {
        CentroFormDialog(
            initial = editingCentro,
            onDismiss = { showCentroForm = false },
            onSave = { centro ->
                viewModel.saveCentro(centro) { error ->
                    Toast.makeText(context, error ?: "Centro guardado", Toast.LENGTH_SHORT).show()
                    if (error == null) showCentroForm = false
                }
            }
        )
    }

    if (showTramoForm) {
        TramoFormDialog(
            initial = editingTramo,
            centros = state.centros,
            onDismiss = { showTramoForm = false },
            onSave = { tramo ->
                viewModel.saveTramo(tramo) { error ->
                    Toast.makeText(context, error ?: "Tramo guardado", Toast.LENGTH_SHORT).show()
                    if (error == null) showTramoForm = false
                }
            }
        )
    }
}

@Composable
private fun CentroCard(
    centro: CentroCultivoEntity,
    onEdit: () -> Unit,
    onToggle: () -> Unit
) {
    AquaCard(contentPadding = PaddingValues(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = centro.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${centro.codigo} · ${centro.comuna}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = if (centro.estado == "ACTIVO") "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (centro.estado == "ACTIVO") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Editar centro")
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (centro.estado == "ACTIVO") {
                        Icons.Rounded.VisibilityOff
                    } else {
                        Icons.Rounded.Visibility
                    },
                    contentDescription = if (centro.estado == "ACTIVO") "Desactivar" else "Activar"
                )
            }
        }
    }
}

@Composable
private fun TramoCard(
    tramo: TramoEntity,
    centroNombre: String,
    onEdit: () -> Unit,
    onToggle: () -> Unit
) {
    AquaCard(contentPadding = PaddingValues(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${tramo.tren} · ${tramo.linea}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$centroNombre · ${tramo.longitudCm} cm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = if (tramo.estado == "ACTIVO") "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tramo.estado == "ACTIVO") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Editar tramo")
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (tramo.estado == "ACTIVO") {
                        Icons.Rounded.VisibilityOff
                    } else {
                        Icons.Rounded.Visibility
                    },
                    contentDescription = if (tramo.estado == "ACTIVO") "Desactivar" else "Activar"
                )
            }
        }
    }
}

@Composable
private fun CentroFormDialog(
    initial: CentroCultivoEntity?,
    onDismiss: () -> Unit,
    onSave: (CentroCultivoEntity) -> Unit
) {
    var codigo by remember(initial) { mutableStateOf(initial?.codigo.orEmpty()) }
    var nombre by remember(initial) { mutableStateOf(initial?.nombre.orEmpty()) }
    var comuna by remember(initial) { mutableStateOf(initial?.comuna.orEmpty()) }
    var estado by remember(initial) { mutableStateOf(initial?.estado ?: "ACTIVO") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = if (initial == null) "Nuevo centro" else "Editar centro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                AquaTextField(
                    value = codigo,
                    onValueChange = { codigo = it },
                    label = "Código",
                    placeholder = "CONC-410"
                )
                AquaTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre",
                    placeholder = "Concesión Dalcahue #104"
                )
                AquaTextField(
                    value = comuna,
                    onValueChange = { comuna = it },
                    label = "Comuna",
                    placeholder = "Dalcahue"
                )
                AquaDropdownField(
                    label = "Estado",
                    selected = estado,
                    options = listOf("ACTIVO", "INACTIVO"),
                    onSelected = { estado = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    SecondaryButton(
                        text = "Cancelar",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Guardar",
                        onClick = {
                            onSave(
                                CentroCultivoEntity(
                                    id = initial?.id ?: 0,
                                    codigo = codigo,
                                    nombre = nombre,
                                    comuna = comuna,
                                    estado = estado
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TramoFormDialog(
    initial: TramoEntity?,
    centros: List<CentroCultivoEntity>,
    onDismiss: () -> Unit,
    onSave: (TramoEntity) -> Unit
) {
    var centroSeleccionado by remember(initial, centros) {
        mutableStateOf(
            centros.firstOrNull { it.id == initial?.centroId }?.nombre
                ?: centros.firstOrNull()?.nombre.orEmpty()
        )
    }
    var tren by remember(initial) { mutableStateOf(initial?.tren.orEmpty()) }
    var linea by remember(initial) { mutableStateOf(initial?.linea.orEmpty()) }
    var longitud by remember(initial) { mutableStateOf(initial?.longitudCm?.toString().orEmpty()) }
    var estado by remember(initial) { mutableStateOf(initial?.estado ?: "ACTIVO") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = if (initial == null) "Nuevo tramo" else "Editar tramo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                AquaDropdownField(
                    label = "Centro",
                    selected = centroSeleccionado,
                    options = centros.map { it.nombre },
                    onSelected = { centroSeleccionado = it },
                    placeholder = "Seleccionar centro"
                )
                AquaTextField(
                    value = tren,
                    onValueChange = { tren = it },
                    label = "Tren",
                    placeholder = "Tren 01"
                )
                AquaTextField(
                    value = linea,
                    onValueChange = { linea = it },
                    label = "Línea",
                    placeholder = "Línea L-01"
                )
                AquaTextField(
                    value = longitud,
                    onValueChange = { value ->
                        longitud = value.filter { it.isDigit() }
                    },
                    label = "Largo del tramo (cm)",
                    placeholder = "150",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                AquaDropdownField(
                    label = "Estado",
                    selected = estado,
                    options = listOf("ACTIVO", "INACTIVO"),
                    onSelected = { estado = it }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    SecondaryButton(
                        text = "Cancelar",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Guardar",
                        onClick = {
                            val centroId = centros
                                .firstOrNull { it.nombre == centroSeleccionado }
                                ?.id
                                ?: 0L
                            onSave(
                                TramoEntity(
                                    id = initial?.id ?: 0,
                                    centroId = centroId,
                                    tren = tren,
                                    linea = linea,
                                    longitudCm = longitud.toIntOrNull() ?: 0,
                                    estado = estado
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
