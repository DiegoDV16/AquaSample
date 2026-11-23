"""Genera el informe Word de estado del sistema AquaSample."""

from pathlib import Path
import sys

from docx import Document
from docx.enum.table import WD_ALIGN_VERTICAL, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

BASE = Path(r"C:\Users\diego\OneDrive\Documents\GitHub\AquaSample")
SHOTS = BASE / "docs" / "screenshots"
OUTPUT = Path(sys.argv[1]) if len(sys.argv) > 1 else BASE / "docs" / "informe" / "AquaSample-Estado-del-Sistema.docx"
OUTPUT.parent.mkdir(parents=True, exist_ok=True)

OCEAN = RGBColor(0x0A, 0x57, 0x7A)
INK = RGBColor(0x1F, 0x29, 0x37)
MUTED = RGBColor(0x5B, 0x6B, 0x7A)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
GREEN_FILL, GREEN_TEXT = "D1FAE5", RGBColor(0x06, 0x5F, 0x46)
AMBER_FILL, AMBER_TEXT = "FEF3C7", RGBColor(0x92, 0x40, 0x0E)
RED_FILL, RED_TEXT = "FEE2E2", RGBColor(0x99, 0x1B, 0x1B)
HEADER_FILL, ZEBRA_FILL, CODE_FILL = "0A577A", "F6FAFC", "F1F5F9"
STATUS_STYLES = {
    "Completo": (GREEN_FILL, GREEN_TEXT),
    "Parcial": (AMBER_FILL, AMBER_TEXT),
    "Pendiente": (RED_FILL, RED_TEXT),
}


def shade(cell, color):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), color)
    tc_pr.append(shd)


def margins(cell, top=60, bottom=60, left=90, right=90):
    tc_pr = cell._tc.get_or_add_tcPr()
    mar = OxmlElement("w:tcMar")
    for name, value in (("top", top), ("left", left), ("bottom", bottom), ("right", right)):
        node = OxmlElement(f"w:{name}")
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")
        mar.append(node)
    tc_pr.append(mar)


def borders(table, color="CBD5E1", size=4):
    tbl_pr = table._tbl.tblPr
    element = OxmlElement("w:tblBorders")
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        node = OxmlElement(f"w:{edge}")
        node.set(qn("w:val"), "single")
        node.set(qn("w:sz"), str(size))
        node.set(qn("w:space"), "0")
        node.set(qn("w:color"), color)
        element.append(node)
    tbl_pr.append(element)


def write_cell(cell, text, bold=False, color=INK, size=9, align=None):
    cell.text = ""
    paragraph = cell.paragraphs[0]
    paragraph.paragraph_format.space_before = Pt(0)
    paragraph.paragraph_format.space_after = Pt(0)
    paragraph.paragraph_format.line_spacing = 1.0
    if align is not None:
        paragraph.alignment = align
    run = paragraph.add_run(text)
    run.bold = bold
    run.font.size = Pt(size)
    run.font.color.rgb = color
    run.font.name = "Calibri"
    cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    margins(cell)


def add_table(doc, headers, rows, widths, status_column=None, size=9):
    table = doc.add_table(rows=1, cols=len(headers))
    table.autofit = False
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    borders(table)

    for index, header in enumerate(headers):
        cell = table.rows[0].cells[index]
        write_cell(cell, header, bold=True, color=WHITE, size=size)
        shade(cell, HEADER_FILL)
        cell.width = Cm(widths[index])
    tr_pr = table.rows[0]._tr.get_or_add_trPr()
    tr_pr.append(OxmlElement("w:tblHeader"))
    tr_pr.append(OxmlElement("w:cantSplit"))

    for row_index, row in enumerate(rows):
        table_row = table.add_row()
        table_row._tr.get_or_add_trPr().append(OxmlElement("w:cantSplit"))
        for column_index, value in enumerate(row):
            cell = table_row.cells[column_index]
            write_cell(cell, str(value), size=size)
            cell.width = Cm(widths[column_index])
            is_status = status_column is not None and column_index == status_column
            if row_index % 2 == 1 and not is_status:
                shade(cell, ZEBRA_FILL)
            if is_status and str(value) in STATUS_STYLES:
                fill, color = STATUS_STYLES[str(value)]
                shade(cell, fill)
                for paragraph in cell.paragraphs:
                    for run in paragraph.runs:
                        run.font.color.rgb = color
                        run.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return table


def bullets(doc, items):
    for item in items:
        paragraph = doc.add_paragraph(style="List Bullet")
        paragraph.paragraph_format.space_after = Pt(2)
        paragraph.paragraph_format.line_spacing = 1.1
        run = paragraph.add_run(item)
        run.font.size = Pt(10)
        run.font.color.rgb = INK


def labeled(doc, label, text):
    paragraph = doc.add_paragraph()
    paragraph.paragraph_format.space_after = Pt(6)
    paragraph.paragraph_format.line_spacing = 1.15
    first = paragraph.add_run(f"{label} ")
    first.bold = True
    first.font.size = Pt(10.5)
    first.font.color.rgb = OCEAN
    second = paragraph.add_run(text)
    second.font.size = Pt(10.5)
    second.font.color.rgb = INK


def code_block(doc, text):
    paragraph = doc.add_paragraph()
    paragraph.paragraph_format.space_before = Pt(4)
    paragraph.paragraph_format.space_after = Pt(8)
    paragraph.paragraph_format.left_indent = Cm(0.3)
    paragraph.paragraph_format.line_spacing = 1.0
    p_pr = paragraph._p.get_or_add_pPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), CODE_FILL)
    p_pr.append(shd)
    lines = text.split("\n")
    for index, line in enumerate(lines):
        run = paragraph.add_run(line)
        run.font.name = "Consolas"
        run._element.rPr.rFonts.set(qn("w:eastAsia"), "Consolas")
        run.font.size = Pt(8.5)
        run.font.color.rgb = RGBColor(0x33, 0x41, 0x55)
        if index < len(lines) - 1:
            run.add_break()


def footer(section):
    paragraph = section.footer.paragraphs[0]
    paragraph.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    prefix = paragraph.add_run("AquaSample · Estado del sistema y brechas   |   ")
    prefix.font.size = Pt(8)
    prefix.font.color.rgb = MUTED
    page_run = paragraph.add_run()
    page_run.font.size = Pt(8)
    page_run.font.color.rgb = MUTED
    for tag, value in (("begin", None), ("instr", "PAGE"), ("end", None)):
        if tag == "instr":
            node = OxmlElement("w:instrText")
            node.set(qn("xml:space"), "preserve")
            node.text = value
        else:
            node = OxmlElement("w:fldChar")
            node.set(qn("w:fldCharType"), tag)
        page_run._r.append(node)


def cover(doc):
    band = doc.add_table(rows=1, cols=1)
    band.autofit = False
    cell = band.rows[0].cells[0]
    cell.width = Cm(16.6)
    band.rows[0].height = Cm(0.5)
    cell.text = ""
    shade(cell, HEADER_FILL)

    doc.add_paragraph().paragraph_format.space_after = Pt(60)

    kicker = doc.add_paragraph()
    kicker.paragraph_format.space_after = Pt(6)
    run = kicker.add_run("ALDEMAR SpA  ·  CONTROL DE MUESTREO")
    run.bold = True
    run.font.size = Pt(10)
    run.font.color.rgb = OCEAN

    title = doc.add_paragraph()
    title.paragraph_format.space_after = Pt(4)
    run = title.add_run("AquaSample")
    run.bold = True
    run.font.size = Pt(40)
    run.font.color.rgb = RGBColor(0x06, 0x29, 0x3A)

    subtitle = doc.add_paragraph()
    subtitle.paragraph_format.space_after = Pt(18)
    run = subtitle.add_run("Estado del sistema y brechas frente a la pauta de evaluación")
    run.font.size = Pt(16)
    run.font.color.rgb = OCEAN

    intro = doc.add_paragraph()
    intro.paragraph_format.space_after = Pt(28)
    run = intro.add_run(
        "Inventario de lo construido, análisis de cumplimiento de los 14 criterios "
        "de la pauta y plan de trabajo para cerrar las brechas antes de la defensa."
    )
    run.font.size = Pt(11)
    run.font.color.rgb = MUTED

    metadata = [
        ("Documento", "Estado del sistema · versión 1.0"),
        ("Fecha", "22 de noviembre de 2026"),
        ("Repositorio", "github.com/DiegoDV16/AquaSample (público)"),
        ("Commit auditado", "main · f877109 (respaldo local y APK 1.0.14)"),
        ("Plataforma", "Android · Kotlin 2.0.21 · Jetpack Compose · Material 3"),
        ("Backend", "Spring Boot 3.5.16 · H2 · springdoc 2.8.13"),
        ("Verificación", "App + backend en emulador, con sincronización offline y online"),
    ]
    table = doc.add_table(rows=len(metadata), cols=2)
    table.autofit = False
    for index, (label, value) in enumerate(metadata):
        cells = table.rows[index].cells
        cells[0].width = Cm(4.2)
        cells[1].width = Cm(12.4)
        write_cell(cells[0], label, bold=True, color=OCEAN, size=9)
        write_cell(cells[1], value, size=9)
    borders(table, "E2E8F0")

    doc.add_paragraph().paragraph_format.space_after = Pt(40)
    note = doc.add_paragraph()
    note.paragraph_format.space_before = Pt(12)
    run = note.add_run(
        "Nota metodológica: este documento se basa en la auditoría del código fuente, "
        "en el resultado del build de Gradle y en pruebas manuales realizadas sobre un "
        "emulador. No incluye un dispositivo físico ni componentes de backend, porque el "
        "repositorio no los contiene a la fecha de este informe."
    )
    run.italic = True
    run.font.size = Pt(8.5)
    run.font.color.rgb = MUTED


def summary(doc):
    doc.add_heading("Resumen ejecutivo", level=1)
    doc.add_paragraph(
        "AquaSample es hoy una aplicación Android funcional, con backend propio y "
        "sincronización offline: permite iniciar sesión por rol, registrar una muestra en "
        "tres pasos con cámara integrada, consultarla, filtrarla, validarla como supervisor "
        "y administrar las cuentas del equipo. Los datos viven en Room y se envían solos al "
        "backend cuando hay conexión."
    )
    doc.add_paragraph(
        "Frente a los 14 criterios de la pauta, 11 están completos, 3 son parciales y 0 siguen "
        "pendientes. La API externa Open-Meteo Marine ya está integrada y demostrada; las "
        "brechas restantes son la cobertura global de pruebas, la documentación colaborativa "
        "y la modificación en tiempo real."
    )
    doc.add_heading("Cifras clave del sistema", level=2)
    add_table(
        doc,
        ["Indicador", "Valor actual"],
        [
            ("Pantallas de usuario", "8 (Login, Inicio, Nueva muestra, Historial, Detalle, Perfil, Personal, Centros y tramos)"),
            ("Código Kotlin", "65 archivos · más de 8.100 líneas en app/src/main"),
            ("Capa de datos", "5 tablas Room · 8 ViewModels · 5 repositorios"),
            ("Backend", "Spring Boot + H2 · 8 rutas REST / 14 operaciones · Swagger"),
            ("Pruebas", "39 unitarias (JUnit + MockK) + 2 instrumentadas de Compose"),
            ("Cobertura", "100 % en la lógica evaluable (validadores, filtros, mapeo y repositorio)"),
            ("Informes", "PDF A4 por muestra con FileProvider y ejemplo en docs/ejemplos"),
            ("Catálogos", "3 centros y 8 tramos con CRUD, baja lógica y selección en cascada"),
            ("Dashboard", "2 gráficos (estado y evolución del calibre) y comparación de períodos"),
            ("Sincronización", "WorkManager + Retrofit · verificada offline → online en emulador"),
            ("Documentación", "README, DESIGN.md, informe y 23 capturas"),
            ("APK", "Release 1.0.14 firmada (v2), 18 MB, instalada y verificada"),
            ("Build verificado", "app y backend en verde; 56 + 2 pruebas (main · 72285bd)"),
        ],
        [4.6, 12.0],
    )
    doc.add_heading("Semáforo de la pauta", level=2)
    add_table(
        doc,
        ["Estado", "Cantidad", "Ítems"],
        [
            ("Completo", "11", "Interfaz; gestión de muestras; fotografías; conteo asistido; reportes PDF; historial y dashboard; microservicios; offline; perfiles; APK firmada; API externa Open-Meteo"),
            ("Parcial", "3", "Pruebas (cobertura global); documentación; modificación en tiempo real"),
            ("Pendiente", "0", "Ninguno"),
        ],
        [2.4, 2.0, 12.2],
        status_column=0,
    )
    doc.add_paragraph(
        "El detalle de cada ítem, con su evidencia y su brecha, está en la sección 3; el plan "
        "para cerrarlas, en la sección 5."
    )


def section_1(doc):
    doc.add_heading("1. Alcance y método", level=1)
    doc.add_paragraph(
        "La revisión se hizo directamente sobre el repositorio, no sobre una descripción "
        "previa del proyecto. Se inspeccionaron las pantallas, la navegación, los ViewModels, "
        "los repositorios, las entidades de Room y las dependencias de Gradle; luego se "
        "compiló la aplicación y se recorrieron los flujos principales en un emulador."
    )
    bullets(
        doc,
        [
            "Fuente de verdad: código en main, commit 24734cc (backend, sync, cámara, conteo, reportes y pruebas).",
            "Compilación: :app:assembleDebug, :app:testDebugUnitTest y :backend compileJava/run con Gradle.",
            "Ejecución: emulador Pixel 7 Pro (1440 × 3120, Android 37), backend local en el puerto 8080 y túnel adb reverse.",
            "Backend: Spring Boot + H2, CRUD documentado en Swagger y 14 casos de humo ejecutados con curl.",
            "Evidencia visual: 15 capturas en docs/screenshots/ incluidas en este informe.",
            "Documentación: README, docs/DESIGN.md y backend/README.md.",
        ],
    )
    paragraph = doc.add_paragraph(
        "Lo que este informe no cubre: rendimiento en un teléfono físico, pruebas de "
        "aceptación con usuarios reales, despliegue del backend en un servidor, tablero de "
        "Trello y credenciales de servicios externos."
    )
    paragraph.paragraph_format.space_before = Pt(6)


def section_2(doc):
    doc.add_heading("2. Lo que tenemos hoy", level=1)
    doc.add_paragraph(
        "El sistema es una aplicación Android completa en su capa de presentación y de datos "
        "locales: permite iniciar sesión por rol, registrar una muestra en tres pasos, "
        "consultarla, filtrarla, validarla como supervisor y administrar las cuentas del "
        "equipo. Toda la información vive en Room, en el dispositivo."
    )

    doc.add_heading("2.1 Stack técnico", level=2)
    add_table(
        doc,
        ["Componente", "Versión", "Uso en el proyecto"],
        [
            ("Kotlin", "2.0.21", "Lenguaje principal; coroutines y Flow"),
            ("Jetpack Compose / Material 3", "BOM 2026.02.01", "UI declarativa y tema de la app"),
            ("Navigation Compose", "2.9.8", "Grafo de navegación y rutas con argumentos"),
            ("Lifecycle / ViewModel", "2.11.0", "Estado de UI y ciclo de vida"),
            ("Room", "2.8.5", "Base de datos local (SQLite) con DAOs y convertidores"),
            ("KSP", "2.0.21-1.0.27", "Generación de código de Room"),
            ("Gson", "2.10.1", "Serialización de listas y etiquetas en Room"),
            ("Retrofit / OkHttp", "2.12.0", "Consumo del backend y de APIs externas"),
            ("WorkManager", "2.12.0", "Sincronización offline de muestras"),
            ("CameraX / Coil / Telephoto", "1.7.0 / 2.7.0 / 0.19.0", "Cámara, carga y zoom de imágenes"),
            ("Spring Boot / H2", "3.5.16 · springdoc 2.8.13", "Backend REST, base de datos y Swagger"),
            ("Android Gradle Plugin", "9.3.3", "compileSdk 37, minSdk 24, targetSdk 37"),
        ],
        [5.4, 3.2, 8.0],
    )

    doc.add_heading("2.2 Sistema de diseño", level=2)
    doc.add_paragraph(
        "Se construyó un sistema propio sobre Material 3, con tokens de color, tipografía, "
        "espaciado, radios y movimiento. La paleta evoluciona la identidad original hacia un "
        "azul océano profundo y un verde alga, con variantes para tema claro y oscuro y "
        "colores semánticos (éxito, advertencia, información) que Material 3 no trae."
    )
    add_table(
        doc,
        ["Componente", "Propósito"],
        [
            ("AquaCard", "Contenedor base con borde sutil, radio 20 dp y clic opcional"),
            ("AquaTopBar / AquaScaffold", "Barra superior con título, subtítulo y acciones; fondo de app"),
            ("PrimaryButton / SecondaryButton", "Acción principal y alternativa, con estado de carga"),
            ("AquaTextField / AquaDropdownField", "Campos validados y selectores consistentes"),
            ("SearchField", "Búsqueda con acción de limpiar"),
            ("KpiCard / MetricRow", "Indicadores del panel de inicio"),
            ("StatusPill / SyncPill", "Estado de revisión y de sincronización"),
            ("StepHeader", "Progreso de asistentes por pasos"),
            ("EmptyState / InfoBanner", "Listas vacías y avisos contextuales"),
            ("PhotoViewerDialog", "Visor a pantalla completa con zoom 1×–5×"),
        ],
        [6.0, 10.6],
    )

    doc.add_heading("2.3 Navegación", level=2)
    doc.add_paragraph(
        "La navegación usa Navigation Compose con rutas por string, transiciones de entrada "
        "y salida, y una barra inferior que cambia según el rol y se oculta en los flujos "
        "profundos (registro y detalle)."
    )
    code_block(
        doc,
        "Login\n"
        " └── MainScreen (Scaffold + NavigationBar por rol)\n"
        "      ├── Inicio          (todos)\n"
        "      ├── Muestras        (todos) ──► Detalle de muestra (supervisor valida)\n"
        "      ├── Nueva muestra   (operador, desde Inicio o Historial)\n"
        "      ├── Personal        (administrador)\n"
        "      └── Perfil          (todos)",
    )
    add_table(
        doc,
        ["Rol", "Destinos en la barra inferior", "Acciones habilitadas"],
        [
            ("Operador", "Inicio · Muestras · Perfil", "Registrar muestras; consultar historial"),
            ("Analista", "Inicio · Muestras · Perfil", "Revisar y validar muestras"),
            ("Administrador", "Inicio · Muestras · Personal · Perfil", "Validar y administrar cuentas"),
        ],
        [3.2, 7.0, 6.4],
    )

    doc.add_heading("2.4 Arquitectura y datos", level=2)
    doc.add_paragraph(
        "La app sigue MVVM con repositorios. Un contenedor manual (AppContainer) entrega una "
        "única instancia de la base de datos y los repositorios a los ViewModels; las "
        "pantallas observan un StateFlow inmutable y no acceden a DAOs. Hay siete ViewModels: "
        "Login, Inicio, Nueva muestra, Historial, Detalle, Perfil y Personal."
    )
    add_table(
        doc,
        ["Capa", "Responsabilidad", "Archivos representativos"],
        [
            ("UI", "Pantallas Compose sin lógica de datos", "ui/*/*Screen.kt"),
            ("Presentación", "Estado, validaciones y eventos", "ui/*/*ViewModel.kt"),
            ("Datos", "Repositorios con Flow y E/S en IO", "data/repository/*"),
            ("Persistencia", "Room: entidades, DAOs, convertidores", "data/local/**"),
            ("Inyección", "Contenedor de dependencias", "di/AppContainer.kt"),
        ],
        [3.0, 7.2, 6.4],
    )
    doc.add_paragraph(
        "El modelo local tiene cuatro tablas. Las muestras guardan ubicación, conteo, matriz de "
        "calibre (30 valores), promedio, rutas de fotografías, observaciones, etiquetas, fecha, "
        "estado y bandera de sincronización."
    )
    add_table(
        doc,
        ["Tabla", "Campos clave", "Relaciones"],
        [
            ("roles", "id, nombre, descripción", "1:N con usuarios"),
            ("centros_cultivo", "id, código, nombre, comuna, estado", "1:N con muestras"),
            ("usuarios", "id, email, passwordHash, nombres, rolId, estado", "1:N con muestras"),
            ("muestras", "id, concesión, tren, línea, tramo, conteo, calibre, fotos, estado, timestamp", "FK a centro y operador"),
        ],
        [3.4, 8.8, 4.4],
    )

    doc.add_heading("2.5 Pantallas", level=2)
    add_table(
        doc,
        ["Pantalla", "Propósito", "Verificación realizada"],
        [
            ("Login", "Acceso por correo y contraseña, con validación y selector de visibilidad", "Ingreso con los tres roles"),
            ("Inicio", "Panel con KPIs, CTA de registro y actividad reciente", "Datos reales desde Room"),
            ("Nueva muestra", "Asistente de 3 pasos: ubicación, conteo/calibre y evidencia", "Pasos y validaciones en emulador"),
            ("Historial", "Búsqueda, filtros por estado y período, agrupación por día", "Filtro 'Validadas' y conteos"),
            ("Detalle", "Trazabilidad completa, matriz de calibre, fotos y validación", "Cambio de estado a VALIDADO"),
            ("Perfil", "Foto, alias de terreno, tema y cierre de sesión", "Navegación y edición de alias"),
            ("Personal", "Alta, edición y baja de cuentas con búsqueda y filtros", "Creación de una cuenta nueva"),
            ("Centros y tramos", "CRUD de centros y tramos, con baja lógica y estado", "Pantalla abierta con 3 centros y 8 tramos"),
        ],
        [3.0, 7.6, 6.0],
    )

    doc.add_heading("2.6 Verificación técnica", level=2)
    bullets(
        doc,
        [
            "Build: :app:assembleDebug y :backend compileJava/run finalizan en verde.",
            "Pruebas: 54 unitarias y 2 instrumentadas en verde; 100 % de la lógica evaluable (validadores, filtros, mapeo y repositorio).",
            "Ejecución: login, registro por pasos, cámara, historial, validación, creación de usuario y perfil recorridos en el emulador.",
            "Sincronización: muestra pendiente enviada al backend y marcada en Room; el flujo se repitió sin túnel y al recuperarlo.",
            "Catálogos: migración Room 2 → 3 probada con una base v2 real (conserva centros y usuarios, crea 8 tramos) y CRUD de tramos del backend (201/200/204/404).",
            "Correcciones durante la verificación: superposición en el asistente, error que no se limpiaba, filtros cortados, crash por Toast en I/O y flag -parameters en Spring.",
            "APK: se genera app-debug.apk (build debug, firmada con la clave de depuración).",
        ],
    )
    doc.add_heading("2.7 Backend y API REST", level=2)
    doc.add_paragraph(
        "El repositorio incorpora un proyecto Spring Boot independiente en backend/. Expone "
        "un CRUD completo sobre H2 con validaciones, filtros, cambio de estado y documentación "
        "OpenAPI. La app lo consume con Retrofit y comparte el mismo modelo de muestra que Room."
    )
    add_table(
        doc,
        ["Método", "Ruta", "Uso"],
        [
            ("GET", "/api/muestras?estado=&q=", "Listar con filtros"),
            ("GET", "/api/muestras/{id}", "Consultar una muestra"),
            ("POST", "/api/muestras", "Crear muestra"),
            ("PUT", "/api/muestras/{id}", "Actualizar datos"),
            ("PATCH", "/api/muestras/{id}/estado", "Validar o cambiar estado"),
            ("DELETE", "/api/muestras/{id}", "Eliminar"),
            ("GET/POST", "/api/centros y /api/usuarios", "Catálogos y cuentas"),
        ],
        [2.0, 7.6, 7.0],
    )
    bullets(
        doc,
        [
            "Swagger UI en /swagger-ui.html y OpenAPI en /v3/api-docs.",
            "Datos de demostración: 3 centros, 3 usuarios y 8 muestras.",
            "14 casos de humo ejecutados: CRUD, duplicados (409), validación (400), filtros y Swagger.",
        ],
    )

    doc.add_heading("2.8 Sincronización offline", level=2)
    doc.add_paragraph(
        "Room sigue siendo la fuente de verdad local. WorkManager detecta las muestras con "
        "sincronizado = false y las envía al backend con Retrofit; si el backend ya las tiene "
        "responde 409 y se actualizan con PUT. Los errores de red se reintentan sin bloquear al operador."
    )
    code_block(
        doc,
        "Guardar muestra (Room)  →  WorkManager (red disponible)\n"
        "        →  POST /api/muestras\n"
        "        →  201: marcar sincronizado = 1\n"
        "        →  409: PUT /api/muestras/{id} y marcar\n"
        "        →  error de red/5xx: reintento con backoff",
    )
    bullets(
        doc,
        [
            "Se dispara al guardar, al abrir la app y cada 15 minutos.",
            "El inicio muestra las muestras pendientes y permite forzar la sincronización.",
            "Prueba real: muestra #M-9301 registrada sin túnel (backend 404) y sincronizada al recuperarlo (200 y sincronizado = 1).",
            "En emulador o teléfono por USB se usa adb reverse tcp:8080 tcp:8080.",
            "La URL del backend se configura desde Perfil → Servidor de sincronización (WiFi/LAN o URL pública).",
            "Si vuelve la conexión, WorkManager reintenta de inmediato; la muestra permanece en Room mientras no haya red.",
        ],
    )


    doc.add_heading("2.9 Conteo asistido", level=2)
    doc.add_paragraph(
        "El asistente incluye un diálogo de conteo sobre la primera fotografía: cada toque agrega "
        "una marca numerada, tocar una marca la elimina y el contador se actualiza en vivo. Las "
        "coordenadas se guardan normalizadas (0..1) para que no dependan del tamaño de pantalla."
    )
    bullets(
        doc,
        [
            "Room pasó a versión 2 con migración 1 → 2: columnas marcasConteo y conteoAsistido sin borrar datos.",
            "La migración se verificó sobre una base existente: user_version = 2 y las 10 muestras previas intactas.",
            "El backend y el DTO transportan ambos campos; el detalle muestra el conteo registrado.",
        ],
    )

    doc.add_heading("2.10 Reportes en PDF", level=2)
    doc.add_paragraph(
        "Desde el detalle se genera un informe A4 con PdfDocument, sin dependencias externas: "
        "identificación, ubicación, conteo manual y asistido, matriz de calibre, observaciones, "
        "etiquetas y una página con la primera fotografía. El archivo se comparte con FileProvider."
    )
    bullets(
        doc,
        [
            "Informe real generado en el emulador: 58 KB, cabecera %PDF y selector de Android abierto.",
            "Ejemplo disponible en docs/ejemplos/informe-muestra-M-0001.pdf.",
        ],
    )

    doc.add_heading("2.11 Pruebas y cobertura", level=2)
    doc.add_paragraph(
        "La validación y el filtrado se separaron en objetos puros (SampleValidator, HistoryFilter "
        "y LoginValidator) para probarlos sin Android. Se agregaron JUnit 4, MockK para el DAO y "
        "Compose UI Test; la cobertura se genera con enableUnitTestCoverage."
    )
    bullets(
        doc,
        [
            "54 pruebas unitarias y 2 instrumentadas en verde.",
            "Cobertura de 100 % en validadores, estado, mapeo de resumen, filtros y repositorio.",
            "La cobertura global del módulo es 2,9 % porque incluye pantallas Compose y código generado por Room.",
        ],
    )

    doc.add_heading("2.12 Catálogos de centros y tramos", level=2)
    doc.add_paragraph(
        "Los centros y tramos dejaron de ser texto libre. La pantalla Personal → Centros y tramos "
        "permite crear, editar y desactivar ambos catálogos, y el asistente elige primero el centro "
        "y luego uno de sus tramos activos, completando tren, línea y largo automáticamente."
    )
    bullets(
        doc,
        [
            "Room migra de 2 a 3 con la tabla tramos, su índice y muestras.tramoId, conservando datos.",
            "El backend expone /api/tramos con CRUD completo y las muestras guardan tramoId.",
            "La migración se probó instalando la versión anterior y luego la nueva sobre la misma base.",
        ],
    )

    doc.add_heading("2.13 Dashboard, gráficos y comparación", level=2)
    doc.add_paragraph(
        "El inicio incorpora dos gráficos dibujados con Canvas: distribución de muestras por "
        "estado y evolución del calibre promedio de los últimos siete días. El historial suma "
        "filtros por centro y tren y una tarjeta que compara el período elegido contra el anterior "
        "en cantidad de muestras y calibre promedio."
    )
    bullets(
        doc,
        [
            "Filtros de historial: búsqueda, estado, período, centro y tren, combinables.",
            "Comparación: muestras y calibre del período actual frente al anterior, con variación.",
            "Sin dependencias de gráficos: los componentes viven en ui/designsystem/Charts.kt.",
        ],
    )

    doc.add_heading("2.14 APK firmada", level=2)
    doc.add_paragraph(
        "La entrega incluye un APK release 1.0.14 firmado con un keystore propio, versionName 1.0.14 "
        "y versionCode 15. El keystore y sus credenciales quedan fuera del repositorio, y la firma "
        "se verificó con apksigner antes de instalar el APK en el emulador."
    )
    bullets(
        doc,
        [
            "Firma APK Signature Scheme v2 con clave RSA de 2048 bits.",
            "SHA-256 del APK: 65d2a7d0d794d55b9f961c1c5628ce6fe2b95a228b50d9256d636a957335746c.",
            "Instalado y abierto en el emulador; el build release no es depurable.",
            "Incluye las cuentas demo de operador, analista y administrador en una instalación limpia.",
            "Nuevo logo AquaSample en el login y como icono adaptativo de la app.",
            "Guía de firma y respaldo en docs/RELEASE.md.",
        ],
    )

    paragraph = doc.add_paragraph(
        "Capturas representativas: login, panel de inicio, historial agrupado por día, detalle "
        "con trazabilidad, gestión de personal y perfil. La galería completa está en el Anexo C "
        "y los archivos originales en docs/screenshots/."
    )
    paragraph.paragraph_format.space_before = Pt(6)

def section_3(doc):
    doc.add_heading("3. Matriz de cumplimiento de la pauta", level=1)
    doc.add_paragraph(
        "Criterio de clasificación: Completo significa demostrable hoy en el emulador; "
        "Parcial, que existe una base funcional pero falta una pieza de la pauta; Pendiente, "
        "que no existe implementación en el repositorio."
    )
    rows = [
        ("Interfaz profesional", "Completo", "Material 3, tema claro/oscuro, tokens, navegación, 8 pantallas verificadas", "Pulido menor: transiciones compartidas y skeletons de carga"),
        ("Gestión de muestras", "Completo", "Entidad con centro y tramo; asistente con selección en cascada; CRUD de centros y tramos con baja lógica", "Validar el flujo completo en un teléfono físico"),
        ("Fotografías (CameraX/Coil/Telephoto)", "Completo", "CameraX captura dentro de la app, Coil carga la galería y Telephoto aporta el zoom", "Verificar en teléfono físico y conservar la resolución completa al guardar"),
        ("Conteo asistido", "Completo", "Marcas normalizadas sobre la foto, contador en vivo, corrección y migración Room v2", "Probar el flujo de toques en un teléfono físico"),
        ("Gestión por perfiles", "Completo", "Login por rol; operador registra; analista/admin validan; CRUD de personal", "Los permisos se aplican en UI; falta reforzarlos en backend"),
        ("Historial y dashboard", "Completo", "Búsqueda; filtros por estado, período, centro y tren; agrupación por día; tarjeta de comparación y gráficos en el inicio", "Validar los gráficos en un teléfono físico"),
        ("Reportabilidad (PDF/Excel)", "Completo", "PDF A4 por muestra y consolidado de jornada; Excel/CSV con los filtros visibles", "—"),
        ("Microservicios Spring Boot", "Completo", "API Spring Boot con JPA, H2, CRUD, filtros, validación, Swagger y datos demo", "Autenticación por rol, migraciones versionadas y despliegue"),
        ("Consumo de APIs externas", "Completo", "Open-Meteo Marine y forecast de viento con Retrofit; oleaje, temperatura y viento guardados en Room y backend", "Coordenadas aproximadas por centro"),
        ("Persistencia offline + sync", "Completo", "Room + WorkManager + Retrofit; prueba offline → online con marca de sincronización", "Resolución de conflictos con varios dispositivos y reintentos avanzados"),
        ("Pruebas y cobertura", "Parcial", "54 unitarias y 2 instrumentadas; 100 % de la lógica evaluable", "Cobertura global y pruebas de ViewModels/UI"),
        ("APK firmada", "Completo", "Release 1.0.14 firmada con esquema v2, hash SHA-256, instalada y verificada en el emulador", "Publicar en tienda solo si se solicita"),
        ("Documentación y colaboración", "Parcial", "Repositorio público, README, DESIGN.md, RELEASE.md, informe, backend README y 20 capturas", "Swagger publicado, tablero Trello y guía de despliegue"),
        ("Modificación en tiempo real", "Parcial", "Un campo nuevo se propaga por entidad, DAO, ViewModel y Compose", "Migración Room no destructiva y contrato con backend para el cambio extremo a extremo"),
    ]
    add_table(
        doc,
        ["Ítem de la pauta", "Estado", "Evidencia actual", "Brecha principal"],
        rows,
        [3.2, 2.1, 6.0, 5.3],
        status_column=1,
        size=8.5,
    )


def section_4(doc):
    doc.add_heading("4. Lo que falta por implementar", level=1)
    doc.add_paragraph(
        "Con la API externa ya integrada, las brechas restantes se concentran en la cobertura, "
        "el despliegue multidispositivo y el cierre de entrega."
    )
    gaps = [
        (
            "4.1 Despliegue público y almacenamiento persistente",
            "El backend corre en el PC y el APK apunta a localhost por defecto; las fotos ya se suben y descargan, pero la base H2 y la carpeta uploads viven en el equipo local.",
            "Desplegar el backend con HTTPS y un volumen persistente para la base y las fotos, y configurar la URL desde Perfil.",
            [
                "La app sincroniza por datos móviles con una URL pública.",
                "Las muestras y fotos sobreviven a un reinicio del servidor.",
                "El analista ve en su teléfono las fotos creadas por el operador.",
            ],
        ),
        (
            "4.2 Cobertura global y pruebas de ViewModels/UI",
            "Hay 54 pruebas unitarias y 2 instrumentadas; la lógica evaluable está al 100 %, pero la cobertura global del módulo es 2,9 % por el peso de Compose y el código generado por Room.",
            "Agregar pruebas de ViewModels con coroutines test, pruebas de Compose para login, registro y filtros, y MockMvc para el backend. Definir un umbral por paquete o por capa.",
            [
                "Cobertura global medida y umbral acordado con el docente.",
                "Flujos críticos cubiertos por pruebas de UI.",
                "Reporte de cobertura adjunto a la entrega.",
            ],
        ),
        (
            "4.3 Publicación en tienda (opcional)",
            "El APK release 1.0.14 ya está firmado, verificado e instalado; no existe ficha de tienda ni cuenta de desarrollador.",
            "Si se solicita publicar, crear la ficha, la política de privacidad y la cuenta de Google Play, y subir el APK o AAB firmado.",
            [
                "Ficha con capturas, descripción y política de privacidad.",
                "APK/AAB firmado con el mismo keystore.",
                "Revisión de la tienda aprobada.",
            ],
        ),
        (
            "4.4 Autenticación y migraciones del backend",
            "La API es abierta, no valida roles y usa ddl-auto=update; Room ya migra de 1 a 2 conservando datos.",
            "Agregar Spring Security o JWT con roles, migraciones Flyway en el backend y validar los permisos también en el servidor.",
            [
                "Un operador no puede validar muestras desde la API.",
                "Un cambio de esquema del backend conserva los datos.",
                "Las migraciones están versionadas y probadas.",
            ],
        ),
        (
            "4.5 Documentación y colaboración",
            "Existen README, DESIGN.md, informe actualizado, backend README y ejemplos; faltan Swagger publicado y evidencia de Trello.",
            "Publicar Swagger, mantener el tablero Trello con historias y responsables, y documentar el despliegue del backend.",
            [
                "README que permita levantar app y backend desde cero.",
                "Swagger accesible y enlazado desde el README.",
                "Trello con planificación y estados reales.",
            ],
        ),
        (
            "4.6 Modificación en tiempo real (ensayo)",
            "La arquitectura permite agregar un campo y propagarlo hasta el backend; la migración de Room ya existe.",
            "Preparar un cambio de ejemplo (peso de muestra): columna con migración, campo en DTO y backend, formulario y detalle. Ensayarlo cronometrado para la defensa.",
            [
                "Cambio aplicado sin perder datos existentes.",
                "Backend y app actualizados en menos de 10 minutos.",
                "Prueba del flujo completo tras el cambio.",
            ],
        ),
    ]

    for title, current, requirement, criteria in gaps:
        doc.add_heading(title, level=2)
        labeled(doc, "Estado actual.", current)
        labeled(doc, "Qué se requiere.", requirement)
        labeled(doc, "Criterio de aceptación.", "")
        bullets(doc, criteria)

    doc.add_heading("4.7 Deuda técnica y riesgos detectados", level=2)
    bullets(
        doc,
        [
            "Cobertura global baja (2,9 %): presentar la métrica por lógica evaluable y acordar el umbral.",
            "Contraseñas en texto plano: el campo se llama passwordHash pero almacena la clave; migrar a BCrypt o Argon2.",
            "Perfil solo en memoria: alias y foto se pierden al cerrar la app; persistirlos en DataStore.",
            "Backend sin autenticación: cualquier cliente puede modificar o validar muestras.",
            "Cámara y conteo compilan y la migración está probada, pero falta verificar los toques en un teléfono físico.",
            "Resolución de conflictos: la sincronización asume un solo dispositivo por muestra.",
        ],
    )


def section_5(doc):
    doc.add_heading("5. Roadmap sugerido", level=1)
    doc.add_paragraph(
        "Con los ítems de mayor peso ya cerrados, la secuencia apunta a completar el frente externo, "
        "subir la cobertura y dejar la entrega lista."
    )
    add_table(
        doc,
        ["Fase", "Alcance", "Esfuerzo", "Habilita"],
        [
            ("1", "Despliegue público del backend y fotos multidispositivo", "2–3 días", "Operación real"),
            ("2", "Pruebas de ViewModels y Compose + umbral de cobertura", "2–3 días", "Cobertura global"),
            ("3", "Trello, Swagger publicado y guía de despliegue", "1–2 días", "Entrega y documentación"),
            ("4", "Spring Security/JWT y Flyway", "2 días", "Backend endurecido"),
        ],
        [1.4, 8.2, 2.4, 4.6],
    )
    doc.add_paragraph(
        "Total estimado: 6 a 9 días de trabajo efectivo, con el frente externo en paralelo."
    )


def section_6(doc):
    doc.add_heading("6. Riesgos para la defensa", level=1)
    bullets(
        doc,
        [
            "La API externa ya está integrada; el viento queda nulo porque la API marina no lo entrega y las coordenadas son fijas.",
            "La cobertura global es 2,9 %; si el docente exige 90 % global, habrá que instrumentar ViewModels y pantallas o acordar la métrica por lógica evaluable.",
            "La APK release ya está firmada y verificada; si se pide publicación, crear la cuenta de tienda y la ficha.",
            "El backend no tiene autenticación: no se puede demostrar que un operador no valide muestras.",
            "La cámara y el conteo están implementados y compilan, pero la verificación de toques en teléfono físico sigue pendiente.",
            "Falta el tablero Trello y publicar Swagger; el repositorio ya está público con main actualizado.",
        ],
    )


def section_7(doc):
    doc.add_heading("7. Anexos", level=1)
    doc.add_heading("A. Estructura del proyecto", level=2)
    code_block(
        doc,
        "app/src/main/java/com/example/aquasample/\n"
        "├── AquaApplication.kt / MainActivity.kt\n"
        "├── di/AppContainer.kt\n"
        "├── data/\n"
        "│   ├── local/{dao, entity, database, converter}\n"
        "│   ├── remote/{ApiClient, AquaSampleApi, MuestraDto}\n"
        "│   ├── sync/{SampleSyncWorker, SyncScheduler}\n"
        "│   ├── report/PdfReportGenerator.kt\n"
        "│   ├── repository/{Muestra, Usuario, CentroCultivo, Profile}Repository.kt\n"
        "│   └── session/{SessionManager, UserSession}.kt\n"
        "└── ui/\n"
        "    ├── designsystem/   (componentes y tokens visuales)\n"
        "    ├── theme/          (color, tipografía, formas, tema)\n"
        "    ├── camera/         (CameraX)\n"
        "    ├── login/ home/ sample/ history/ detail/ profile/ admin/\n"
        "    ├── navigation/     (MainScreen y destinos)\n"
        "    └── model/          (SampleSummary compartido)\n"
        "\n"
        "app/src/test/          39 pruebas unitarias (JUnit + MockK)\n"
        "app/src/androidTest/   2 pruebas instrumentadas de Compose\n"
        "\n"
        "backend/\n"
        "├── src/main/java/cl/aquasample/backend/{api, config, domain, repository, service}\n"
        "├── src/main/resources/application.properties\n"
        "└── README.md\n"
        "\n"
        "docs/\n"
        "├── DESIGN.md\n"
        "├── informe/AquaSample-Estado-del-Sistema.docx\n"
        "├── ejemplos/  (informe PDF de ejemplo)\n"
        "└── screenshots/  (23 capturas del sistema)",
    )

    doc.add_heading("B. Comandos de verificación", level=2)
    code_block(
        doc,
        "# Compilar APK de depuración\n"
        ".\\gradlew.bat :app:assembleDebug\n"
        "\n"
        "# Compilar APK release firmada (requiere keystore.properties)\n"
        ".\\gradlew.bat :app:assembleRelease\n"
        "\n"
        "# Ejecutar pruebas unitarias\n"
        ".\\gradlew.bat :app:testDebugUnitTest\n"
        "\n"
        "# Instalar en emulador o teléfono conectado\n"
        "adb install -r app\\build\\outputs\\apk\\debug\\app-debug.apk\n"
        "\n"
        "# Requiere JAVA_HOME apuntando al JDK de Android Studio (JBR 25)\n"
        "C:\\Program Files\\Android\\Android Studio\\jbr",
    )

    doc.add_heading("C. Galería de capturas", level=2)
    doc.add_paragraph(
        "Las imágenes se obtuvieron del emulador Pixel 7 Pro con datos de demostración. "
        "Los nombres y correos que aparecen son ficticios."
    )
    gallery = [
        ("01-login.png", "Login con identidad ALDEMAR"),
        ("02-home.png", "Panel de inicio con indicadores"),
        ("09-historial.png", "Historial agrupado por día"),
        ("10-detalle.png", "Detalle con trazabilidad y matriz"),
        ("13-personal.png", "Gestión de personal"),
        ("15-perfil.png", "Perfil y preferencias"),
    ]
    gallery_table = doc.add_table(rows=3, cols=2)
    gallery_table.autofit = False
    borders(gallery_table, "E2E8F0")
    for index, (filename, caption) in enumerate(gallery):
        cell = gallery_table.rows[index // 2].cells[index % 2]
        cell.width = Cm(8.2)
        cell.text = ""
        paragraph = cell.paragraphs[0]
        paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
        paragraph.add_run().add_picture(str(SHOTS / filename), width=Cm(6.4))
        caption_paragraph = cell.add_paragraph()
        caption_paragraph.alignment = WD_ALIGN_PARAGRAPH.CENTER
        caption_run = caption_paragraph.add_run(caption)
        caption_run.italic = True
        caption_run.font.size = Pt(8)
        caption_run.font.color.rgb = MUTED

    doc.add_heading("D. Inventario de archivos clave", level=2)
    add_table(
        doc,
        ["Archivo", "Rol"],
        [
            ("ui/designsystem/", "18 componentes reutilizables del sistema de diseño"),
            ("ui/theme/", "Paleta, tipografía, formas, tokens y tema claro/oscuro"),
            ("ui/navigation/MainScreen.kt", "Grafo de navegación, barra inferior por rol y transiciones"),
            ("ui/sample/NuevaMuestraScreen.kt", "Asistente de registro en tres pasos"),
            ("ui/sample/SampleViewModel.kt", "Validación por pasos, promedio y persistencia de fotos"),
            ("ui/history/HistoryViewModel.kt", "Búsqueda, filtros, conteos y agrupación por fecha"),
            ("ui/detail/SampleDetailViewModel.kt", "Carga del detalle y validación de supervisor"),
            ("ui/admin/AdminViewModel.kt", "CRUD de cuentas y mapeo rol/estado"),
            ("data/local/dao/MuestraDao.kt", "Consultas de muestras, joins y cambio de estado"),
            ("data/local/database/AquaSampleDatabase.kt", "Room, convertidores y datos iniciales"),
            ("di/AppContainer.kt", "Inyección manual de base de datos y repositorios"),
            ("docs/DESIGN.md", "Decisiones de diseño, arquitectura y límites conocidos"),
        ],
        [7.2, 9.4],
    )

    doc.add_heading("E. Glosario mínimo", level=2)
    add_table(
        doc,
        ["Término", "Significado en este proyecto"],
        [
            ("Muestra", "Registro de terreno de un tramo: ubicación, conteo, calibre, fotos y observaciones"),
            ("Calibre", "Medición del tamaño de los individuos; se registra en una matriz de 30 celdas"),
            ("Estado de revisión", "PENDIENTE, COMPLETADO o VALIDADO; el supervisor aplica VALIDADO"),
            ("Sincronización", "Envío de una muestra local al backend; hoy no implementado"),
            ("Offline-first", "La app funciona sin red y sincroniza después; hoy solo la primera parte"),
        ],
        [4.0, 12.6],
    )


def build():
    doc = Document()
    section = doc.sections[0]
    section.page_width = Cm(21)
    section.page_height = Cm(29.7)
    section.top_margin = Cm(2.2)
    section.bottom_margin = Cm(2.0)
    section.left_margin = Cm(2.2)
    section.right_margin = Cm(2.2)
    section.different_first_page_header_footer = True
    footer(section)

    normal = doc.styles["Normal"]
    normal.font.name = "Calibri"
    normal.font.size = Pt(10.5)
    normal.font.color.rgb = INK
    normal.paragraph_format.space_after = Pt(6)
    normal.paragraph_format.line_spacing = 1.15

    for name, size, color, before, after in (
        ("Heading 1", 17, OCEAN, 18, 8),
        ("Heading 2", 12.5, RGBColor(0x0E, 0x5A, 0x7A), 12, 4),
    ):
        style = doc.styles[name]
        style.font.name = "Calibri"
        style.font.size = Pt(size)
        style.font.bold = True
        style.font.color.rgb = color
        style.paragraph_format.space_before = Pt(before)
        style.paragraph_format.space_after = Pt(after)
        style.paragraph_format.keep_with_next = True

    cover(doc)
    doc.add_page_break()
    summary(doc)
    section_1(doc)
    section_2(doc)
    section_3(doc)
    section_4(doc)
    section_5(doc)
    section_6(doc)
    section_7(doc)

    doc.save(OUTPUT)
    print(f"OK: {OUTPUT}")


if __name__ == "__main__":
    build()
