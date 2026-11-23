package com.example.aquasample

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aquasample.data.remote.AuthTokenStore
import com.example.aquasample.data.session.SessionManager
import com.example.aquasample.ui.login.LoginScreen
import com.example.aquasample.ui.navigation.MainScreen
import com.example.aquasample.ui.theme.AquaSampleTheme
import com.example.aquasample.ui.theme.Motion

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        setContent {
            AquaSampleTheme {
                val session by SessionManager.currentSession.collectAsStateWithLifecycle()
                val view = LocalView.current
                val context = LocalContext.current
                val lightBackground = MaterialTheme.colorScheme.background.luminance() > 0.5f

                // El login tiene un encabezado azul a pantalla completa: allí los
                // iconos de sistema deben ser claros; en el resto, siguen al tema.
                LaunchedEffect(session, lightBackground) {
                    val window = (context as? Activity)?.window ?: return@LaunchedEffect
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars =
                        if (session == null) false else lightBackground
                    controller.isAppearanceLightNavigationBars = lightBackground
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = session,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(Motion.Medium)) togetherWith
                                fadeOut(animationSpec = tween(Motion.Short))
                        },
                        label = "session"
                    ) { currentSession ->
                        if (currentSession == null) {
                            LoginScreen()
                        } else {
                            MainScreen(
                                userSession = currentSession,
                                onLogout = {
                                    AuthTokenStore.clear()
                                    SessionManager.logout()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
