# Tablero Trello de AquaSample

El tablero se llama **AquaSample · Semana 16** y usa cuatro listas:

| Lista | Uso |
|---|---|
| **Backlog** | Todo lo que falta o está bloqueado |
| **En curso** | Una tarjeta por persona como máximo |
| **En revisión** | Listo para probar o revisar |
| **Hecho** | Verificado y fusionado en `main` |

## Etiquetas

- **App** (azul): pantallas, ViewModels, Room.
- **Backend** (verde): Spring Boot, JPA, JWT.
- **Datos** (naranja): sincronización, respaldo, fotos.
- **Calidad** (rojo): pruebas, cobertura, revisión.
- **Entrega** (morado): documentación, Trello, Swagger, APK.

## Tarjetas mínimas

Las tarjetas están en [`trello/tarjetas.csv`](trello/tarjetas.csv), listas para
copiar al tablero. Cada una debe tener:

- Responsable asignado.
- Etiqueta.
- Checklist de 3 pasos: implementar, probar, documentar.
- Enlace al commit o pull request.

## Cómo crearlo

1. Crear un tablero llamado **AquaSample · Semana 16**.
2. Crear las cuatro listas.
3. Crear las etiquetas con los colores indicados.
4. Crear una tarjeta por fila del CSV y asignar responsable.
5. Mover a **Hecho** solo cuando esté en `main` y probado.

## Definición de “Hecho”

- Código en `main` con commit identificable.
- Build y pruebas en verde.
- Documentación o README actualizado.
- Evidencia de prueba (captura, log o comando).
