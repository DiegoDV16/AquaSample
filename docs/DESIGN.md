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
| `PhotoViewerDialog` | Visor con Coil y zoom por pellizco (Telephoto) |
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
ui/              Compose: pantallas stateless + ViewModels (StateFlow)
data/
  local/         Room (entidades, DAOs, base de datos)
  remote/        Retrofit: API, DTO y cliente
  sync/          WorkManager: worker y planificador
  report/        Generación de informes PDF (PdfDocument)
  repository/    Repositorios (Muestra, Usuario, Centro, Perfil)
di/              AppContainer (inyección manual, una instancia por app)
backend/         Spring Boot: JPA + H2 + REST + Swagger (proyecto Gradle aparte)
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
  `filesDir/muestras/` y la entidad guarda las rutas; el detalle las muestra con
  Coil y las amplía con Telephoto.
- **Cámara**: CameraX (`Preview` + `ImageCapture`) captura un JPEG en resolución
  completa dentro de la app ([ui/camera/CameraCaptureDialog.kt](../app/src/main/java/com/example/aquasample/ui/camera/CameraCaptureDialog.kt)).

### Backend

- Proyecto Gradle independiente en `backend/` (Spring Boot 3.5, Java 21, H2).
- Entidades `Muestra`, `CentroCultivo` y `Usuario`; CRUD REST, filtros y cambio
  de estado; validación con Bean Validation y respuestas 400/404/409.
- Swagger/OpenAPI en `/swagger-ui.html` y datos de demostración al arrancar.
- La app se conecta por Retrofit a `http://127.0.0.1:8080/`; para el emulador o
  un teléfono por USB se usa `adb reverse tcp:8080 tcp:8080`.

### Sincronización offline

```
Guardar muestra (Room)  →  WorkManager (red disponible)
        →  POST /api/muestras
        →  201: marcar sincronizado = 1
        →  409: PUT /api/muestras/{id} y marcar
        →  error de red/5xx: reintento con backoff
```

Se dispara al guardar una muestra, al abrir la app y cada 15 minutos con
restricción de red. El inicio muestra cuántas muestras quedan pendientes y
permite forzar la sincronización.

### Conteo asistido

- `CountingDialog` muestra la primera foto y permite marcar individuos con un
  toque; tocar una marca existente la elimina.
- Las coordenadas se normalizan (0..1) respecto de la imagen y se guardan como
  JSON en `marcasConteo`; `conteoAsistido` mantiene el total para listados.
- Room pasa a **versión 2** con una migración `1 → 2` que agrega ambas columnas
  sin borrar datos. Se eliminó el `fallbackToDestructiveMigration`.

### Reportes

- `PdfReportGenerator` arma un informe A4 con `PdfDocument` (sin dependencias
  externas): identificación, ubicación, conteo manual y asistido, matriz de
  calibre, observaciones, etiquetas y una página con la primera fotografía.
- El detalle lo genera en `Dispatchers.IO`, lo guarda en `cacheDir/reportes/` y lo
  comparte con `FileProvider` mediante un `Intent.ACTION_SEND`.

### Pruebas

- La lógica de validación y filtrado se extrajo a objetos puros
  (`SampleValidator`, `HistoryFilter`, `LoginValidator`) para probarla sin Android.
- **JUnit 4 + MockK** en `app/src/test`: 39 pruebas de validadores, estado,
  mapeo de resumen, filtros, repositorio y serialización de marcas.
- **Compose UI Test** en `app/src/androidTest`: 2 pruebas instrumentadas.
- Cobertura con `enableUnitTestCoverage` y la tarea
  `createDebugUnitTestCoverageReport`. La métrica global incluye UI y código
  generado por Room; la lógica evaluable se mide por clase.

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
- **Backend sin autenticación**: la API es abierta y no valida roles; debe
  agregarse Spring Security/JWT antes de producción.
- **Migraciones**: Room ya migra de 1 a 2 conservando datos; el backend sigue con
  `ddl-auto=update` y debería pasar a Flyway.
- **Contraseñas**: se comparan en texto plano por compatibilidad con los datos
  existentes; debe migrarse a hash (BCrypt/Argon2) antes de producción.
