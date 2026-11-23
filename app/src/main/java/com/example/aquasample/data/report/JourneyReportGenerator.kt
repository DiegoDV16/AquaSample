package com.example.aquasample.data.report

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.aquasample.data.local.entity.MuestraConDetalle
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reporte consolidado de varias muestras (jornada o centro) en PDF o CSV. */
class JourneyReportGenerator(private val context: Context) {

    suspend fun generatePdf(samples: List<MuestraConDetalle>): File = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "reportes").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val file = File(directory, "AquaSample_Jornada_$stamp.pdf")

        val document = PdfDocument()
        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(842, 595, pageNumber).create()
        )
        var canvas = page.canvas
        var y = drawHeader(canvas, pageNumber, samples.size)

        val rowPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 8f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.5f
        }

        samples.forEachIndexed { index, detalle ->
            if (y > 545) {
                drawFooter(canvas, pageNumber)
                document.finishPage(page)
                pageNumber++
                page = document.startPage(
                    PdfDocument.PageInfo.Builder(842, 595, pageNumber).create()
                )
                canvas = page.canvas
                y = drawHeader(canvas, pageNumber, samples.size)
            }

            val muestra = detalle.muestra
            val values = listOf(
                muestra.id,
                muestra.fechaHora,
                detalle.centroNombre ?: muestra.concesion,
                "${muestra.tren} / ${muestra.linea}",
                muestra.conteoManual,
                String.format(Locale.US, "%.2f", muestra.averageCalibre),
                muestra.estado,
                detalle.operadorNombreCompleto ?: muestra.operatorAlias
            )
            val xs = floatArrayOf(20f, 90f, 230f, 390f, 500f, 555f, 630f, 710f)
            values.forEachIndexed { column, value ->
                canvas.drawText(value.take(28), xs[column], y, rowPaint)
            }
            if (index % 2 == 1) {
                canvas.drawLine(20f, y + 4f, 822f, y + 4f, linePaint)
            }
            y += 18f
        }

        drawFooter(canvas, pageNumber)
        document.finishPage(page)
        file.outputStream().use { document.writeTo(it) }
        document.close()
        file
    }

    suspend fun generateCsv(samples: List<MuestraConDetalle>): File = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "reportes").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val file = File(directory, "AquaSample_Jornada_$stamp.csv")
        file.writeText(JourneyCsv.build(samples), Charsets.UTF_8)
        file
    }

    private fun drawHeader(canvas: android.graphics.Canvas, page: Int, total: Int): Float {
        val title = Paint().apply {
            color = Color.rgb(0, 90, 140)
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtitle = Paint().apply {
            color = Color.DKGRAY
            textSize = 9f
            isAntiAlias = true
        }
        val header = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawText("AquaSample · Reporte consolidado", 20f, 28f, title)
        canvas.drawText(
            "$total muestras · generado ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}",
            20f,
            45f,
            subtitle
        )

        canvas.drawRect(20f, 58f, 822f, 76f, Paint().apply { color = Color.rgb(0, 90, 140) })
        val headers = listOf("ID", "Fecha", "Centro", "Tren / Línea", "Conteo", "Calibre", "Estado", "Operador")
        val xs = floatArrayOf(20f, 90f, 230f, 390f, 500f, 555f, 630f, 710f)
        headers.forEachIndexed { index, text -> canvas.drawText(text, xs[index], 70f, header) }
        return 92f
    }

    private fun drawFooter(canvas: android.graphics.Canvas, page: Int) {
        val paint = Paint().apply {
            color = Color.GRAY
            textSize = 7f
            isAntiAlias = true
        }
        canvas.drawText("AquaSample · página $page", 20f, 585f, paint)
    }
}

/** Genera el CSV con BOM UTF-8 y separador ; para abrirlo directamente en Excel. */
object JourneyCsv {

    fun build(samples: List<MuestraConDetalle>): String {
        val builder = StringBuilder()
        builder.append('\uFEFF')
        builder.append("ID;Fecha;Centro;Tren;Linea;Conteo;Calibre promedio (mm);Estado;Operador;Observaciones\n")
        samples.forEach { detalle ->
            val muestra = detalle.muestra
            val fields = listOf(
                muestra.id,
                muestra.fechaHora,
                detalle.centroNombre ?: muestra.concesion,
                muestra.tren,
                muestra.linea,
                muestra.conteoManual,
                String.format(Locale.US, "%.2f", muestra.averageCalibre),
                muestra.estado,
                detalle.operadorNombreCompleto ?: muestra.operatorAlias,
                muestra.observaciones
            )
            builder.append(fields.joinToString(";") { escape(it) }).append('\n')
        }
        return builder.toString()
    }

    private fun escape(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
