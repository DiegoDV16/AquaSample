"""Genera el informe Word de estado del sistema AquaSample."""

from pathlib import Path

from docx import Document
from docx.enum.table import WD_ALIGN_VERTICAL, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

BASE = Path(r"C:\Users\diego\OneDrive\Documents\GitHub\AquaSample")
SHOTS = BASE / "docs" / "screenshots"
OUTPUT = BASE / "docs" / "AquaSample-Estado-del-Sistema.docx"

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
        ("Commit auditado", "758b10b · feat(ui): rediseño completo con Material 3"),
        ("Plataforma", "Android · Kotlin 2.0.21 · Jetpack Compose · Material 3"),
        ("Verificación", "Gradle assembleDebug + pruebas en emulador Pixel 7 Pro (Android 37)"),
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
        "AquaSample es hoy una aplicación Android funcional y con una interfaz profesional: "
        "permite iniciar sesión por rol, registrar una muestra en tres pasos, consultarla, "
        "filtrarla, validarla como supervisor y administrar las cuentas del equipo. Toda la "
        "información se guarda en Room, en el dispositivo, y el proyecto compila sin errores."
    )
    doc.add_paragraph(
        "Frente a los 14 criterios de la pauta, 2 están completos, 5 son parciales y 7 siguen "
        "pendientes. Las brechas críticas son el backend, el consumo de API externa, la "
        "sincronización offline, la cámara con CameraX y las pruebas con cobertura. Sin esas "
        "piezas, varios ítems quedan en cero aunque la aplicación se vea terminada."
    )
    doc.add_heading("Cifras clave del sistema", level=2)
    add_table(
        doc,
        ["Indicador", "Valor actual"],
        [
            ("Pantallas de usuario", "7 (Login, Inicio, Nueva muestra, Historial, Detalle, Perfil, Personal)"),
            ("Código Kotlin", "54 archivos · 6.982 líneas en app/src/main"),
            ("Capa de datos", "4 tablas Room · 7 ViewModels · 4 repositorios"),
            ("Backend y endpoints", "0 (el repositorio solo contiene el módulo :app)"),
            ("Pruebas automatizadas", "2 de plantilla (1 unitaria, 1 instrumentada); cobertura no medida"),
            ("Documentación", "docs/DESIGN.md + 15 capturas en docs/screenshots"),
            ("Build verificado", "assembleDebug y testDebugUnitTest en verde (commit 758b10b)"),
        ],
        [4.6, 12.0],
    )
    doc.add_heading("Semáforo de la pauta", level=2)
    add_table(
        doc,
        ["Estado", "Cantidad", "Ítems"],
        [
            ("Completo", "2", "Interfaz profesional; gestión por perfiles"),
            ("Parcial", "5", "Gestión de muestras; historial/dashboard; APK firmada; documentación y colaboración; modificación en tiempo real"),
            ("Pendiente", "7", "Fotografías con CameraX/Coil/Telephoto; conteo asistido; reportabilidad; backend Spring Boot; API externa; offline con WorkManager; pruebas y cobertura"),
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
            "Fuente de verdad: código en la rama main, commit 758b10b.",
            "Compilación: :app:assembleDebug y :app:testDebugUnitTest con Gradle.",
            "Ejecución: emulador Pixel 7 Pro (1440 × 3120, Android 37), app instalada por ADB.",
            "Evidencia visual: 15 capturas en docs/screenshots/ incluidas en este informe.",
            "Documentación de diseño: docs/DESIGN.md (tokens, componentes, navegación y arquitectura).",
        ],
    )
    paragraph = doc.add_paragraph(
        "Lo que este informe no cubre: rendimiento en un teléfono físico, pruebas de "
        "aceptación con usuarios reales, backend (no existe en el repositorio), tablero de "
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
        ],
        [3.0, 7.6, 6.0],
    )

    doc.add_heading("2.6 Verificación técnica", level=2)
    bullets(
        doc,
        [
            "Build: :app:assembleDebug finaliza con BUILD SUCCESSFUL en todas las fases del rediseño.",
            "Pruebas: :app:testDebugUnitTest en verde; existe una prueba unitaria de plantilla y una instrumentada, ambas sin cobertura de lógica real.",
            "Ejecución: flujos de login, registro por pasos, historial, validación de supervisor, creación de usuario y perfil recorridos en el emulador.",
            "Correcciones durante la verificación: superposición de contenido en el asistente, error que no se limpiaba al editar, filtros de historial cortados y un crash por Toast lanzado desde el hilo de I/O.",
            "APK: se genera app-debug.apk (build debug, firmada con la clave de depuración).",
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
        ("Interfaz profesional", "Completo", "Material 3, tema claro/oscuro, tokens, navegación, 7 pantallas verificadas", "Pulido menor: transiciones compartidas y skeletons de carga"),
        ("Gestión de muestras", "Parcial", "Entidad con concesión, tren, línea, tramo, fechas, observaciones y estado; asistente de registro", "Centros solo precargados; tren y línea son texto; sin CRUD de centros ni entidad de tramo"),
        ("Fotografías (CameraX/Coil/Telephoto)", "Pendiente", "Cámara del sistema y visor con zoom; las rutas se guardan en disco", "CameraX, archivo en resolución completa, Coil, Telephoto y permisos"),
        ("Conteo asistido", "Pendiente", "Conteo manual y matriz de calibre 6×5 con promedio automático", "Marcar choritos sobre la foto, contar, corregir y persistir las marcas"),
        ("Gestión por perfiles", "Completo", "Login por rol; operador registra; analista/admin validan; CRUD de personal", "Los permisos se aplican en UI; falta reforzarlos en backend"),
        ("Historial y dashboard", "Parcial", "Búsqueda, filtros por estado y período, agrupación por día, detalle y KPIs", "Gráficos, comparación de períodos y filtros por centro/tren"),
        ("Reportabilidad (PDF/Excel)", "Pendiente", "—", "Generación y exportación de informes, plantilla y compartir"),
        ("Microservicios Spring Boot", "Pendiente", "—", "Entidades, endpoints CRUD, base de datos y Swagger/OpenAPI"),
        ("Consumo de APIs externas", "Pendiente", "—", "Retrofit/OkHttp, permiso INTERNET y Open-Meteo Marine con manejo de errores"),
        ("Persistencia offline + sync", "Pendiente", "Room local funciona; la muestra nace PENDIENTE y sin sincronizar", "WorkManager, cola de pendientes, reintentos, conflictos y endpoint"),
        ("Pruebas y cobertura", "Pendiente", "Build y pruebas de plantilla en verde", "MockK, Compose UI Test, JaCoCo/Kover y cobertura superior al 90 % de la lógica"),
        ("APK firmada", "Parcial", "APK debug generada e instalada en el emulador", "Keystore, signingConfig de release, versionCode y APK firmada en teléfono"),
        ("Documentación y colaboración", "Parcial", "Repositorio público, historial de ramas/PRs, DESIGN.md y 15 capturas", "README, Swagger publicado, tablero Trello y guía de instalación"),
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
        "Las brechas se ordenan por impacto en la evaluación: primero lo que hoy deja ítems "
        "en cero, después lo que mejora la experiencia y finalmente la entrega."
    )
    gaps = [
        (
            "4.1 Backend Spring Boot y Swagger",
            "El repositorio solo contiene el módulo :app; no existe backend, endpoints, base de datos remota ni documentación OpenAPI.",
            "Crear un proyecto Spring Boot (Gradle o Maven) con entidades Muestra, Centro y Usuario, repositorios JPA, servicios y controladores REST para el CRUD. Agregar springdoc-openapi para Swagger UI, una base PostgreSQL o H2 y datos iniciales coherentes con la app.",
            ["Endpoints documentados y probables desde Swagger UI.", "El CRUD responde con códigos HTTP correctos y validaciones.", "La app puede leer la misma estructura que hoy guarda Room."],
        ),
        (
            "4.2 Consumo de API externa con Retrofit",
            "No hay Retrofit, OkHttp ni permiso INTERNET en el manifiesto. La app no consulta condiciones marinas.",
            "Incorporar Retrofit + converter de Kotlinx Serialization o Gson, declarar android.permission.INTERNET y consumir Open-Meteo Marine (oleaje, temperatura del mar, viento). Mostrar el resultado en el panel de inicio con estado de carga, error y reintento.",
            ["La app muestra datos marinos reales y su hora de actualización.", "Si no hay red, se informa el error y no se bloquea el registro.", "La llamada está separada en un repositorio y es testeable con MockWebServer."],
        ),
        (
            "4.3 Persistencia offline real y WorkManager",
            "Room guarda localmente y el registro funciona sin red, pero no existe sincronización: la bandera sincronizado queda en false y no hay cola de envío.",
            "Definir un estado de sincronización (pendiente, en curso, sincronizada, error), una tabla o consulta de pendientes y un Worker periódico con restricción de red. Al recuperar conectividad, enviar las muestras al backend, reintentar con backoff y resolver duplicados por id.",
            ["Registrar una muestra en modo avión y verla como pendiente.", "Al volver la red, el Worker la envía y el estado cambia sin intervención.", "Los fallos se reintentan y quedan visibles para el operador."],
        ),
        (
            "4.4 Cámara, Coil y zoom Telephoto",
            "Se usa TakePicturePreview, que entrega una miniatura y delega en la cámara del sistema. No hay CameraX, Coil ni Telephoto; las imágenes se muestran con BitmapFactory.",
            "Migrar a CameraX (Preview + ImageCapture) guardando un archivo JPEG en resolución completa, mostrar las fotos con Coil desde su ruta y habilitar zoom de imagen con Telephoto. Declarar el permiso CAMERA y manejar la denegación.",
            ["Captura dentro de la app con vista previa y archivo persistente.", "Galería del detalle carga con Coil y soporta zoom fluido.", "La foto sobrevive al cierre y se ve en el detalle."],
        ),
        (
            "4.5 Conteo asistido sobre la fotografía",
            "El conteo es manual (campo numérico) y el calibre se ingresa en una matriz; no se marcan individuos sobre la imagen.",
            "Agregar un lienzo sobre la foto para marcar y desmarcar puntos, un contador derivado de las marcas, corrección manual y persistencia de las coordenadas (por ejemplo, JSON en la muestra o tabla hija).",
            ["Marcar N individuos actualiza el contador en vivo.", "Se puede eliminar una marca y corregir el total.", "Las marcas se recuperan al abrir el detalle."],
        ),
        (
            "4.6 Reportabilidad en PDF o Excel",
            "No existen dependencias ni pantallas de exportación.",
            "Generar un informe de muestra o de jornada con encabezado ALDEMAR, datos de ubicación, conteo, calibre y fotografías. Usar PdfDocument/iText para PDF o Apache POI para Excel, guardar en almacenamiento de la app y compartir con FileProvider.",
            ["Un botón exporta y abre el compartir del sistema.", "El archivo incluye los datos y al menos una foto.", "El nombre del archivo identifica muestra y fecha."],
        ),
        (
            "4.7 Analítica: gráficos y comparación de períodos",
            "El historial filtra y agrupa, y el inicio muestra KPIs, pero no hay gráficos ni comparación entre semanas o centros.",
            "Incorporar una librería de gráficos (Vico, MPAndroidChart o Canvas propio) para evolución de calibre y conteo, y un selector de período que compare dos rangos. Mantener el cálculo en el ViewModel para poder testearlo.",
            ["Un gráfico de evolución por fecha con datos reales.", "Comparación de dos períodos con variación porcentual.", "Filtros por centro y tren además de estado y fecha."],
        ),
        (
            "4.8 Pruebas unitarias, instrumentadas y cobertura",
            "Solo hay pruebas de plantilla (una unitaria y una instrumentada) y no se mide cobertura.",
            "Tests de ViewModels y validaciones con JUnit y MockK, pruebas de Compose para los flujos críticos, y JaCoCo o Kover con umbral. Priorizar lógica evaluable: validación por pasos, filtros, promedio de calibre y mapeos.",
            ["Cobertura superior al 90 % de la lógica evaluable.", "Reporte de cobertura generado y adjunto a la entrega.", "Pruebas de UI para login, registro y filtro de historial."],
        ),
        (
            "4.9 APK firmada",
            "Solo se genera el APK de depuración, firmado con la clave debug; no hay signingConfig ni build release.",
            "Crear un keystore, configurar signingConfigs para release, definir versionCode/versionName y generar el APK firmado. Instalarlo en un teléfono físico y verificar la firma con apksigner.",
            ["APK release instalada y funcionando en el teléfono de la defensa.", "Firma verificable y documentada (sin publicar la contraseña).", "Versión y nombre coherentes con la entrega."],
        ),
        (
            "4.10 Documentación y colaboración",
            "Existe DESIGN.md y capturas, y el repositorio es público con ramas y PRs. Faltan README, Swagger publicado y evidencia de Trello.",
            "Escribir un README con arquitectura, requisitos, instalación y credenciales de prueba; publicar Swagger del backend; mantener el tablero Trello con historias y responsables; documentar commits por integrante.",
            ["README que permita levantar app y backend desde cero.", "Swagger accesible y enlazado desde el README.", "Trello con planificación y estados reales."],
        ),
        (
            "4.11 Modificación en tiempo real (ensayo)",
            "La arquitectura permite agregar un campo y propagarlo, pero no hay backend y la base usa fallbackToDestructiveMigration, lo que borra datos al cambiar el esquema.",
            "Preparar un cambio de ejemplo (por ejemplo, peso de muestra o calibre promedio): columna nueva con migración de Room, DTO y endpoint en backend, campo en el formulario y en el detalle. Ensayarlo cronometrado para la defensa.",
            ["Cambio aplicado sin perder datos existentes.", "Backend y app actualizados en menos de 10 minutos.", "Prueba del flujo completo tras el cambio."],
        ),
    ]
    for title, current, requirement, criteria in gaps:
        doc.add_heading(title, level=2)
        labeled(doc, "Estado actual.", current)
        labeled(doc, "Qué se requiere.", requirement)
        labeled(doc, "Criterio de aceptación.", "")
        bullets(doc, criteria)

    doc.add_heading("4.12 Deuda técnica y riesgos detectados", level=2)
    bullets(
        doc,
        [
            "fallbackToDestructiveMigration: cualquier cambio de esquema borra las muestras; debe reemplazarse por migraciones explícitas antes de la defensa.",
            "Contraseñas en texto plano: el campo se llama passwordHash pero almacena la clave; migrar a BCrypt o Argon2.",
            "Perfil solo en memoria: alias y foto se pierden al cerrar la app; persistirlos en DataStore.",
            "Fotografías: la persistencia está implementada, pero no se verificó de punta a punta guardar una muestra con foto real.",
            "Sin permiso INTERNET: cualquier integración de red requiere modificar el manifiesto y, si se usa HTTP local, una configuración de seguridad de red.",
            "Estados de sincronización: hoy la muestra nueva queda PENDIENTE y sincronizado = false, pero no hay proceso que la envíe.",
        ],
    )


def section_5(doc):
    doc.add_heading("5. Roadmap sugerido", level=1)
    doc.add_paragraph(
        "La secuencia prioriza desbloquear ítems de la pauta antes de pulir. Los esfuerzos son "
        "estimaciones de trabajo efectivo y suponen dedicación parcial del equipo."
    )
    add_table(
        doc,
        ["Fase", "Alcance", "Esfuerzo", "Habilita"],
        [
            ("1", "Backend Spring Boot: entidades, CRUD, base de datos y Swagger", "3–4 días", "Microservicios + base para sync"),
            ("2", "Retrofit + Open-Meteo + permiso INTERNET", "1–2 días", "Consumo de API externa"),
            ("3", "WorkManager, cola de pendientes y sincronización con backend", "2–3 días", "Persistencia offline real"),
            ("4", "CameraX + Coil + Telephoto y conteo asistido sobre foto", "3–4 días", "Fotografías y conteo asistido"),
            ("5", "Gráficos, comparación de períodos y exportación PDF/Excel", "2–3 días", "Dashboard y reportabilidad"),
            ("6", "MockK, Compose UI Test, cobertura, APK release firmada y README", "2–3 días", "Pruebas, firma y documentación"),
        ],
        [1.4, 8.2, 2.4, 4.6],
    )
    doc.add_paragraph(
        "Total estimado: 13 a 19 días de trabajo efectivo. La ruta crítica es backend → "
        "sincronización; cámara y reportes pueden avanzar en paralelo si hay dos integrantes."
    )


def section_6(doc):
    doc.add_heading("6. Riesgos para la defensa", level=1)
    bullets(
        doc,
        [
            "Si el docente pide un cambio de campo y no hay migración de Room, se pierden las muestras delante de él.",
            "Sin backend, los ítems de microservicios, API externa y sincronización quedan en cero aunque la app se vea completa.",
            "La demo offline actual solo prueba que Room escribe; no demuestra sincronización, que es lo que exige la pauta.",
            "Las capturas y los datos de demo usan nombres ficticios; revisar que no haya datos personales reales en el repositorio público.",
            "El APK de depuración no sirve como entrega firmada; instalar el release en el teléfono antes de la presentación.",
            "El repositorio debe estar público y el enlace enviado antes de la defensa; el commit auditado es 758b10b.",
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
        "│   ├── repository/{Muestra, Usuario, CentroCultivo, Profile}Repository.kt\n"
        "│   └── session/{SessionManager, UserSession}.kt\n"
        "└── ui/\n"
        "    ├── designsystem/   (componentes y tokens visuales)\n"
        "    ├── theme/          (color, tipografía, formas, tema)\n"
        "    ├── login/ home/ sample/ history/ detail/ profile/ admin/\n"
        "    ├── navigation/     (MainScreen y destinos)\n"
        "    └── model/          (SampleSummary compartido)\n"
        "\n"
        "docs/\n"
        "├── DESIGN.md\n"
        "└── screenshots/  (15 capturas del sistema)",
    )

    doc.add_heading("B. Comandos de verificación", level=2)
    code_block(
        doc,
        "# Compilar APK de depuración\n"
        ".\\gradlew.bat :app:assembleDebug\n"
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
