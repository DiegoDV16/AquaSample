package com.example.aquasample.ui.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aquasample.ui.theme.AquaSampleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestraLaSeccionDeRespaldo() {
        composeRule.setContent {
            AquaSampleTheme {
                ProfileContent(
                    state = ProfileUiState(
                        alias = "Francisca",
                        savedAlias = "Francisca",
                        nombreCompleto = "Francisca Rojas",
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

        composeRule.onNodeWithText("Respaldo").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Exportar respaldo").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Importar respaldo").performScrollTo().assertIsDisplayed()
    }
}
