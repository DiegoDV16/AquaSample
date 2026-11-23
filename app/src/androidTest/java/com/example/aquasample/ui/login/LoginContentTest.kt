package com.example.aquasample.ui.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aquasample.ui.theme.AquaSampleTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setLogin(
        state: LoginUiState = LoginUiState(),
        onSubmit: () -> Unit = {}
    ) {
        composeRule.setContent {
            AquaSampleTheme {
                LoginContent(
                    state = state,
                    onEmailChanged = {},
                    onPasswordChanged = {},
                    onTogglePasswordVisibility = {},
                    onSubmit = onSubmit
                )
            }
        }
    }

    @Test
    fun muestraFormularioYDisparaLogin() {
        var submitted = false
        setLogin(onSubmit = { submitted = true })

        composeRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresar").assertIsDisplayed()
        composeRule.onNodeWithText("Ingresar").performClick()

        assertTrue(submitted)
    }

    @Test
    fun muestraErrorDeCredenciales() {
        setLogin(state = LoginUiState(errorMessage = "Contraseña incorrecta."))

        composeRule.onNodeWithText("Contraseña incorrecta.").assertIsDisplayed()
    }
}
