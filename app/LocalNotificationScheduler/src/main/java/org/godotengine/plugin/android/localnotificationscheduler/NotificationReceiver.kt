package org.godotengine.plugin.android.localnotificationscheduler

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
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

        Log.d("GodotNotif", "Delivered scheduled notification: $title - $text")
    }
}
