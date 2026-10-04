package com.example.aquasample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.aquasample.data.session.SessionManager
import com.example.aquasample.ui.login.LoginScreen
import com.example.aquasample.ui.navigation.MainScreen
import com.example.aquasample.ui.theme.AquaSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val session by SessionManager.currentSession.collectAsState()

            AquaSampleTheme {
                if (session == null) {
                    LoginScreen(
                        onLoginSuccess = { role ->
                            Toast.makeText(
                                this,
                                "Bienvenido (${role.displayName}). Sesión iniciada.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                } else {
                    MainScreen(
                        userSession = session!!,
                        onLogout = {
                            SessionManager.logout()
                            Toast.makeText(
                                this,
                                "Sesión cerrada correctamente.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}
