package org.godotengine.plugin.android.localnotificationscheduler

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.util.Log
import androidx.core.content.edit

class NotificationReceiver : BroadcastReceiver() {
    private val notificationHandler = NotificationHandler()

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val channelId = intent.getStringExtra("channelId") ?: "godot_notification_channel"

        val title = intent.getStringExtra("title") ?: "Reminder"
        val text = intent.getStringExtra("text") ?: ""
        val priority = intent.getIntExtra("priority", NotificationCompat.PRIORITY_DEFAULT)
        val autoCancel = intent.getBooleanExtra("autoCancel", true)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(priority)
            .setAutoCancel(autoCancel)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, builder.build())

        Log.d(Constants.LOG_TAG, "Delivered scheduled notification: $title - $text")

        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains("notif_${id}_interval")) {
            val intervalMillis = prefs.getLong("notif_${id}_interval", 0)
            if (intervalMillis > 0) {
                val triggerAtMillis = System.currentTimeMillis() + intervalMillis
                prefs.edit {
                    putLong("notif_${id}_trigger", triggerAtMillis)
                }

                notificationHandler.scheduleNotification(
                    context,
                    id,
                    channelId,
                    title,
                    text,
                    priority,
                    autoCancel,
                    triggerAtMillis,
                    intervalMillis
                )
            }
        }
    }
}
