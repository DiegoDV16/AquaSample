# Verificación E2E de sincronización y fotos

Fecha: 23/11/2026 · Build debug sobre `main` con JWT y Flyway.

## Escenario

1. Backend con Flyway y JWT en `localhost:8080`.
2. Emulador con la app debug y `adb reverse tcp:8080 tcp:8080`.
3. Se sembró una muestra pendiente `#M-9500` con una foto local y se inyectó
   un JWT válido en `shared_prefs/aquasample_auth.xml`.
4. Se abrió la app para que WorkManager ejecutara el worker.

## Resultado

### Backend

```json
{
  "id": "#M-9500",
  "estado": "PENDIENTE",
  "photoCount": 1,
  "fotos": ["/api/fotos/3b204a1b-8276-400e-8a8a-365aa2f90bd5.png"],
  "conteoManual": 30
}
```

- La foto se subió y quedó con ruta remota, no con la ruta local del teléfono.

### App (Room)

```text
#M-9500 | sincronizado=1 | fotos=[".../files/muestras/remoto_M-9500_1.jpg"]
```

- La app descargó la foto remota a `files/muestras/remoto_M-9500_1.jpg`.
- WorkManager registró `Worker result SUCCESS`.
- También se descargaron las muestras del backend (`#M-0001` a `#M-0008`,
  `#M-7777`, `#M-8888`, `#M-9100`, `#M-9200`, `#M-9300`, `#M-9301`).

## Edición desde la UI

Con la sesión de administrador iniciada (login manual; el JWT quedó guardado):

1. Se abrió **Muestras → #M-9500**.
2. Se tocó el ícono **Editar muestra**.
3. Se cambió el conteo de `30` a `42` y se guardó.
4. El worker envió el `PUT` y el backend quedó actualizado:

```json
{
  "id": "#M-9500",
  "conteoManual": 42,
  "estado": "PENDIENTE"
}
```

Evidencia: [`screenshots/34-edicion-e2e.png`](screenshots/34-edicion-e2e.png).

## Notificación de pendientes

Con una muestra pendiente (`#M-9600`) y sin JWT, el worker falló como se esperaba
y publicó la notificación del canal `aquasample_sync`:

```text
AquaSample · Muestras sin sincronizar
1 muestra(s) guardadas en el teléfono esperando conexión al servidor.
```

Verificado con `dumpsys notification --noredact` y captura del panel:

Evidencia: [`screenshots/35-notificacion.png`](screenshots/35-notificacion.png).

## Respaldo de ida y vuelta

Prueba Robolectric `BackupManagerTest`:

1. Inserta `#M-9700` con una foto local.
2. Exporta `.aquabackup`.
3. Borra la muestra de Room.
4. Importa el respaldo.
5. Verifica: 1 importada, 1 foto restaurada, muestra con `sincronizado=false` y
   archivo de foto existente.

Resultado: **1 prueba, 0 fallos**.

## Sesión persistente y 401

- Login manual y cierre forzado de la app (`force-stop`).
- Al reabrir, la app entró directo al panel sin pedir login.
- Evidencia: [`screenshots/36-sesion-persistente.png`](screenshots/36-sesion-persistente.png).
- Se reemplazó el JWT por uno inválido y se abrió la app.
- El worker recibió 401, `SessionManager` limpió sesión y token, y la app volvió al login.
- `shared_prefs/aquasample_session.xml` quedó vacío.
- Evidencia: [`screenshots/37-sesion-expirada.png`](screenshots/37-sesion-expirada.png).

## Limitación del ensayo

La interacción automática con el formulario de login por coordenadas fue
inestable al principio; se resolvió enfocando el campo y usando `TAB` para pasar
a la contraseña. El login manual y el guardado del JWT quedaron verificados.

## Reproducir

```powershell
# Backend
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat -p backend run

# Emulador
adb reverse tcp:8080 tcp:8080

# Token
$login = curl.exe -s -X POST -H "Content-Type: application/json" `
  --data-binary '@login.json' http://localhost:8080/api/auth/login | ConvertFrom-Json

# Sembrar #M-9500 con foto y el token, abrir la app y consultar
curl.exe -H "Authorization: Bearer $($login.token)" `
  http://localhost:8080/api/muestras/%23M-9500
```

## Conclusión

El flujo **muestra pendiente → subida de foto → POST al backend → descarga de la
foto → Room marcado como sincronizado** funciona de punta a punta con JWT.
