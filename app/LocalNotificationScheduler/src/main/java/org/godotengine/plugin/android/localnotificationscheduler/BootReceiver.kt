package org.godotengine.plugin.android.localnotificationscheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class BootReceiver : BroadcastReceiver() {
    private val notificationHandler = NotificationHandler()


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("GodotNotif", "Device rebooted, restoring scheduled notifications...")

            val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            val ids : Set<String> = prefs.getStringSet(Constants.NOTIF_ID_NAME,  setOf<String>())!!

            for (idStr in ids) {
                val id = idStr.toInt()
                val channelId = prefs.getString("notif_${id}_channelId", "default") ?: "default"
                val title = prefs.getString("notif_${id}_title", "Reminder") ?: "Reminder"
                val text = prefs.getString("notif_${id}_text", "") ?: ""
                val priority = prefs.getInt("notif_${id}_priority", 0)
                val autoCancel = prefs.getBoolean("notif_${id}_autoCancel", true)
                val triggerAtMillis = prefs.getLong("notif_${id}_trigger", 0)
                val intervalMillis = prefs.getLong("notif_${id}_interval", 0)

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
