package com.rbook.data.repository

import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val version: String,
    val versionCode: Int = 0,
    val downloadUrl: String,
    val notes: String = "",
)

/** Remote update contract. Replace UPDATE_MANIFEST_URL with the production CDN endpoint.
 * Expected JSON: {"version":"1.1.0","versionCode":10100,"url":"https://...apk","notes":"..."}.
 * versionName 只是展示给用户看的；是否更新必须用 versionCode 做严格数值比较。
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
                UpdateInfo(
                    version = version,
                    versionCode = json.optInt("versionCode", 0),
                    downloadUrl = url,
                    notes = json.optString("notes"),
                )
        } finally {
            connection.disconnect()
        }
    }
}
