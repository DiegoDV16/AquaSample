package com.example.aquasample.ui.sample

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquasample.ui.theme.AquaSampleTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NuevaMuestraScreen(
    modifier: Modifier = Modifier,
    viewModel: SampleViewModel = viewModel(),
    onSavedComplete: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            Toast.makeText(
                context,
                "Registro guardado exitosamente en base local (Room)",
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetSavedSuccess()
            onSavedComplete()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nueva Muestra",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "MYTILUS BIOMASS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // Badge ONLINE
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "ONLINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Avatar de Usuario
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OP",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón Cierre de Sesión Global
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stepper / Barra de Avance
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paso 2 de 3 - 65% completado",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "#TR-8842",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { 0.65f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Ubicación de Terreno (Dropdowns)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ubicación del Cultivo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Dropdown Concesión
                    LocationDropdown(
                        label = "Concesión / Centro",
                        options = listOf("Concesión Dalcahue #104", "Concesión Castro #208", "Concesión Quellón #301"),
                        selectedOption = uiState.concesion,
                        onOptionSelected = viewModel::onConcesionChanged
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dropdown Tren
                        Box(modifier = Modifier.weight(1f)) {
                            LocationDropdown(
                                label = "Tren",
                                options = listOf("Tren 01", "Tren 02", "Tren 03", "Tren 04"),
                                selectedOption = uiState.tren,
                                onOptionSelected = viewModel::onTrenChanged
                            )
                        }

                        // Dropdown Línea
                        Box(modifier = Modifier.weight(1f)) {
                            LocationDropdown(
                                label = "Línea de Cultivo",
                                options = listOf("Línea L-01", "Línea L-02", "Línea L-03", "Línea L-04"),
                                selectedOption = uiState.linea,
                                onOptionSelected = viewModel::onLineaChanged
                            )
                        }
                    }
                }
            }

            // Sección de Parámetros y Grilla de Calibre
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Parámetros de Muestreo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.tramoLongitudCm,
                            onValueChange = viewModel::onTramoLongitudChanged,
                            label = { Text("Tramo (cm)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = uiState.conteoManual,
                            onValueChange = viewModel::onConteoManualChanged,
                            label = { Text("Conteo Manual") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Indicador destacado de Calibre Promedio Automático
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Calibre Promedio Calculado",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Automático según grilla 6x5",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = String.format(java.util.Locale.getDefault(), "%.1f mm", uiState.averageCalibre),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Grilla de Calibre Matriz 6x5 (30 campos)
                    Text(
                        text = "Grilla de Registro de Calibre (mm) - Matriz 6x5:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 6 Filas x 5 Columnas
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until 6) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (col in 0 until 5) {
                                    val index = row * 5 + col
                                    OutlinedTextField(
                                        value = uiState.calibreMatrix[index],
                                        onValueChange = { viewModel.onCalibreCellChanged(index, it) },
                                        singleLine = true,
                                        placeholder = { Text("${index + 1}", fontSize = 11.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card Evidencia Fotográfica (Obligatorio, máx 5 fotos, vista completa sin recortar, zoom gran escala)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                var selectedPhotoForFullscreen by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

                val cameraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicturePreview()
                ) { bitmap ->
                    if (bitmap != null) {
                        val added = viewModel.onPhotoCaptured(bitmap)
                        if (added) {
                            Toast.makeText(context, "Foto capturada con éxito", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Límite alcanzado: Máximo 5 fotos permitidas", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            if (bitmap != null) {
                                val added = viewModel.onPhotoAdded(bitmap)
                                if (added) {
                                    Toast.makeText(context, "Imagen agregada desde galería", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Límite alcanzado: Máximo 5 fotos permitidas", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (uiState.photoCount == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Evidencia Fotográfica *",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.photoCount == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = if (uiState.photoCount > 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.photoCount > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (uiState.photoCount > 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.photoCount}/5 ${if (uiState.photoCount == 1) "Foto" else "Fotos"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.photoCount > 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Recuadro de vista previa de todas las fotos añadidas (LazyRow con imágenes completas y botón X)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (uiState.photoCount == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.photoBitmaps.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(uiState.photoBitmaps.size) { index ->
                                    val bitmap = uiState.photoBitmaps[index]
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp, 140.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .clickable { selectedPhotoForFullscreen = bitmap }
                                    ) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Foto ${index + 1}",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit // Completa, no recortada
                                        )

                                        // Botón X para eliminar en la esquina superior derecha
                                        IconButton(
                                            onClick = { viewModel.onRemovePhoto(index) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(28.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                                                    shape = CircleShape
                                                )
                                                .padding(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar foto",
                                                tint = MaterialTheme.colorScheme.onError,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Campo obligatorio: Sin fotos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Debe adjuntar al menos 1 foto (máximo 5)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retomar Foto", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Subir Adicional", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
                        }
                    }
                }

                // Diálogo de visualización a gran escala (sobre la pantalla actual)
                if (selectedPhotoForFullscreen != null) {
                    Dialog(
                        onDismissRequest = { selectedPhotoForFullscreen = null }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.95f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Barra superior con título y botón de cierre
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Vista a Gran Escala",
                                        color = androidx.compose.ui.graphics.Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { selectedPhotoForFullscreen = null },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cerrar",
                                            tint = androidx.compose.ui.graphics.Color.White
                                        )
                                    }
                                }

                                // Imagen completa (no recortada)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = selectedPhotoForFullscreen!!.asImageBitmap(),
                                        contentDescription = "Imagen a gran escala",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit // Completa, no recortada
                                    )
                                }

                                // Botón para volver a la pantalla actual
                                Button(
                                    onClick = { selectedPhotoForFullscreen = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .height(48.dp)
                                ) {
                                    Text("Volver a la pantalla", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Observaciones de Terreno y Tags Rápidos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Observaciones de Terreno",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = uiState.observaciones,
                        onValueChange = viewModel::onObservacionesChanged,
                        placeholder = { Text("Escriba notas sobre el estado de la línea o semillas...") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tags Rápidos
                    Text(
                        text = "Etiquetas Rápidas de Terreno:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val availableTags = listOf("#ValvasSanas", "#SinFouling", "#SustratoAdecuado", "#CrecimientoOptimo")

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableTags.forEach { tag ->
                            val isSelected = uiState.selectedTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onToggleTag(tag) },
                                label = { Text(tag) },
                                leadingIcon = if (isSelected) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Botón Guardar Registro (Valida que exista al menos 1 foto)
            Button(
                onClick = {
                    viewModel.onSaveSample {
                        Toast.makeText(
                            context,
                            "Campo obligatorio: Debe adjuntar al menos una foto de evidencia",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Registro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onOptionSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NuevaMuestraScreenPreview() {
    AquaSampleTheme {
        NuevaMuestraScreen()
    }
}
