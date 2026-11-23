package com.example.aquasample.data.session

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aquasample.data.remote.AuthTokenStore
import com.example.aquasample.data.repository.ProfileRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [32], application = Application::class)
class SessionManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefsName = "aquasample_session"

    @Before
    fun setUp() {
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("aquasample_auth", Context.MODE_PRIVATE).edit().clear().commit()
        AuthTokenStore.init(context)
        SessionManager.init(context)
        SessionManager.logout()
        ProfileRepository.reset()
    }

    @After
    fun tearDown() {
        SessionManager.logout()
        ProfileRepository.reset()
    }

    @Test
    fun `persiste y restaura la sesion`() {
        SessionManager.loginWithRole(UserRole.ADMINISTRADOR, "admin@aquasample.cl")
        assertNotNull(SessionManager.currentSession.value)

        val json = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .getString("session", null)
        assertNotNull(json)
        assertTrue(json!!.contains("ADMINISTRADOR"))

        SessionManager.init(context)
        val restored = SessionManager.currentSession.value
        assertEquals("admin@aquasample.cl", restored?.email)
        assertEquals(UserRole.ADMINISTRADOR, restored?.role)
    }

    @Test
    fun `logout limpia sesion y token`() {
        AuthTokenStore.token = "token-de-prueba"
        SessionManager.loginWithRole(UserRole.OPERADOR)
        SessionManager.logout()

        assertNull(SessionManager.currentSession.value)
        assertNull(AuthTokenStore.token)
        assertNull(
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                .getString("session", null)
        )
    }

    @Test
    fun `401 cierra la sesion y deja mensaje`() {
        AuthTokenStore.token = "token-vencido"
        SessionManager.loginWithRole(UserRole.ANALISTA)

        SessionManager.onUnauthorized()

        assertNull(SessionManager.currentSession.value)
        assertNull(AuthTokenStore.token)
        assertEquals(
            "Tu sesión expiró. Inicia sesión nuevamente.",
            SessionManager.message.value
        )
        SessionManager.consumeMessage()
        assertNull(SessionManager.message.value)
    }
}
