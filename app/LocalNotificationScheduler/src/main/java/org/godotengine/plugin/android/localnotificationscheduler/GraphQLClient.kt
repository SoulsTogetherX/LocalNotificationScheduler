package org.godotengine.plugin.android.localnotificationscheduler

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class GraphQLClient(private val endpoint: String, private val authToken: String) {
    private val client = OkHttpClient()

    fun executeQuery(query: String, variables: JSONObject, callback: (String?, Exception?) -> Unit) {
        val payload = JSONObject()
            .put("query", query)
            .put("variables", variables)

        val body = payload.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", authToken)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val text = response.body?.string()
                    if (!response.isSuccessful) {
                        callback(null, IOException("HTTP ${response.code}: $text"))
                    } else {
                        callback(text, null)
                    }
                }
            }
        })
    }
}
