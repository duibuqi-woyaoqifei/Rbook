package com.rbook.data.repository

import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val version: String, val downloadUrl: String, val notes: String = "")

/** Remote update contract. Replace UPDATE_MANIFEST_URL with the production CDN endpoint.
 * Expected JSON: {"version":"1.1.0","url":"https://...apk","notes":"..."}.
 */
object UpdateRepository {
    const val UPDATE_MANIFEST_URL =
        "https://fastly.jsdelivr.net/gh/duibuqi-woyaoqifei/Rbook@release-bin/update.json"

    fun fetch(): UpdateInfo? {
        val connection = (URL(UPDATE_MANIFEST_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            requestMethod = "GET"
        }
        return try {
            if (connection.responseCode !in 200..299) return null
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val version = json.optString("version").trim()
            val url = json.optString("url").trim()
            if (version.isBlank() || !Uri.parse(url).isHierarchical) null else
                UpdateInfo(version, url, json.optString("notes"))
        } finally {
            connection.disconnect()
        }
    }
}
