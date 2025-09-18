package org.godotengine.plugin.android.localnotificationscheduler

import android.content.pm.PackageManager
import android.util.Log
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot




@Suppress("unused")
class GodotAndroidPlugin(godot: Godot) : GodotPlugin(godot) {

    private val notificationHandler = NotificationHandler()

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
        if (requestCode == Constants.REQUEST_NOTIF_PERMISSION) {
            if (grantResults != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                emitSignal("permission_granted")
                Log.d(Constants.LOG_TAG, "Permission granted by user.")
            } else {
                emitSignal("permission_denied")
                Log.d(Constants.LOG_TAG, "Permission denied by user.")
            }
        }
    }



    @UsedByGodot
    fun requestNotificationPermission() {
        val activity = activity ?: return

        if (notificationHandler.requestNotificationPermission(activity)) {
            emitSignal("permission_granted")
        }
    }

    @UsedByGodot
    fun hasNotificationPermission(): Boolean {
        val activity = activity ?: return false

        return notificationHandler.hasNotificationPermission(activity)
    }


    @UsedByGodot
    fun createNotificationChannel(channelId : String) {
        val activity = activity ?: return

        notificationHandler.createNotificationChannel(activity, channelId)
    }

    @UsedByGodot
    fun scheduleNotification(
        id: Int,
        channelId: String,
        title: String,
        text: String,
        priority : Int,
        autoCancel: Boolean,
        triggerAtMillis: Long,
        intervalMillis: Long?
    ) {
        val activity = activity ?: return

        notificationHandler.scheduleNotification(
            activity,
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

    @UsedByGodot
    fun scheduleInstantNotification(
        id: Int,
        channelId: String,
        title: String,
        text: String,
        priority : Int,
        autoCancel: Boolean
    ) {
        val activity = activity ?: return

        notificationHandler.scheduleInstantNotification(
            activity,
            id,
            channelId,
            title,
            text,
            priority,
            autoCancel
        )
    }

    @UsedByGodot
    fun cancelScheduledNotification(id: Int) {
        val activity = activity ?: return

        notificationHandler.cancelScheduledNotification(
            activity,
            id
        )
    }
}