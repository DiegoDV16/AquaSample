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
4. `PATCH http://localhost:8080/api/muestras/%23M-0001/estado?estado=VALIDADO` → actualiza
5. `GET https://marine-api.open-meteo.com/v1/marine?...` → clima
