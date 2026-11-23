# Verificación final de AquaSample

Fecha de corte: 23/11/2026 · `main` en el commit de esta rama.

## Matriz de funcionalidad

| Funcionalidad | Evidencia | Estado |
|---|---|---|
| Login con roles | Login admin/operador/analista; JWT emitido | ✅ |
| Registro de muestra | Asistente de 3 pasos; Room + backend | ✅ |
| Fotos compartidas | Subida `POST /api/fotos` y descarga a `files/muestras` | ✅ |
| Clima | Open-Meteo Marine + viento; guardado en Room y backend | ✅ |
| Edición | Diálogo de conteo/calibre/observaciones; PUT al backend | ✅ |
| Reportes | PDF por muestra y consolidado; CSV para Excel | ✅ |
| Sincronización | Push, pull, tombstones, conflicto validado | ✅ |
| Notificaciones | Canal de pendientes y validadas | ✅ |
| Respaldo | ZIP `.aquabackup` con fotos y catálogos | ✅ |
| Seguridad | JWT + BCrypt; `/api/**` protegido | ✅ |
| Pruebas | 64 app + 6 backend + 2 instrumentadas | ✅ |

## Comandos de reproducción

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"

# Pruebas
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat -p backend test

# Cobertura
.\gradlew.bat :app:createDebugUnitTestCoverageReport

# APK firmada
.\gradlew.bat :app:assembleRelease
```

## Seguridad verificada

```text
GET /api/muestras sin token        -> 401
POST /api/auth/login (correcto)    -> 200 + JWT
GET /api/muestras con token        -> 200
POST /api/auth/login (mala clave)  -> 401
```

## Prueba de humo con APK firmada

- [x] APK 1.0.16 instalada sobre una instalación limpia.
- [x] Login con `admin@aquasample.cl`.
- [x] Inicio muestra el panel y las tarjetas de resumen.
- [x] La app no se cierra al abrir Inicio.
- [ ] Historial abre una muestra (no había muestras en la instalación limpia).
- [ ] Detalle abre el PDF o el editor.

Evidencia: [`screenshots/30-verificacion-final.png`](screenshots/30-verificacion-final.png).

## Sincronización E2E con JWT y foto

- [x] Muestra `#M-9500` subida al backend con `photoCount=1`.
- [x] Foto guardada como `/api/fotos/3b204a1b-....png`.
- [x] App descargó la foto a `files/muestras/remoto_M-9500_1.jpg`.
- [x] Room marcó `sincronizado=1`.
- [x] Worker terminó en `SUCCESS`.

Detalle: [`VERIFICACION-E2E.md`](VERIFICACION-E2E.md).

## Edición E2E

- [x] Login manual con guardado del JWT.
- [x] Historial abrió `#M-9500`.
- [x] Diálogo de edición cambió el conteo de 30 a 42.
- [x] El backend quedó con `conteoManual=42`.
- [x] Evidencia: [`screenshots/34-edicion-e2e.png`](screenshots/34-edicion-e2e.png).

## Notificación E2E

- [x] Muestra pendiente `#M-9600` sin JWT.
- [x] Notificación “Muestras sin sincronizar” publicada.
- [x] Evidencia: [`screenshots/35-notificacion.png`](screenshots/35-notificacion.png).

## Respaldo E2E

- [x] Exportar `.aquabackup` con muestra y foto.
- [x] Borrar la muestra e importar el respaldo.
- [x] Muestra y foto restauradas; queda pendiente de sincronizar.
- [x] `BackupManagerTest` en verde.

## Limitaciones conocidas

1. **Cobertura global 4,2 %**: Compose y Room generan mucho código; la lógica
   evaluable está cubierta, pero no el 90 % global.
2. **Persistencia en Render**: el plan gratuito no tiene disco; H2 y `uploads/`
   se pierden en cada redeploy. Flyway ya versiona el esquema.
3. **HTTPS y secretos**: falta forzar HTTPS y rotar `AQUASAMPLE_JWT_SECRET` en
   producción.
4. **Fotos**: se sincronizan al subir; las fotos antiguas de otro teléfono se
   descargan al abrir la muestra, no antes.
5. **Modificación en vivo**: el runbook está listo; falta ejecutarlo delante del
   docente y registrar el tiempo real.
6. **Verificación física**: cámara, conteo y notificaciones no se probaron en un
   teléfono real.

## Conclusión

La app es funcional de punta a punta para el flujo de terreno: registrar,
fotografiar, contar, editar, sincronizar, validar, reportar y respaldar. Las
brechas restantes son de calidad/operación, no de funcionalidad principal.
