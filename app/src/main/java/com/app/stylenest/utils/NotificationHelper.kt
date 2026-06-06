package com.app.stylenest.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.stylenest.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "order_notifications"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Order Updates"
            val descriptionText = "Notifications for successful orders and delivery updates"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showOrderSuccessNotification(context: Context, orderNumber: String) {
        // Create an intent to open the app when the notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_def_app_icon) // Replace with your app's actual icon if you have one
            .setContentTitle("Order Confirmed! 🎉")
            .setContentText("Your order $orderNumber has been successfully placed.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                // If permission is not granted on Android 13+, this will throw an exception or just fail silently.
                // We've requested permission in MainActivity, so hopefully it's granted.
                notify(NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
