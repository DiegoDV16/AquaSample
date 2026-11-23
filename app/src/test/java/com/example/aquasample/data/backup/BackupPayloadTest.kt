package com.example.aquasample.data.backup

import com.example.aquasample.data.local.entity.MuestraEntity
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupPayloadTest {

    @Test
    fun `serializa y recupera muestras del respaldo`() {
        val payload = BackupPayload(
            muestras = listOf(
                MuestraEntity(
                    id = "#M-0001",
                    concesion = "Dalcahue",
                    conteoManual = "30",
                    calibreMatrix = listOf("34.5", "35.0"),
                    fotos = listOf("fotos/M-0001_1.jpg"),
                    sincronizado = false
                )
            )
        )

        val json = Gson().toJson(payload)
        val restored = Gson().fromJson(json, BackupPayload::class.java)

        assertEquals(1, restored.version)
        assertEquals(1, restored.muestras.size)
        assertEquals("#M-0001", restored.muestras[0].id)
        assertEquals(listOf("34.5", "35.0"), restored.muestras[0].calibreMatrix)
        assertEquals(listOf("fotos/M-0001_1.jpg"), restored.muestras[0].fotos)
    }
}
