package org.godotengine.plugin.android.localnotificationscheduler

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.CountDownLatch

class UpdateInfoWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {

    override fun doWork(): Result {
        val authToken = inputData.getString("authToken") ?: return fail("Missing authToken")
        val variablesStr = inputData.getString("variables") ?: return fail("Missing variables")
        val query = inputData.getString("query") ?: return fail("Missing query")
        val endpoint = inputData.getString("endpoint") ?: return fail("Missing endpoint")

        val variables: JSONObject = try {
            JSONObject(variablesStr)
        } catch (e: JSONException) {
            return fail("Invalid variables JSON: ${e.message}")
        }

        val gql = GraphQLClient(endpoint, authToken)

        var success = false
        var msg: String? = null
        val latch = CountDownLatch(1)

        try {
            gql.executeQuery(query, variables) { result, error ->
                if (error != null) {
                    success = false
                    msg = error.message
                } else {
                    success = true
                    msg = result
                }
                latch.countDown()
            }
            latch.await() // Wait for async HTTP response
        } catch (e: Exception) {
            Log.e("Cognito Appsync Query", "Worker exception", e)
            return fail("Worker exception: ${e.message}")
        }

        return if (success) {
            Log.i("Cognito Appsync Query", "GraphQL success: $msg")
            Result.success(
                Data.Builder()
                    .putBoolean("success", true)
                    .putString("message", msg ?: "Success")
                    .build()
            )
        } else {
            Log.e("Cognito Appsync Query", "GraphQL failure: $msg")
            Result.failure(
                Data.Builder()
                    .putBoolean("success", false)
                    .putString("message", msg ?: "Unknown error")
                    .build()
            )
        }
    }

    private fun fail(reason: String): Result {
        Log.e("Cognito Appsync Query", reason)
        return Result.failure(
            Data.Builder()
                .putBoolean("success", false)
                .putString("message", reason)
                .build()
        )
    }
}
