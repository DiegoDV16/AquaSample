# AquaSample · Backend

API REST en Spring Boot para el registro, consulta y validación de muestras de
choritos. Es el complemento del cliente Android del repositorio.

## Requisitos

- JDK 21 o superior (el proyecto compila con `--release 21`).
- El Gradle Wrapper del repositorio (`gradlew.bat` en Windows, `./gradlew` en Linux/macOS).

## Cómo ejecutarlo

```bash
# desde la raíz del repositorio
.\gradlew.bat -p backend run
```

La API queda disponible en `http://localhost:8080`.

| Recurso | URL |
|---|---|
| API | `http://localhost:8080/api/muestras` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Consola H2 | `http://localhost:8080/h2-console` (JDBC `jdbc:h2:file:./data/aquasample`, usuario `sa`, sin contraseña) |

Desde el emulador Android se recomienda un túnel de ADB para evitar el NAT y el
firewall de Windows:

```bash
adb reverse tcp:8080 tcp:8080
```

La app apunta a `http://127.0.0.1:8080/`; con el túnel activo llega al backend del
PC. En un teléfono físico también sirve `adb reverse`, o bien cambiar la URL por la
IP de la máquina en la red local (por ejemplo `http://192.168.1.50:8080/`) y
recompilar.

La base H2 se guarda en `backend/data/` y se crea sola al arrancar. Los datos de
demostración (3 centros, 3 usuarios y 8 muestras) se cargan si las tablas están vacías.

## Endpoints

### Muestras

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/muestras?estado=&q=` | Lista ordenada por fecha, con filtros opcionales |
| GET | `/api/muestras/{id}` | Obtiene una muestra (el `#` se envía como `%23`) |
| POST | `/api/muestras` | Crea una muestra (201) |
| PUT | `/api/muestras/{id}` | Actualiza los datos |
| PATCH | `/api/muestras/{id}/estado?estado=VALIDADO` | Cambia el estado de revisión |
| DELETE | `/api/muestras/{id}` | Elimina (204) |

Estados aceptados: `PENDIENTE`, `COMPLETADO`, `VALIDADO`, `RECHAZADO`.

### Centros y usuarios

| Método | Ruta | Descripción |
|---|---|---|
| GET/POST | `/api/centros` | Lista y crea centros de cultivo |
| GET/PUT/DELETE | `/api/centros/{id}` | Consulta, actualiza y elimina |
| GET/POST | `/api/usuarios` | Lista y crea cuentas |
| GET | `/api/usuarios/{id}` | Consulta una cuenta |
| PATCH | `/api/usuarios/{id}/estado?estado=ACTIVO` | Activa o desactiva |

## Modelo de muestra

La entidad replica la tabla local de la app Android para que la sincronización sea
directa. Los campos `calibreMatrix`, `fotos` y `tags` se guardan como texto JSON,
igual que en Room:

```json
{
  "id": "#M-0009",
  "centroId": 1,
  "operadorId": "USR-001",
  "concesion": "Concesión Dalcahue #104",
  "tren": "Tren 01",
  "linea": "Línea L-09",
  "tramoLongitudCm": "150",
  "conteoManual": 30,
  "calibreMatrix": "[34.0,35.0]",
  "averageCalibre": 34.5,
  "photoCount": 1,
  "fotos": "[]",
  "observaciones": "Prueba de API",
  "tags": "[]",
  "estado": "PENDIENTE",
  "sincronizado": false,
  "operatorAlias": "Francisca Rojas"
}
```

## Estado actual y pendientes

Implementado:

- CRUD completo de muestras, centros y usuarios.
- Filtros por estado y texto, cambio de estado y validación con Bean Validation.
- Swagger/OpenAPI, CORS y consola H2.
- Datos de demostración.

Pendiente:

- Autenticación y autorización por rol (hoy la API es abierta).
- Migraciones versionadas (Flyway/Liquibase) en lugar de `ddl-auto=update`.
- Endpoint de sincronización por lote y resolución de conflictos.
- Pruebas de integración con `MockMvc` y Testcontainers.
