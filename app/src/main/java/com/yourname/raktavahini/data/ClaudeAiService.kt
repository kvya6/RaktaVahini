
package com.yourname.raktavahini.data

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.withContext

import okhttp3.MediaType.Companion.toMediaType

import okhttp3.OkHttpClient

import okhttp3.Request

import okhttp3.RequestBody.Companion.toRequestBody

import org.json.JSONArray

import org.json.JSONObject

import java.util.concurrent.TimeUnit

object ClaudeAiService {

    private const val API_KEY ="AIzaSyAdKH2gsgtEmLN1x75C2-8Pin1JQLuOBAM"

    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    private val client = OkHttpClient.Builder()

        .connectTimeout(30, TimeUnit.SECONDS)

        .readTimeout(30, TimeUnit.SECONDS)

        .build()

    suspend fun askAdvisor(question: String): String = withContext(Dispatchers.IO) {

        try {

            val systemInstruction = "You are RaktaVahini AI Advisor, a helpful assistant for blood donation in India. Answer questions about blood types, eligibility, donation process, and health tips concisely in 2-4 sentences. Be empathetic and encouraging."

            val body = JSONObject().apply {

                put("system_instruction", JSONObject().apply {

                    put("parts", JSONArray().apply {

                        put(JSONObject().put("text", systemInstruction))

                    })

                })

                put("contents", JSONArray().apply {

                    put(JSONObject().apply {

                        put("parts", JSONArray().apply {

                            put(JSONObject().put("text", question))

                        })

                    })

                })

            }.toString()

            val request = Request.Builder()

                .url("$API_URL?key=$API_KEY")

                .addHeader("Content-Type", "application/json")

                .post(body.toRequestBody("application/json".toMediaType()))

                .build()

            val response = client.newCall(request).execute()

            val responseBody = response.body?.string() ?: return@withContext "No response."

            if (!response.isSuccessful) {

                return@withContext "Error ${response.code}: Please check your API key."

            }

            JSONObject(responseBody)

                .getJSONArray("candidates")

                .getJSONObject(0)

                .getJSONObject("content")

                .getJSONArray("parts")

                .getJSONObject(0)

                .getString("text")

        } catch (e: Exception) {

            "Could not reach AI. Please check your internet. (${e.message})"

        }

    }

}

