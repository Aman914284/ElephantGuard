package org.forestdept.seemsai.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import org.forestdept.seemsai.MainActivity
import org.forestdept.seemsai.R
import org.forestdept.seemsai.model.DefenseAlertLevel

/**
 * Android System Notification Engine for high-priority tactical alerts.
 */
class NotificationEngine(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "seems_ai_wildlife_alerts"
        const val CHANNEL_NAME = "SEEMS-AI Wildlife Defense Alerts"
        const val NOTIFICATION_ID = 9910
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency notifications for NH-33 Dalma corridor wildlife crossings."
                enableLights(true)
                lightColor = 0xFFEF4444.toInt()
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showCriticalHazardNotification(
        riskScore: Int,
        ttcString: String,
        distanceMeters: Double,
        alertLevel: DefenseAlertLevel
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val title = when (alertLevel) {
            DefenseAlertLevel.RED -> "🚨 CRITICAL WILDLIFE ALERT — NH-33"
            DefenseAlertLevel.AMBER -> "⚠️ ELEPHANT PROXIMITY WARNING"
            DefenseAlertLevel.YELLOW -> "🟡 WILDLIFE CAUTION — DALMA"
            DefenseAlertLevel.GREEN -> "🟢 CORRIDOR PATROL NORMAL"
        }

        val body = "Elephant detected ${distanceMeters.toInt()}m from highway axis.\n" +
                "Risk Score: $riskScore/100 | TTC: $ttcString | Speed Limit: 20 km/h"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
