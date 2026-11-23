package com.example.aquasample.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncStatusStoreTest {

    @Test
    fun `registra error visible`() {
        SyncStatusStore.error("No se pudo conectar")

        assertTrue(SyncStatusStore.state.value.isError)
        assertEquals("No se pudo conectar", SyncStatusStore.state.value.message)
    }

    @Test
    fun `registra sincronizacion correcta`() {
        SyncStatusStore.success(0)

        assertFalse(SyncStatusStore.state.value.isError)
        assertEquals("Todo sincronizado", SyncStatusStore.state.value.message)
    }
}
