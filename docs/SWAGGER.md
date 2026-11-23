# Swagger y OpenAPI

## Local

Con el backend corriendo:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat -p backend run
```

- Swagger UI: **http://localhost:8080/swagger-ui.html**
- OpenAPI JSON: **http://localhost:8080/v3/api-docs**
- Health: **http://localhost:8080/api/health**

El snapshot del contrato está en [`openapi.json`](openapi.json); sirve para
revisar la API sin levantar el backend.

## Autenticación

1. Ejecuta `POST /api/auth/login`:

```json
{ "email": "admin@aquasample.cl", "password": "admin123" }
```

2. Copia el `token`.
3. En `curl`:

```powershell
curl.exe -H "Authorization: Bearer TU_TOKEN" http://localhost:8080/api/muestras
```

Swagger UI aún no declara el esquema Bearer, por lo que no muestra el botón
**Authorize**. Los endpoints protegidos se prueban con `curl` o agregando el
esquema al `OpenApiConfig`.

## URL pública temporal (demo)

Para mostrar Swagger desde otro equipo sin desplegar en la nube, con el backend
local corriendo:

```powershell
ssh -T -R 80:localhost:8080 serveo.net
```

La terminal entrega una URL como:

```text
https://<subdominio>.serveousercontent.com
```

Agrega `/swagger-ui.html` para abrir Swagger. La URL vive mientras el túnel y el
PC estén activos; no sirve para producción.

## URL pública permanente

Sigue [`DEPLOY.md`](DEPLOY.md) y el `render.yaml`. Al terminar:

```text
https://aquasample-backend.onrender.com/swagger-ui.html
```

Render plan gratuito no incluye disco: H2 y `uploads/` se pierden al reiniciar.
Para uso real agrega un volumen persistente o migra a PostgreSQL.
