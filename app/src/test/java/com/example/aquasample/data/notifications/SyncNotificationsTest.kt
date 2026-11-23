package com.example.aquasample.data.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowApplication
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [32], application = Application::class)
class SyncNotificationsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `crea el canal de sincronizacion`() {
        SyncNotifications.createChannel(context)

        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = manager.getNotificationChannel("aquasample_sync")

        assertNotNull(channel)
        assertEquals("Sincronización de muestras", channel.name)
    }

    @Test
    fun `publica y cancela el aviso de pendientes`() {
        SyncNotifications.createChannel(context)
        SyncNotifications.notifyPending(context, 3)

        val shadow = shadowOf(context.getSystemService(NotificationManager::class.java))
        assertEquals(1, shadow.allNotifications.size)

        SyncNotifications.cancelPending(context)
        assertTrue(shadow.allNotifications.isEmpty())
    }

    @Test
    fun `publica aviso de muestra validada`() {
        SyncNotifications.createChannel(context)
        SyncNotifications.notifyValidated(context, "#M-0001")

        val shadow = shadowOf(context.getSystemService(NotificationManager::class.java))
        assertEquals(1, shadow.allNotifications.size)
        assertEquals(
            "Muestra validada",
            shadow.allNotifications.first().extras.getCharSequence("android.title").toString()
        )
    }
}
