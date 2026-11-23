package com.example.aquasample.data.report

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aquasample.data.local.entity.MuestraConDetalle
import com.example.aquasample.data.local.entity.MuestraEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], application = Application::class)
class JourneyReportGeneratorTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun sample(id: String) = MuestraConDetalle(
        muestra = MuestraEntity(
            id = id,
            concesion = "Dalcahue",
            tren = "Tren 02",
            linea = "Línea L-03",
            conteoManual = "30",
            averageCalibre = 36.4,
            estado = "VALIDADO",
            observaciones = "Sin novedad"
        ),
        centroNombre = "Dalcahue",
        operadorNombreCompleto = "Francisca Rojas"
    )

    @Test
    fun `genera csv consolidado en cache`() = runTest {
        val file = JourneyReportGenerator(context).generateCsv(listOf(sample("#M-0001")))

        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        val content = file.readText()
        assertTrue(content.contains("#M-0001"))
        assertTrue(content.contains("Dalcahue"))
    }
}
