package org.forestdept.seemsai.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.forestdept.seemsai.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "elephant_guard_emergency_channel"
    private const val CHANNEL_NAME = "Elephant Guard Emergency Alerts"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "High-priority critical wildlife encounter warnings and siren notifications"
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showEmergencyNotification(
        context: Context,
        elephantCount: Int,
        confidencePercent: Int,
        riskScore: Int,
        locationStr: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("🚨 ELEPHANT DETECTED • CRITICAL RISK")
            .setContentText("$elephantCount Elephant(s) detected ($confidencePercent% Conf) at $locationStr. Risk: $riskScore/100")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚨 CRITICAL WILDLIFE ALERT\n• Elephant Count: $elephantCount\n• Confidence: $confidencePercent%\n• DMRS Risk: $riskScore/100\n• GPS: $locationStr\n• Action: Immediate Corridor Evacuation Advised.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun showSosEmergencyNotification(
        context: Context,
        elephantCount: Int,
        threatLevel: String,
        reporter: String,
        distanceKm: Double,
        locationStr: String,
        notes: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distFormatted = String.format("%.2f km away", distanceKm)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("🚨 5KM SOS: ELEPHANT SPOTTED ($distFormatted)")
            .setContentText("$elephantCount Elephant(s) reported by $reporter ($distFormatted). Threat: $threatLevel")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("🚨 5KM EMERGENCY WILDLIFE SOS\n• Target: $elephantCount Elephant(s)\n• Danger Level: $threatLevel\n• Distance to You: $distFormatted (INSIDE 5KM ZONE)\n• Reported By: $reporter\n• Sector: $locationStr\n• Notes: $notes\n• Action: Caution in corridor. Open map for safe evacuation.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
