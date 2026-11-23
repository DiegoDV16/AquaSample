# Conectar la app con el backend

La app guarda las muestras en Room y las envía al backend cuando puede. Para que
la sincronización funcione, el teléfono debe poder alcanzar la URL configurada en
**Perfil → Servidor de sincronización**.

## Opción A · mismo WiFi (rápido para la defensa)

1. En el PC, obtén la IP local:

```powershell
ipconfig | Select-String "IPv4"
```

2. Deja el backend corriendo:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat -p backend run
```

3. Permite el puerto 8080 en el Firewall de Windows (una vez, como administrador):

```powershell
New-NetFirewallRule -DisplayName "AquaSample Backend" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

4. En el teléfono, conéctate al **mismo WiFi** y en la app entra a
   **Perfil → Servidor de sincronización → Cambiar dirección**:

```text
http://192.168.1.50:8080/
```

(reemplaza por tu IPv4).

5. Guarda. La app intenta sincronizar de inmediato; también lo hace al abrir,
   al guardar una muestra y cada 15 minutos.

## Opción B · datos móviles (backend público)

El teléfono en datos móviles no puede llegar a la IP privada del PC. Necesitas
una URL pública, idealmente HTTPS.

Con Docker:

```powershell
docker build -t aquasample-backend .
docker run -p 8080:8080 aquasample-backend
```

Para publicarlo gratis, sube el repositorio a Render/Railway y crea un Web
Service con:

- **Dockerfile:** `Dockerfile` (raíz del repositorio).
- **Puerto:** el que asigne la plataforma (el contenedor escucha `$PORT`).
- **Disco persistente:** opcional; la base H2 es un archivo y sin disco se
  reinicia con los datos de demostración.

Cuando tengas la URL, configúrala en la app:

```text
https://tu-servicio.onrender.com/
```

Con HTTPS no hace falta tocar la configuración de tráfico claro.

## Comportamiento offline

- Si no hay red, la muestra queda en Room como `sincronizado = false`.
- WorkManager espera a que haya conexión y reintenta con backoff.
- Al recuperar WiFi o datos móviles se dispara un intento inmediato.
- El inicio muestra las muestras pendientes y permite forzar la sincronización.

## Sincronización bidireccional

- Al sincronizar, la app sube las pendientes y descarga las muestras del backend.
- Las muestras descargadas se guardan en Room y se ven con cualquier rol en ese teléfono.
- Una muestra pendiente local no se sobrescribe con la copia del servidor.
- Las fotos se suben al backend y se descargan en los demas telefonos.
- Oleaje, temperatura del mar y viento se guardan en Room y tambien en el backend.
- Las coordenadas son aproximadas por centro; viento en m/s desde api.open-meteo.com.

## Seguridad (JWT)

- `POST /api/auth/login` recibe `{"email":"...","password":"..."}` y devuelve un token JWT.
- Todas las rutas `/api/**` (excepto `/api/auth/**`) exigen `Authorization: Bearer <token>`.
- Las contraseñas se guardan con BCrypt; las cuentas antiguas se migran en el primer login.
- Configura un secreto propio de al menos 32 caracteres:

```powershell
$env:AQUASAMPLE_JWT_SECRET="cambia-este-secreto-de-produccion-0123456789"
```

- Para producción usa HTTPS; el token viaja en cada request.

## Despliegue en Render (Swagger público)

El repositorio incluye `render.yaml` para crear el servicio con Docker.

1. En Render: **New → Blueprint** y conecta el repositorio.
2. Render lee `render.yaml` y crea `aquasample-backend`.
3. El secreto JWT se genera automáticamente (`AQUASAMPLE_JWT_SECRET`).
4. Cuando termine el deploy, la URL queda así:

```text
https://aquasample-backend.onrender.com/
```

5. Comprueba:

```text
https://aquasample-backend.onrender.com/api/health
https://aquasample-backend.onrender.com/swagger-ui.html
```

6. En la app: **Perfil → Servidor** con esa URL y vuelve a iniciar sesión para
   obtener el JWT.

### Persistencia

El plan gratuito de Render no incluye disco. La base H2 y `uploads/` se pierden
en cada redeploy o reinicio. Para conservar muestras y fotos:

- Agrega un **disco persistente** y monta `/app/data` y `/app/uploads`, o
- migra a PostgreSQL y almacenamiento de objetos (S3/Cloudinary).

El esquema se versiona con **Flyway** (`backend/src/main/resources/db/migration`).
Al arrancar, una base limpia ejecuta `V2__schema_inicial.sql`; una base existente
se adopta con baseline 1 y la misma migración idempotente. Para la defensa alcanza
con el plan gratuito si se demuestra la sincronización en la misma sesión.
