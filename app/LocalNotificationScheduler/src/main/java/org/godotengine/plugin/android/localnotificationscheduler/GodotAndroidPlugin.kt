package org.godotengine.plugin.android.localnotificationscheduler

import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.content.pm.PackageManager
import android.util.Log
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot


private const val REQUEST_NOTIF_PERMISSION = 1001


@Suppress("unused")
class GodotAndroidPlugin(godot: Godot) : GodotPlugin(godot) {

    val notificationHandler = NotificationHandler()


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
                Log.d(Constants.LOG_TAG, "Notification permission granted by user.")
            } else {
                emitSignal("permission_denied")
                Log.d(Constants.LOG_TAG, "Notification permission denied by user.")
            }
        }
    }



    @UsedByGodot
    fun requestNotificationPermission() {
        notificationHandler.requestNotificationPermission(
            activity
        )
    }

    @UsedByGodot
    fun hasNotificationPermission(): Boolean {
        return notificationHandler.hasNotificationPermission(
            activity
        )
    }


    @UsedByGodot
    fun requestExactAlarmPermission() {
        notificationHandler.requestExactAlarmPermission(
            activity
        )
    }

    @UsedByGodot
    fun canScheduleExactAlarms(): Boolean {
        return notificationHandler.canScheduleExactAlarms(
            activity
        )
    }


    @UsedByGodot
    fun createNotificationChannel() {
        notificationHandler.createNotificationChannel(
            activity,
            Constants.CHANNEL_ID
        )
    }


    @UsedByGodot
    fun scheduleNotification(
        id: Int,
        title: String,
        text: String,
        daysOfWeek: IntArray,
        hourOfDay : Int,
        minute : Int
    ) {
        notificationHandler.scheduleNotification(
            activity,
            id,
            Constants.CHANNEL_ID,
            title,
            text,
            daysOfWeek.toSet(),
            hourOfDay,
            minute
        )
    }

    @UsedByGodot
    fun scheduleInstantNotification(
        id: Int,
        title: String,
        text: String
    ) {
        notificationHandler.scheduleInstantNotification(
            activity,
            id,
            Constants.CHANNEL_ID,
            title,
            text,
            IMPORTANCE_DEFAULT,
            true
        )
    }

    @UsedByGodot
    fun cancelScheduledNotification(id: Int) {
        notificationHandler.cancelScheduledNotification(
            activity,
            id
        )
    }
}