# AquaSample — Documentación de la API (Postman)

## 🔧 Introducción

**AquaSample** es una app Android (Compose) + backend **Spring Boot** (microservicios) para el registro de muestras de choritos.

- **Backend base URL:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **Consola H2:** `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:file:./data/aquasample`, usuario `sa`, sin contraseña)

> Para el emulador Android: `adb reverse tcp:8080 tcp:8080`

## 🚀 Cómo levantar el backend

```bash
.\gradlew.bat -p backend run
```

---

## 📦 Microservicio: Muestras

Base: `http://localhost:8080/api/muestras`

| # | Método | Ruta | Descripción | Body / Query |
|---|---|---|---|---|
| 1 | GET | `/api/muestras` | Lista muestras (filtros opcionales: `?estado=&q=`) | — |
| 2 | GET | `/api/muestras/{id}` | Obtiene una muestra | id: `/api/muestras/%23M-0001` |
| 3 | POST | `/api/muestras` | Crea muestra (201) | JSON en body |
| 4 | PUT | `/api/muestras/{id}` | Actualiza muestra | JSON en body |
| 5 | PATCH | `/api/muestras/{id}/estado?estado=VALIDADO` | Cambia estado | — |
| 6 | DELETE | `/api/muestras/{id}` | Elimina muestra (204) | — |

**Estados válidos:** `PENDIENTE`, `COMPLETADO`, `VALIDADO`, `RECHAZADO`.

### POST /api/muestras — Body de ejemplo

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

### PATCH /api/muestras/{id}/estado — Parámetros

- `estado`: `VALIDADO` (o `PENDIENTE` / `COMPLETADO` / `RECHAZADO`)

---

## 🌦️ Microservicio: Open-Meteo Marine API (integración de clima)

Base: `https://marine-api.open-meteo.com/v1/marine`

```
GET https://marine-api.open-meteo.com/v1/marine?latitude=-35.9&longitude=-72.9&current=wave_height,sea_surface_temperature,wind_speed_10m
```

**Respuesta esperada:**

```json
{
  "current": {
    "time": "2026-11-24T22:00",
    "wave_height": 1.25,
    "sea_surface_temperature": 18.4,
    "wind_speed_10m": 4.1
  }
}
```

---

## 📋 Checklist para probar en Postman

1. Levantar backend → `.\gradlew.bat -p backend run`
2. `GET http://localhost:8080/api/muestras` → **200** + lista de muestras
3. `POST http://localhost:8080/api/muestras` → body JSON → **201 Created**
4. `PATCH /api/muestras/%23M-0001/estado?estado=VALIDADO` → actualiza
5. `GET https://marine-api.open-meteo.com/v1/marine?...` → clima

---

# 🧪 Guía completa: de cero a ejecutar los tests en Postman

## Paso 0 — Requisitos previos

1. **Java 21+** instalado (el backend compila con `--release 21`).
2. **Backend corriendo** (ver sección anterior): `.\gradlew.bat -p backend run` → `http://localhost:8080`.
3. **Postman instalado**: <https://www.postman.com> (Windows/Mac/Linux) o app nativa.
4. (Opcional para Android) `adb reverse tcp:8080 tcp:8080`.

## Paso 1 — Iniciar Postman

1. Abrí Postman.
2. Si es la primera vez: creá cuenta o usá *Skip*.
3. Hacé clic en **"New"** → **Request** (o **+ → Request**).

## Paso 2 — Crear el primer request

1. Nombrá el request: `GET - Muestras - Lista`.
2. **Method**: `GET`.
3. **URL**: `http://localhost:8080/api/muestras`
4. Click **Send**.
5. Esperá la respuesta en la zona inferior: debe salir **200 OK** con un JSON de lista.

## Paso 3 — Probar cada endpoint

### 1️⃣ GET /api/muestras — Listar
- Method: `GET`
- URL: `http://localhost:8080/api/muestras`
- Send → esperá **200** con array de muestras.

### 2️⃣ POST /api/muestras — Crear
- Method: `POST`
- URL: `http://localhost:8080/api/muestras`
- Body → **raw → JSON** → pegar el body de ejemplo.
- Send → esperá **201 Created**.

### 3️⃣ GET /api/muestras/{id} — Obtener una
- Method: `GET`
- URL: `http://localhost:8080/api/muestras/%23M-0001` (el `#` se escribe `%23`).
- Send → esperá **200**.

### 4️⃣ PUT /api/muestras/{id} — Actualizar
- Method: `PUT`
- URL: `http://localhost:8080/api/muestras/%23M-0001`
- Body → JSON completo (igual que POST).
- Send → esperá **200**.

### 5️⃣ PATCH /api/muestras/{id}/estado — Cambiar estado
- Method: `PATCH`
- URL: `http://localhost:8080/api/muestras/%23M-0001/estado?estado=VALIDADO`
- Send → esperá **200**.

### 6️⃣ DELETE /api/muestras/{id} — Eliminar
- Method: `DELETE`
- URL: `http://localhost:8080/api/muestras/%23M-0001`
- Send → esperá **204**.

### 7️⃣ GET Open-Meteo — Condiciones marinas
- Method: `GET`
- URL: `https://marine-api.open-meteo.com/v1/marine?latitude=-35.9&longitude=-72.9&current=wave_height,sea_surface_temperature,wind_speed_10m`
- Send → esperá el JSON con `current`.

## Paso 4 — Cómo armar una colección en Postman (opcional)

1. En Postman → **Collections** → **New Collection**.
2. Nombrala: `AquaSample API`.
3. Agregá los requests dentro.
4. Guardá.

## ✅ Checklist final de ejecución

- [ ] Backend corriendo (`.\gradlew.bat -p backend run`).
- [ ] `GET /api/muestras` → 200 ✅
- [ ] `POST /api/muestras` → 201 ✅
- [ ] `GET /api/muestras/%23M-0001` → 200 ✅
- [ ] `PATCH .../estado?estado=VALIDADO` → 200 ✅
- [ ] `DELETE /api/muestras/%23M-0001` → 204 ✅
- [ ] Open-Meteo → JSON clima ✅

---

## 🧭 Resumen rápido de comandos / pasos

```bash
# 1. Levantar backend (terminal)
cd C:\Users\Zomgr\Documents\GitHub\AquaSample
.\gradlew.bat -p backend run

# 2. En Postman: armar los 7 requests descritos arriba.

# 3. Probar.
```

---

## 🆘 Solución de problemas comunes

- **Error de conexión / timeout** → verificá que el backend esté corriendo y en el puerto 8080.
- **404** → revisar la ruta y el encoding del `#` (`%23`).
- **400 en POST** → validar el JSON del body (campos requeridos según validación).

---

## 📌 Resumen final del estado del repo

- Rama: `implementación-api`
- Docs de API para Postman: `docs/api-postman.md`
- Commits recientes: `a4143a7` (docs API), `f8d57bd` (muestra climática), `2f854c3` (guarda climática), `488a96f` (restaura Open-Meteo), `2e9312c` (merge main).
- Build: ✅ `BUILD SUCCESSFUL`.

---

**¿Querés que te ayude con algún endpoint en específico en Postman** (armar el request paso a paso, ver headers, etc.)? 🔍
