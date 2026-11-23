package com.example.aquasample.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aquasample.ui.theme.Spacing
import java.io.File

/**
 * Cámara propia con CameraX: vista previa en vivo y captura JPEG en resolución
 * completa dentro del almacenamiento de la app.
 */
@Composable
fun CameraCaptureDialog(
    onDismiss: () -> Unit,
    onCaptured: (File) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasPermission) {
                CameraPreview(
                    onCaptured = onCaptured,
                    onError = { }
                )
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(Spacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        text = "Necesitamos acceso a la cámara para tomar la evidencia.",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Permitir cámara")
                    }
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.lg)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Cerrar cámara",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onCaptured: (File) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val cameraProvider = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching {
            val provider = ProcessCameraProvider.getInstance(context).get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                capture
            )
            cameraProvider.value = provider
            imageCapture.value = capture
        }.onFailure { error ->
            onError(error.localizedMessage ?: "No se pudo abrir la cámara")
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraProvider.value?.unbindAll() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        val capture = imageCapture.value
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            if (isCapturing) {
                CircularProgressIndicator(color = Color.White)
            } else {
                IconButton(
                    onClick = {
                        val current = capture ?: return@IconButton
                        val directory = File(context.filesDir, "muestras").apply { mkdirs() }
                        val file = File(directory, "captura_${System.currentTimeMillis()}.jpg")
                        val options = ImageCapture.OutputFileOptions.Builder(file).build()

                        isCapturing = true
                        current.takePicture(
                            options,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    isCapturing = false
                                    onCaptured(file)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    isCapturing = false
                                    onError(exception.message ?: "No se pudo capturar la foto")
                                }
                            }
                        )
                    },
                    enabled = capture != null,
                    modifier = Modifier
                        .size(76.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhotoCamera,
                        contentDescription = "Tomar foto",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}
