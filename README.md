# AquaSample

AplicaciÃ³n Android para el registro, revisiÃ³n y trazabilidad de muestras de choritos
en centros de cultivo de ALDEMAR SpA, con backend REST propio y sincronizaciÃ³n
offline.

## Funcionalidades

**App Android**
- Login por rol: operador, analista y administrador.
- Registro de muestras en un asistente de tres pasos (ubicaciÃ³n, conteo/calibre y evidencia).
- CÃ¡mara propia con CameraX, galerÃ­a con Coil y visor con zoom (Telephoto).
- Conteo asistido: marcar individuos sobre la foto, contador en vivo y correcciÃ³n.
- CatÃ¡logos de centros y tramos con alta, ediciÃ³n y baja lÃ³gica; el asistente elige el tramo en cascada y completa tren, lÃ­nea y largo.
- ExportaciÃ³n de un informe PDF por muestra y comparticiÃ³n con FileProvider.
- Historial con bÃºsqueda, filtros por estado, perÃ­odo, centro y tren, agrupaciÃ³n por dÃ­a y comparaciÃ³n con el perÃ­odo anterior.
- Dashboard con grÃ¡ficos de distribuciÃ³n por estado y evoluciÃ³n del calibre.
- Detalle con trazabilidad, matriz de calibre, fotos y validaciÃ³n de supervisor.
- Perfil con alias de terreno, foto y tema claro/oscuro.
- GestiÃ³n de personal con altas, ediciones y bajas.
- SincronizaciÃ³n offline: Room + WorkManager + Retrofit.
- Servidor configurable desde **Perfil** (IP del PC en WiFi o URL pública), con reintento automático al recuperar conexión.
- Sincronización bidireccional: sube pendientes y descarga las muestras del backend, sin pisar las locales.
- Fotos compartidas: se suben al backend y se descargan en los demas telefonos.
- Clima sincronizado: oleaje, temperatura del mar y viento via Open-Meteo, guardados en Room y backend.
- Edición de muestras guardadas: conteo, matriz de calibre y observaciones, con resincronización automática.
- Reportes consolidados: PDF de la jornada filtrada y Excel/CSV con todas las muestras visibles.
- Borrado logico y conflictos: las eliminaciones se propagan y una muestra validada en el servidor no se sobrescribe.
- Notificaciones: aviso de muestras pendientes de sincronizar y de muestras validadas por el analista.
- Respaldo local: exporta/importa un .aquabackup con muestras, catalogos y fotos.

**Backend**
- API REST Spring Boot con entidades JPA, H2, validaciones y manejo de errores.
- CRUD de muestras, centros y usuarios; filtros y cambio de estado.
- Swagger/OpenAPI y datos de demostraciÃ³n.

## Stack

| Capa | TecnologÃ­a |
|---|---|
| UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| PresentaciÃ³n | MVVM, ViewModel, StateFlow |
| Datos | Room, DataStore pendiente, Retrofit + Gson |
| Trabajo en segundo plano | WorkManager |
| CÃ¡mara e imÃ¡genes | CameraX, Coil, Telephoto |
| Backend | Spring Boot 3.5, Spring Data JPA, H2, springdoc-openapi |
| Build | Gradle 9.5, AGP 9.3.3, compileSdk 37, minSdk 24 |

## Estructura

```
app/                    AplicaciÃ³n Android
  src/main/java/com/example/aquasample/
    ui/                 Pantallas, componentes y ViewModels
    data/               Room, Retrofit y sincronizaciÃ³n
    di/                 Contenedor de dependencias
backend/                API REST Spring Boot
docs/                   DocumentaciÃ³n y capturas
  DESIGN.md             Sistema de diseÃ±o y arquitectura
  AquaSample-Estado-del-Sistema.docx
```

## CÃ³mo ejecutarlo

Requisitos: JDK 21 o superior (el JBR de Android Studio sirve), Android Studio y un
emulador o telÃ©fono con depuraciÃ³n USB.

```powershell
# 1. Backend (deja la terminal abierta)
.\gradlew.bat -p backend run

# 2. TÃºnel para que el emulador o el telÃ©fono lleguen al backend
adb reverse tcp:8080 tcp:8080

# 3. App
.\gradlew.bat :app:installDebug
```

- API: `http://localhost:8080/api/muestras`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Consola H2: `http://localhost:8080/h2-console` (JDBC `jdbc:h2:file:./data/aquasample`, usuario `sa`, sin contraseÃ±a)

En un telÃ©fono por WiFi hay que cambiar la URL de `ApiClient` por la IP del PC y
recompilar. En emulador o por USB, `adb reverse` es suficiente.

## Credenciales de demostraciÃ³n

| Rol | Correo | ContraseÃ±a |
|---|---|---|
| Operador | `operador@aquasample.cl` | `operador123` |
| Analista | `analista@aquasample.cl` | `analista123` |
| Administrador | `admin@aquasample.cl` | `admin123` |

## APK firmada

- [AquaSample-1.0.14-release.apk](dist/AquaSample-1.0.14-release.apk) (`versionName` 1.0.0).
- SHA-256: `65d2a7d0d794d55b9f961c1c5628ce6fe2b95a228b50d9256d636a957335746c`.
- Firma verificada con APK Signature Scheme v2; instrucciones en [docs/RELEASE.md](docs/RELEASE.md).
- El keystore y sus credenciales quedan fuera del repositorio.

## VerificaciÃ³n

- `.\gradlew.bat :app:assembleDebug` y `:app:testDebugUnitTest` en verde.
- Backend probado con 14 casos (CRUD, duplicados, validaciÃ³n, filtros y Swagger).
- SincronizaciÃ³n verificada en emulador: muestra pendiente enviada al backend y
  marcada como sincronizada en Room; el mismo flujo se probÃ³ sin tÃºnel y al recuperarlo.
- MigraciÃ³n de Room 1 â†’ 2 verificada sobre una base existente: columnas nuevas y
  las 10 muestras anteriores intactas.
- MigraciÃ³n 2 â†’ 3 probada con una base v2 real: conserva centros y usuarios y crea
  los 8 tramos del catÃ¡logo.
- CRUD de tramos del backend probado (201, 200, 204 y 404 posterior).
- PDF generado desde el detalle (58 KB, A4) y compartido con el selector de Android;
  hay un ejemplo en [docs/ejemplos](docs/ejemplos).
- APK release firmada (v2) instalada en el emulador y verificada con `apksigner`.
- 39 pruebas unitarias (JUnit + MockK) y 2 instrumentadas de Compose en verde.
- Cobertura de la lÃ³gica evaluable: 100 % en validadores, estado, mapeo de resumen,
  filtro de historial y repositorio. Reporte: `:app:createDebugUnitTestCoverageReport`.

## DocumentaciÃ³n

- [DiseÃ±o y arquitectura](docs/DESIGN.md)
- [Informe de estado y brechas](docs/informe/AquaSample-Estado-del-Sistema-v2.docx)
- [Backend](backend/README.md)
- [Conectar la app con el backend](docs/DEPLOY.md)

## Estado

Implementado: interfaz, navegaciÃ³n, perfiles, historial, detalle, cÃ¡mara, conteo
asistido, informes PDF, catÃ¡logos de centros y tramos, backend, sincronizaciÃ³n
offline y gestiÃ³n de personal.

Pendiente: API externa (Open-Meteo, a cargo de otro integrante), exportaciÃ³n a
Excel, pruebas con cobertura, autenticaciÃ³n del backend y APK firmada.

