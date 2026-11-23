package com.example.aquasample.data.backup

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aquasample.data.local.entity.MuestraEntity
import com.example.aquasample.di.AppContainer
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], application = Application::class)
class BackupManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `exporta e importa una muestra con foto`() = runTest {
        val container = AppContainer(context)
        val manager = BackupManager(context, container)
        val dao = container.database.muestraDao()

        val photoDir = File(context.filesDir, "muestras").apply { mkdirs() }
        val photo = File(photoDir, "respaldo_test.png")
        photo.writeBytes(byteArrayOf(1, 2, 3, 4))

        dao.insertSample(
            MuestraEntity(
                id = "#M-9700",
                concesion = "Dalcahue",
                tren = "Tren 02",
                linea = "Línea L-03",
                conteoManual = "30",
                averageCalibre = 36.5,
                fotos = listOf(photo.absolutePath),
                photoCount = 1,
                estado = "COMPLETADO",
                sincronizado = true
            )
        )

        val backup = manager.export()
        assertTrue(backup.exists())
        assertTrue(backup.length() > 0)

        val original = dao.getSampleById("#M-9700")
        assertNotNull(original)
        dao.deleteSample(original!!)
        assertEquals(null, dao.getSampleById("#M-9700"))

        val result = manager.import(Uri.fromFile(backup))

        assertEquals(1, result.imported)
        assertEquals(0, result.skipped)
        assertEquals(1, result.photos)
        val restored = dao.getSampleById("#M-9700")
        assertNotNull(restored)
        assertEquals("Dalcahue", restored!!.concesion)
        assertEquals(1, restored.fotos.size)
        assertTrue(File(restored.fotos.first()).exists())
        assertTrue(!restored.sincronizado)
    }
}
