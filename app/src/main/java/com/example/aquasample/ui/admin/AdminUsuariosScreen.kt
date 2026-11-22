package com.example.aquasample.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquasample.data.local.entity.UsuarioEntity
import com.example.aquasample.data.session.UserRole
import com.example.aquasample.ui.admin.AdminViewModel.Companion.toRolId
import com.example.aquasample.ui.designsystem.AquaCard
import com.example.aquasample.ui.designsystem.AquaDropdownField
import com.example.aquasample.ui.designsystem.AquaScaffold
import com.example.aquasample.ui.designsystem.AquaTextField
import com.example.aquasample.ui.designsystem.AquaTopBar
import com.example.aquasample.ui.designsystem.EmptyState
import com.example.aquasample.ui.designsystem.PrimaryButton
import com.example.aquasample.ui.designsystem.SearchField
import com.example.aquasample.ui.designsystem.SecondaryButton
import com.example.aquasample.ui.designsystem.SectionHeader
import com.example.aquasample.ui.theme.AquaSampleTheme
import com.example.aquasample.ui.theme.Spacing
import com.example.aquasample.ui.theme.ThemeManager
import com.example.aquasample.ui.theme.statusColors

/**
 * Gestión de personal: altas, ediciones y bajas de cuentas, con búsqueda y
 * filtro por rol. Solo el administrador accede a esta pantalla.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminUsuariosScreen(
    modifier: Modifier = Modifier,
    currentUserRole: UserRole = UserRole.ADMINISTRADOR,
    onLogout: () -> Unit = {},
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val isDarkMode by ThemeManager.isDarkMode.collectAsState()

    var search by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf<UserRole?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<TeamMember?>(null) }
    var deletingMember by remember { mutableStateOf<TeamMember?>(null) }

    val filtered = state.members.filter { member ->
        (roleFilter == null || member.role == roleFilter) &&
            (search.isBlank() ||
                member.name.contains(search, ignoreCase = true) ||
                member.email.contains(search, ignoreCase = true))
    }

    AquaScaffold(
        modifier = modifier,
        topBar = {
            AquaTopBar(
                title = "Personal",
                subtitle = "${state.members.size} cuentas registradas",
                actions = {
                    IconButton(onClick = { ThemeManager.setDarkMode(!isDarkMode) }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = if (isDarkMode) "Activar modo claro" else "Activar modo oscuro"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUserRole == UserRole.ADMINISTRADOR) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingMember = null
                        showForm = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Nuevo usuario") }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Spacing.xl,
                end = Spacing.xl,
                top = Spacing.sm,
                bottom = 104.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item(key = "stats") {
                TeamStats(members = state.members)
            }

            item(key = "search") {
                SearchField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = "Buscar por nombre o correo"
                )
            }

            item(key = "role-filters") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    FilterChip(
                        selected = roleFilter == null,
                        onClick = { roleFilter = null },
                        label = { Text("Todos") },
                        colors = adminChipColors()
                    )
                    FilterChip(
                        selected = roleFilter == UserRole.OPERADOR,
                        onClick = { roleFilter = UserRole.OPERADOR },
                        label = { Text("Operadores") },
                        colors = adminChipColors()
                    )
                    FilterChip(
                        selected = roleFilter == UserRole.ANALISTA,
                        onClick = { roleFilter = UserRole.ANALISTA },
                        label = { Text("Analistas") },
                        colors = adminChipColors()
                    )
                    FilterChip(
                        selected = roleFilter == UserRole.ADMINISTRADOR,
                        onClick = { roleFilter = UserRole.ADMINISTRADOR },
                        label = { Text("Admins") },
                        colors = adminChipColors()
                    )
                }
            }

            item(key = "result-count") {
                SectionHeader(
                    title = if (filtered.size == 1) "1 cuenta" else "${filtered.size} cuentas",
                    subtitle = if (state.error != null) state.error else null
                )
            }

            if (!state.isLoading && filtered.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        icon = Icons.Rounded.Person,
                        title = "Sin cuentas que mostrar",
                        message = "Ajusta la búsqueda o crea una cuenta nueva para el equipo.",
                        actionLabel = "Nuevo usuario",
                        onAction = {
                            editingMember = null
                            showForm = true
                        }
                    )
                }
            }

            items(items = filtered, key = { it.id }) { member ->
                UserCard(
                    member = member,
                    canManage = currentUserRole == UserRole.ADMINISTRADOR,
                    onEdit = {
                        editingMember = member
                        showForm = true
                    },
                    onDelete = { deletingMember = member }
                )
            }
        }
    }

    if (showForm) {
        UserFormDialog(
            initial = editingMember,
            existing = state.members,
            onDismiss = { showForm = false },
            onSave = { entity, callback ->
                viewModel.saveUser(entity) { error ->
                    if (error == null) {
                        Toast.makeText(
                            context,
                            if (editingMember == null) "Cuenta creada" else "Cuenta actualizada",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    callback(error)
                }
            }
        )
    }

    deletingMember?.let { member ->
        AlertDialog(
            onDismissRequest = { deletingMember = null },
            title = { Text("Eliminar cuenta", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se eliminará la cuenta de ${member.name} (${member.email}). " +
                        "Las muestras ya registradas conservarán su historial."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(member.id) { error ->
                            Toast.makeText(
                                context,
                                error ?: "Cuenta eliminada",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        deletingMember = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingMember = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TeamStats(members: List<TeamMember>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        StatCard(
            label = "Operadores",
            value = members.count { it.role == UserRole.OPERADOR }.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Analistas",
            value = members.count { it.role == UserRole.ANALISTA }.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Admins",
            value = members.count { it.role == UserRole.ADMINISTRADOR }.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    AquaCard(modifier = modifier, contentPadding = PaddingValues(Spacing.md)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UserCard(
    member: TeamMember,
    canManage: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AquaCard(contentPadding = PaddingValues(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name
                        .split(" ")
                        .mapNotNull { it.firstOrNull() }
                        .take(2)
                        .joinToString("")
                        .uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(Spacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    RolePill(role = member.role)
                    AccountStatusPill(status = member.status)
                }
            }

            if (canManage) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Editar ${member.name}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Eliminar ${member.name}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RolePill(role: UserRole) {
    val container = when (role) {
        UserRole.ADMINISTRADOR -> MaterialTheme.colorScheme.primaryContainer
        UserRole.ANALISTA -> MaterialTheme.colorScheme.secondaryContainer
        UserRole.OPERADOR -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when (role) {
        UserRole.ADMINISTRADOR -> MaterialTheme.colorScheme.onPrimaryContainer
        UserRole.ANALISTA -> MaterialTheme.colorScheme.onSecondaryContainer
        UserRole.OPERADOR -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(shape = CircleShape, color = container, contentColor = content) {
        Text(
            text = role.shortLabel,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
        )
    }
}

private val UserRole.shortLabel: String
    get() = when (this) {
        UserRole.OPERADOR -> "Operador"
        UserRole.ANALISTA -> "Analista"
        UserRole.ADMINISTRADOR -> "Admin"
    }

@Composable
private fun AccountStatusPill(status: String) {
    val active = status.equals("ACTIVO", ignoreCase = true)
    Surface(
        shape = CircleShape,
        color = if (active) {
            MaterialTheme.statusColors.successContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        contentColor = if (active) {
            MaterialTheme.statusColors.onSuccessContainer
        } else {
            MaterialTheme.colorScheme.onErrorContainer
        }
    ) {
        Text(
            text = if (active) "Activo" else "Inactivo",
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
        )
    }
}

@Composable
private fun adminChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
)

@Composable
private fun UserFormDialog(
    initial: TeamMember?,
    existing: List<TeamMember>,
    onDismiss: () -> Unit,
    onSave: (UsuarioEntity, (String?) -> Unit) -> Unit
) {
    var nombres by remember(initial) { mutableStateOf(initial?.nombres.orEmpty()) }
    var apellidoPaterno by remember(initial) { mutableStateOf(initial?.apellidoPaterno.orEmpty()) }
    var apellidoMaterno by remember(initial) { mutableStateOf(initial?.apellidoMaterno.orEmpty()) }
    var email by remember(initial) { mutableStateOf(initial?.email.orEmpty()) }
    var password by remember(initial) { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var role by remember(initial) { mutableStateOf(initial?.role ?: UserRole.OPERADOR) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    fun submit() {
        val trimmedEmail = email.trim()
        error = when {
            nombres.isBlank() -> "Ingresa el nombre."
            !EMAIL_REGEX.matches(trimmedEmail) -> "Ingresa un correo electrónico válido."
            existing.any { it.email.equals(trimmedEmail, ignoreCase = true) && it.id != initial?.id } ->
                "Ese correo ya está registrado en otra cuenta."
            initial == null && password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
            initial != null && password.isNotBlank() && password.length < 6 ->
                "La nueva contraseña debe tener al menos 6 caracteres."
            else -> null
        }
        if (error != null) return

        saving = true
        val entity = UsuarioEntity(
            id = initial?.id ?: "USR-${System.currentTimeMillis().toString().takeLast(6)}",
            email = trimmedEmail,
            passwordHash = if (password.isBlank()) initial?.passwordHash.orEmpty() else password,
            nombres = nombres.trim(),
            apellidoPaterno = apellidoPaterno.trim(),
            apellidoMaterno = apellidoMaterno.trim(),
            rolId = role.toRolId(),
            estado = initial?.status ?: "ACTIVO",
            creadoEn = initial?.creadoEn ?: System.currentTimeMillis()
        )

        onSave(entity) { message ->
            saving = false
            if (message != null) {
                error = message
            } else {
                onDismiss()
            }
        }
    }

    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    text = if (initial == null) "Nueva cuenta" else "Editar cuenta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (initial == null) {
                        "La cuenta podrá iniciar sesión con el correo y la contraseña indicados."
                    } else {
                        "Deja la contraseña en blanco para conservar la actual."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AquaTextField(
                    value = nombres,
                    onValueChange = { nombres = it; error = null },
                    label = "Nombres *"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AquaTextField(
                        value = apellidoPaterno,
                        onValueChange = { apellidoPaterno = it },
                        label = "Ap. paterno",
                        modifier = Modifier.weight(1f)
                    )
                    AquaTextField(
                        value = apellidoMaterno,
                        onValueChange = { apellidoMaterno = it },
                        label = "Ap. materno",
                        modifier = Modifier.weight(1f)
                    )
                }
                AquaTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    label = "Correo electrónico *",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                AquaTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = if (initial == null) "Contraseña *" else "Nueva contraseña (opcional)",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) {
                                    Icons.Rounded.VisibilityOff
                                } else {
                                    Icons.Rounded.Visibility
                                },
                                contentDescription = "Mostrar u ocultar contraseña"
                            )
                        }
                    }
                )
                AquaDropdownField(
                    label = "Rol asignado",
                    selected = role.displayName,
                    options = UserRole.entries.map { it.displayName },
                    onSelected = { selected ->
                        role = UserRole.entries.first { it.displayName == selected }
                    }
                )

                if (error != null) {
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    SecondaryButton(
                        text = "Cancelar",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = if (initial == null) "Crear" else "Guardar",
                        onClick = { submit() },
                        loading = saving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun AdminPreview() {
    AquaSampleTheme {
        AquaCard {
            UserCard(
                member = TeamMember(
                    id = "USR-001",
                    nombres = "Francisca",
                    apellidoPaterno = "Rojas",
                    apellidoMaterno = "Soto",
                    email = "operador@aquasample.cl",
                    role = UserRole.OPERADOR,
                    status = "ACTIVO",
                    creadoEn = 0L,
                    passwordHash = ""
                ),
                onEdit = {},
                onDelete = {}
            )
        }
    }
}
