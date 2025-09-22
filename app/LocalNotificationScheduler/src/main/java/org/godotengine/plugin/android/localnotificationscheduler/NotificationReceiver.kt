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
        val title = intent.getStringExtra("title") ?: "Title"
        val text = intent.getStringExtra("text") ?: "Text"
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
        val hasId = (prefs.getStringSet(Constants.NOTIF_ID_NAME,  setOf<String>())!!).contains(id.toString())

        if (hasId) {
            val interval = prefs.getLong("notif_${id}_interval", 0)

            if (interval > 0) {
                val current = System.currentTimeMillis()
                var trigger = prefs.getLong("notif_${id}_trigger", 0)

                trigger += (((current - trigger) / interval) + 1) * interval

                prefs.edit {
                    putLong("notif_${id}_trigger", trigger)
                }

                notificationHandler.scheduleNotification(
                    context,
                    id,
                    channelId,
                    title,
                    text,
                    priority,
                    autoCancel,
                    trigger,
                    interval
                )

                Log.d(Constants.LOG_TAG, "Request repeat notification: $title - $text")
                return
            }

            notificationHandler.cancelScheduledNotification(
                context,
                id
            )
        }
    }
}
