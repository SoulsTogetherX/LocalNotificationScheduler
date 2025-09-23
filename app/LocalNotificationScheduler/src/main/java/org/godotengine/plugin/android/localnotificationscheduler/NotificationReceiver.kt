package org.godotengine.plugin.android.localnotificationscheduler


import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.util.Log


class NotificationReceiver : BroadcastReceiver() {
    private val notificationHandler = NotificationHandler()

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val channelId = intent.getStringExtra("channelId") ?: "godot_notification_channel"
        val title = intent.getStringExtra("title") ?: "Title"
        val text = intent.getStringExtra("text") ?: "Text"

        val daysOfWeek = intent.getIntArrayExtra("daysOfWeek")?.toSet() ?: setOf()
        val hourOfDay = intent.getIntExtra("hourOfDay", 0)
        val minute = intent.getIntExtra("minute", 0)


        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, builder.build())

        Log.d(Constants.LOG_TAG, "Delivered scheduled notification: $title - $text")


        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val hasId = (prefs.getStringSet(Constants.NOTIF_ID_NAME,  setOf<String>()))?.contains(id.toString()) ?: false

        if (hasId) {
            notificationHandler.scheduleNotification(
                context,
                id,
                channelId,
                title,
                text,
                daysOfWeek,
                hourOfDay,
                minute
            )

            Log.d(Constants.LOG_TAG, "Request repeat notification: $title - $text")
            return
        }

        Log.d(Constants.LOG_TAG, "Error when requesting repeat notification: $title - $text")
    }
}
