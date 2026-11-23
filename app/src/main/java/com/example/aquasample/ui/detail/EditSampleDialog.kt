package com.example.aquasample.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.ui.designsystem.AquaTextField
import com.example.aquasample.ui.designsystem.PrimaryButton
import com.example.aquasample.ui.designsystem.SecondaryButton
import com.example.aquasample.ui.theme.Spacing

/** Edición de los datos principales de una muestra ya guardada. */
@Composable
fun EditSampleDialog(
    sample: MuestraEntity,
    onDismiss: () -> Unit,
    onSave: (conteo: String, calibres: List<String>, observaciones: String) -> Unit
) {
    var conteo by remember(sample.id) { mutableStateOf(sample.conteoManual) }
    var observaciones by remember(sample.id) { mutableStateOf(sample.observaciones) }
    var calibres by remember(sample.id) {
        mutableStateOf(
            List(30) { index -> sample.calibreMatrix.getOrNull(index).orEmpty() }
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
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
                    text = "Editar muestra",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                AquaTextField(
                    value = conteo,
                    onValueChange = { conteo = it.filter { char -> char.isDigit() } },
                    label = "Conteo de individuos",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text(
                    text = "Calibre (mm)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                for (row in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (column in 0 until 5) {
                            val index = row * 5 + column
                            OutlinedTextField(
                                value = calibres[index],
                                onValueChange = { value ->
                                    val updated = calibres.toMutableList()
                                    updated[index] = value.filter {
                                        it.isDigit() || it == '.' || it == ','
                                    }
                                    calibres = updated
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )
                        }
                    }
                }

                AquaTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = "Observaciones",
                    placeholder = "Observaciones de terreno"
                )

                error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    SecondaryButton(
                        text = "Cancelar",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Guardar",
                        onClick = {
                            val validationError = SampleEditValidator.validate(conteo, calibres)
                            error = validationError
                            if (validationError == null) {
                                onSave(conteo, calibres, observaciones)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
