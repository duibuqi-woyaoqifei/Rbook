package com.rbook.data.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import com.rbook.data.readium.ReadiumManager
import com.rbook.domain.model.Book
import com.rbook.domain.parser.BookParser
import dagger.hilt.android.qualifiers.ApplicationContext
import org.readium.r2.shared.publication.services.cover
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class BookParserImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val readiumManager: ReadiumManager
) : BookParser {

    override suspend fun parse(uri: Uri): Book? {
        val fileName = getFileName(uri) ?: return null
        val format = fileName.substringAfterLast(".").uppercase()
        if (format !in SUPPORTED_FORMATS) return null
        val timestamp = System.currentTimeMillis()
        
        val destinationFile = File(context.filesDir, "books/${timestamp}_$fileName")
        destinationFile.parentFile?.mkdirs()
        
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destinationFile).use { output ->
                input.copyTo(output)
            }
        }

        var author = "未知作者"
        var title = fileName.substringBeforeLast(".")
        var coverPath: String? = null

        if (format == "EPUB") {
            try {
                val publication = readiumManager.openPublication(destinationFile)
                if (publication != null) {
                    // 1. 读取标题与作者
                    val pubTitle = publication.metadata.title
                    if (!pubTitle.isNullOrBlank()) {
                        title = pubTitle
                    }
                    val authors = publication.metadata.authors
                    if (authors.isNotEmpty() && authors.first().name.isNotBlank()) {
                        author = authors.first().name
                    }

                    // 2. 提取封面图片
                    val coverBitmap = publication.cover()
                    if (coverBitmap != null) {
                        val coversDir = File(context.filesDir, "covers")
                        coversDir.mkdirs()
                        val coverFile = File(coversDir, "${timestamp}_cover.png")
                        FileOutputStream(coverFile).use { out ->
                            coverBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                        }
                        coverPath = coverFile.absolutePath
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Book(
            id = 0,
            title = title,
            author = author,
            path = destinationFile.absolutePath,
            format = format,
            coverPath = coverPath,
            progress = 0f
        )
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    private companion object {
        val SUPPORTED_FORMATS = setOf("EPUB", "PDF", "TXT")
    }
}
