# APK firmada Â· AquaSample 1.0.9

**Equipo:** Diego AndrÃ©s Soto BarrÃ­a Â· Vicente Javier HernÃ¡ndez Pardo

## Archivo entregado

| Dato | Valor |
|---|---|
| Archivo | `dist/AquaSample-1.0.9-release.apk` |
| TamaÃ±o | 18.967.648 bytes |
| `versionName` | 1.0.1 |
| `versionCode` | 2 |
| SHA-256 | `1e2904f1a54c2e002b79b97ddaa3b5af0236333eb4f8e2a0c0752e74f660611c` |

El hash tambiÃ©n estÃ¡ en `dist/AquaSample-1.0.9-release.apk.sha256`.

## Firma

- Esquema: **APK Signature Scheme v2**.
- Clave: RSA de 2048 bits, validez 10.000 dÃ­as.
- DN del certificado:
  `CN=AquaSample, OU=Desarrollo, O=ALDEMAR SpA, L=Castro, ST=Los Lagos, C=CL`
- SHA-256 del certificado:
  `09fe06b81d39eeb27d9ad8fc8134db8a6484318fd34c5595467170df097e3900`

VerificaciÃ³n:

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --verbose --print-certs dist/AquaSample-1.0.9-release.apk
```

## Keystore

El keystore **no se publica** en el repositorio:

- Keystore: `C:\Users\diego\AquaSample-keys\aquasample-release.jks`
- Credenciales: `keystore.properties` en la raÃ­z del proyecto (ignorado por Git)

Respalda ambos archivos. Sin el keystore no se pueden publicar actualizaciones con la misma
firma, y la contraseÃ±a no debe subirse al repositorio.

## Recompilar el release

1. Crea `keystore.properties` en la raÃ­z (o restaura el respaldo):

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

## Instalar en el telÃ©fono

La firma de release es distinta de la de debug, asÃ­ que hay que desinstalar la versiÃ³n de
desarrollo antes de instalar esta:

```powershell
adb uninstall com.example.aquasample
adb install dist/AquaSample-1.0.9-release.apk
```

La versiÃ³n release no es depurable y conserva todas las funciones de la app.

