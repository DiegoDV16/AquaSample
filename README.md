# AquaSample

Aplicación Android para el registro, revisión y trazabilidad de muestras de choritos
en centros de cultivo de ALDEMAR SpA, con backend REST propio y sincronización
offline.

## Funcionalidades

**App Android**
- Login por rol: operador, analista y administrador.
- Registro de muestras en un asistente de tres pasos (ubicación, conteo/calibre y evidencia).
- Cámara propia con CameraX, galería con Coil y visor con zoom (Telephoto).
- Conteo asistido: marcar individuos sobre la foto, contador en vivo y corrección.
- Catálogos de centros y tramos con alta, edición y baja lógica; el asistente elige el tramo en cascada y completa tren, línea y largo.
- Exportación de un informe PDF por muestra y compartición con FileProvider.
- Historial con búsqueda, filtros por estado y período, agrupación por día.
- Detalle con trazabilidad, matriz de calibre, fotos y validación de supervisor.
- Perfil con alias de terreno, foto y tema claro/oscuro.
- Gestión de personal con altas, ediciones y bajas.
- Sincronización offline: Room + WorkManager + Retrofit.

**Backend**
- API REST Spring Boot con entidades JPA, H2, validaciones y manejo de errores.
- CRUD de muestras, centros y usuarios; filtros y cambio de estado.
- Swagger/OpenAPI y datos de demostración.

## Stack

| Capa | Tecnología |
|---|---|
| UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| Presentación | MVVM, ViewModel, StateFlow |
| Datos | Room, DataStore pendiente, Retrofit + Gson |
| Trabajo en segundo plano | WorkManager |
| Cámara e imágenes | CameraX, Coil, Telephoto |
| Backend | Spring Boot 3.5, Spring Data JPA, H2, springdoc-openapi |
| Build | Gradle 9.5, AGP 9.3.3, compileSdk 37, minSdk 24 |

## Estructura

```
app/                    Aplicación Android
  src/main/java/com/example/aquasample/
    ui/                 Pantallas, componentes y ViewModels
    data/               Room, Retrofit y sincronización
    di/                 Contenedor de dependencias
backend/                API REST Spring Boot
docs/                   Documentación y capturas
  DESIGN.md             Sistema de diseño y arquitectura
  AquaSample-Estado-del-Sistema.docx
```

## Cómo ejecutarlo

Requisitos: JDK 21 o superior (el JBR de Android Studio sirve), Android Studio y un
emulador o teléfono con depuración USB.

```powershell
# 1. Backend (deja la terminal abierta)
.\gradlew.bat -p backend run

# 2. Túnel para que el emulador o el teléfono lleguen al backend
adb reverse tcp:8080 tcp:8080

# 3. App
.\gradlew.bat :app:installDebug
```

- API: `http://localhost:8080/api/muestras`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Consola H2: `http://localhost:8080/h2-console` (JDBC `jdbc:h2:file:./data/aquasample`, usuario `sa`, sin contraseña)

En un teléfono por WiFi hay que cambiar la URL de `ApiClient` por la IP del PC y
recompilar. En emulador o por USB, `adb reverse` es suficiente.

## Credenciales de demostración

| Rol | Correo | Contraseña |
|---|---|---|
| Operador | `operador@aquasample.cl` | `operador123` |
| Analista | `analista@aquasample.cl` | `analista123` |
| Administrador | `admin@aquasample.cl` | `admin123` |

## Verificación

- `.\gradlew.bat :app:assembleDebug` y `:app:testDebugUnitTest` en verde.
- Backend probado con 14 casos (CRUD, duplicados, validación, filtros y Swagger).
- Sincronización verificada en emulador: muestra pendiente enviada al backend y
  marcada como sincronizada en Room; el mismo flujo se probó sin túnel y al recuperarlo.
- Migración de Room 1 → 2 verificada sobre una base existente: columnas nuevas y
  las 10 muestras anteriores intactas.
- Migración 2 → 3 probada con una base v2 real: conserva centros y usuarios y crea
  los 8 tramos del catálogo.
- CRUD de tramos del backend probado (201, 200, 204 y 404 posterior).
- PDF generado desde el detalle (58 KB, A4) y compartido con el selector de Android;
  hay un ejemplo en [docs/ejemplos](docs/ejemplos).
- 39 pruebas unitarias (JUnit + MockK) y 2 instrumentadas de Compose en verde.
- Cobertura de la lógica evaluable: 100 % en validadores, estado, mapeo de resumen,
  filtro de historial y repositorio. Reporte: `:app:createDebugUnitTestCoverageReport`.

## Documentación

- [Diseño y arquitectura](docs/DESIGN.md)
- [Informe de estado y brechas](docs/informe/AquaSample-Estado-del-Sistema.docx)
- [Backend](backend/README.md)

## Estado

Implementado: interfaz, navegación, perfiles, historial, detalle, cámara, conteo
asistido, informes PDF, catálogos de centros y tramos, backend, sincronización
offline y gestión de personal.

Pendiente: API externa (Open-Meteo, a cargo de otro integrante), exportación a
Excel, pruebas con cobertura, autenticación del backend y APK firmada.
