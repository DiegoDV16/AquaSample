package com.example.aquasample.ui.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aquasample.ui.theme.AquaSampleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Prueba de UI instrumentada sobre un componente del sistema de diseño. */
@RunWith(AndroidJUnit4::class)
class StatusPillTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun muestra_la_etiqueta_del_estado() {
        composeRule.setContent {
            AquaSampleTheme {
                StatusPill(status = SampleStatus.VALIDADO)
            }
        }

        composeRule.onNodeWithText("Validado").assertIsDisplayed()
    }
}
