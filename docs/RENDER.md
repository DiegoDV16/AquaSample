# Despliegue en Render

Esta rama deja el backend listo para Render: imagen Docker, health check,
variables de entorno y perfil `prod`.

## 1. Crear el servicio

1. Entra a <https://render.com> y elige **New → Blueprint**.
2. Conecta el repositorio `DiegoDV16/AquaSample`.
3. Selecciona la rama **`feature/render-deploy`**.
4. Render lee `render.yaml` y crea `aquasample-backend`.

El build usa el `Dockerfile` de la raíz. La primera vez tarda varios minutos
porque descarga Gradle y las dependencias.

## 2. Variables

`render.yaml` configura:

- `AQUASAMPLE_JWT_SECRET`: generado por Render.
- `SPRING_PROFILES_ACTIVE=prod`.
- `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`.

No necesitas poner puerto: el contenedor escucha `$PORT`.

## 3. Verificar

Cuando el deploy termine, abre:

```text
https://TU-SERVICIO.onrender.com/api/health
https://TU-SERVICIO.onrender.com/swagger-ui.html
```

Debe responder:

```json
{"service":"aquasample-backend","status":"ok"}
```

## 4. Configurar la app

En **Perfil → Servidor de sincronización**:

```text
https://TU-SERVICIO.onrender.com/
```

Con barra final. Pulsa **Probar conexión** y luego **Guardar y sincronizar**.
Si la sesión expiró, vuelve a iniciar sesión.

## 5. Plan gratuito

- El servicio se suspende por inactividad; la primera petición puede tardar.
- **No hay disco persistente**: H2 (`data/`) y `uploads/` se pierden en cada
  redeploy o reinicio.
- Para una demo en la misma sesión alcanza. Para conservar datos, usa disco pago
  o migra a PostgreSQL + almacenamiento de objetos.

## 6. Disco persistente (plan pago)

En `render.yaml`, descomenta `disk` y las dos variables:

```yaml
disk:
  name: aquasample-data
  mountPath: /var/data
  sizeGB: 1

envVars:
  - key: AQUASAMPLE_DB_URL
    value: jdbc:h2:file:/var/data/aquasample
  - key: AQUASAMPLE_UPLOAD_DIR
    value: /var/data/uploads
```

No dupliques `envVars`: agrega esas claves a la lista existente.

## 7. Problemas comunes

| Síntoma | Revisión |
|---|---|
| 502 al abrir | Deploy aún arrancando o servicio dormido |
| 401 en Swagger | Falta JWT; usa `/api/auth/login` y `curl` |
| App “no se pudo conectar” | URL mal escrita, sin barra final o servicio dormido |
| Datos desaparecen | Plan gratuito sin disco |
| Build falla | Revisa Logs en Render; el Dockerfile necesita `gradlew` y `backend/` |
