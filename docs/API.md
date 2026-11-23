# API AquaSample

Base local: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Autenticación

1. `POST /api/auth/login`

```json
{ "email": "admin@aquasample.cl", "password": "admin123" }
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": "USR-003",
  "email": "admin@aquasample.cl",
  "nombre": "Andrea",
  "rolId": 3,
  "expiresInMs": 28800000
}
```

2. Enviar el token en las demás rutas:

```http
Authorization: Bearer <token>
```

Sin token, `/api/**` responde **401**. El secreto se configura con
`AQUASAMPLE_JWT_SECRET` (mínimo 32 caracteres).

## Endpoints

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| POST | `/api/auth/login` | Login y JWT | No |
| GET | `/api/health` | Health check | No |
| GET | `/api/muestras` | Lista muestras (incluye tombstones) | Sí |
| GET | `/api/muestras/{id}` | Muestra por id | Sí |
| POST | `/api/muestras` | Crea muestra | Sí |
| PUT | `/api/muestras/{id}` | Actualiza muestra | Sí |
| PATCH | `/api/muestras/{id}/estado?estado=VALIDADO` | Cambia estado | Sí |
| DELETE | `/api/muestras/{id}` | Borrado lógico (tombstone) | Sí |
| POST | `/api/fotos` | Sube foto multipart (`file`) | Sí |
| GET | `/api/fotos/{archivo}` | Descarga foto | Sí |
| GET | `/api/centros` | Catálogo de centros | Sí |
| GET | `/api/tramos` | Catálogo de tramos | Sí |
| GET | `/api/usuarios` | Usuarios del backend | Sí |

## Ejemplo con curl

```powershell
$login = curl.exe -s -X POST -H "Content-Type: application/json" `
  --data-binary '@login.json' http://localhost:8080/api/auth/login | ConvertFrom-Json

curl.exe -H "Authorization: Bearer $($login.token)" `
  http://localhost:8080/api/muestras
```

## Códigos de error

| Código | Significado |
|---|---|
| 400 | Datos inválidos |
| 401 | Sin token, token expirado o credenciales incorrectas |
| 403 | Usuario inactivo |
| 404 | Recurso inexistente |
| 409 | Muestra duplicada |
