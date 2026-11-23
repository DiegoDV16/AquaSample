# Sincronización en el teléfono

Si una muestra queda **pendiente** aunque tengas WiFi o datos móviles, el
problema casi siempre es la dirección del servidor.

## 1. Revisar la URL

En la app: **Perfil → Servidor de sincronización**.

- `127.0.0.1` o `localhost` → en un teléfono apunta **al propio teléfono**, no al PC.
- Si el backend corre en tu PC y estás en la misma WiFi:
  `http://192.168.1.50:8080/` (tu IPv4 real).
- Si estás en datos móviles: necesitas una **URL pública** (Render o túnel).

En la versión 1.0.21 hay un botón **Probar conexión** que consulta
`/api/health` y dice si el servidor responde.

## 2. Probar desde el navegador del teléfono

Abre en Chrome del teléfono:

```text
http://IP_DEL_PC:8080/api/health
```

Debe responder:

```json
{"service":"aquasample-backend","status":"ok"}
```

Si no responde:

- El backend no está corriendo.
- El teléfono y el PC no están en la misma red.
- El Firewall de Windows bloquea el puerto 8080.
- La URL pública/túnel expiró.

## 3. Estados que muestra la app

Desde 1.0.21, la tarjeta de pendientes muestra el motivo:

- “No se pudo conectar con el servidor. Revisa la URL y la red.”
- “Sesión expirada. Inicia sesión nuevamente.”
- “El servidor rechazó #M-… (400/403/409).”

## 4. 401

Si el JWT expiró:

1. La app cierra la sesión automáticamente.
2. Vuelve a iniciar sesión.
3. Pulsa **Sincronizar**.

## 5. Fotos

La muestra sube primero la foto. Si la foto es muy grande o falla la red, la
muestra queda pendiente y se reintenta.

## 6. Comandos útiles

```powershell
# Backend local
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat -p backend run

# Túnel público temporal (para datos móviles)
ssh -T -R 80:localhost:8080 serveo.net

# Firewall
New-NetFirewallRule -DisplayName "AquaSample Backend" `
  -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```
