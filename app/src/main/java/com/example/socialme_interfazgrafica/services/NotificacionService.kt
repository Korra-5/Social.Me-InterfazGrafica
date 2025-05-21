// services/NotificacionService.kt
package com.example.socialme_interfazgrafica.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.socialme_interfazgrafica.R
import com.example.socialme_interfazgrafica.MainActivity
import com.example.socialme_interfazgrafica.model.NotificacionDTO

object NotificacionService {

    private const val CHANNEL_ID = "socialme_notification_channel"
    private const val NOTIFICATION_GROUP = "socialme_notifications"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SocialMe Notificaciones"
            val descriptionText = "Canal para notificaciones de SocialMe"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, notificacion: NotificacionDTO) {
        // Intent para abrir la app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pasar datos extra según el tipo de notificación
            putExtra("notificacionId", notificacion._id)
            putExtra("notificacionTipo", notificacion.tipo)
            putExtra("entidadId", notificacion.entidadId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construcción de la notificación
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Debes crear este icono
            .setContentTitle(notificacion.titulo)
            .setContentText(notificacion.mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Se cierra al hacer tap
            .setGroup(NOTIFICATION_GROUP)

        // Mostrar notificación
        with(NotificationManagerCompat.from(context)) {
            notify(notificacion._id.hashCode(), builder.build())
        }
    }
}