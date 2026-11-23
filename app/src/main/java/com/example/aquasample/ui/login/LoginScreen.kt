package com.example.aquasample.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquasample.R
import com.example.aquasample.data.session.UserRole
import com.example.aquasample.ui.designsystem.AquaTextField
import com.example.aquasample.ui.designsystem.PrimaryButton
import com.example.aquasample.ui.theme.AquaSampleTheme
import com.example.aquasample.ui.theme.Motion
import com.example.aquasample.ui.theme.Ocean700
import com.example.aquasample.ui.theme.Ocean900
import com.example.aquasample.ui.theme.Spacing

/**
 * Pantalla de acceso. La validación y el estado viven en [LoginViewModel];
 * esta capa solo dibuja y delega eventos.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (UserRole) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoggedIn, state.authenticatedUserRole) {
        val role = state.authenticatedUserRole
        if (state.isLoggedIn && role != null) {
            onLoginSuccess(role)
        }
    }

    LoginContent(
        state = state,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onSubmit = viewModel::onLoginSubmit,
        modifier = modifier
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Ocean900, Ocean700)))
    ) {
        LoginBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            LoginBrand()

            Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.xxl)) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "Accede con tu cuenta corporativa para continuar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(Spacing.xl))

                AquaTextField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    label = "Correo electrónico",
                    placeholder = "nombre@aquasample.cl",
                    leadingIcon = Icons.Rounded.Mail,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(Spacing.md))

                AquaTextField(
                    value = state.password,
                    onValueChange = onPasswordChanged,
                    label = "Contraseña",
                    leadingIcon = Icons.Rounded.Lock,
                    trailingIcon = {
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = if (state.isPasswordVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                }
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    visualTransformation = if (state.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }
                )

                AnimatedVisibility(
                    visible = state.errorMessage != null,
                    enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(Motion.Short)) +
                        expandVertically(animationSpec = androidx.compose.animation.core.tween(Motion.Medium)),
                    exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(Motion.Short)) +
                        shrinkVertically(animationSpec = androidx.compose.animation.core.tween(Motion.Short))
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.md),
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
                            Text(
                                text = state.errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.xl))

                PrimaryButton(
                    text = "Ingresar",
                    onClick = onSubmit,
                    loading = state.isLoading
                )

                Spacer(Modifier.height(Spacing.md))

                Text(
                    text = "¿Problemas de acceso? Contacta a tu supervisor.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(Spacing.xl))

        Text(
            text = "AquaSample 1.0.18 · Muestreo de choritos",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.70f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun LoginBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = Color.White.copy(alpha = 0.06f),
            radius = size.width * 0.55f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.08f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = size.width * 0.38f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.05f, size.height * 0.78f)
        )

        val wave = Path().apply {
            moveTo(0f, size.height * 0.93f)
            cubicTo(
                size.width * 0.22f,
                size.height * 0.87f,
                size.width * 0.34f,
                size.height * 0.99f,
                size.width * 0.55f,
                size.height * 0.94f
            )
            cubicTo(
                size.width * 0.74f,
                size.height * 0.89f,
                size.width * 0.86f,
                size.height * 0.98f,
                size.width,
                size.height * 0.92f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = wave, color = Color.White.copy(alpha = 0.05f))
    }
}

@Composable
private fun LoginBrand() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xxl, bottom = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo AquaSample",
            modifier = Modifier.size(128.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(Spacing.lg))

        Text(
            text = "AquaSample",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Control de muestreo de choritos",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.80f)
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LoginContentPreview() {
    AquaSampleTheme {
        LoginContent(
            state = LoginUiState(
                email = "operador@aquasample.cl",
                password = "••••••••",
                isPasswordVisible = false
            ),
            onEmailChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSubmit = {}
        )
    }
}
