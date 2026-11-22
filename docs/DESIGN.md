# AquaSample · Sistema de diseño y arquitectura

Documento de referencia del rediseño de la aplicación Android de muestreo de choritos
para ALDEMAR SpA. Resume las decisiones visuales, los componentes, el mapa de
navegación y la arquitectura de capas.

---

## 1. Principios de diseño

1. **Lectura en terreno.** Contraste alto, tipografía nítida y superficies limpias:
   la app se usa con guantes, casco y luz solar directa.
2. **Una acción principal por pantalla.** Cada pantalla tiene un botón primario
   evidente; el resto se jerarquiza con botones secundarios o texto.
3. **Estado honesto.** La interfaz no simula conexión ni sincronización: muestra
   "Pendiente" cuando la muestra aún no se ha enviado al servidor.
4. **Consistencia antes que variedad.** Los mismos tokens de color, radio y
   espaciado en las seis pantallas; sin sombras profundas ni colores ad hoc.
5. **Movimiento con propósito.** Transiciones breves (150–250 ms) que explican
   el cambio de contexto, no decoración.

---

## 2. Color

La paleta parte de la identidad original (azul océano y verde menta) y se amplía
a una escala con contraste suficiente para AA.

| Rol | Claro | Oscuro |
|---|---|---|
| `primary` | `#0A577A` (océano profundo) | `#6FC9EE` |
| `onPrimary` | `#FFFFFF` | `#04121A` |
| `primaryContainer` | `#DDF1F8` | `#083D57` |
| `secondary` | `#1F7A57` (verde alga) | `#7FD3AC` |
| `secondaryContainer` | `#D3F0E2` | `#0B4A34` |
| `tertiary` | `#7A5200` (arena) | `#FFD48A` |
| `background` | `#F6FAFC` | `#07141B` |
| `surface` | `#FFFFFF` | `#0B1B23` |
| `onSurface` | `#0B1B23` | `#E7F1F6` |
| `outlineVariant` | `#C9D9E2` | `#2E4652` |
| `error` | `#B3261E` | `#FFB4AB` |

Además se definen colores semánticos que Material 3 no incluye (éxito,
advertencia, información) en `ui/theme/Color.kt` y se exponen mediante
`LocalAquaStatusColors`, accesibles como `MaterialTheme.statusColors`.

---

## 3. Tipografía

Familia del sistema (sin fuentes empaquetadas para no inflar el APK), con pesos y
tracking ajustados:

| Estilo | Tamaño / línea | Uso |
|---|---|---|
| `headlineLarge` | 30 / 38 | Marca en login |
| `headlineMedium` | 26 / 34 | Cifras y títulos de sección |
| `headlineSmall` | 22 / 30 | Código de muestra, precio visual |
| `titleLarge` | 20 / 28 | Título de pantalla |
| `titleMedium` | 16 / 24 | Encabezados de tarjeta |
| `titleSmall` | 14 / 20 | Filas y listas |
| `bodyMedium` | 14 / 21 | Texto base |
| `bodySmall` | 12 / 18 | Texto auxiliar |
| `labelLarge` | 14 / 20 | Botones |
| `labelMedium` | 12 / 16 | Chips y etiquetas |
| `labelSmall` | 11 / 16 | Metadatos |

---

## 4. Espaciado, formas y movimiento

- **Espaciado** (`ui/theme/Tokens.kt`): 2, 4, 8, 12, 16, 20, 24, 32, 40 dp.
  Padding de pantalla: 20 dp; separación entre bloques: 16–20 dp.
- **Radios**: 8 (celdas), 12 (chips), 16 (campos y botones), 20 (tarjetas),
  28 (hero y hojas).
- **Movimiento**: 150 ms (feedback), 250 ms (transición de pantalla), 400 ms
  (apariciones). Curva estándar `FastOutSlowIn`, énfasis `(0.2, 0, 0, 1)`.

---

## 5. Componentes

Todos viven en `ui/designsystem/` y consumen solo tokens del tema.

| Componente | Uso |
|---|---|
| `AquaCard` | Contenedor base: superficie, borde sutil, radio 20, clic opcional |
| `AquaTopBar` | Título + subtítulo, acción de volver y acciones |
| `AquaScaffold` | Scaffold con fondo de app |
| `PrimaryButton` / `SecondaryButton` | Acción principal / alternativa, con estado de carga |
| `StepHeader` | Progreso de un asistente por pasos |
| `AquaTextField` | Campo con validación, icono y texto de apoyo |
| `AquaDropdownField` | Selector cerrado |
| `SearchField` | Búsqueda con limpiar |
| `KpiCard` / `MetricRow` | Indicadores del panel |
| `StatusPill` / `SyncPill` | Estado de revisión y de sincronización |
| `EmptyState` | Lista vacía con acción sugerida |
| `InfoBanner` | Aviso contextual (información, éxito, error) |
| `PhotoViewerDialog` | Visor a pantalla completa con zoom 1×–5× |
| `SectionHeader` | Título de sección con acción "Ver todo" |

---

## 6. Mapa de navegación

```
Login
 └── MainScreen (Scaffold + NavigationBar por rol)
      ├── Inicio            (todos)
      ├── Muestras          (todos) ──► Detalle de muestra
      ├── Nueva muestra     (operador, desde Inicio o Historial)
      ├── Personal          (administrador)
      └── Perfil            (todos)
```

- **Operador**: Inicio · Muestras · Perfil (crea muestras).
- **Analista**: Inicio · Muestras · Perfil (valida muestras).
- **Administrador**: Inicio · Muestras · Personal · Perfil.

La barra inferior se oculta en "Nueva muestra" y "Detalle", que son flujos
profundos. Las transiciones son fade + desplazamiento horizontal de 1/16 del ancho.

---

## 7. Arquitectura

```
ui/            Compose: pantallas stateless + ViewModels (StateFlow)
data/          Repositorios + Room (entidades, DAOs, base de datos)
di/            AppContainer (inyección manual, una instancia por app)
```

- **MVVM**: cada pantalla observa un `StateFlow<UiState>` inmutable; las
  pantallas no acceden a DAOs ni a la base de datos.
- **Repositorios**: `MuestraRepository`, `UsuarioRepository`,
  `CentroCultivoRepository`, `ProfileRepository`. Ejecutan E/S en
  `Dispatchers.IO` y exponen `Flow` para Room.
- **Contenedor**: `AquaApplication.container` entrega repositorios a los
  ViewModels mediante `Application.appContainer`; no se usa Hilt para mantener
  el proyecto liviano y explicable en la defensa.
- **Foto persistente**: al guardar una muestra, los bitmaps se escriben en
  `filesDir/muestras/` y la entidad guarda las rutas; el detalle las decodifica
  con muestreo para evitar OOM.

### Modelo de datos

- `roles` (1:N) `usuarios`
- `centros_cultivo` (1:N) `muestras`
- `usuarios` (1:N) `muestras`
- `muestras`: ubicación (concesión, tren, línea, tramo), conteo, matriz de
  calibre (30 valores serializados), promedio, fotos, observaciones, etiquetas,
  fecha, estado (`PENDIENTE` / `COMPLETADO` / `VALIDADO`) y sincronización.

---

## 8. Pantallas

| Pantalla | Archivo | Estado |
|---|---|---|
| Login | `ui/login/LoginScreen.kt` | Rediseñada |
| Inicio / panel | `ui/home/HomeScreen.kt` | Rediseñada |
| Nueva muestra (3 pasos) | `ui/sample/NuevaMuestraScreen.kt` | Rediseñada |
| Historial | `ui/history/HistorialMuestrasScreen.kt` | Rediseñada |
| Detalle | `ui/detail/SampleDetailScreen.kt` | Nueva |
| Perfil | `ui/profile/ProfileScreen.kt` | Rediseñada |
| Personal | `ui/admin/AdminUsuariosScreen.kt` | Rediseñada |

Capturas en `docs/screenshots/`.

---

## 9. Decisiones y límites conocidos

- **Rutas con strings** (no *type-safe navigation*): suficiente para seis
  destinos y evita un plugin adicional.
- **Perfil en memoria**: alias y foto se mantienen durante la sesión; persistirlos
  en DataStore queda como paso siguiente.
- **Sin backend todavía**: la muestra se guarda como `PENDIENTE` y
  `sincronizado = false`; la sincronización con WorkManager está pendiente.
- **Cámara**: se usa `TakePicturePreview` (cámara del sistema). Migrar a CameraX
  con archivo en resolución completa es el siguiente hito.
- **Contraseñas**: se comparan en texto plano por compatibilidad con los datos
  existentes; debe migrarse a hash (BCrypt/Argon2) antes de producción.
