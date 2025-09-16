package org.godotengine.plugin.android.localnotificationscheduler

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot


@Suppress("unused")
class GodotAndroidPlugin(godot: Godot) : GodotPlugin(godot) {

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    /**
     * IMPORTANT: variablesJson must be a valid JSON string (send from GDScript via JSON.stringify).
     */
    @UsedByGodot
    fun runQuery(authToken: String, endpoint: String, query: String, variablesJson: String) {
        val input = workDataOf(
            "authToken" to authToken,
            "variables" to variablesJson,
            "query" to query,
            "endpoint" to endpoint
        )

        val request = OneTimeWorkRequestBuilder<UpdateInfoWorker>()
            .setInputData(input)
            .build()

        val ctx = activity?.applicationContext ?: return
        WorkManager.getInstance(ctx).enqueue(request)
    }
}
