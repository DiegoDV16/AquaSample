package com.example.aquasample.data.report

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.aquasample.data.local.entity.MuestraEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Genera un informe PDF de una muestra con PdfDocument (sin dependencias externas).
 * Incluye identificación, ubicación, conteo, matriz de calibre, observaciones y
 * una página con la primera fotografía disponible.
 */
class PdfReportGenerator(private val context: Context) {

    private val primary = Color.parseColor("#0A577A")
    private val ink = Color.parseColor("#1F2937")
    private val muted = Color.parseColor("#5B6B7A")
    private val line = Color.parseColor("#CBD5E1")

    suspend fun generate(
        muestra: MuestraEntity,
        centroNombre: String?,
        operadorNombre: String?
    ): File = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "reportes").apply { mkdirs() }
        val safeId = muestra.id.replace("#", "").replace("/", "-")
        val file = File(directory, "AquaSample_$safeId.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        drawFirstPage(page.canvas, muestra, centroNombre, operadorNombre)
        document.finishPage(page)

        val photoPath = muestra.fotos.firstOrNull()
        if (photoPath != null) {
            val bitmap = decodePhoto(photoPath)
            if (bitmap != null) {
                val photoPage = document.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
                )
                drawPhotoPage(photoPage.canvas, bitmap, muestra.id)
                document.finishPage(photoPage)
                bitmap.recycle()
            }
        }

        FileOutputStream(file).use { output -> document.writeTo(output) }
        document.close()
        file
    }

    private fun drawFirstPage(
        canvas: Canvas,
        muestra: MuestraEntity,
        centroNombre: String?,
        operadorNombre: String?
    ) {
        val header = Paint().apply { color = primary }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 92f, header)

        val brand = textPaint(24f, Color.WHITE, bold = true)
        canvas.drawText("AquaSample", 40f, 48f, brand)
        val subtitle = textPaint(10f, Color.WHITE)
        canvas.drawText("Informe de muestra · ALDEMAR SpA", 40f, 70f, subtitle)

        var y = 128f
        canvas.drawText(muestra.id, 40f, y, textPaint(20f, primary, bold = true))
        y += 18f
        canvas.drawText(
            muestra.fechaHora.ifBlank { "Sin fecha registrada" },
            40f,
            y,
            textPaint(10f, muted)
        )
        y += 26f

        y = section(canvas, "Identificación", y)
        y = row(canvas, "Centro", centroNombre ?: muestra.concesion, y)
        y = row(canvas, "Operador", operadorNombre ?: muestra.operatorAlias, y)
        y = row(canvas, "Estado", muestra.estado, y)
        y = row(canvas, "Sincronización", if (muestra.sincronizado) "Sincronizada" else "Pendiente", y)

        y += 8f
        y = section(canvas, "Ubicación", y)
        y = row(canvas, "Concesión", muestra.concesion, y)
        y = row(canvas, "Tren / Línea", "${muestra.tren} · ${muestra.linea}", y)
        y = row(canvas, "Tramo", "${muestra.tramoLongitudCm} cm", y)

        y += 8f
        y = section(canvas, "Conteo y calibre", y)
        y = row(canvas, "Conteo manual", "${muestra.conteoManual} individuos", y)
        y = row(
            canvas,
            "Conteo asistido",
            if (muestra.conteoAsistido > 0) "${muestra.conteoAsistido} marcas" else "Sin marcas",
            y
        )
        y = row(
            canvas,
            "Calibre promedio",
            String.format(Locale.getDefault(), "%.1f mm", muestra.averageCalibre),
            y
        )

        y += 6f
        canvas.drawText("Matriz de calibre", 40f, y, textPaint(11f, ink, bold = true))
        y += 12f
        drawMatrix(canvas, muestra.calibreMatrix, y)
        y += 6 * 22f + 10f

        y = section(canvas, "Observaciones", y)
        val observations = muestra.observaciones.ifBlank { "Sin observaciones registradas." }
        y = drawWrapped(canvas, observations, 40f, y, CONTENT_WIDTH, textPaint(10f, ink), 15f)

        if (muestra.tags.isNotEmpty()) {
            y += 8f
            y = section(canvas, "Etiquetas", y)
            y = drawWrapped(
                canvas,
                muestra.tags.joinToString("  ") { "#$it" },
                40f,
                y,
                CONTENT_WIDTH,
                textPaint(10f, muted),
                15f
            )
        }

        val footerY = PAGE_HEIGHT - 36f
        val footerPaint = Paint().apply {
            color = line
            strokeWidth = 0.8f
        }
        canvas.drawLine(40f, footerY - 14f, PAGE_WIDTH - 40f, footerY - 14f, footerPaint)
        val generated = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generado el $generated · Uso interno ALDEMAR SpA", 40f, footerY, textPaint(8f, muted))
    }

    private fun drawPhotoPage(canvas: Canvas, bitmap: android.graphics.Bitmap, sampleId: String) {
        canvas.drawText("Evidencia fotográfica · $sampleId", 40f, 60f, textPaint(16f, primary, bold = true))

        val availableWidth = CONTENT_WIDTH
        val availableHeight = PAGE_HEIGHT - 180f
        val scale = minOf(availableWidth / bitmap.width, availableHeight / bitmap.height)
        val drawWidth = bitmap.width * scale
        val drawHeight = bitmap.height * scale
        val left = 40f + (availableWidth - drawWidth) / 2f
        val top = 90f

        canvas.drawBitmap(
            bitmap,
            null,
            RectF(left, top, left + drawWidth, top + drawHeight),
            Paint().apply { isFilterBitmap = true }
        )
        canvas.drawRect(
            left,
            top,
            left + drawWidth,
            top + drawHeight,
            Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = line
            }
        )
    }

    private fun drawMatrix(canvas: Canvas, values: List<String>, startY: Float) {
        val cellWidth = CONTENT_WIDTH / 5f
        val cellHeight = 20f
        val border = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            color = line
        }
        val valuePaint = textPaint(9f, ink)

        for (row in 0 until 6) {
            for (column in 0 until 5) {
                val index = row * 5 + column
                val left = 40f + column * cellWidth
                val top = startY + row * cellHeight
                canvas.drawRect(left, top, left + cellWidth, top + cellHeight, border)
                val value = values.getOrNull(index).orEmpty().ifBlank { "—" }
                canvas.drawText(value, left + 6f, top + 14f, valuePaint)
            }
        }
    }

    private fun section(canvas: Canvas, title: String, startY: Float): Float {
        val paint = textPaint(11f, primary, bold = true)
        canvas.drawText(title.uppercase(Locale.getDefault()), 40f, startY, paint)
        canvas.drawLine(40f, startY + 4f, PAGE_WIDTH - 40f, startY + 4f, Paint().apply {
            color = line
            strokeWidth = 0.7f
        })
        return startY + 18f
    }

    private fun row(canvas: Canvas, label: String, value: String, startY: Float): Float {
        canvas.drawText(label, 40f, startY, textPaint(9.5f, muted))
        canvas.drawText(value.ifBlank { "—" }, 200f, startY, textPaint(10f, ink))
        return startY + 17f
    }

    private fun drawWrapped(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float
    ): Float {
        var y = startY
        var current = StringBuilder()
        text.split(" ").forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                canvas.drawText(current.toString(), x, y, paint)
                y += lineHeight
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) {
            canvas.drawText(current.toString(), x, y, paint)
            y += lineHeight
        }
        return y
    }

    private fun textPaint(size: Float, color: Int, bold: Boolean = false): Paint = Paint().apply {
        this.color = color
        textSize = size
        isAntiAlias = true
        typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
    }

    private fun decodePhoto(path: String): android.graphics.Bitmap? {
        val file = File(path)
        if (!file.exists()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > 1400 || bounds.outHeight / sampleSize > 1400) {
            sampleSize *= 2
        }
        return runCatching {
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        }.getOrNull()
    }

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val CONTENT_WIDTH = PAGE_WIDTH - 80f
    }
}
