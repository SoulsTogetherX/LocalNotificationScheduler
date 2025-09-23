package org.godotengine.plugin.android.localnotificationscheduler

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import java.util.Calendar


class NotificationHandler {
    private fun saveNotification(
        context : Context,
        id: Int,
        channelId: String,
        title: String,
        text: String,
        daysOfWeek: Set<Int>,
        hourOfDay : Int,
        minute : Int
    ) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit {
            putString("notif_${id}_channelId", channelId)
            putString("notif_${id}_title", title)
            putString("notif_${id}_text", text)

            putStringSet("notif_${id}_daysOfWeek", daysOfWeek.map { it.toString() }.toSet())
            putInt("notif_${id}_hourOfDay", hourOfDay)
            putInt("notif_${id}_minute", minute)

            val ids = prefs.getStringSet(Constants.NOTIF_ID_NAME, emptySet())!!.toMutableSet()
            ids.add(id.toString())
            putStringSet(Constants.NOTIF_ID_NAME, ids)
            apply()
        }
    }

    private fun removeNotification(
        context : Context,
        id: Int
    ) {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit {
            remove("notif_${id}_channel")
            remove("notif_${id}_title")
            remove("notif_${id}_text")

            remove("notif_${id}_daysOfWeek")
            remove("notif_${id}_hourOfDay")
            remove("notif_${id}_minute")

            var ids : Set<String> = prefs.getStringSet(Constants.NOTIF_ID_NAME,  setOf<String>())!!
            ids = ids.minus(id.toString())
            putStringSet(Constants.NOTIF_ID_NAME,ids)

            apply()
        }
    }


    fun requestNotificationPermission(
        activity : Activity?
    ): Boolean {
        val activity = activity ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    Constants.REQUEST_NOTIF_PERMISSION
                )

                Log.d(Constants.LOG_TAG, "Notification permission requested.")
                return false
            }

            Log.d(Constants.LOG_TAG, "Notification permission already granted.")
            return true
        }

        Log.d(Constants.LOG_TAG, "No runtime permission needed (API < 33).")
        return true
    }

    fun hasNotificationPermission(
        context : Context?
    ): Boolean {
        val context = context ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On Android 12 and below, permission is implicitly granted
            true
        }
    }


    fun requestExactAlarmPermission(context: Context?) {
        val context = context ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!canScheduleExactAlarms(context)) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = "package:${context.packageName}".toUri()
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    fun canScheduleExactAlarms(context: Context?): Boolean {
        val context = context ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }


    fun createNotificationChannel(
        context : Context?,
        channelId : String
    ) {
        val context = context ?: return

        Log.d(Constants.LOG_TAG, "Notification channel created.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Godot Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Basic test channel"
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(
        context : Context?,
        id: Int,
        channelId: String,
        title: String,
        text: String,
        daysOfWeek: Set<Int>,
        hourOfDay : Int,
        minute : Int
    ) {
        val context = context ?: return

        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (next.before(now) || !daysOfWeek.contains(next.get(Calendar.DAY_OF_WEEK))) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        val trigger = next.timeInMillis

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("channelId", channelId)
            putExtra("title", title)
            putExtra("text", text)
            putExtra("daysOfWeek", daysOfWeek.toIntArray())
            putExtra("hourOfDay", hourOfDay)
            putExtra("minute", minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (canScheduleExactAlarms(context)) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger,
                    pendingIntent
                )
            } catch (secEx: SecurityException) {
                Log.w(Constants.LOG_TAG, "Exact alarm blocked - falling back to inexact: ${secEx.message}")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger,
                    pendingIntent
                )
            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG, "Unexpected error scheduling alarm", e)
            }
        } else {
            // No exact alarm permission/approval -> fallback to best-effort inexact alarm
            Log.w(Constants.LOG_TAG, "Cannot schedule exact alarm (not allowed). Using setAndAllowWhileIdle fallback.")
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger,
                    pendingIntent
                )
            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG, "Failed to set fallback inexact alarm", e)
            }
        }

        saveNotification(
            context,
            id,
            channelId,
            title,
            text,
            daysOfWeek,
            hourOfDay,
            minute
        )

        Log.d(
            Constants.LOG_TAG,
            "Schedule Notification"
        )
    }

    fun scheduleInstantNotification(
        context : Context?,
        id: Int,
        channelId: String,
        title: String,
        text: String,
        priority : Int,
        autoCancel: Boolean
    ) {
        val context = context ?: return

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.btn_star)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(priority)
            .setAutoCancel(autoCancel)

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, builder.build())
        Log.d(Constants.LOG_TAG, "Notification requested: $title - $text")
    }

    fun cancelScheduledNotification(
        context : Context?,
        id: Int
    ) {
        val context = context ?: return

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        removeNotification(context, id)

        Log.d(Constants.LOG_TAG, "Notification, with id $id, was canceled.")
    }
}