package com.example.aquasample.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.aquasample.ui.theme.Spacing
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

/**
 * Visor de fotografías a pantalla completa con zoom y desplazamiento.
 *
 * Usa Coil para cargar la imagen (Bitmap, File o ruta) y Telephoto para el
 * gesto de zoom, con doble toque para acercar.
 */
@Composable
fun PhotoViewerDialog(
    model: Any,
    onDismiss: () -> Unit,
    contentDescription: String = "Fotografía ampliada"
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val zoomState = rememberZoomableState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomState),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = model,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
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
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(Spacing.xxl),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(Spacing.lg)
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = "Pellizca para ampliar",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
