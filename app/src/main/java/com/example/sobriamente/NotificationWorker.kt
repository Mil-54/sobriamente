package com.example.sobriamente

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val CHANNEL_ID = "sobriamente_daily_motivation"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            // Verificar si tenemos permiso
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // No hay permiso, salir sin error
                    return Result.success()
                }
            }

            // Crear canal de notificación (necesario para Android 8+)
            createNotificationChannel()

            // Obtener días sobrio
            val prefs = context.getSharedPreferences("sobriety_prefs", Context.MODE_PRIVATE)
            val startDate = prefs.getLong("start_date", 0L)

            val daysCount = if (startDate != 0L) {
                val diff = System.currentTimeMillis() - startDate
                TimeUnit.MILLISECONDS.toDays(diff)
            } else {
                0L
            }

            // Frases motivacionales aleatorias
            val motivationalMessages = listOf(
                "¡Llevas $daysCount días! Cada día cuenta. 💪",
                "Tu fuerza inspira. $daysCount días de victoria. 🌟",
                "Día $daysCount: Sigues adelante, sigue así. ✨",
                "¡$daysCount días sobrio! Eres más fuerte de lo que crees. 🦁",
                "Recordatorio: Ya llevas $daysCount días. No te rindas ahora. 🔥"
            )

            val message = if (daysCount > 0) {
                motivationalMessages.random()
            } else {
                "¿Listo para comenzar tu viaje? Abre la app y establece tu fecha de inicio. 🚀"
            }

            // Crear intent para abrir la app al tocar la notificación
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Construir la notificación
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Sobriamente - Motivación Diaria")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            // Mostrar la notificación
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Motivación Diaria"
            val descriptionText = "Recordatorios diarios de progreso y motivación"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}