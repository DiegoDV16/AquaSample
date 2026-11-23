package com.example.aquasample.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.aquasample.MainActivity
import com.example.aquasample.R

/** Notificaciones locales de sincronización y validación. */
object SyncNotifications {

    private const val CHANNEL_ID = "aquasample_sync"
    private const val PENDING_ID = 1001
    private const val VALIDATED_BASE_ID = 2000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sincronización de muestras",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos de muestras pendientes y validadas por el analista"
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    fun notifyPending(context: Context, count: Int) {
        if (!canNotify(context)) return
        val notification = base(context)
            .setContentTitle("Muestras sin sincronizar")
            .setContentText("$count muestra(s) esperando conexión al servidor.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$count muestra(s) guardadas en el teléfono esperando conexión al servidor.")
            )
            .setOngoing(count > 0)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(PENDING_ID, notification) }
    }

    fun cancelPending(context: Context) {
        runCatching { NotificationManagerCompat.from(context).cancel(PENDING_ID) }
    }

    fun notifyValidated(context: Context, sampleId: String) {
        if (!canNotify(context)) return
        val notification = base(context)
            .setContentTitle("Muestra validada")
            .setContentText("$sampleId fue validada por el analista.")
            .setAutoCancel(true)
            .build()
        runCatching {
            NotificationManagerCompat.from(context)
                .notify(VALIDATED_BASE_ID + sampleId.hashCode().and(0xFFF), notification)
        }
    }

    private fun base(context: Context) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setColor(0xFF00658C.toInt())
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(contentIntent(context))

    private fun contentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}
