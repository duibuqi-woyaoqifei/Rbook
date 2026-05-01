package com.rbook.data.readium

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.asset.FileAsset
import org.readium.r2.streamer.Streamer
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadiumManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val streamer = Streamer(context)

    suspend fun openPublication(file: File): Publication? {
        return try {
            val asset = FileAsset(file)
            val result = streamer.open(asset, allowUserInteraction = false)
            result.getOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
