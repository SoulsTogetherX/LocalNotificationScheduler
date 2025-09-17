// NotificationReceiver.kt
package org.godotengine.plugin.android.localnotificationscheduler

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val channelId = intent.getStringExtra("channelId")!!
        val title = intent.getStringExtra("title") ?: "Reminder"
        val text = intent.getStringExtra("text") ?: ""
        val autoCancel = (intent.getStringExtra("autoCancel") ?: true) as Boolean

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(autoCancel)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, builder.build())
    }
}
