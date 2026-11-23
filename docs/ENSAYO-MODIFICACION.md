# Ensayo de modificación en tiempo real

Este documento es el guion para demostrar que AquaSample se puede modificar
delante del docente sin improvisar. Incluye una modificación grande (campo
nuevo de punta a punta) y una variante corta.

## 1. Antes de empezar (5 minutos)

```powershell
# Terminal 1: backend con JWT
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat -p backend run

# Terminal 2: túnel para el emulador o teléfono por USB
adb reverse tcp:8080 tcp:8080

# Terminal 3: estado base
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat -p backend test
```

Comprobar:

- [ ] `http://localhost:8080/api/health` responde `{"status":"ok"}`.
- [ ] Login en la app con `admin@aquasample.cl / admin123`.
- [ ] La app muestra el servidor `http://127.0.0.1:8080/`.
- [ ] Hay al menos una muestra visible en Historial.

## 2. Modificación grande: agregar “Marea” (10–15 minutos)

Objetivo: agregar un campo `marea` (msnm) que se capture al registrar, se vea en
el detalle, se guarde en Room, viaje al backend y salga en el PDF.

### 2.1 Backend

1. `backend/.../domain/Muestra.java`: agregar `private Double marea;` con
   getter/setter y copiarlo en `copyFrom`.
2. Reiniciar el backend (Hibernate agrega la columna con `ddl-auto=update`).
3. Probar:

```powershell
curl.exe -s http://localhost:8080/api/health
```

### 2.2 Room

1. `MuestraEntity.kt`: `val marea: Double? = null`.
2. `AquaSampleDatabase.kt`: subir `version = 6` y agregar `MIGRATION_5_6`:

```sql
ALTER TABLE muestras ADD COLUMN marea REAL
```

3. Registrar la migración en `.addMigrations(...)`.

### 2.3 Formulario

1. `SampleUiState.kt`: `val marea: String = ""`.
2. `NuevaMuestraScreen.kt`: campo numérico “Marea (msnm)” en el paso de medición.
3. `SampleViewModel.kt`: actualizar el estado y guardar `marea.toDoubleOrNull()`.

### 2.4 Sincronización

1. `MuestraDto.kt`: `val marea: Double? = null`; mapear en `toDto()` y `toEntity()`.
2. El worker no necesita cambios: envía el DTO completo.

### 2.5 Detalle y PDF

1. `SampleDetailScreen.kt`: mostrar “Marea” en la tarjeta de métricas.
2. `PdfReportGenerator.kt`: agregar la línea en el informe.

### 2.6 Verificación

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

- [ ] Crear una muestra con marea `1.8`.
- [ ] Verla en detalle.
- [ ] Sincronizar y confirmar el campo en el backend:

```powershell
curl.exe -H "Authorization: Bearer $token" http://localhost:8080/api/muestras
```

- [ ] Exportar el PDF y ver la línea.

## 3. Variante corta: índice de condición (5 minutos)

Si el tiempo aprieta, no tocar la base: calcular un índice con el calibre
promedio y mostrarlo en el detalle y el PDF.

1. `SampleDetailScreen.kt`: `val indice = (sample.averageCalibre / 40.0 * 100)`.
2. Mostrar `Índice de condición: 91 %`.
3. `PdfReportGenerator.kt`: agregar la misma línea.
4. Compilar y mostrar el cambio.

Esto demuestra modificación de lógica + UI + PDF sin migración.

## 4. Cronómetro sugerido

| Paso | Tiempo |
|---|---|
| Backend + Room | 4 min |
| Formulario + estado | 3 min |
| DTO + sync | 2 min |
| Detalle + PDF | 2 min |
| Compilar y probar | 3 min |
| **Total** | **14 min** |

## 5. Plan B

- Si falla la compilación: volver a `main` (`git checkout -- .`) y mostrar la
  variante corta.
- Si falla la migración: borrar datos de la app o usar una instalación limpia.
- Si falla el backend: mostrar la app en modo offline y el runbook.

## 6. Evidencia para el informe

- Captura del campo nuevo en el formulario.
- Captura del detalle.
- JSON del backend con `marea`.
- PDF con la línea.
- Tiempo real medido.

## 7. Ejecución real del ensayo

El ensayo se ejecutó el 23/11/2026 con la variante corta:

- Cambio: **índice de condición** (`calibre promedio / 40 mm * 100`).
- Archivos: `ConditionIndex.kt`, detalle, `PdfReportGenerator.kt` y prueba.
- Inicio: 11:15:24 · Fin: 11:17:47 → **2 min 23 s** incluyendo pruebas y build.
- Resultado: índice visible en el detalle y en el PDF.
- Pruebas: 3 nuevas; total 72 unitarias, 0 fallos.
- APK: 1.0.17 (`versionCode 18`).

Este tiempo queda como referencia; en la defensa se puede usar la variante
grande (campo Marea) con el presupuesto de 14 minutos.
