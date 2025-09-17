package org.godotengine.plugin.android.localnotificationscheduler

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot


private const val REQUEST_NOTIF_PERMISSION = 1001


@Suppress("unused")
class GodotAndroidPlugin(godot: Godot) : GodotPlugin(godot) {
    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    override fun getPluginSignals(): Set<SignalInfo> {
        return setOf(
            SignalInfo("permission_granted"),
            SignalInfo("permission_denied")
        )
    }

    override fun onMainRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>?,
        grantResults: IntArray?
    ) {
        super.onMainRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIF_PERMISSION) {
            if (grantResults != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                emitSignal("permission_granted")
                Log.d("GodotNotif", "Permission granted by user.")
            } else {
                emitSignal("permission_denied")
                Log.d("GodotNotif", "Permission denied by user.")
            }
        }
    }



    @UsedByGodot
    fun requestNotificationPermission() {
        val activity = activity ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIF_PERMISSION
                )

                Log.d("GodotNotif", "Notification permission requested.")
            } else {
                emitSignal("permission_granted")
                Log.d("GodotNotif", "Notification permission already granted.")
            }
        } else {
            emitSignal("permission_granted")
            Log.d("GodotNotif", "No runtime permission needed (API < 33).")
        }
    }

    @UsedByGodot
    fun hasNotificationPermission(): Boolean {
        val activity = activity ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On Android 12 and below, permission is implicitly granted
            true
        }
    }


    @UsedByGodot
    fun createNotificationChannel(channelId : String) {
        Log.d("GodotNotif", "Notification channel created.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val activity = activity ?: return
            val channel = NotificationChannel(
                channelId,
                "Godot Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Basic test channel"
            }

            val manager =
                activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    @UsedByGodot
    fun scheduleRepeatingNotification(
        id: Int,
        title: String,
        text: String,
        channelId: String,
        autoCancel: Boolean,
        triggerAtMillis: Long,
        intervalMillis: Long
    ) {
        val activity = activity ?: return

        val intent = Intent(activity, NotificationReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("text", text)
            putExtra("channelId", channelId)
            putExtra("autoCancel", autoCancel)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            intervalMillis,
            pendingIntent
        )

        Log.d(
            "GodotNotif",
            "Schedule Repeating Notification at triggerAtMillis '$triggerAtMillis' and at intervalMillis '$intervalMillis'"
        )
    }

    @UsedByGodot
    fun scheduleNotification(
        id: Int,
        title: String,
        text: String,
        channelId: String,
        autoCancel: Boolean,
        triggerAtMillis: Long
    ) {
        val activity = activity ?: return

        val intent = Intent(activity, NotificationReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("text", text)
            putExtra("channelId", channelId)
            putExtra("autoCancel", autoCancel)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )

        Log.d(
            "GodotNotif",
            "Schedule Repeating Notification at triggerAtMillis '$triggerAtMillis'"
        )
    }

    @UsedByGodot
    fun scheduleInstantNotification(
        id: Int,
        title: String,
        text: String,
        channelId: String,
        priority : Int,
        autoCancel: Boolean
    ) {
        val activity = activity ?: return

        val builder = NotificationCompat.Builder(activity, channelId)
            .setSmallIcon(android.R.drawable.btn_star)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(priority)
            .setAutoCancel(autoCancel)

        val manager =
            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, builder.build())
        Log.d("GodotNotif", "Notification requested: $title - $text")
    }

    @UsedByGodot
    fun cancelScheduledNotification(id: Int) {
        val activity = activity ?: return

        val intent = Intent(activity, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        Log.d("GodotNotif", "Notification, id $id', canceled.")
    }
}