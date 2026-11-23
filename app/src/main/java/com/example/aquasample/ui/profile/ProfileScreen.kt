package com.example.aquasample.ui.profile

import android.graphics.Bitmap
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquasample.AquaApplication
import com.example.aquasample.appContainer
import com.example.aquasample.data.backup.BackupManager
import com.example.aquasample.data.remote.ServerConfig
import com.example.aquasample.data.sync.SyncScheduler
import com.example.aquasample.ui.designsystem.AquaCard
import com.example.aquasample.ui.designsystem.AquaScaffold
import com.example.aquasample.ui.designsystem.AquaTextField
import com.example.aquasample.ui.designsystem.AquaTopBar
import com.example.aquasample.ui.designsystem.PrimaryButton
import com.example.aquasample.ui.designsystem.ScreenContentPadding
import com.example.aquasample.ui.designsystem.SecondaryButton
import com.example.aquasample.ui.designsystem.SectionHeader
import com.example.aquasample.ui.theme.AquaSampleTheme
import com.example.aquasample.ui.theme.Spacing
import com.example.aquasample.ui.theme.ThemeManager
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Perfil del usuario: foto, alias de terreno, apariencia y cierre de sesión.
 */
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) viewModel.onPhotoSelected(bitmap)
    }

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
                    Toast.makeText(context, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.onPhotoSelected(bitmap)
                }
            }
        }
    }

    LaunchedEffect(state.savedConfirmation) {
        if (state.savedConfirmation) {
            Toast.makeText(context, "Alias actualizado", Toast.LENGTH_SHORT).show()
            viewModel.consumeConfirmation()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    BackupManager(context, (context.applicationContext as AquaApplication).container).import(uri)
                }
                val message = result.error
                    ?: "Respaldo importado: ${result.imported} muestras, " +
                    "${result.skipped} omitidas, ${result.photos} fotos."
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    val exportBackup: () -> Unit = {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    BackupManager(context, (context.applicationContext as AquaApplication).container).export()
                }
            }.onSuccess { file ->
                shareBackup(context, file)
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    "No se pudo crear el respaldo: ${error.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    ProfileContent(
        state = state,
        onAliasChanged = viewModel::onAliasChanged,
        onSaveAlias = viewModel::saveAlias,
        onPickPhoto = { galleryLauncher.launch("image/*") },
        onTakePhoto = { cameraLauncher.launch(null) },
        onRemovePhoto = viewModel::removePhoto,
        onExportBackup = exportBackup,
        onImportBackup = {
            importLauncher.launch(arrayOf("application/octet-stream", "application/zip", "*/*"))
        },
        onBack = onBack,
        onLogout = onLogout,
        modifier = modifier
    )
}

@Composable
fun ProfileContent(
    state: ProfileUiState,
    onAliasChanged: (String) -> Unit,
    onSaveAlias: () -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkMode by ThemeManager.isDarkMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showServerDialog by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf(ServerConfig.baseUrl) }

    AquaScaffold(
        modifier = modifier,
        topBar = {
            AquaTopBar(
                title = "Perfil",
                subtitle = "Cuenta de terreno",
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(ScreenContentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            AquaCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val photo = state.photo
                        if (photo != null) {
                            Image(
                                bitmap = photo.asImageBitmap(),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = state.nombreCompleto.take(1).uppercase(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        text = state.nombreCompleto.ifBlank { "Usuario" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = state.roleLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = state.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(Spacing.lg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        SecondaryButton(
                            text = "Galería",
                            icon = Icons.Rounded.PhotoLibrary,
                            onClick = onPickPhoto,
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryButton(
                            text = "Cámara",
                            icon = Icons.Rounded.PhotoCamera,
                            onClick = onTakePhoto,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (state.photo != null) {
                        Spacer(Modifier.height(Spacing.xs))
                        IconButton(onClick = onRemovePhoto) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Quitar foto",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            SectionHeader(
                title = "Alias en terreno",
                subtitle = "Se usa como firma del operador en cada muestra."
            )

            AquaCard {
                AquaTextField(
                    value = state.alias,
                    onValueChange = onAliasChanged,
                    label = "Alias",
                    placeholder = "Ej: Francisca R. / Cuadrilla 3",
                    supportingText = "Máximo 40 caracteres"
                )
                Spacer(Modifier.height(Spacing.md))
                PrimaryButton(
                    text = "Guardar alias",
                    onClick = onSaveAlias,
                    enabled = state.hasChanges
                )
            }

            SectionHeader(title = "Apariencia")
            AquaCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo oscuro",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Recomendado para jornadas nocturnas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { ThemeManager.setDarkMode(it) }
                    )
                }
            }

            SectionHeader(title = "Servidor de sincronización")
            AquaCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(
                        text = "Dirección actual",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ServerConfig.baseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "En WiFi usa la IP del PC (ej. http://192.168.1.50:8080/). " +
                            "En datos móviles necesitas una URL pública o un túnel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    SecondaryButton(
                        text = "Cambiar dirección",
                        icon = Icons.Rounded.Dns,
                        onClick = {
                            serverUrl = ServerConfig.baseUrl
                            showServerDialog = true
                        }
                    )
                }
            }

            SectionHeader(title = "Respaldo")
            AquaCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(
                        text = "Guarda tus muestras, catálogos y fotos en un archivo .aquabackup. " +
                            "Al importarlo no se pisan las muestras existentes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    SecondaryButton(
                        text = "Exportar respaldo",
                        icon = Icons.Rounded.FileDownload,
                        onClick = onExportBackup
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    SecondaryButton(
                        text = "Importar respaldo",
                        icon = Icons.Rounded.FileUpload,
                        onClick = onImportBackup
                    )
                }
            }

            SectionHeader(title = "Sesión")
            AquaCard {
                ProfileInfoRow(label = "Rol", value = state.roleLabel.ifBlank { "—" })
                ProfileInfoRow(label = "Correo", value = state.email.ifBlank { "—" })
                Spacer(Modifier.height(Spacing.lg))
                SecondaryButton(
                    text = "Cerrar sesión",
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    onClick = onLogout
                )
            }

            Spacer(Modifier.height(Spacing.sm))
            Text(
                text = "AquaSample 1.0.18",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showServerDialog) {
        Dialog(
            onDismissRequest = { showServerDialog = false },
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
                    modifier = Modifier.padding(Spacing.xl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(
                        text = "Servidor de sincronización",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    AquaTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = "URL del backend",
                        placeholder = "http://192.168.1.50:8080/"
                    )
                    Text(
                        text = "Debe terminar en /. Ejemplos: http://192.168.1.50:8080/ " +
                            "o https://aquasample.onrender.com/",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        SecondaryButton(
                            text = "Cancelar",
                            onClick = { showServerDialog = false },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = "Guardar",
                            onClick = {
                                ServerConfig.baseUrl = serverUrl
                                showServerDialog = false
                                Toast.makeText(
                                    context,
                                    "Servidor actualizado. Sincronizando…",
                                    Toast.LENGTH_SHORT
                                ).show()
                                SyncScheduler.syncNow(context)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun ProfileContentPreview() {
    AquaSampleTheme {
        ProfileContent(
            state = ProfileUiState(
                alias = "Francisca R.",
                savedAlias = "Francisca R.",
                nombreCompleto = "Francisca Rojas Soto",
                email = "operador@aquasample.cl",
                roleLabel = "Operador de Terreno"
            ),
            onAliasChanged = {},
            onSaveAlias = {},
            onPickPhoto = {},
            onTakePhoto = {},
            onRemovePhoto = {},
            onExportBackup = {},
            onImportBackup = {},
            onBack = {},
            onLogout = {}
        )
    }
}

private fun shareBackup(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/octet-stream"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Respaldo AquaSample")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir respaldo"))
}
