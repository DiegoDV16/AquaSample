# APK firmada · AquaSample 1.0.0

**Equipo:** Diego Andrés Soto Barría · Vicente Javier Hernández Pardo

## Archivo entregado

| Dato | Valor |
|---|---|
| Archivo | `dist/AquaSample-1.0.0-release.apk` |
| Tamaño | 18.193.212 bytes |
| `versionName` | 1.0.0 |
| `versionCode` | 1 |
| SHA-256 | `98804a7943681f33693e3028873ed08134d6e0adbef3b29d6e5677ed88976686` |

El hash también está en `dist/AquaSample-1.0.0-release.apk.sha256`.

## Firma

- Esquema: **APK Signature Scheme v2**.
- Clave: RSA de 2048 bits, validez 10.000 días.
- DN del certificado:
  `CN=AquaSample, OU=Desarrollo, O=ALDEMAR SpA, L=Castro, ST=Los Lagos, C=CL`
- SHA-256 del certificado:
  `09fe06b81d39eeb27d9ad8fc8134db8a6484318fd34c5595467170df097e3900`

Verificación:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --verbose --print-certs dist/AquaSample-1.0.0-release.apk
```

## Keystore

El keystore **no se publica** en el repositorio:

- Keystore: `C:\Users\diego\AquaSample-keys\aquasample-release.jks`
- Credenciales: `keystore.properties` en la raíz del proyecto (ignorado por Git)

Respalda ambos archivos. Sin el keystore no se pueden publicar actualizaciones con la misma
firma, y la contraseña no debe subirse al repositorio.

## Recompilar el release

1. Crea `keystore.properties` en la raíz (o restaura el respaldo):

```properties
storeFile=C:/ruta/a/aquasample-release.jks
storePassword=********
keyAlias=aquasample
keyPassword=********
```

2. Compila:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleRelease
```

3. El APK queda en `app/build/outputs/apk/release/app-release.apk`.

Si `keystore.properties` no existe, el build de release se genera sin firmar.

## Instalar en el teléfono

La firma de release es distinta de la de debug, así que hay que desinstalar la versión de
desarrollo antes de instalar esta:

```powershell
adb uninstall com.example.aquasample
adb install dist/AquaSample-1.0.0-release.apk
```

La versión release no es depurable y conserva todas las funciones de la app.
